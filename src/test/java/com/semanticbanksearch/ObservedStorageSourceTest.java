package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;
import net.runelite.api.InventoryID;
import net.runelite.api.widgets.ComponentID;
import org.junit.Test;

public class ObservedStorageSourceTest
{
    @Test
    public void safeDirectSourcesIncludeOnlyStableStorageContainers()
    {
        List<ObservedStorageSource> sources = ObservedStorageSource.safeDirectInventorySources();

        assertEquals(3, sources.size());
        assertSource(
            ObservedStorageSource.bank(),
            InventoryID.BANK,
            ComponentID.BANK_ITEM_CONTAINER,
            StorageSourceType.BANK,
            "Bank");
        assertSource(
            ObservedStorageSource.seedVault(),
            InventoryID.SEED_VAULT,
            ComponentID.SEED_VAULT_ITEM_CONTAINER,
            StorageSourceType.OTHER_STORAGE,
            "Seed Vault");
        assertSource(
            ObservedStorageSource.groupStorage(),
            InventoryID.GROUP_STORAGE,
            ComponentID.GROUP_STORAGE_ITEM_CONTAINER,
            StorageSourceType.OTHER_STORAGE,
            "Group Storage");
    }

    @Test
    public void unsafeContainersAreNotAllowListed()
    {
        List<ObservedStorageSource> sources = ObservedStorageSource.safeDirectInventorySources();

        assertFalse(containsInventory(sources, InventoryID.TRADE));
        assertFalse(containsInventory(sources, InventoryID.TRADEOTHER));
        assertFalse(containsInventory(sources, InventoryID.INVENTORY));
        assertFalse(containsInventory(sources, InventoryID.EQUIPMENT));
        assertFalse(containsInventory(sources, InventoryID.BARROWS_REWARD));
        assertFalse(containsInventory(sources, InventoryID.TOA_REWARD_CHEST));
        assertFalse(containsInventory(sources, InventoryID.WILDERNESS_LOOT_CHEST));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void safeDirectSourcesListCannotBeMutated()
    {
        List<ObservedStorageSource> sources = ObservedStorageSource.safeDirectInventorySources();

        sources.set(0, ObservedStorageSource.groupStorage());
    }

    private static void assertSource(
        ObservedStorageSource source,
        InventoryID inventoryId,
        int visibleComponentId,
        StorageSourceType sourceType,
        String sourceName)
    {
        assertEquals(inventoryId, source.getInventoryId());
        assertEquals(visibleComponentId, source.getVisibleComponentId());
        assertEquals(sourceType, source.getSourceType());
        assertEquals(sourceName, source.getSourceName());
        assertEquals(sourceType + "|" + sourceName, source.key());
    }

    private static boolean containsInventory(List<ObservedStorageSource> sources, InventoryID inventoryId)
    {
        for (ObservedStorageSource source : sources)
        {
            if (source.getInventoryId() == inventoryId)
            {
                return true;
            }
        }
        return false;
    }
}
