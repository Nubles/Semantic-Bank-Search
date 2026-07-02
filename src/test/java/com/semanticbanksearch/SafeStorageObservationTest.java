package com.semanticbanksearch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

public class SafeStorageObservationTest
{
    @Test
    public void startupVisibilityResetMarksRememberedSafeStorageNotVisible()
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "Prayer potion(4)", 3, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(101, "Ranarr seed", 5, StorageSourceType.OTHER_STORAGE, "Seed Vault", true, 1_000L);
        index.record(102, "Dragon axe", 1, StorageSourceType.OTHER_STORAGE, "Group Storage", true, 1_000L);

        SafeStorageObservation.markAllSafeStorageSourcesNotVisible(index);

        for (ObservedItem item : index.items())
        {
            assertFalse(item.isCurrentlyVisible());
        }
    }

    @Test
    public void unchangedVisibleSnapshotDoesNotReportStorageChange()
    {
        StorageIndex index = new StorageIndex();
        index.record(101, "Ranarr seed", 5, StorageSourceType.OTHER_STORAGE, "Seed Vault", true, 1_000L);

        boolean changed = SafeStorageObservation.replaceVisibleSourceItemsIfChanged(
            index,
            ObservedStorageSource.seedVault(),
            Arrays.asList(new ObservedItem(
                101,
                "Ranarr seed",
                5,
                StorageSourceType.OTHER_STORAGE,
                "Seed Vault",
                true,
                2_000L)));

        assertFalse(changed);
    }

    @Test
    public void changedVisibleSnapshotReportsStorageChange()
    {
        StorageIndex index = new StorageIndex();
        index.record(101, "Ranarr seed", 5, StorageSourceType.OTHER_STORAGE, "Seed Vault", true, 1_000L);

        boolean changed = SafeStorageObservation.replaceVisibleSourceItemsIfChanged(
            index,
            ObservedStorageSource.seedVault(),
            Arrays.asList(new ObservedItem(
                101,
                "Ranarr seed",
                7,
                StorageSourceType.OTHER_STORAGE,
                "Seed Vault",
                true,
                2_000L)));

        assertTrue(changed);
    }
}

