package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
}
