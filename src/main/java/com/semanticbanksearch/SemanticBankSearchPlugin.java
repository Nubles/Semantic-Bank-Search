package com.semanticbanksearch;

import com.google.gson.Gson;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.AccountHashChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.plugins.itemstats.Effect;
import net.runelite.client.plugins.itemstats.ItemStatChangesService;
import net.runelite.client.plugins.itemstats.StatChange;
import net.runelite.client.plugins.itemstats.stats.Stats;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Semantic Bank Search",
	description = "Beta: searches observed bank and storage items by purpose using local semantic rules.",
	tags = {"bank", "search", "items", "storage", "utility"}
)
public class SemanticBankSearchPlugin extends Plugin
{
	private static final long SAVE_INTERVAL_MILLIS = 60_000L;
	private static final String LOGGED_OUT_STATUS = "Log in to use account-local storage.";
	private static final String MAXIMUM_REMEMBERED_ENTRIES_KEY = "maximumRememberedEntries";

	private enum PanelMode
	{
		SEARCH,
		ALL_INDEXED,
		READINESS,
		COVERAGE_AUDIT
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
	private ClientThread clientThread;

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

	@Inject
	private ItemStatChangesService itemStatChangesService;

	private final RelativeRankingClassifier rankingClassifier = new RelativeRankingClassifier();
	private final AtomicLong accountSessionEpoch = new AtomicLong();
	private AccountSessionController sessionController;
	private StorageIndex index;
	private SemanticSearchEngine engine;
	private SemanticBankFilter bankFilter;
	private SemanticCoverageAnalyzer coverageAnalyzer;
	private ReadinessAnalyzer readinessAnalyzer;
	private ObservedStorageScanner storageScanner;
	private final Set<String> visibleStorageSourceKeys = new HashSet<>();
	private SemanticBankSearchPanel panel;
	private ThreadBridge threadBridge;
	private SemanticBankSearchOverlay overlay;
	private NavigationButton navigationButton;
	private String currentQuery = "";
	private PanelMode panelMode = PanelMode.SEARCH;
	private long viewRevision;
	private boolean bankOpen;
	private long lastPersistMillis;
	private long lastPersistAttemptMillis;
	private boolean persistenceRetryPending;
	private String pendingStatusNotice = "";
	private String lastPersistenceFailureNotice = "";

	@Provides
	SemanticBankSearchConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SemanticBankSearchConfig.class);
	}

	@Override
	protected void startUp()
	{
		LegacyStorageCleanup.remove(configManager::unsetConfiguration);
		AccountStorageRepository repository = new AccountStorageRepository(
			RuneLite.RUNELITE_DIR.toPath(),
			gson,
			Clock.systemUTC());
		StorageRetentionPolicy retentionPolicy = new StorageRetentionPolicy(
			config.maximumRememberedEntries());
		sessionController = new AccountSessionController(
			repository,
			this::resolveItemName,
			retentionPolicy);
		threadBridge = ThreadBridge.runtime(clientThread);

		List<SemanticRule> rules = SemanticLibrary.create();
		engine = new SemanticSearchEngine(rules);
		bankFilter = new SemanticBankFilter(engine, this::resolveItemMetadata, this::visibleBankItemIds);
		coverageAnalyzer = new SemanticCoverageAnalyzer(rules);
		readinessAnalyzer = new ReadinessAnalyzer(rules, ReadinessPackLibrary.create());
		storageScanner = new ObservedStorageScanner(
			client::getItemContainer,
			this::isWidgetVisible,
			itemManager::canonicalize,
			this::resolveItemName);
		overlay = new SemanticBankSearchOverlay(config);
		overlayManager.add(overlay);
		panel = createPanel();
		navigationButton = NavigationButton.builder()
			.tooltip("Semantic Bank Search")
			.icon(createIcon())
			.priority(7)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navigationButton);

		bankOpen = isBankOpen();
		handleAccountHashChanged(client.getAccountHash());
	}

	@Override
	protected void shutDown()
	{
		beginAccountTransition();
		markAllStorageSourcesNotVisible();
		if (sessionController != null)
		{
			sessionController.deactivate();
		}
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
		threadBridge = null;
		sessionController = null;
		overlay = null;
		engine = null;
		bankFilter = null;
		coverageAnalyzer = null;
		readinessAnalyzer = null;
		storageScanner = null;
		visibleStorageSourceKeys.clear();
		navigationButton = null;
		index = null;
		bankOpen = false;
		lastPersistMillis = 0L;
		persistenceRetryPending = false;
		pendingStatusNotice = "";
	}

	@Subscribe
	public void onAccountHashChanged(AccountHashChanged event)
	{
		handleAccountHashChanged(client.getAccountHash());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event != null)
		{
			handleGameStateChanged(event.getGameState(), client.getAccountHash());
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event == null
			|| !SemanticBankSearchConfig.GROUP.equals(event.getGroup())
			|| !MAXIMUM_REMEMBERED_ENTRIES_KEY.equals(event.getKey()))
		{
			return;
		}

		ThreadBridge bridge = threadBridge;
		if (bridge == null)
		{
			return;
		}

		long capturedEpoch = accountSessionEpoch.get();
		bridge.submitClient(() -> {
			if (capturedEpoch != accountSessionEpoch.get())
			{
				return;
			}
			applyRetentionConfigChange();
		});
	}

	private void applyRetentionConfigChange()
	{
		if (config == null || sessionController == null)
		{
			return;
		}

		boolean trimmed = sessionController.updateRetentionPolicy(
			config.maximumRememberedEntries());
		if (!trimmed)
		{
			return;
		}

		refreshActivePanelMode();
		persist(System.currentTimeMillis());
	}

	void handleAccountHashChanged(long accountHash)
	{
		if (sessionController == null)
		{
			return;
		}

		AccountKey requestedAccount = AccountKey.fromAccountHash(accountHash).orElse(null);
		if (requestedAccount == null)
		{
			deactivateAccount();
			return;
		}
		if (requestedAccount.equals(sessionController.activeAccountKeyOrNull()))
		{
			index = sessionController.activeIndexOrNull();
			return;
		}

		beginAccountTransition();
		markAllStorageSourcesNotVisible();
		publishAccountTransitionClear();
		lastPersistMillis = 0L;

		AccountSessionUpdate update = sessionController.switchTo(accountHash);
		index = update.getActiveIndex();
		if (index != null)
		{
			startObservedStorageLifecycle(System.currentTimeMillis());
		}
		long finalRevision = setViewState(PanelMode.SEARCH, "");
		String finalStatus = lastPersistenceFailureNotice.isEmpty()
			? latestNotice(update, "")
			: lastPersistenceFailureNotice;
		publishPanelSnapshot(PanelViewSnapshot.clear(finalRevision, finalStatus));
	}

	void handleGameStateChanged(GameState gameState, long accountHash)
	{
		if (gameState == GameState.LOGGED_IN)
		{
			handleAccountHashChanged(accountHash);
		}
		else if (gameState == GameState.LOGIN_SCREEN
			|| gameState == GameState.LOGIN_SCREEN_AUTHENTICATOR)
		{
			deactivateAccount();
		}
	}

	private void deactivateAccount()
	{
		beginAccountTransition();
		markAllStorageSourcesNotVisible();
		publishAccountTransitionClear();
		AccountSessionUpdate update = sessionController == null
			? null
			: sessionController.deactivate();
		index = null;
		visibleStorageSourceKeys.clear();
		lastPersistMillis = 0L;
		long finalRevision = setViewState(PanelMode.SEARCH, "");
		publishPanelSnapshot(PanelViewSnapshot.clear(
			finalRevision,
			latestNotice(update, LOGGED_OUT_STATUS)));
	}

	private static String latestNotice(AccountSessionUpdate update, String fallback)
	{
		if (update == null || update.getNotices().isEmpty())
		{
			return fallback;
		}
		List<String> notices = update.getNotices();
		return notices.get(notices.size() - 1);
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

	@Subscribe
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		if (event == null || !"bankSearchFilter".equals(event.getEventName()) || bankFilter == null)
		{
			return;
		}

		int intStackSize = client.getIntStackSize();
		int objectStackSize = client.getObjectStackSize();
		if (intStackSize < 2 || objectStackSize < 1)
		{
			return;
		}

		int[] intStack = client.getIntStack();
		Object[] objectStack = client.getObjectStack();
		Object inputValue = objectStack[objectStackSize - 1];
		if (!(inputValue instanceof String))
		{
			return;
		}

		int itemId = intStack[intStackSize - 1];
		Boolean decision = bankFilter.decision((String) inputValue, itemId);
		if (decision != null)
		{
			intStack[intStackSize - 2] = decision ? 1 : 0;
		}
	}

	SemanticBankSearchPanel createPanel()
	{
		return new SemanticBankSearchPanel(
			query -> submitClientCommand(() -> runSearch(query)),
			() -> submitClientCommand(this::showIndexedItems),
			query -> submitClientCommand(() -> runReadiness(query)),
			() -> submitClientCommand(this::showCoverageAudit),
			() -> submitClientCommand(this::clearSearch));
	}

	private void submitClientCommand(Runnable command)
	{
		ThreadBridge bridge = threadBridge;
		long submittedEpoch = accountSessionEpoch.get();
		if (bridge != null)
		{
			bridge.submitClient(() -> {
				if (accountSessionEpoch.get() == submittedEpoch)
				{
					command.run();
				}
			});
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

	private void runReadiness(String query)
	{
		String cleanedQuery = clean(query);
		long revision = setViewState(PanelMode.READINESS, cleanedQuery);
		if (cleanedQuery.isEmpty())
		{
			refreshReadiness(cleanedQuery, revision);
			return;
		}

		refreshReadiness(cleanedQuery, revision);
	}
	private void clearSearch()
	{
		long revision = setViewState(PanelMode.SEARCH, "");
		clearOverlayHighlights();
		String status = nextSnapshotStatus(hasActiveAccount() ? "" : LOGGED_OUT_STATUS);
		publishPanelSnapshot(PanelViewSnapshot.clear(revision, status));
	}

	private boolean hasActiveAccount()
	{
		return index != null
			&& sessionController != null
			&& sessionController.activeAccountKeyOrNull() != null;
	}

	private void showIndexedItems()
	{
		long revision = setViewState(PanelMode.ALL_INDEXED, "");
		clearOverlayHighlights();
		refreshIndexedItems(revision);
	}

	private void showCoverageAudit()
	{
		long revision = setViewState(PanelMode.COVERAGE_AUDIT, "");
		clearOverlayHighlights();
		refreshCoverageAudit(revision);
	}

	private void refreshActivePanelMode()
	{
		ViewState viewState = snapshotViewState();
		if (viewState.panelMode == PanelMode.ALL_INDEXED)
		{
			refreshIndexedItems(viewState.revision);
			return;
		}
		if (viewState.panelMode == PanelMode.COVERAGE_AUDIT)
		{
			refreshCoverageAudit(viewState.revision);
			return;
		}
		if (viewState.panelMode == PanelMode.READINESS)
		{
			refreshReadiness(viewState.query, viewState.revision);
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
		PanelViewSnapshot snapshot = PanelViewSnapshot.allIndexed(
			revision,
			items,
			nextSnapshotStatus(indexedStatus(items)));
		if (isCurrentViewRevision(revision))
		{
			publishPanelSnapshot(snapshot);
		}
	}

	private void refreshCoverageAudit(long revision)
	{
		if (panel == null || coverageAnalyzer == null || index == null)
		{
			return;
		}

		List<SemanticCoverageResult> results = coverageAnalyzer.analyze(index.items());
		PanelViewSnapshot snapshot = PanelViewSnapshot.coverageAudit(
			revision,
			results,
			nextSnapshotStatus(coverageStatus(results)));
		if (isCurrentViewRevision(revision))
		{
			publishPanelSnapshot(snapshot);
		}
	}

	private void refreshReadiness(String query, long revision)
	{
		if (panel == null || overlay == null || readinessAnalyzer == null || index == null)
		{
			return;
		}

		ReadinessResult result = readinessAnalyzer.analyze(query, index);
		List<Integer> highlightedItemIds = readinessHighlightedItemIds(result);
		PanelViewSnapshot snapshot = PanelViewSnapshot.readiness(
			revision,
			query,
			result,
			nextSnapshotStatus(readinessStatus(result)));
		if (isCurrentViewRevision(revision))
		{
			overlay.setHighlightedItemIds(highlightedItemIds);
			publishPanelSnapshot(snapshot);
		}
	}

	private void refreshCurrentSearch(String query, long revision)
	{
		if (panel == null || overlay == null || engine == null || index == null || query.isEmpty())
		{
			return;
		}

		List<SemanticSearchResult> results = rankingClassifier.recognizes(query)
			? relativeRankingResults(query)
			: engine.search(query, index);
		List<Integer> highlightedItemIds = new ArrayList<>();
		for (SemanticSearchResult result : results)
		{
			if (result.isHighlightable())
			{
				highlightedItemIds.add(result.getItemId());
			}
		}
		PanelViewSnapshot snapshot = PanelViewSnapshot.search(
			revision,
			query,
			results,
			nextSnapshotStatus(statusText(results)));
		if (isCurrentViewRevision(revision))
		{
			overlay.setHighlightedItemIds(highlightedItemIds);
			publishPanelSnapshot(snapshot);
		}
	}

	private void clearOverlayHighlights()
	{
		if (overlay != null)
		{
			overlay.setHighlightedItemIds(new ArrayList<>());
		}
	}

	private void publishPanelSnapshot(PanelViewSnapshot snapshot)
	{
		ThreadBridge bridge = threadBridge;
		SemanticBankSearchPanel targetPanel = panel;
		long publishedEpoch = accountSessionEpoch.get();
		if (bridge == null || targetPanel == null || snapshot == null)
		{
			return;
		}
		bridge.submitSwing(() -> {
			if (accountSessionEpoch.get() == publishedEpoch)
			{
				targetPanel.applySnapshot(snapshot);
			}
		});
	}

	private void beginAccountTransition()
	{
		accountSessionEpoch.incrementAndGet();
		lastPersistAttemptMillis = 0L;
		persistenceRetryPending = false;
		pendingStatusNotice = "";
		lastPersistenceFailureNotice = "";
	}

	private void publishAccountTransitionClear()
	{
		long transitionRevision = setViewState(PanelMode.SEARCH, "");
		clearOverlayHighlights();
		publishPanelSnapshot(PanelViewSnapshot.clear(transitionRevision));
	}

	private String nextSnapshotStatus(String fallback)
	{
		if (pendingStatusNotice.isEmpty())
		{
			return fallback;
		}

		String notice = pendingStatusNotice;
		pendingStatusNotice = "";
		return notice;
	}

	private List<SemanticSearchResult> relativeRankingResults(String query)
	{
		List<ObservedItem> observedItems = index.items();
		Map<Integer, BankItemMetadata> metadataById = new HashMap<>();
		List<BankItemMetadata> ownedMetadata = new ArrayList<>();
		for (ObservedItem item : observedItems)
		{
			if (!metadataById.containsKey(item.getItemId()))
			{
				BankItemMetadata metadata = resolveItemMetadata(item.getItemId());
				metadataById.put(item.getItemId(), metadata);
				if (metadata != null)
				{
					ownedMetadata.add(metadata);
				}
			}
		}

		List<SemanticSearchResult> results = new ArrayList<>();
		for (ObservedItem item : observedItems)
		{
			BankItemMetadata metadata = metadataById.get(item.getItemId());
			if (rankingClassifier.matches(query, metadata, ownedMetadata))
			{
				results.add(new SemanticSearchResult(
					item.getItemId(),
					item.getName(),
					item.getQuantity(),
					item.getSourceType(),
					item.getSourceName(),
					item.isCurrentlyVisible(),
					"Owned ranking",
					rankingClassifier.explanation(query, metadata),
					rankingClassifier.score(query, metadata)));
			}
		}
		results.sort(Comparator
			.comparingInt(SemanticSearchResult::getScore).reversed()
			.thenComparing(SemanticSearchResult::getItemName, String.CASE_INSENSITIVE_ORDER));
		return results;
	}
	private static List<Integer> readinessHighlightedItemIds(ReadinessResult result)
	{
		List<Integer> highlightedItemIds = new ArrayList<>();
		if (result == null || !result.isMatched())
		{
			return highlightedItemIds;
		}
		for (ReadinessSlotResult slotResult : result.getSlotResults())
		{
			for (SemanticSearchResult item : slotResult.getOwnedItems())
			{
				if (item.isHighlightable() && !highlightedItemIds.contains(item.getItemId()))
				{
					highlightedItemIds.add(item.getItemId());
				}
			}
		}
		return highlightedItemIds;
	}

	private long setViewState(PanelMode panelMode, String query)
	{
		this.panelMode = panelMode;
		currentQuery = query == null ? "" : query;
		return ++viewRevision;
	}

	private ViewState snapshotViewState()
	{
		return new ViewState(panelMode, currentQuery, viewRevision);
	}

	private boolean isCurrentViewRevision(long revision)
	{
		return revision == viewRevision;
	}
	void startObservedStorageLifecycle(long now)
	{
		boolean clearedVisibleStorage = markAllStorageSourcesNotVisible();
		boolean observingStorage = config != null && config.rememberObservedStorage();
		if (observingStorage)
		{
			observeSafeStorage(now);
		}
		boolean trimmedStorage = trimActiveIndex();
		if (trimmedStorage || (!observingStorage && clearedVisibleStorage))
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
			boolean trimmedStorage = trimActiveIndex();
			boolean visibilityLost = !visibleStorageSourceKeys.containsAll(previouslyVisibleSourceKeys);
			boolean scheduledPersistence = persistenceRetryPending
				? persistenceRetryDue(now)
				: lastPersistMillis == 0L || now - lastPersistMillis >= SAVE_INTERVAL_MILLIS;
			boolean shouldPersist = trimmedStorage || visibilityLost || scheduledPersistence;
			if (changed || trimmedStorage)
			{
				refreshActivePanelMode();
			}
			if (shouldPersist)
			{
				persist(now);
			}
			return shouldPersist;
		}

		boolean hadVisibleStorage = !visibleStorageSourceKeys.isEmpty();
		if (hadVisibleStorage)
		{
			markAllStorageSourcesNotVisible();
		}
		boolean trimmedStorage = trimActiveIndex();
		boolean scheduledRetry = persistenceRetryPending && persistenceRetryDue(now);
		if (scheduledRetry || hadVisibleStorage || trimmedStorage)
		{
			refreshActivePanelMode();
			persist(now);
			return true;
		}
		return false;
	}

	private boolean persistenceRetryDue(long now)
	{
		return now - lastPersistAttemptMillis >= SAVE_INTERVAL_MILLIS;
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

	void rememberVisibleStorageSourceForTesting(ObservedStorageSource source)
	{
		visibleStorageSourceKeys.add(source.key());
	}

	void setLastPersistMillisForTesting(long lastPersistMillis)
	{
		this.lastPersistMillis = lastPersistMillis;
	}

	void setSearchComponentsForTesting(
		StorageIndex index,
		SemanticCoverageAnalyzer coverageAnalyzer,
		SemanticBankSearchPanel panel,
		SemanticBankSearchOverlay overlay)
	{
		setSearchComponentsForTesting(index, coverageAnalyzer, null, panel, overlay);
	}

	void setSearchComponentsForTesting(
		StorageIndex index,
		SemanticCoverageAnalyzer coverageAnalyzer,
		ReadinessAnalyzer readinessAnalyzer,
		SemanticBankSearchPanel panel,
		SemanticBankSearchOverlay overlay)
	{
		this.index = index;
		this.coverageAnalyzer = coverageAnalyzer;
		this.readinessAnalyzer = readinessAnalyzer;
		this.panel = panel;
		this.overlay = overlay;
	}

	void setRuntimeDependenciesForTesting(
		AccountSessionController sessionController,
		ThreadBridge threadBridge)
	{
		this.sessionController = sessionController;
		this.threadBridge = threadBridge;
	}

	void showCoverageAuditForTesting()
	{
		showCoverageAudit();
	}

	void showReadinessForTesting(String query)
	{
		runReadiness(query);
	}

	String coverageStatusForTesting(List<SemanticCoverageResult> results)
	{
		return coverageStatus(results);
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
		return changed;
	}

	private boolean trimActiveIndex()
	{
		return index != null
			&& sessionController != null
			&& sessionController.trimActiveIndex();
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

	private BankItemMetadata resolveItemMetadata(int itemId)
	{
		ItemComposition itemComposition = itemManager.getItemComposition(itemId);
		if (itemComposition == null)
		{
			return null;
		}

		String[] actions = itemComposition.getInventoryActions();
		boolean wieldable = false;
		boolean actionEquipable = false;
		boolean edible = false;
		boolean drinkable = false;
		if (actions != null)
		{
			for (String action : actions)
			{
				if ("Wield".equalsIgnoreCase(action))
				{
					wieldable = true;
				}
				actionEquipable |= "Wield".equalsIgnoreCase(action) || "Wear".equalsIgnoreCase(action);
				edible |= "Eat".equalsIgnoreCase(action);
				drinkable |= "Drink".equalsIgnoreCase(action);
			}
		}

		ItemStats stats = itemManager.getItemStats(itemId);
		ItemEquipmentStats equipment = stats == null ? null : stats.getEquipment();
		return new BankItemMetadata(
			resolveItemName(itemId),
			actionEquipable || stats != null && stats.isEquipable(),
			wieldable,
			edible,
			drinkable,
			equipment == null ? -1 : equipment.getSlot(),
			equipment == null ? 0 : equipment.getAstab(),
			equipment == null ? 0 : equipment.getAslash(),
			equipment == null ? 0 : equipment.getAcrush(),
			equipment == null ? 0 : equipment.getAmagic(),
			equipment == null ? 0 : equipment.getArange(),
			equipment == null ? 0 : equipment.getStr(),
			equipment == null ? 0 : equipment.getRstr(),
			equipment == null ? 0 : equipment.getMdmg(),
			equipment == null ? 0 : equipment.getPrayer(),
			resolveHealing(itemId));
	}

	private int resolveHealing(int itemId)
	{
		try
		{
			Effect effect = itemStatChangesService.getItemStatChanges(itemId);
			if (effect == null)
			{
				return -1;
			}

			for (StatChange change : effect.calculate(client).getStatChanges())
			{
				if (change != null && change.getStat() == Stats.HITPOINTS)
				{
					return Math.max(0, change.getTheoretical());
				}
			}
		}
		catch (RuntimeException ignored)
		{
			// Missing or level-dependent data should not satisfy numerical queries.
		}
		return -1;
	}


	private List<Integer> visibleBankItemIds()
	{
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank == null)
		{
			return new ArrayList<>();
		}

		List<Integer> itemIds = new ArrayList<>();
		Set<Integer> seen = new HashSet<>();
		for (Item item : bank.getItems())
		{
			if (item != null && item.getId() > 0 && seen.add(item.getId()))
			{
				itemIds.add(item.getId());
			}
		}
		return itemIds;
	}

	private void persist(long persistedAtMillis)
	{
		if (!hasActiveAccount())
		{
			return;
		}

		lastPersistAttemptMillis = persistedAtMillis;
		String failureNotice = sessionController.persist().orElse("");
		if (!failureNotice.isEmpty())
		{
			lastPersistenceFailureNotice = failureNotice;
			boolean firstFailure = !persistenceRetryPending;
			persistenceRetryPending = true;
			if (firstFailure)
			{
				publishPersistenceFailureNotice(failureNotice);
			}
			return;
		}

		persistenceRetryPending = false;
		pendingStatusNotice = "";
		lastPersistenceFailureNotice = "";
		lastPersistMillis = persistedAtMillis;
	}

	private void publishPersistenceFailureNotice(String failureNotice)
	{
		pendingStatusNotice = failureNotice;
		if (panel == null)
		{
			return;
		}

		long noticeRevision = setViewState(panelMode, currentQuery);
		if (panelMode == PanelMode.SEARCH && currentQuery.isEmpty())
		{
			publishPanelSnapshot(PanelViewSnapshot.clear(
				noticeRevision,
				nextSnapshotStatus("")));
			return;
		}
		refreshActivePanelMode();
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

	private String coverageStatus(List<SemanticCoverageResult> results)
	{
		if (results == null || results.isEmpty())
		{
			return "";
		}

		int covered = 0;
		for (SemanticCoverageResult result : results)
		{
			if (result.isCovered())
			{
				covered++;
			}
		}
		return "Covered " + covered + " of " + results.size() + " observed items.";
	}

	private String readinessStatus(ReadinessResult result)
	{
		if (result == null || !result.isMatched())
		{
			return "";
		}
		int required = result.getRequiredSlotCount();
		if (required == 0)
		{
			return "Readiness: no required slots in this pack.";
		}
		return "Readiness: " + result.getCoveredRequiredSlotCount() + " of " + required + " required slots covered.";
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
