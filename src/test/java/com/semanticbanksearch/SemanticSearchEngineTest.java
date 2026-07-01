package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
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
    public void bankResultsAreNotHighlightableAfterSourceVisibilityIsCleared()
    {
        StorageIndex index = new StorageIndex();
        index.record(1, "Prayer potion(4)", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.markSourceNotVisible(StorageSourceType.BANK, "Bank");

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("prayer restoration", index);

        assertEquals(1, results.size());
        assertFalse(results.get(0).isHighlightable());
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

        List<SemanticSearchResult> results = new SemanticSearchEngine(Collections.emptyList()).search("barrows tele", index);

        assertEquals(1, results.size());
        assertEquals("Barrows teleport", results.get(0).getItemName());
        assertEquals("Item name match", results.get(0).getCategory());
    }

    @Test
    public void itemMatchingMultipleSemanticRulesAppearsOnce()
    {
        StorageIndex index = new StorageIndex();
        index.record(60, "Clue hunter garb", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("clue utility warm clothing", index);

        assertEquals(1, results.size());
        assertEquals("Clue hunter garb", results.get(0).getItemName());
    }

    @Test
    public void teleportJewelleryFindsOwnedTeleportJewellery()
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "Games necklace(8)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(101, "Hammer", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("teleport jewellery", index);

        assertEquals(1, results.size());
        assertEquals("Games necklace(8)", results.get(0).getItemName());
    }

    @Test
    public void staminaFindsRunEnergyPotion()
    {
        StorageIndex index = new StorageIndex();
        index.record(110, "Stamina potion(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("stamina", index);

        assertEquals(1, results.size());
        assertEquals("Stamina potion(4)", results.get(0).getItemName());
    }

    @Test
    public void antifireFindsDragonProtectionPotion()
    {
        StorageIndex index = new StorageIndex();
        index.record(120, "Extended antifire(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("antifire", index);

        assertEquals(1, results.size());
        assertEquals("Extended antifire(4)", results.get(0).getItemName());
    }

    @Test
    public void rangedAmmoFindsArrowsAndBolts()
    {
        StorageIndex index = new StorageIndex();
        index.record(130, "Rune arrow", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(131, "Diamond bolts (e)", 50, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(132, "Air rune", 500, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("ranged ammo", index);

        assertEquals(2, results.size());
        assertTrue(names(results).contains("Rune arrow"));
        assertTrue(names(results).contains("Diamond bolts (e)"));
        assertFalse(names(results).contains("Air rune"));
    }

    @Test
    public void magicRunesFindsOwnedRunes()
    {
        StorageIndex index = new StorageIndex();
        index.record(140, "Law rune", 500, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(141, "Nature rune", 500, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(142, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("magic runes", index);

        assertEquals(2, results.size());
        assertTrue(names(results).contains("Law rune"));
        assertTrue(names(results).contains("Nature rune"));
        assertFalse(names(results).contains("Rune platebody"));
    }

    @Test
    public void farmingToolsFindsOwnedFarmingTools()
    {
        StorageIndex index = new StorageIndex();
        index.record(150, "Seed dibber", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(151, "Magic secateurs", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("farming tools", index);

        assertEquals(2, results.size());
        assertTrue(names(results).contains("Seed dibber"));
        assertTrue(names(results).contains("Magic secateurs"));
    }

    @Test
    public void fishingToolsFindsOwnedFishingTools()
    {
        StorageIndex index = new StorageIndex();
        index.record(160, "Harpoon", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(161, "Lobster pot", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("fishing tools", index);

        assertEquals(2, results.size());
        assertTrue(names(results).contains("Harpoon"));
        assertTrue(names(results).contains("Lobster pot"));
    }

    @Test
    public void clueToolsFindsLightSourceAndSpade()
    {
        StorageIndex index = new StorageIndex();
        index.record(170, "Bullseye lantern", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(171, "Spade", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("clue tools", index);

        assertEquals(2, results.size());
        assertTrue(names(results).contains("Bullseye lantern"));
        assertTrue(names(results).contains("Spade"));
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
