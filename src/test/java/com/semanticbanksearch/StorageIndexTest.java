package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class StorageIndexTest
{
    @Test
    public void recordsObservedBankItemsByCanonicalItemAndSource()
    {
        StorageIndex index = new StorageIndex();

        index.record(100, "Prayer potion(4)", 3, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<ObservedItem> items = index.items();
        assertEquals(1, items.size());
        assertEquals(100, items.get(0).getItemId());
        assertEquals("Prayer potion(4)", items.get(0).getName());
        assertEquals(3, items.get(0).getQuantity());
        assertEquals(StorageSourceType.BANK, items.get(0).getSourceType());
        assertTrue(items.get(0).isCurrentlyVisible());
    }

    @Test
    public void updatesExistingItemSourceInsteadOfDuplicatingIt()
    {
        StorageIndex index = new StorageIndex();

        index.record(100, "Prayer potion(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(100, "Prayer potion(4)", 5, StorageSourceType.BANK, "Bank", true, 2_000L);

        assertEquals(1, index.items().size());
        assertEquals(5, index.items().get(0).getQuantity());
        assertEquals(2_000L, index.items().get(0).getLastSeenMillis());
    }

    @Test
    public void clearsVisibilityForSourceWhenBankIsClosed()
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "Prayer potion(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        index.markSourceNotVisible(StorageSourceType.BANK, "Bank");

        assertFalse(index.items().get(0).isCurrentlyVisible());
    }

    @Test
    public void replacingVisibleSourceItemsClearsMissingItems()
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "Prayer potion(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(200, "Super restore(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        index.replaceVisibleSourceItems(
            StorageSourceType.BANK,
            "Bank",
            Arrays.asList(new ObservedItem(100, "Prayer potion(4)", 2, StorageSourceType.BANK, "Bank", true, 2_000L)));

        assertTrue(itemById(index.items(), 100).isCurrentlyVisible());
        assertFalse(itemById(index.items(), 200).isCurrentlyVisible());
    }

    @Test
    public void returnedItemsCannotMutateIndexState()
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "Prayer potion(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        index.items().get(0).setCurrentlyVisible(false);

        assertTrue(index.items().get(0).isCurrentlyVisible());
    }

    @Test
    public void retainedReturnedItemsDoNotChangeWhenIndexChanges()
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "Prayer potion(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        ObservedItem retainedItem = index.items().get(0);

        index.markSourceNotVisible(StorageSourceType.BANK, "Bank");

        assertTrue(retainedItem.isCurrentlyVisible());
        assertFalse(index.items().get(0).isCurrentlyVisible());
    }

    @Test
    public void trimsOldEntriesByMaximumCount()
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "Old item", 1, StorageSourceType.BANK, "Bank", false, 1_000L);
        index.record(101, "Middle item", 1, StorageSourceType.BANK, "Bank", false, 2_000L);
        index.record(102, "New item", 1, StorageSourceType.BANK, "Bank", false, 3_000L);

        index.trimToLimits(2, 2);

        assertEquals(2, index.items().size());
        assertEquals(101, index.items().get(0).getItemId());
        assertEquals(102, index.items().get(1).getItemId());
    }

    @Test
    public void trimsEachStorageSourceBeforeAccountTotal()
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "First source oldest", 1, StorageSourceType.OTHER_STORAGE, "First", false, 3_000L);
        index.record(101, "First source middle", 1, StorageSourceType.OTHER_STORAGE, "First", false, 4_000L);
        index.record(102, "First source newest", 1, StorageSourceType.OTHER_STORAGE, "First", false, 5_000L);
        index.record(200, "Second source oldest", 1, StorageSourceType.OTHER_STORAGE, "Second", false, 1_000L);
        index.record(201, "Second source newest", 1, StorageSourceType.OTHER_STORAGE, "Second", false, 2_000L);

        index.trimToLimits(4, 2);

        assertEquals(Arrays.asList(200, 201, 101, 102), itemIds(index.items()));
    }

    @Test
    public void returnsSnapshotsWithoutReorderingMutableBackingState()
    {
        StorageIndex withoutSnapshot = equalTimestampIndex();
        StorageIndex withSnapshot = equalTimestampIndex();

        withSnapshot.items();

        withoutSnapshot.record(1, "Old item", 1, StorageSourceType.BANK, "Bank", false, 2_000L);
        withSnapshot.record(1, "Old item", 1, StorageSourceType.BANK, "Bank", false, 2_000L);
        withoutSnapshot.record(3, "New item", 1, StorageSourceType.BANK, "Bank", false, 2_000L);
        withSnapshot.record(3, "New item", 1, StorageSourceType.BANK, "Bank", false, 2_000L);

        withoutSnapshot.trimToLimits(2, 2);
        withSnapshot.trimToLimits(2, 2);

        assertEquals(itemIds(withoutSnapshot.items()), itemIds(withSnapshot.items()));
        assertEquals(Arrays.asList(2, 3), itemIds(withSnapshot.items()));
    }

    @Test
    public void evictsLowestItemIdWhenTimestampsMatch()
    {
        StorageIndex index = new StorageIndex();
        index.record(2, "Later item ID", 1, StorageSourceType.BANK, "Bank", false, 1_000L);
        index.record(1, "Earlier item ID", 1, StorageSourceType.BANK, "Bank", false, 1_000L);

        index.trimToLimits(1, 1);

        assertEquals(1, index.items().size());
        assertEquals(2, index.items().get(0).getItemId());
    }

    @Test
    public void evictsEarlierSourceTypeWhenTimestampsAndItemIdsMatch()
    {
        StorageIndex index = new StorageIndex();
        index.record(1, "Bank item", 1, StorageSourceType.BANK, "Bank", false, 1_000L);
        index.record(1, "Poh item", 1, StorageSourceType.POH_STORAGE, "Poh", false, 1_000L);

        index.trimToLimits(1, 1);

        assertEquals(1, index.items().size());
        assertEquals(StorageSourceType.POH_STORAGE, index.items().get(0).getSourceType());
    }

    @Test
    public void evictsCaseInsensitiveEarlierSourceNameWhenOtherFieldsMatch()
    {
        StorageIndex index = new StorageIndex();
        index.record(1, "Zulu source", 1, StorageSourceType.OTHER_STORAGE, "zulu", false, 1_000L);
        index.record(1, "Alpha source", 1, StorageSourceType.OTHER_STORAGE, "Alpha", false, 1_000L);

        index.trimToLimits(1, 1);

        assertEquals(1, index.items().size());
        assertEquals("zulu", index.items().get(0).getSourceName());
    }

    @Test(timeout = 15_000L)
    public void recordsUpdatesAndTrimsTwentyThousandEntriesWithinGenerousBudget()
    {
        StorageIndex index = new StorageIndex();
        for (int itemId = 1; itemId <= 20_000; itemId++)
        {
            index.record(
                itemId,
                "Item " + itemId,
                1,
                StorageSourceType.BANK,
                "Bank",
                false,
                itemId);
        }
        for (int itemId = 20_000; itemId >= 1; itemId--)
        {
            index.record(
                itemId,
                "Updated item " + itemId,
                2,
                StorageSourceType.BANK,
                "Bank",
                false,
                itemId);
        }

        assertEquals(20_000, index.items().size());
        index.trimToLimits(10_000, 10_000);
        assertEquals(10_000, index.items().size());
        assertEquals(10_001, index.items().get(0).getItemId());
        assertEquals(2, index.items().get(0).getQuantity());
    }

    @Test

    public void nonBankSourceItemsBecomeRememberedWhenSourceCloses()
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "Ranarr seed", 5, StorageSourceType.OTHER_STORAGE, "Seed Vault", true, 1_000L);

        index.markSourceNotVisible(StorageSourceType.OTHER_STORAGE, "Seed Vault");

        List<ObservedItem> items = index.items();
        assertEquals(1, items.size());
        assertEquals("Ranarr seed", items.get(0).getName());
        assertEquals(StorageSourceType.OTHER_STORAGE, items.get(0).getSourceType());
        assertEquals("Seed Vault", items.get(0).getSourceName());
        assertFalse(items.get(0).isCurrentlyVisible());
    }

    private static ObservedItem itemById(List<ObservedItem> items, int itemId)
    {
        for (ObservedItem item : items)
        {
            if (item.getItemId() == itemId)
            {
                return item;
            }
        }
        throw new AssertionError("Missing item " + itemId);
    }
    private static StorageIndex equalTimestampIndex()

    {
        StorageIndex index = new StorageIndex();
        index.record(2, "Newer item", 1, StorageSourceType.BANK, "Bank", false, 2_000L);
        index.record(1, "Older item", 1, StorageSourceType.BANK, "Bank", false, 1_000L);
        return index;
    }

    private static List<Integer> itemIds(List<ObservedItem> items)
    {
        List<Integer> itemIds = new java.util.ArrayList<>();
        for (ObservedItem item : items)
        {
            itemIds.add(item.getItemId());
        }
        return itemIds;
    }
}
