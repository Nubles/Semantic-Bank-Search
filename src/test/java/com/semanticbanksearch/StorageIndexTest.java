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

        index.trimToMaximumEntries(2);

        assertEquals(2, index.items().size());
        assertEquals(101, index.items().get(0).getItemId());
        assertEquals(102, index.items().get(1).getItemId());
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
}
