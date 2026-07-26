package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StorageRetentionPolicyTest
{
    @Test
    public void defaultsToFiveThousandEntries()
    {
        StorageIndex index = new StorageIndex();
        for (int itemId = 1; itemId <= 5_001; itemId++)
        {
            index.record(itemId, "Item " + itemId, 1, StorageSourceType.BANK, "Bank", false, itemId);
        }

        new StorageRetentionPolicy(StorageRetentionPolicy.DEFAULT_MAXIMUM_ENTRIES).apply(index);

        assertEquals(5_000, index.items().size());
        assertFalse(containsItem(index, 1));
        assertTrue(containsItem(index, 5_001));
    }

    @Test
    public void clampsConfiguredLimitToHardCeiling()
    {
        StorageIndex index = new StorageIndex();
        record(index, 1, StorageSourceType.BANK, "Bank", false, 1_000L);
        record(index, 2, StorageSourceType.BANK, "Bank", false, 2_000L);
        record(index, 3, StorageSourceType.BANK, "Bank", false, 3_000L);

        new StorageRetentionPolicy(3, 2).apply(index);

        assertEquals(2, index.items().size());
        assertFalse(containsItem(index, 1));
    }

    @Test
    public void evictsOldestNonVisibleEntriesFirst()
    {
        StorageIndex index = new StorageIndex();
        record(index, 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        record(index, 2, StorageSourceType.BANK, "Bank", false, 2_000L);
        record(index, 3, StorageSourceType.BANK, "Bank", false, 3_000L);

        new StorageRetentionPolicy(2, 2).apply(index);

        assertTrue(containsItem(index, 1));
        assertFalse(containsItem(index, 2));
        assertTrue(containsItem(index, 3));
    }

    @Test
    public void neverEvictsVisibleBankEntries()
    {
        StorageIndex index = new StorageIndex();
        record(index, 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        record(index, 2, StorageSourceType.BANK, "Bank", true, 2_000L);
        record(index, 3, StorageSourceType.BANK, "Bank", true, 3_000L);
        record(index, 4, StorageSourceType.OTHER_STORAGE, "Seed Vault", false, 4_000L);

        new StorageRetentionPolicy(2, 2).apply(index);

        assertEquals(3, index.items().size());
        assertTrue(containsItem(index, 1));
        assertTrue(containsItem(index, 2));
        assertTrue(containsItem(index, 3));
        assertFalse(containsItem(index, 4));
    }

    private static void record(
        StorageIndex index,
        int itemId,
        StorageSourceType sourceType,
        String sourceName,
        boolean currentlyVisible,
        long lastSeenMillis)
    {
        index.record(itemId, "Item " + itemId, 1, sourceType, sourceName, currentlyVisible, lastSeenMillis);
    }

    private static boolean containsItem(StorageIndex index, int itemId)
    {
        for (ObservedItem item : index.items())
        {
            if (item.getItemId() == itemId)
            {
                return true;
            }
        }
        return false;
    }
}
