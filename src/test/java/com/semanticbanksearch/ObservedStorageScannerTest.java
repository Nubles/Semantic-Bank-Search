package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Node;
import net.runelite.api.widgets.ComponentID;
import org.junit.Test;

public class ObservedStorageScannerTest
{
    @Test
    public void scansVisibleSafeDirectContainers()
    {
        ObservedStorageScanner scanner = new ObservedStorageScanner(
            inventoryId -> inventoryId == InventoryID.SEED_VAULT
                ? new FakeItemContainer(new Item(100, 7), new Item(101, 0), new Item(-1, 1), null)
                : null,
            packedComponentId -> packedComponentId == ComponentID.SEED_VAULT_ITEM_CONTAINER,
            itemId -> itemId + 10_000,
            canonicalItemId -> canonicalItemId == 10_100 ? "Ranarr seed" : "");

        List<ObservedStorageSnapshot> snapshots = scanner.scan(
            Collections.singletonList(ObservedStorageSource.seedVault()),
            12_345L);

        assertEquals(1, snapshots.size());
        assertEquals(ObservedStorageSource.seedVault().key(), snapshots.get(0).getSource().key());
        assertEquals(1, snapshots.get(0).getItems().size());

        ObservedItem item = snapshots.get(0).getItems().get(0);
        assertEquals(10_100, item.getItemId());
        assertEquals("Ranarr seed", item.getName());
        assertEquals(7, item.getQuantity());
        assertEquals(StorageSourceType.OTHER_STORAGE, item.getSourceType());
        assertEquals("Seed Vault", item.getSourceName());
        assertTrue(item.isCurrentlyVisible());
        assertEquals(12_345L, item.getLastSeenMillis());
    }

    @Test
    public void hiddenContainersDoNotProduceSnapshotsEvenWhenContainerIsCached()
    {
        ObservedStorageScanner scanner = new ObservedStorageScanner(
            inventoryId -> new FakeItemContainer(new Item(200, 1)),
            packedComponentId -> false,
            itemId -> itemId,
            canonicalItemId -> "Dragon axe");

        List<ObservedStorageSnapshot> snapshots = scanner.scan(
            Collections.singletonList(ObservedStorageSource.groupStorage()),
            12_345L);

        assertTrue(snapshots.isEmpty());
    }

    @Test
    public void absentContainersDoNotProduceSnapshots()
    {
        ObservedStorageScanner scanner = new ObservedStorageScanner(
            inventoryId -> null,
            packedComponentId -> true,
            itemId -> itemId,
            canonicalItemId -> "Dragon axe");

        List<ObservedStorageSnapshot> snapshots = scanner.scan(
            Collections.singletonList(ObservedStorageSource.groupStorage()),
            12_345L);

        assertTrue(snapshots.isEmpty());
    }

    @Test
    public void returnedSnapshotsCannotMutateScannerState()
    {
        ObservedStorageScanner scanner = new ObservedStorageScanner(
            inventoryId -> new FakeItemContainer(new Item(200, 1)),
            packedComponentId -> packedComponentId == ComponentID.GROUP_STORAGE_ITEM_CONTAINER,
            itemId -> itemId,
            canonicalItemId -> canonicalItemId == 200 ? "Dragon axe" : "");

        List<ObservedStorageSnapshot> snapshots = scanner.scan(
            Collections.singletonList(ObservedStorageSource.groupStorage()),
            12_345L);
        snapshots.get(0).getItems().clear();

        List<ObservedStorageSnapshot> rescanned = scanner.scan(
            Collections.singletonList(ObservedStorageSource.groupStorage()),
            12_346L);

        assertEquals(1, rescanned.size());
        assertEquals(1, rescanned.get(0).getItems().size());
        assertEquals("Dragon axe", rescanned.get(0).getItems().get(0).getName());
    }

    private static final class FakeItemContainer implements ItemContainer
    {
        private final Item[] items;

        private FakeItemContainer(Item... items)
        {
            this.items = Arrays.copyOf(items, items.length);
        }

        @Override
        public int getId()
        {
            return 0;
        }

        @Override
        public Item[] getItems()
        {
            return Arrays.copyOf(items, items.length);
        }

        @Override
        public Item getItem(int index)
        {
            return index >= 0 && index < items.length ? items[index] : null;
        }

        @Override
        public boolean contains(int itemId)
        {
            return find(itemId) >= 0;
        }

        @Override
        public int count(int itemId)
        {
            int quantity = 0;
            for (Item item : items)
            {
                if (item != null && item.getId() == itemId)
                {
                    quantity += item.getQuantity();
                }
            }
            return quantity;
        }

        @Override
        public int size()
        {
            return items.length;
        }

        @Override
        public int count()
        {
            int count = 0;
            for (Item item : items)
            {
                if (item != null)
                {
                    count++;
                }
            }
            return count;
        }

        @Override
        public int find(int itemId)
        {
            for (int i = 0; i < items.length; i++)
            {
                if (items[i] != null && items[i].getId() == itemId)
                {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public Node getNext()
        {
            return null;
        }

        @Override
        public Node getPrevious()
        {
            return null;
        }

        @Override
        public long getHash()
        {
            return 0L;
        }
    }
}
