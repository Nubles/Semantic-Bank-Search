package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class SemanticBankSearchPluginTest
{
    @Test
    public void startupWithRememberedVisibleStorageAndNoVisibleWidgetClearsVisibleStatusOnly()
    {
        StorageIndex index = new StorageIndex();
        index.record(101, "Ranarr seed", 5, StorageSourceType.OTHER_STORAGE, "Seed Vault", true, 1_000L);
        SemanticBankSearchPlugin plugin = pluginWith(index, noVisibleStorageScanner(), config(true));

        plugin.startObservedStorageLifecycle(2_000L);

        assertEquals(1, index.items().size());
        assertEquals("Ranarr seed", index.items().get(0).getName());
        assertFalse(index.items().get(0).isCurrentlyVisible());
    }

    @Test
    public void visibleSourceCloseMarksSourceNotVisibleAndRetainsRememberedItems()
    {
        StorageIndex index = new StorageIndex();
        index.record(101, "Ranarr seed", 5, StorageSourceType.OTHER_STORAGE, "Seed Vault", true, 1_000L);
        SemanticBankSearchPlugin plugin = pluginWith(index, noVisibleStorageScanner(), config(true));
        plugin.rememberVisibleStorageSourceForTesting(ObservedStorageSource.seedVault());

        plugin.handleObservedStorageTick(2_000L);

        assertEquals(1, index.items().size());
        assertEquals("Ranarr seed", index.items().get(0).getName());
        assertFalse(index.items().get(0).isCurrentlyVisible());
    }

    @Test
    public void configOffTransitionFromVisibleSafeStorageClearsVisibleStatus()
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "Prayer potion(4)", 3, StorageSourceType.BANK, "Bank", true, 1_000L);
        SemanticBankSearchPlugin plugin = pluginWith(index, noVisibleStorageScanner(), config(false));
        plugin.rememberVisibleStorageSourceForTesting(ObservedStorageSource.bank());

        plugin.handleObservedStorageTick(2_000L);

        assertEquals(1, index.items().size());
        assertFalse(index.items().get(0).isCurrentlyVisible());
    }

    @Test
    public void visibilityLossPersistsImmediately()
    {
        StorageIndex index = new StorageIndex();
        index.record(101, "Ranarr seed", 5, StorageSourceType.OTHER_STORAGE, "Seed Vault", true, 1_000L);
        AtomicInteger persistCount = new AtomicInteger();
        SemanticBankSearchPlugin plugin = pluginWith(index, noVisibleStorageScanner(), config(true));
        plugin.setObservedStoragePersistenceForTesting(ignored -> persistCount.incrementAndGet());
        plugin.rememberVisibleStorageSourceForTesting(ObservedStorageSource.seedVault());
        plugin.setLastPersistMillisForTesting(1_950L);

        plugin.handleObservedStorageTick(2_000L);

        assertEquals(1, persistCount.get());
    }

    @Test
    public void startupWithRememberingDisabledPersistsClearedVisibleStatus()
    {
        StorageIndex index = new StorageIndex();
        index.record(101, "Ranarr seed", 5, StorageSourceType.OTHER_STORAGE, "Seed Vault", true, 1_000L);
        AtomicInteger persistCount = new AtomicInteger();
        SemanticBankSearchPlugin plugin = pluginWith(index, noVisibleStorageScanner(), config(false));
        plugin.setObservedStoragePersistenceForTesting(ignored -> persistCount.incrementAndGet());

        plugin.startObservedStorageLifecycle(2_000L);

        assertFalse(index.items().get(0).isCurrentlyVisible());
        assertEquals(1, persistCount.get());
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
    private static SemanticBankSearchPlugin pluginWith(
        StorageIndex index,
        ObservedStorageScanner scanner,
        SemanticBankSearchConfig config)
    {
        SemanticBankSearchPlugin plugin = new SemanticBankSearchPlugin();
        plugin.setObservedStorageLifecycleStateForTesting(index, scanner, config);
        plugin.setObservedStoragePersistenceForTesting(ignored -> { });
        return plugin;
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
