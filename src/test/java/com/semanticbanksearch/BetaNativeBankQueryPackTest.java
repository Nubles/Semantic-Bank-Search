package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.runelite.api.EquipmentInventorySlot;
import org.junit.Before;
import org.junit.Test;

public class BetaNativeBankQueryPackTest
{
    private Map<Integer, BankItemMetadata> items;
    private SemanticBankFilter filter;

    @Before
    public void setUp()
    {
        items = new HashMap<>();
        items.put(1, weapon("Dragon scimitar", 67, 0, 0, 0));
        items.put(2, weapon("Dragon hunter crossbow", 0, 95, 0, 0));
        items.put(3, weapon("Magic shortbow", 0, 69, 0, 0));
        items.put(4, gear("Proselyte hauberk", EquipmentInventorySlot.BODY, 8));
        items.put(5, gear("Rune platebody", EquipmentInventorySlot.BODY, 0));
        items.put(6, gear("Berserker ring", EquipmentInventorySlot.RING, 4));
        items.put(7, food("Shark", 20));
        items.put(8, food("Manta ray", 22));
        items.put(9, potion("Prayer potion(4)"));
        items.put(10, potion("Prayer potion(1)"));
        items.put(11, potion("Super combat potion(4)"));
        items.put(12, drink("Beer"));
        items.put(13, weapon("Blessed sword", 55, 0, 0, 4));
        filter = new SemanticBankFilter(
            new SemanticSearchEngine(SemanticLibrary.create()),
            items::get,
            () -> new ArrayList<>(items.keySet()));
    }

    @Test
    public void betaPlayerQueryPackHasExpectedPositivesAndNegatives()
    {
        assertQuery("weapon", ids(1, 2, 3), ids(7, 9));
        assertQuery("weps", ids(1, 2, 3), ids(7, 9));
        assertQuery("armor", ids(4, 5), ids(1, 6));
        assertQuery("food", ids(7, 8), ids(9, 12));
        assertQuery("pots", ids(9, 10, 11), ids(7, 12));
        assertQuery("full pots", ids(9, 11), ids(10, 12));
        assertQuery("4 dose prayer pots", ids(9), ids(10, 11));
        assertQuery("melee gear with prayer", ids(13), ids(1, 4));
        assertQuery("ranged weapons for dragons", ids(2), ids(1, 3));
        assertQuery("food over 18 hp", ids(7, 8), ids(9));
        assertQuery("best food", ids(8), ids(7));
        assertQuery("best prayer gear", ids(4, 6), ids(5));
        assertQuery("strongest ranged weapon", ids(2), ids(3));
    }

    @Test
    public void nativeAndBankTagsQueriesPassThrough()
    {
        assertNull(filter.decision("shark", 7));
        assertNull(filter.decision("tag:slayer", 1));
    }

    @Test(timeout = 5000L)
    public void rankingHandlesOneThousandOwnedItemsWithOneResolutionEach()
    {
        Map<Integer, BankItemMetadata> largeBank = new HashMap<>();
        List<Integer> ids = new ArrayList<>();
        for (int id = 1; id <= 1000; id++)
        {
            ids.add(id);
            largeBank.put(id, food("Test food " + id, id == 1000 ? 30 : 10));
        }

        AtomicInteger resolutions = new AtomicInteger();
        SemanticBankFilter largeFilter = new SemanticBankFilter(
            new SemanticSearchEngine(SemanticLibrary.create()),
            id ->
            {
                resolutions.incrementAndGet();
                return largeBank.get(id);
            },
            () -> ids);

        int matches = 0;
        for (int id : ids)
        {
            if (Boolean.TRUE.equals(largeFilter.decision("best food", id)))
            {
                matches++;
            }
        }

        assertEquals(1, matches);
        assertEquals(1000, resolutions.get());
    }

    private void assertQuery(String query, List<Integer> positives, List<Integer> negatives)
    {
        for (int itemId : positives)
        {
            assertTrue(query + " should include " + itemId, filter.decision(query, itemId));
        }
        for (int itemId : negatives)
        {
            assertFalse(query + " should exclude " + itemId, filter.decision(query, itemId));
        }
    }

    private static List<Integer> ids(Integer... values)
    {
        return Arrays.asList(values);
    }

    private static BankItemMetadata weapon(String name, int melee, int ranged, int magic, int prayer)
    {
        return new BankItemMetadata(
            name, true, true, false, false, EquipmentInventorySlot.WEAPON.getSlotIdx(),
            melee, melee, melee, magic, ranged, melee, ranged, 0f, prayer, -1);
    }

    private static BankItemMetadata gear(String name, EquipmentInventorySlot slot, int prayer)
    {
        return new BankItemMetadata(
            name, true, false, false, false, slot.getSlotIdx(),
            0, 0, 0, 0, 0, 0, 0, 0f, prayer, -1);
    }

    private static BankItemMetadata food(String name, int healing)
    {
        return new BankItemMetadata(
            name, false, false, true, false, -1,
            0, 0, 0, 0, 0, 0, 0, 0f, 0, healing);
    }

    private static BankItemMetadata potion(String name)
    {
        return new BankItemMetadata(
            name, false, false, false, true, -1,
            0, 0, 0, 0, 0, 0, 0, 0f, 0, -1);
    }

    private static BankItemMetadata drink(String name)
    {
        return new BankItemMetadata(
            name, false, false, false, true, -1,
            0, 0, 0, 0, 0, 0, 0, 0f, 0, -1);
    }
}
