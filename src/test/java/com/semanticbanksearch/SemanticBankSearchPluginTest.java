package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import java.awt.Component;
import java.awt.Container;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import net.runelite.api.GameState;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SemanticBankSearchPluginTest
{
    private static final long ACCOUNT_A = 101L;
    private static final long ACCOUNT_B = 202L;
    private static final String LOGGED_OUT_STATUS = "Log in to use account-local storage.";

    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void accountChangeClearsOldHighlightsBeforeLoadingNewIndex() throws IOException
    {
        AccountStorageRepository repository = repository();
        repository.save(accountKey(ACCOUNT_B), index(202, "Account B item", false));
        AtomicReference<RecordingOverlay> overlayReference = new AtomicReference<>();
        AtomicBoolean highlightsClearedDuringLoad = new AtomicBoolean();
        AccountSessionController controller = new AccountSessionController(
            repository,
            itemId -> {
                RecordingOverlay overlay = overlayReference.get();
                highlightsClearedDuringLoad.set(
                    overlay != null && overlay.lastHighlightedItemIds.isEmpty());
                return "Loaded item " + itemId;
            },
            new StorageRetentionPolicy(800));
        StorageIndex firstIndex = controller.switchTo(ACCOUNT_A).getActiveIndex();
        firstIndex.record(101, "Account A item", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        RecordingOverlay overlay = new RecordingOverlay(config(true));
        overlay.setHighlightedItemIds(Collections.singletonList(101));
        overlayReference.set(overlay);
        SemanticBankSearchPlugin plugin = new SemanticBankSearchPlugin();
        plugin.setObservedStorageLifecycleStateForTesting(firstIndex, noVisibleStorageScanner(), config(true));
        plugin.setSearchComponentsForTesting(
            firstIndex,
            new SemanticCoverageAnalyzer(Collections.emptyList()),
            null,
            overlay);
        plugin.setRuntimeDependenciesForTesting(
            controller,
            new ThreadBridge(Runnable::run, Runnable::run));
        plugin.rememberVisibleStorageSourceForTesting(ObservedStorageSource.bank());

        plugin.handleAccountHashChanged(ACCOUNT_B);

        assertTrue(highlightsClearedDuringLoad.get());
        assertTrue(overlay.lastHighlightedItemIds.isEmpty());
        assertEquals(accountKey(ACCOUNT_B), controller.activeAccountKeyOrNull());
        assertTrue(containsItem(controller.activeIndexOrNull(), 202));
        assertFalse(repository.load(accountKey(ACCOUNT_A), id -> "Item " + id)
            .index().items().get(0).isCurrentlyVisible());
    }

    @Test
    public void loginScreenDeactivatesAccount() throws IOException
    {
        TestRuntime runtime = runtime(config(true), noVisibleStorageScanner());
        runtime.index.record(101, "Account A item", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        runtime.plugin.rememberVisibleStorageSourceForTesting(ObservedStorageSource.bank());

        runtime.plugin.handleGameStateChanged(GameState.LOGIN_SCREEN, ACCOUNT_A);

        assertNull(runtime.controller.activeAccountKeyOrNull());
        assertNull(runtime.controller.activeIndexOrNull());
        StorageIndex persisted = runtime.repository.load(accountKey(ACCOUNT_A), id -> "Item " + id).index();
        assertTrue(containsItem(persisted, 101));
        assertFalse(persisted.items().get(0).isCurrentlyVisible());
    }

    @Test
    public void loggedInStateActivatesCurrentAccountHash()
    {
        AccountSessionController controller = controller();
        SemanticBankSearchPlugin plugin = new SemanticBankSearchPlugin();
        plugin.setObservedStorageLifecycleStateForTesting(null, noVisibleStorageScanner(), config(true));
        plugin.setRuntimeDependenciesForTesting(
            controller,
            new ThreadBridge(Runnable::run, Runnable::run));

        plugin.handleGameStateChanged(GameState.LOGGED_IN, ACCOUNT_A);

        assertEquals(accountKey(ACCOUNT_A), controller.activeAccountKeyOrNull());
        assertTrue(controller.activeIndexOrNull().items().isEmpty());
    }

    @Test
    public void transientGameStatesKeepActiveAccount()
    {
        TestRuntime runtime = runtime(config(true), noVisibleStorageScanner());
        StorageIndex activeIndex = runtime.controller.activeIndexOrNull();

        runtime.plugin.handleGameStateChanged(GameState.HOPPING, 0L);
        runtime.plugin.handleGameStateChanged(GameState.CONNECTION_LOST, 0L);
        runtime.plugin.handleGameStateChanged(GameState.LOADING, 0L);

        assertEquals(accountKey(ACCOUNT_A), runtime.controller.activeAccountKeyOrNull());
        assertSame(activeIndex, runtime.controller.activeIndexOrNull());
    }

    @Test
    public void panelActionsAreDeferredOntoClientThread() throws Exception
    {
        List<Runnable> clientTasks = new ArrayList<>();
        List<Runnable> swingTasks = new ArrayList<>();
        SemanticBankSearchPlugin plugin = new SemanticBankSearchPlugin();
        plugin.setRuntimeDependenciesForTesting(
            controller(),
            new ThreadBridge(clientTasks::add, swingTasks::add));
        AtomicReference<SemanticBankSearchPanel> panel = new AtomicReference<>();
        runOnEdt(() -> panel.set(plugin.createPanel()));

        runOnEdt(() -> {
            findButton(panel.get(), "Search").doClick();
            findButton(panel.get(), "All").doClick();
            findButton(panel.get(), "Readiness").doClick();
            findButton(panel.get(), "Coverage").doClick();
            findButton(panel.get(), "Clear").doClick();
        });

        assertEquals(5, clientTasks.size());
        assertTrue(swingTasks.isEmpty());
    }

    @Test
    public void panelRendersAreDeferredOntoSwingThread() throws Exception
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "Coins", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
        SemanticBankSearchPanel panel = createPanelOnEdt();
        List<Runnable> swingTasks = new ArrayList<>();
        SemanticBankSearchPlugin plugin = new SemanticBankSearchPlugin();
        plugin.setSearchComponentsForTesting(
            index,
            new SemanticCoverageAnalyzer(Collections.emptyList()),
            panel,
            null);
        plugin.setRuntimeDependenciesForTesting(
            controller(),
            new ThreadBridge(Runnable::run, swingTasks::add));
        AtomicReference<Long> initialRevision = new AtomicReference<>();
        runOnEdt(() -> initialRevision.set(panel.lastRenderedRevisionForTesting()));

        plugin.showCoverageAuditForTesting();

        assertEquals(1, swingTasks.size());
        runOnEdt(() -> assertEquals(initialRevision.get().longValue(), panel.lastRenderedRevisionForTesting()));
        runOnEdt(swingTasks.get(0));
        runOnEdt(() -> assertEquals(1L, panel.lastRenderedRevisionForTesting()));
    }

    @Test
    public void noActiveAccountProducesEmptyLoggedOutSnapshot() throws Exception
    {
        StorageIndex staleIndex = index(101, "Stale account item", true);
        RecordingOverlay overlay = new RecordingOverlay(config(true));
        overlay.setHighlightedItemIds(Collections.singletonList(101));
        AtomicReference<RecordingPanel> panelReference = new AtomicReference<>();
        runOnEdt(() -> panelReference.set(new RecordingPanel()));
        List<Runnable> swingTasks = new ArrayList<>();
        AccountSessionController controller = controller();
        SemanticBankSearchPlugin plugin = new SemanticBankSearchPlugin();
        plugin.setSearchComponentsForTesting(
            staleIndex,
            new SemanticCoverageAnalyzer(Collections.emptyList()),
            panelReference.get(),
            overlay);
        plugin.setRuntimeDependenciesForTesting(
            controller,
            new ThreadBridge(Runnable::run, swingTasks::add));

        plugin.handleAccountHashChanged(0L);

        assertEquals(1, swingTasks.size());
        assertTrue(overlay.lastHighlightedItemIds.isEmpty());
        runOnEdt(swingTasks.get(0));
        PanelViewSnapshot snapshot = panelReference.get().lastSnapshot;
        assertEquals(PanelViewSnapshot.Kind.CLEAR, snapshot.getKind());
        assertEquals(LOGGED_OUT_STATUS, snapshot.getStatus());
        assertTrue(snapshot.getSearchResults().isEmpty());
        assertTrue(snapshot.getIndexedItems().isEmpty());
        assertNull(controller.activeIndexOrNull());
    }

    @Test
    public void accountNoticeAppearsInNextSnapshot() throws Exception
    {
        Path damagedIndex = indexPath(ACCOUNT_B);
        Files.createDirectories(damagedIndex.getParent());
        Files.writeString(damagedIndex, "{damaged", StandardCharsets.UTF_8);
        AccountSessionController controller = controller();
        StorageIndex firstIndex = controller.switchTo(ACCOUNT_A).getActiveIndex();
        AtomicReference<RecordingPanel> panelReference = new AtomicReference<>();
        runOnEdt(() -> panelReference.set(new RecordingPanel()));
        List<Runnable> swingTasks = new ArrayList<>();
        SemanticBankSearchPlugin plugin = new SemanticBankSearchPlugin();
        plugin.setObservedStorageLifecycleStateForTesting(firstIndex, noVisibleStorageScanner(), config(true));
        plugin.setSearchComponentsForTesting(
            firstIndex,
            new SemanticCoverageAnalyzer(Collections.emptyList()),
            panelReference.get(),
            null);
        plugin.setRuntimeDependenciesForTesting(
            controller,
            new ThreadBridge(Runnable::run, swingTasks::add));

        plugin.handleAccountHashChanged(ACCOUNT_B);

        assertEquals(1, swingTasks.size());
        runOnEdt(swingTasks.get(0));
        assertEquals(
            "A damaged local index was quarantined; this account will rebuild as storage is observed.",
            panelReference.get().lastSnapshot.getStatus());
    }

    @Test
    public void shutdownPersistsAndDeactivatesAccount() throws IOException
    {
        TestRuntime runtime = runtime(config(true), noVisibleStorageScanner());
        runtime.index.record(101, "Account A item", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        runtime.plugin.rememberVisibleStorageSourceForTesting(ObservedStorageSource.bank());

        runtime.plugin.shutDown();

        assertNull(runtime.controller.activeAccountKeyOrNull());
        assertNull(runtime.controller.activeIndexOrNull());
        StorageIndex persisted = runtime.repository.load(accountKey(ACCOUNT_A), id -> "Item " + id).index();
        assertTrue(containsItem(persisted, 101));
        assertFalse(persisted.items().get(0).isCurrentlyVisible());
    }

    @Test
    public void startupWithRememberedVisibleStorageAndNoVisibleWidgetClearsVisibleStatusOnly()
    {
        TestRuntime runtime = runtime(
            index(101, "Ranarr seed", true, StorageSourceType.OTHER_STORAGE, "Seed Vault"),
            config(true),
            noVisibleStorageScanner());

        runtime.plugin.startObservedStorageLifecycle(2_000L);

        assertEquals(1, runtime.index.items().size());
        assertEquals("Ranarr seed", runtime.index.items().get(0).getName());
        assertFalse(runtime.index.items().get(0).isCurrentlyVisible());
    }

    @Test
    public void visibleSourceCloseMarksSourceNotVisibleAndRetainsRememberedItems()
    {
        TestRuntime runtime = runtime(
            index(101, "Ranarr seed", true, StorageSourceType.OTHER_STORAGE, "Seed Vault"),
            config(true),
            noVisibleStorageScanner());
        runtime.plugin.rememberVisibleStorageSourceForTesting(ObservedStorageSource.seedVault());

        runtime.plugin.handleObservedStorageTick(2_000L);

        assertEquals(1, runtime.index.items().size());
        assertEquals("Ranarr seed", runtime.index.items().get(0).getName());
        assertFalse(runtime.index.items().get(0).isCurrentlyVisible());
    }

    @Test
    public void configOffTransitionFromVisibleSafeStorageClearsVisibleStatus()
    {
        TestRuntime runtime = runtime(
            index(100, "Prayer potion(4)", true),
            config(false),
            noVisibleStorageScanner());
        runtime.plugin.rememberVisibleStorageSourceForTesting(ObservedStorageSource.bank());

        runtime.plugin.handleObservedStorageTick(2_000L);

        assertEquals(1, runtime.index.items().size());
        assertFalse(runtime.index.items().get(0).isCurrentlyVisible());
    }

    @Test
    public void visibilityLossPersistsImmediately()
    {
        TestRuntime runtime = runtime(
            index(101, "Ranarr seed", true, StorageSourceType.OTHER_STORAGE, "Seed Vault"),
            config(true),
            noVisibleStorageScanner());
        runtime.plugin.rememberVisibleStorageSourceForTesting(ObservedStorageSource.seedVault());
        runtime.plugin.setLastPersistMillisForTesting(1_950L);

        runtime.plugin.handleObservedStorageTick(2_000L);

        StorageIndex persisted = persistedIndex(runtime);
        assertTrue(containsItem(persisted, 101));
        assertFalse(persisted.items().get(0).isCurrentlyVisible());
    }

    @Test
    public void startupWithRememberingDisabledPersistsClearedVisibleStatus()
    {
        TestRuntime runtime = runtime(
            index(101, "Ranarr seed", true, StorageSourceType.OTHER_STORAGE, "Seed Vault"),
            config(false),
            noVisibleStorageScanner());

        runtime.plugin.startObservedStorageLifecycle(2_000L);

        assertFalse(runtime.index.items().get(0).isCurrentlyVisible());
        StorageIndex persisted = persistedIndex(runtime);
        assertTrue(containsItem(persisted, 101));
        assertFalse(persisted.items().get(0).isCurrentlyVisible());
    }

    @Test
    public void startupWithRememberingDisabledTrimsOversizedIndexAndPersists()
    {
        StorageIndex oversizedIndex = new StorageIndex();
        for (int itemId = 1; itemId <= 801; itemId++)
        {
            oversizedIndex.record(
                itemId,
                "Item " + itemId,
                1,
                StorageSourceType.BANK,
                "Bank",
                false,
                itemId);
        }
        TestRuntime runtime = runtime(
            oversizedIndex,
            config(false),
            noVisibleStorageScanner());

        runtime.plugin.startObservedStorageLifecycle(2_000L);

        assertEquals(800, runtime.index.items().size());
        assertEquals(800, persistedIndex(runtime).items().size());
    }
    @Test
    public void coverageStatusCountsCoveredObservedItems()
    {
        SemanticBankSearchPlugin plugin = new SemanticBankSearchPlugin();
        List<SemanticCoverageResult> results = Arrays.asList(
            coverageResult("Prayer potion(4)", Collections.singletonList("Prayer restoration")),
            coverageResult("Mystery item", Collections.emptyList()));

        assertEquals("Covered 1 of 2 observed items.", plugin.coverageStatusForTesting(results));
    }

    @Test
    public void coverageAuditClearsOverlayHighlightsWithoutAddingCoverageHighlights()
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "Prayer potion(4)", 3, StorageSourceType.BANK, "Bank", true, 1_000L);
        RecordingOverlay overlay = new RecordingOverlay(config(true));
        overlay.setHighlightedItemIds(Collections.singletonList(100));
        SemanticBankSearchPanel panel = new SemanticBankSearchPanel(ignored -> { }, () -> { }, () -> { }, () -> { });
        SemanticCoverageAnalyzer coverageAnalyzer = new SemanticCoverageAnalyzer(Collections.singletonList(
            new SemanticRule(
                "Prayer restoration",
                "Restores prayer points.",
                Collections.singletonList("prayer restoration"),
                Collections.singletonList("prayer potion"),
                Collections.emptyMap(),
                10)));
        SemanticBankSearchPlugin plugin = new SemanticBankSearchPlugin();
        plugin.setSearchComponentsForTesting(index, coverageAnalyzer, panel, overlay);

        plugin.showCoverageAuditForTesting();

        assertEquals(Collections.emptyList(), overlay.lastHighlightedItemIds);
    }


    @Test
    public void readinessHighlightsOwnedVisibleItems()
    {
        StorageIndex index = new StorageIndex();
        index.record(300, "Barrows teleport", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(301, "Prayer potion(4)", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
        RecordingOverlay overlay = new RecordingOverlay(config(true));
        overlay.setHighlightedItemIds(Collections.singletonList(999));
        SemanticBankSearchPanel panel = new SemanticBankSearchPanel(ignored -> { }, () -> { }, ignored -> { }, () -> { }, () -> { });
        SemanticBankSearchPlugin plugin = new SemanticBankSearchPlugin();
        plugin.setSearchComponentsForTesting(
            index,
            new SemanticCoverageAnalyzer(SemanticLibrary.create()),
            new ReadinessAnalyzer(SemanticLibrary.create(), ReadinessPackLibrary.create()),
            panel,
            overlay);

        plugin.showReadinessForTesting("barrows trip");

        assertTrue(overlay.lastHighlightedItemIds.contains(300));
        assertTrue(overlay.lastHighlightedItemIds.contains(301));
        assertFalse(overlay.lastHighlightedItemIds.contains(999));
    }

    @Test
    public void pluginRendersAreDeferredOntoSwingExecutor() throws Exception
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "Coins", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
        SemanticBankSearchPanel panel = createPanelOnEdt();
        List<Runnable> swingTasks = new ArrayList<>();
        SemanticBankSearchPlugin plugin = new SemanticBankSearchPlugin();
        plugin.setSearchComponentsForTesting(
            index,
            new SemanticCoverageAnalyzer(Collections.emptyList()),
            panel,
            null);
        plugin.setRuntimeDependenciesForTesting(
            controller(),
            new ThreadBridge(Runnable::run, swingTasks::add));
        AtomicReference<Long> initialRevision = new AtomicReference<>();
        runOnEdt(() -> initialRevision.set(panel.lastRenderedRevisionForTesting()));

        plugin.showCoverageAuditForTesting();

        assertEquals(1, swingTasks.size());
        runOnEdt(() -> assertEquals(initialRevision.get().longValue(), panel.lastRenderedRevisionForTesting()));

        runOnEdt(swingTasks.get(0));

        runOnEdt(() -> assertEquals(1L, panel.lastRenderedRevisionForTesting()));
    }

    @Test
    public void pluginPreservesRevisionsWhenSwingTasksRunOutOfOrder() throws Exception
    {
        StorageIndex index = new StorageIndex();
        index.record(300, "Barrows teleport", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(301, "Prayer potion(4)", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
        SemanticBankSearchPanel panel = createPanelOnEdt();
        List<Runnable> swingTasks = new ArrayList<>();
        SemanticBankSearchPlugin plugin = new SemanticBankSearchPlugin();
        plugin.setSearchComponentsForTesting(
            index,
            new SemanticCoverageAnalyzer(SemanticLibrary.create()),
            new ReadinessAnalyzer(SemanticLibrary.create(), ReadinessPackLibrary.create()),
            panel,
            new RecordingOverlay(config(true)));
        plugin.setRuntimeDependenciesForTesting(
            controller(),
            new ThreadBridge(Runnable::run, swingTasks::add));

        plugin.showCoverageAuditForTesting();
        plugin.showReadinessForTesting("barrows trip");

        assertEquals(2, swingTasks.size());
        runOnEdt(swingTasks.get(1));
        runOnEdt(swingTasks.get(0));

        runOnEdt(() -> {
            assertEquals(2L, panel.lastRenderedRevisionForTesting());
            assertTrue(panel.currentStatusForTesting().startsWith("Readiness:"));
        });
    }

    private TestRuntime runtime(
        SemanticBankSearchConfig config,
        ObservedStorageScanner scanner)
    {
        return runtime(new StorageIndex(), config, scanner);
    }

    private TestRuntime runtime(
        StorageIndex initialIndex,
        SemanticBankSearchConfig config,
        ObservedStorageScanner scanner)
    {
        AccountStorageRepository repository = repository();
        AccountSessionController controller = new AccountSessionController(
            repository,
            itemId -> "Item " + itemId,
            new StorageRetentionPolicy(config.maximumRememberedEntries()));
        StorageIndex activeIndex = controller.switchTo(ACCOUNT_A).getActiveIndex();
        for (ObservedItem item : initialIndex.items())
        {
            activeIndex.record(
                item.getItemId(),
                item.getName(),
                item.getQuantity(),
                item.getSourceType(),
                item.getSourceName(),
                item.isCurrentlyVisible(),
                item.getLastSeenMillis());
        }
        SemanticBankSearchPlugin plugin = new SemanticBankSearchPlugin();
        plugin.setObservedStorageLifecycleStateForTesting(activeIndex, scanner, config);
        plugin.setRuntimeDependenciesForTesting(
            controller,
            new ThreadBridge(Runnable::run, Runnable::run));
        return new TestRuntime(plugin, controller, repository, activeIndex);
    }

    private static StorageIndex persistedIndex(TestRuntime runtime)
    {
        try
        {
            return runtime.repository.load(accountKey(ACCOUNT_A), id -> "Item " + id).index();
        }
        catch (IOException ex)
        {
            throw new AssertionError("Could not load persisted test index", ex);
        }
    }

    private AccountSessionController controller()
    {
        return new AccountSessionController(
            repository(),
            itemId -> "Item " + itemId,
            new StorageRetentionPolicy(800));
    }

    private AccountStorageRepository repository()
    {
        return new AccountStorageRepository(
            temporaryFolder.getRoot().toPath(),
            new Gson(),
            Clock.systemUTC());
    }

    private Path indexPath(long accountHash)
    {
        return temporaryFolder.getRoot().toPath()
            .resolve("semantic-bank-search")
            .resolve("accounts")
            .resolve(accountKey(accountHash).value())
            .resolve("index.json");
    }

    private static AccountKey accountKey(long accountHash)
    {
        return AccountKey.fromAccountHash(accountHash).get();
    }

    private static StorageIndex index(int itemId, String name, boolean currentlyVisible)
    {
        return index(itemId, name, currentlyVisible, StorageSourceType.BANK, "Bank");
    }

    private static StorageIndex index(
        int itemId,
        String name,
        boolean currentlyVisible,
        StorageSourceType sourceType,
        String sourceName)
    {
        StorageIndex index = new StorageIndex();
        index.record(itemId, name, 1, sourceType, sourceName, currentlyVisible, 1_000L);
        return index;
    }

    private static boolean containsItem(StorageIndex index, int itemId)
    {
        if (index == null)
        {
            return false;
        }
        for (ObservedItem item : index.items())
        {
            if (item.getItemId() == itemId)
            {
                return true;
            }
        }
        return false;
    }

    private static JButton findButton(Container root, String text)
    {
        for (Component component : root.getComponents())
        {
            if (component instanceof JButton && text.equals(((JButton) component).getText()))
            {
                return (JButton) component;
            }
            if (component instanceof Container)
            {
                try
                {
                    return findButton((Container) component, text);
                }
                catch (AssertionError ignored)
                {
                    // Continue through sibling containers.
                }
            }
        }
        throw new AssertionError("Button not found: " + text);
    }

    private static final class TestRuntime
    {
        private final SemanticBankSearchPlugin plugin;
        private final AccountSessionController controller;
        private final AccountStorageRepository repository;
        private final StorageIndex index;

        private TestRuntime(
            SemanticBankSearchPlugin plugin,
            AccountSessionController controller,
            AccountStorageRepository repository,
            StorageIndex index)
        {
            this.plugin = plugin;
            this.controller = controller;
            this.repository = repository;
            this.index = index;
        }
    }

    private static final class RecordingPanel extends SemanticBankSearchPanel
    {
        private PanelViewSnapshot lastSnapshot;

        private RecordingPanel()
        {
            super(ignored -> { }, () -> { }, ignored -> { }, () -> { }, () -> { });
        }

        @Override
        public void applySnapshot(PanelViewSnapshot snapshot)
        {
            super.applySnapshot(snapshot);
            lastSnapshot = snapshot;
        }
    }

    private static SemanticBankSearchPanel createPanelOnEdt() throws Exception
    {
        AtomicReference<SemanticBankSearchPanel> panel = new AtomicReference<>();
        runOnEdt(() -> panel.set(new SemanticBankSearchPanel(ignored -> { }, () -> { }, () -> { })));
        return panel.get();
    }

    private static void runOnEdt(Runnable runnable) throws Exception
    {
        if (SwingUtilities.isEventDispatchThread())
        {
            runnable.run();
            return;
        }
        SwingUtilities.invokeAndWait(runnable);
    }

    private static ObservedStorageScanner noVisibleStorageScanner()
    {
        return new ObservedStorageScanner(
            inventoryId -> null,
            packedComponentId -> false,
            itemId -> itemId,
            itemId -> "Unused");
    }

    private static SemanticBankSearchConfig config(boolean rememberObservedStorage)
    {
        return new SemanticBankSearchConfig()
        {
            @Override
            public boolean rememberObservedStorage()
            {
                return rememberObservedStorage;
            }

            @Override
            public int maximumRememberedEntries()
            {
                return 800;
            }
        };
    }

    private static SemanticCoverageResult coverageResult(String name, List<String> categories)
    {
        return new SemanticCoverageResult(
            new ObservedItem(100, name, 1, StorageSourceType.BANK, "Bank", true, 1_000L),
            categories,
            Collections.emptyList(),
            categories.isEmpty() ? 0 : 10);
    }

    private static class RecordingOverlay extends SemanticBankSearchOverlay
    {
        private List<Integer> lastHighlightedItemIds = new ArrayList<>();

        private RecordingOverlay(SemanticBankSearchConfig config)
        {
            super(config);
        }

        @Override
        public void setHighlightedItemIds(Collection<Integer> itemIds)
        {
            super.setHighlightedItemIds(itemIds);
            lastHighlightedItemIds = itemIds == null ? null : new ArrayList<>(itemIds);
        }
    }
}
