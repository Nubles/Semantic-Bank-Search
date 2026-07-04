package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class ReadinessAnalyzerTest
{
    @Test
    public void barrowsTripShowsOwnedItemsAndMissingRequiredSpade()
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "Barrows teleport", 4, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(101, "Prayer potion(4)", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(102, "Shark", 20, StorageSourceType.BANK, "Bank", true, 1_000L);
        ReadinessAnalyzer analyzer = new ReadinessAnalyzer(SemanticLibrary.create(), ReadinessPackLibrary.create());

        ReadinessResult result = analyzer.analyze("barrows trip", index);

        assertTrue(result.isMatched());
        assertEquals("Barrows trip", result.getPackName());
        assertTrue(hasOwnedSlot(result, "Nearby teleport", "Barrows teleport"));
        assertTrue(hasOwnedSlot(result, "Prayer restoration", "Prayer potion(4)"));
        assertTrue(hasOwnedSlot(result, "Food", "Shark"));
        assertTrue(hasMissingRequiredSlot(result, "Spade"));
    }

    @Test
    public void wildyEscapeShowsEscapeTeleportAndMissingComboFood()
    {
        StorageIndex index = new StorageIndex();
        index.record(200, "Royal seed pod", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(201, "Stamina potion(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        ReadinessAnalyzer analyzer = new ReadinessAnalyzer(SemanticLibrary.create(), ReadinessPackLibrary.create());

        ReadinessResult result = analyzer.analyze("wildy escape", index);

        assertTrue(result.isMatched());
        assertEquals("Wildy escape", result.getPackName());
        assertTrue(hasOwnedSlot(result, "One-click escape teleport", "Royal seed pod"));
        assertTrue(hasOwnedSlot(result, "Run energy", "Stamina potion(4)"));
        assertTrue(hasMissingSlot(result, "Combo food"));
    }

    @Test
    public void unknownReadinessQueryReturnsUnmatchedResult()
    {
        ReadinessAnalyzer analyzer = new ReadinessAnalyzer(SemanticLibrary.create(), ReadinessPackLibrary.create());

        ReadinessResult result = analyzer.analyze("decorate my poh", new StorageIndex());

        assertFalse(result.isMatched());
        assertTrue(result.getSlotResults().isEmpty());
    }

    private static boolean hasOwnedSlot(ReadinessResult result, String slotName, String itemName)
    {
        for (ReadinessSlotResult slotResult : result.getSlotResults())
        {
            if (slotResult.getSlotName().equals(slotName) && containsItem(slotResult.getOwnedItems(), itemName))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean hasMissingRequiredSlot(ReadinessResult result, String slotName)
    {
        for (ReadinessSlotResult slotResult : result.getSlotResults())
        {
            if (slotResult.getSlotName().equals(slotName)
                && slotResult.isMissing()
                && slotResult.getKind() == ReadinessSlotKind.REQUIRED)
            {
                return true;
            }
        }
        return false;
    }

    private static boolean hasMissingSlot(ReadinessResult result, String slotName)
    {
        for (ReadinessSlotResult slotResult : result.getSlotResults())
        {
            if (slotResult.getSlotName().equals(slotName) && slotResult.isMissing())
            {
                return true;
            }
        }
        return false;
    }

    private static boolean containsItem(List<SemanticSearchResult> items, String itemName)
    {
        for (SemanticSearchResult item : items)
        {
            if (item.getItemName().equals(itemName))
            {
                return true;
            }
        }
        return false;
    }
}
