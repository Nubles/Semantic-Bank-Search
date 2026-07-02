package com.semanticbanksearch;

import com.google.gson.Gson;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Semantic Bank Search",
	description = "Searches observed bank and storage items by purpose using local semantic rules.",
	tags = {"bank", "search", "items", "storage", "utility"}
)
public class SemanticBankSearchPlugin extends Plugin
{
	private static final String STORAGE_KEY = "index";
	private static final long SAVE_INTERVAL_MILLIS = 60_000L;

	private enum PanelMode
	{
		SEARCH,
		ALL_INDEXED
	}

	private static class ViewState
	{
		private final PanelMode panelMode;
		private final String query;
		private final long revision;

		private ViewState(PanelMode panelMode, String query, long revision)
		{
			this.panelMode = panelMode;
			this.query = query;
			this.revision = revision;
		}
	}

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SemanticBankSearchConfig config;

	@Inject
	private Gson gson;

	private final Object viewStateLock = new Object();
	private StorageIndex index;
	private SemanticSearchEngine engine;
	private ObservedStorageScanner storageScanner;
	private final Set<String> visibleStorageSourceKeys = new HashSet<>();
	private SemanticBankSearchPanel panel;
	private SemanticBankSearchOverlay overlay;
	private NavigationButton navigationButton;
	private String currentQuery = "";
	private PanelMode panelMode = PanelMode.SEARCH;
	private long viewRevision;
	private volatile boolean bankOpen;
	private long lastPersistMillis;
	private Consumer<StorageIndex> observedStoragePersistence = this::persistToConfig;

	@Provides
	SemanticBankSearchConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SemanticBankSearchConfig.class);
	}

	@Override
	protected void startUp()
	{
		index = SemanticBankSearchStorage.deserialize(
			gson,
			configManager.getConfiguration(SemanticBankSearchConfig.GROUP, STORAGE_KEY));
		engine = new SemanticSearchEngine(SemanticLibrary.create());
		storageScanner = new ObservedStorageScanner(
			client::getItemContainer,
			this::isWidgetVisible,
			itemManager::canonicalize,
			this::resolveItemName);
		overlay = new SemanticBankSearchOverlay(config);
		overlayManager.add(overlay);
		panel = new SemanticBankSearchPanel(this::runSearch, this::showIndexedItems, this::clearSearch);
		navigationButton = NavigationButton.builder()
			.tooltip("Semantic Bank Search")
			.icon(createIcon())
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navigationButton);

		bankOpen = isBankOpen();
		startObservedStorageLifecycle(System.currentTimeMillis());
		clearSearch();
	}

	@Override
	protected void shutDown()
	{
		markAllStorageSourcesNotVisible();
		persist();
		if (overlay != null)
		{
			overlayManager.remove(overlay);
		}
		if (navigationButton != null)
		{
			clientToolbar.removeNavigation(navigationButton);
		}

		setViewState(PanelMode.SEARCH, "");
		panel = null;
		overlay = null;
		engine = null;
		storageScanner = null;
		visibleStorageSourceKeys.clear();
		navigationButton = null;
		index = null;
		bankOpen = false;
		lastPersistMillis = 0L;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (index == null)
		{
			return;
		}

		long now = System.currentTimeMillis();
		bankOpen = isBankOpen();
		handleObservedStorageTick(now);
	}

	private void runSearch(String query)
	{
		String cleanedQuery = clean(query);
		if (cleanedQuery.isEmpty())
		{
			clearSearch();
			return;
		}

		long revision = setViewState(PanelMode.SEARCH, cleanedQuery);
		refreshCurrentSearch(cleanedQuery, revision);
	}

	private void clearSearch()
	{
		long revision = setViewState(PanelMode.SEARCH, "");
		synchronized (viewStateLock)
		{
			if (!isCurrentViewRevision(revision))
			{
				return;
			}
			if (overlay != null)
			{
				overlay.setHighlightedItemIds(new ArrayList<>());
			}
			if (panel != null)
			{
				panel.clearResults();
			}
		}
	}

	private void showIndexedItems()
	{
		long revision = setViewState(PanelMode.ALL_INDEXED, "");
		synchronized (viewStateLock)
		{
			if (!isCurrentViewRevision(revision))
			{
				return;
			}
			if (overlay != null)
			{
				overlay.setHighlightedItemIds(new ArrayList<>());
			}
		}
		refreshIndexedItems(revision);
	}

	private void refreshActivePanelMode()
	{
		ViewState viewState = snapshotViewState();
		if (viewState.panelMode == PanelMode.ALL_INDEXED)
		{
			refreshIndexedItems(viewState.revision);
			return;
		}

		refreshCurrentSearch(viewState.query, viewState.revision);
	}

	private void refreshIndexedItems(long revision)
	{
		if (panel == null || index == null)
		{
			return;
		}

		List<ObservedItem> items = index.items();
		items.sort(Comparator
			.comparing((ObservedItem item) -> !isVisibleBankItem(item))
			.thenComparing(ObservedItem::getName, String.CASE_INSENSITIVE_ORDER)
			.thenComparing(ObservedItem::getSourceName, String.CASE_INSENSITIVE_ORDER));
		synchronized (viewStateLock)
		{
			if (!isCurrentViewRevision(revision))
			{
				return;
			}
			panel.updateIndexedItems(items, indexedStatus(items));
		}
	}

	private void refreshCurrentSearch(String query, long revision)
	{
		if (panel == null || overlay == null || engine == null || index == null || query.isEmpty())
		{
			return;
		}

		List<SemanticSearchResult> results = engine.search(query, index);
		List<Integer> highlightedItemIds = new ArrayList<>();
		for (SemanticSearchResult result : results)
		{
			if (result.isHighlightable())
			{
				highlightedItemIds.add(result.getItemId());
			}
		}
		synchronized (viewStateLock)
		{
			if (!isCurrentViewRevision(revision))
			{
				return;
			}
			overlay.setHighlightedItemIds(highlightedItemIds);
			panel.updateResults(query, results, statusText(results));
		}
	}

	private long setViewState(PanelMode panelMode, String query)
	{
		synchronized (viewStateLock)
		{
			this.panelMode = panelMode;
			currentQuery = query == null ? "" : query;
			return ++viewRevision;
		}
	}

	private ViewState snapshotViewState()
	{
		synchronized (viewStateLock)
		{
			return new ViewState(panelMode, currentQuery, viewRevision);
		}
	}

	private boolean isCurrentViewRevision(long revision)
	{
		synchronized (viewStateLock)
		{
			return revision == viewRevision;
		}
	}

	void startObservedStorageLifecycle(long now)
	{
		boolean clearedVisibleStorage = markAllStorageSourcesNotVisible();
		if (config != null && config.rememberObservedStorage())
		{
			observeSafeStorage(now);
		}
		else if (clearedVisibleStorage)
		{
			persist(now);
		}
	}

	boolean handleObservedStorageTick(long now)
	{
		if (index == null || config == null)
		{
			return false;
		}

		if (config.rememberObservedStorage())
		{
			Set<String> previouslyVisibleSourceKeys = new HashSet<>(visibleStorageSourceKeys);
			boolean changed = observeSafeStorage(now);
			boolean visibilityLost = !visibleStorageSourceKeys.containsAll(previouslyVisibleSourceKeys);
			boolean shouldPersist = lastPersistMillis == 0L || now - lastPersistMillis >= SAVE_INTERVAL_MILLIS || visibilityLost;
			if (shouldPersist)
			{
				persist(now);
			}
			if (changed)
			{
				refreshActivePanelMode();
			}
			return shouldPersist;
		}

		if (!visibleStorageSourceKeys.isEmpty())
		{
			markAllStorageSourcesNotVisible();
			refreshActivePanelMode();
			persist(now);
			return true;
		}
		return false;
	}

	void setObservedStorageLifecycleStateForTesting(
		StorageIndex index,
		ObservedStorageScanner storageScanner,
		SemanticBankSearchConfig config)
	{
		this.index = index;
		this.storageScanner = storageScanner;
		this.config = config;
	}

	void setObservedStoragePersistenceForTesting(Consumer<StorageIndex> observedStoragePersistence)
	{
		this.observedStoragePersistence = observedStoragePersistence;
	}

	void rememberVisibleStorageSourceForTesting(ObservedStorageSource source)
	{
		visibleStorageSourceKeys.add(source.key());
	}

	void setLastPersistMillisForTesting(long lastPersistMillis)
	{
		this.lastPersistMillis = lastPersistMillis;
	}

	private boolean observeSafeStorage(long now)
	{
		if (index == null || storageScanner == null || !config.rememberObservedStorage())
		{
			return false;
		}

		List<ObservedStorageSnapshot> snapshots = storageScanner.scan(
			ObservedStorageSource.safeDirectInventorySources(),
			now);
		Set<String> currentlyVisibleSourceKeys = new HashSet<>();
		boolean changed = false;
		for (ObservedStorageSnapshot snapshot : snapshots)
		{
			ObservedStorageSource source = snapshot.getSource();
			currentlyVisibleSourceKeys.add(source.key());
			changed |= SafeStorageObservation.replaceVisibleSourceItemsIfChanged(
				index,
				source,
				snapshot.getItems());
		}

		for (String previousKey : new HashSet<>(visibleStorageSourceKeys))
		{
			if (!currentlyVisibleSourceKeys.contains(previousKey))
			{
				markSourceKeyNotVisible(previousKey);
				changed = true;
			}
		}
		if (!currentlyVisibleSourceKeys.equals(visibleStorageSourceKeys))
		{
			changed = true;
		}
		visibleStorageSourceKeys.clear();
		visibleStorageSourceKeys.addAll(currentlyVisibleSourceKeys);
		index.trimToMaximumEntries(config.maximumRememberedEntries());
		return changed;
	}

	private boolean markAllStorageSourcesNotVisible()
	{
		if (index == null)
		{
			return false;
		}

		boolean changed = SafeStorageObservation.markAllSafeStorageSourcesNotVisible(index);
		visibleStorageSourceKeys.clear();
		return changed;
	}

	private void markSourceKeyNotVisible(String sourceKey)
	{
		for (ObservedStorageSource source : ObservedStorageSource.safeDirectInventorySources())
		{
			if (source.key().equals(sourceKey))
			{
				index.markSourceNotVisible(source.getSourceType(), source.getSourceName());
				return;
			}
		}
	}

	private String resolveItemName(int itemId)
	{
		if (itemId <= 0)
		{
			return "";
		}

		try
		{
			ItemComposition itemComposition = itemManager.getItemComposition(itemId);
			if (itemComposition == null)
			{
				return "";
			}

			String name = clean(itemComposition.getName());
			return name.equalsIgnoreCase("null") ? "" : name;
		}
		catch (RuntimeException ignored)
		{
			return "";
		}
	}

	private void persist()
	{
		persist(System.currentTimeMillis());
	}

	private void persist(long persistedAtMillis)
	{
		if (index == null)
		{
			return;
		}

		observedStoragePersistence.accept(index);
		lastPersistMillis = persistedAtMillis;
	}

	private void persistToConfig(StorageIndex persistedIndex)
	{
		configManager.setConfiguration(
			SemanticBankSearchConfig.GROUP,
			STORAGE_KEY,
			SemanticBankSearchStorage.serialize(gson, persistedIndex));
	}

	private String indexedStatus(List<ObservedItem> items)
	{
		if (items == null || items.isEmpty())
		{
			return "";
		}
		if (!bankOpen)
		{
			return "Open the bank to refresh visible item status.";
		}
		return "Showing " + items.size() + " observed items.";
	}

	private String statusText(List<SemanticSearchResult> results)
	{
		if (results == null || results.isEmpty())
		{
			return "";
		}
		if (!bankOpen)
		{
			return "Open the bank to see visible item highlights.";
		}
		if (!config.enableHighlights())
		{
			return "Bank highlights are disabled in plugin settings.";
		}
		return "";
	}

	private boolean isBankOpen()
	{
		return isWidgetVisible(ComponentID.BANK_ITEM_CONTAINER);
	}

	private boolean isWidgetVisible(int packedComponentId)
	{
		Widget widget = client.getWidget(packedComponentId);
		return widget != null && !widget.isHidden();
	}

	private static boolean isVisibleBankItem(ObservedItem item)
	{
		return item.isCurrentlyVisible() && item.getSourceType() == StorageSourceType.BANK;
	}

	private static String clean(String text)
	{
		return text == null ? "" : text.trim();
	}

	private static BufferedImage createIcon()
	{
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(new Color(28, 31, 35));
		graphics.fillRoundRect(1, 1, 14, 14, 4, 4);
		graphics.setColor(new Color(0, 190, 255));
		graphics.drawRoundRect(1, 1, 13, 13, 4, 4);
		graphics.drawOval(4, 4, 5, 5);
		graphics.drawLine(8, 8, 12, 12);
		graphics.setColor(new Color(255, 152, 0));
		graphics.fillOval(10, 3, 2, 2);
		graphics.dispose();
		return image;
	}
}
