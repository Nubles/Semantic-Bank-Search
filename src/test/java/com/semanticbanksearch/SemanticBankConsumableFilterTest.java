package com.semanticbanksearch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class SemanticBankConsumableFilterTest
{
    @Test
    public void consumableQueriesFilterLiveBankItems()
    {
        Map<Integer, BankItemMetadata> items = new HashMap<>();
        items.put(1, consumable("Shark", true, false));
        items.put(2, consumable("Prayer potion(4)", false, true));
        items.put(3, consumable("Prayer potion(1)", false, true));
        SemanticBankFilter filter = new SemanticBankFilter(
            new SemanticSearchEngine(SemanticLibrary.create()),
            items::get);

        assertTrue(filter.decision("food", 1));
        assertFalse(filter.decision("food", 2));
        assertTrue(filter.decision("pots", 2));
        assertFalse(filter.decision("pots", 1));
        assertTrue(filter.decision("full pots", 2));
        assertFalse(filter.decision("full pots", 3));
        assertTrue(filter.decision("low dose pots", 3));
    }

    private static BankItemMetadata consumable(String name, boolean edible, boolean drinkable)
    {
        return new BankItemMetadata(
            name, false, false, edible, drinkable, -1,
            0, 0, 0, 0, 0, 0, 0, 0f, 0);
    }
}
