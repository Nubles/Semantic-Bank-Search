package com.semanticbanksearch;

import com.google.gson.Gson;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.InterfaceID;
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
	private static final String BANK_SOURCE_NAME = "Bank";
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
	private SemanticBankSearchPanel panel;
	private SemanticBankSearchOverlay overlay;
	private NavigationButton navigationButton;
	private String currentQuery = "";
	private PanelMode panelMode = PanelMode.SEARCH;
	private long viewRevision;
	private volatile boolean bankOpen;
	private long lastPersistMillis;

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
		if (bankOpen && config.rememberObservedStorage())
		{
			observeBank(System.currentTimeMillis());
		}
		else
		{
			index.markSourceNotVisible(StorageSourceType.BANK, BANK_SOURCE_NAME);
		}
		clearSearch();
	}

	@Override
	protected void shutDown()
	{
		if (index != null)
		{
			index.markSourceNotVisible(StorageSourceType.BANK, BANK_SOURCE_NAME);
		}
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
		boolean currentlyBankOpen = isBankOpen();
		boolean previouslyBankOpen = bankOpen;
		bankOpen = currentlyBankOpen;
		if (currentlyBankOpen && config.rememberObservedStorage())
		{
			observeBank(now);
			if (lastPersistMillis == 0L || now - lastPersistMillis >= SAVE_INTERVAL_MILLIS)
			{
				persist();
			}
			refreshActivePanelMode();
		}
		else if (!currentlyBankOpen && previouslyBankOpen)
		{
			index.markSourceNotVisible(StorageSourceType.BANK, BANK_SOURCE_NAME);
			refreshActivePanelMode();
			persist();
		}
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

	private void observeBank(long now)
	{
		if (index == null || !config.rememberObservedStorage())
		{
			return;
		}

		List<ObservedItem> visibleItems = new ArrayList<>();
		ItemContainer itemContainer = client.getItemContainer(InventoryID.BANK);
		Item[] items = itemContainer == null ? null : itemContainer.getItems();
		if (items != null)
		{
			for (Item item : items)
			{
				if (item == null || item.getId() <= 0 || item.getQuantity() <= 0)
				{
					continue;
				}

				int canonicalId = itemManager.canonicalize(item.getId());
				String name = resolveItemName(canonicalId);
				if (canonicalId > 0 && !name.isEmpty())
				{
					visibleItems.add(new ObservedItem(
						canonicalId,
						name,
						item.getQuantity(),
						StorageSourceType.BANK,
						BANK_SOURCE_NAME,
						true,
						now));
				}
			}
		}

		index.replaceVisibleSourceItems(StorageSourceType.BANK, BANK_SOURCE_NAME, visibleItems);
		index.trimToMaximumEntries(config.maximumRememberedEntries());
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
		if (index == null)
		{
			return;
		}

		configManager.setConfiguration(
			SemanticBankSearchConfig.GROUP,
			STORAGE_KEY,
			SemanticBankSearchStorage.serialize(gson, index));
		lastPersistMillis = System.currentTimeMillis();
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
		Widget bank = client.getWidget(InterfaceID.BANK, 1);
		return bank != null && !bank.isHidden();
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
