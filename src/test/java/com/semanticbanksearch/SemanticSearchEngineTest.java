package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class SemanticSearchEngineTest
{
    @Test
    public void prayerRestorationFindsOwnedPrayerPotions()
    {
        StorageIndex index = new StorageIndex();
        index.record(1, "Prayer potion(4)", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(2, "Shark", 5, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("prayer restoration", index);

        assertEquals(1, results.size());
        assertEquals("Prayer potion(4)", results.get(0).getItemName());
        assertEquals("Prayer restoration", results.get(0).getCategory());
    }

    @Test
    public void warmClothingFindsOwnedWarmItems()
    {
        StorageIndex index = new StorageIndex();
        index.record(10, "Clue hunter garb", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(11, "Monk's robe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("warm clothing", index);

        assertEquals(1, results.size());
        assertEquals("Clue hunter garb", results.get(0).getItemName());
    }

    @Test
    public void webCuttersFindKnivesAndSlashTools()
    {
        StorageIndex index = new StorageIndex();
        index.record(20, "Knife", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(21, "Dragon scimitar", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(22, "Air staff", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("things that cut webs", index);

        assertEquals(2, results.size());
        assertTrue(names(results).contains("Knife"));
        assertTrue(names(results).contains("Dragon scimitar"));
        assertFalse(names(results).contains("Air staff"));
    }

    @Test
    public void crushWeaponsFindOwnedCrushItems()
    {
        StorageIndex index = new StorageIndex();
        index.record(30, "Dragon mace", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(31, "Rune scimitar", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("crush weapons", index);

        assertEquals(1, results.size());
        assertEquals("Dragon mace", results.get(0).getItemName());
    }

    @Test
    public void fastestFoodRanksHigherHealingFoodFirst()
    {
        StorageIndex index = new StorageIndex();
        index.record(40, "Trout", 10, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(41, "Shark", 10, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(42, "Manta ray", 10, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("fastest food I own", index);

        assertEquals("Manta ray", results.get(0).getItemName());
        assertEquals("Shark", results.get(1).getItemName());
        assertEquals("Trout", results.get(2).getItemName());
    }

    @Test
    public void itemNameFallbackStillWorks()
    {
        StorageIndex index = new StorageIndex();
        index.record(50, "Barrows teleport", 2, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("barrows tele", index);

        assertEquals(1, results.size());
        assertEquals("Barrows teleport", results.get(0).getItemName());
    }

    private static String names(List<SemanticSearchResult> results)
    {
        StringBuilder builder = new StringBuilder();
        for (SemanticSearchResult result : results)
        {
            builder.append(result.getItemName()).append("\n");
        }
        return builder.toString();
    }
}
