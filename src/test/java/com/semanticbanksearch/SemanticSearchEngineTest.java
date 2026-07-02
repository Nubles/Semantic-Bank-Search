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

    @Test
    public void vorkathPrepFindsDragonfireAndRangedSupplies()
    {
        StorageIndex index = new StorageIndex();
        index.record(200, "Extended super antifire(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(201, "Dragon crossbow", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(202, "Diamond dragon bolts (e)", 50, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(203, "Lobster pot", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("vorkath gear", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Extended super antifire(4)"));
        assertTrue(names(results).contains("Dragon crossbow"));
        assertTrue(names(results).contains("Diamond dragon bolts (e)"));
        assertFalse(names(results).contains("Lobster pot"));
    }

    @Test
    public void fightCavesPrepFindsPrayerRangedAndSustains()
    {
        StorageIndex index = new StorageIndex();
        index.record(210, "Prayer potion(4)", 4, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(211, "Toxic blowpipe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(212, "Saradomin brew(4)", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(213, "Hammer", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("fight caves supplies", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Prayer potion(4)"));
        assertTrue(names(results).contains("Toxic blowpipe"));
        assertTrue(names(results).contains("Saradomin brew(4)"));
        assertFalse(names(results).contains("Hammer"));
    }

    @Test
    public void zulrahPrepFindsAntipoisonAndSwitchGear()
    {
        StorageIndex index = new StorageIndex();
        index.record(220, "Anti-venom+(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(221, "Trident of the swamp", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(222, "Magic shortbow", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(223, "Spade", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("zulrah setup", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Anti-venom+(4)"));
        assertTrue(names(results).contains("Trident of the swamp"));
        assertTrue(names(results).contains("Magic shortbow"));
        assertFalse(names(results).contains("Spade"));
    }

    @Test
    public void bossPrepKeywordDoesNotMatchEveryBossPrepRule()
    {
        StorageIndex index = new StorageIndex();
        index.record(230, "Extended super antifire(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(231, "Dragon crossbow", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(232, "Spade", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(233, "Rune thrownaxe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("vorkath prep", index);

        assertEquals(2, results.size());
        assertTrue(names(results).contains("Extended super antifire(4)"));
        assertTrue(names(results).contains("Dragon crossbow"));
        assertFalse(names(results).contains("Spade"));
        assertFalse(names(results).contains("Rune thrownaxe"));
    }

    @Test
    public void barrowsTeleportQueryPrefersTeleportRuleOverBossPrep()
    {
        StorageIndex index = new StorageIndex();
        index.record(240, "Barrows teleport", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(241, "Spade", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(242, "Prayer potion(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("barrows teleport", index);

        assertEquals(1, results.size());
        assertEquals("Barrows teleport", results.get(0).getItemName());
        assertEquals("Barrows teleports", results.get(0).getCategory());
    }

    @Test
    public void bareBarrowsQueryFindsTravelItemsWithoutBossPrepBleed()
    {
        StorageIndex index = new StorageIndex();
        index.record(250, "Mort'ton teleport", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(251, "Morytania legs 3", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(252, "Drakan's medallion", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(253, "Spade", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("barrows", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Mort'ton teleport"));
        assertTrue(names(results).contains("Morytania legs 3"));
        assertTrue(names(results).contains("Drakan's medallion"));
        assertFalse(names(results).contains("Spade"));
    }

    @Test
    public void zulrahPrepRanksAntiVenomPlusAbovePlainAntiVenom()
    {
        StorageIndex index = new StorageIndex();
        index.record(260, "Anti-venom(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(261, "Anti-venom+(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("zulrah setup", index);

        assertEquals(2, results.size());
        assertEquals("Anti-venom+(4)", results.get(0).getItemName());
        assertEquals("Anti-venom(4)", results.get(1).getItemName());
    }

    @Test
    public void runEnergyQueryDoesNotReturnCryptPrepItems()
    {
        StorageIndex index = new StorageIndex();
        index.record(270, "Stamina potion(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(271, "Barrows teleport", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(272, "Spade", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("run energy", index);

        assertEquals(1, results.size());
        assertEquals("Stamina potion(4)", results.get(0).getItemName());
        assertEquals("Run energy restoration", results.get(0).getCategory());
        assertFalse(names(results).contains("Barrows teleport"));
        assertFalse(names(results).contains("Spade"));
    }


    @Test
    public void dragonSlayerTaskFindsAntifireAndDragonWeapons()
    {
        StorageIndex index = new StorageIndex();
        index.record(280, "Extended antifire(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(281, "Dragon hunter lance", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(282, "Anti-dragon shield", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(283, "Lobster pot", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("dragon slayer task", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Extended antifire(4)"));
        assertTrue(names(results).contains("Dragon hunter lance"));
        assertTrue(names(results).contains("Anti-dragon shield"));
        assertFalse(names(results).contains("Lobster pot"));
    }

    @Test
    public void dustDevilTaskFindsMaskAndBurstRunes()
    {
        StorageIndex index = new StorageIndex();
        index.record(290, "Slayer helmet", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(291, "Facemask", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(292, "Death rune", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(293, "Dragon hunter lance", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(294, "Occult necklace", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(295, "Rock hammer", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(296, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("dust devil task", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Slayer helmet"));
        assertTrue(names(results).contains("Facemask"));
        assertTrue(names(results).contains("Death rune"));
        assertFalse(names(results).contains("Dragon hunter lance"));
        assertFalse(names(results).contains("Occult necklace"));
        assertFalse(names(results).contains("Rock hammer"));
        assertFalse(names(results).contains("Rune platebody"));
    }

    @Test
    public void basiliskTaskFindsMirrorShield()
    {
        StorageIndex index = new StorageIndex();
        index.record(300, "Mirror shield", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(301, "V's shield", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(302, "Dragon hunter lance", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(303, "Rock hammer", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(304, "Facemask", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(305, "Bronze shield", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("basilisk task", index);

        assertEquals(2, results.size());
        assertTrue(names(results).contains("Mirror shield"));
        assertTrue(names(results).contains("V's shield"));
        assertFalse(names(results).contains("Dragon hunter lance"));
        assertFalse(names(results).contains("Rock hammer"));
        assertFalse(names(results).contains("Facemask"));
        assertFalse(names(results).contains("Bronze shield"));
    }

    @Test
    public void desertTravelFindsWaterskinsAndDesertAccess()
    {
        StorageIndex index = new StorageIndex();
        index.record(310, "Waterskin(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(311, "Desert amulet 2", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(312, "Shantay pass", 5, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(313, "Air rune", 100, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("desert travel", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Waterskin(4)"));
        assertTrue(names(results).contains("Desert amulet 2"));
        assertTrue(names(results).contains("Shantay pass"));
        assertFalse(names(results).contains("Air rune"));
    }

    @Test
    public void coordinateClueFindsNavigationTools()
    {
        StorageIndex index = new StorageIndex();
        index.record(320, "Sextant", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(321, "Watch", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(322, "Chart", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(323, "Rune scimitar", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(324, "Saw", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(325, "Rope", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(326, "Amulet of glory(6)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(327, "Stamina potion(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("coordinate clue", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Sextant"));
        assertTrue(names(results).contains("Watch"));
        assertTrue(names(results).contains("Chart"));
        assertFalse(names(results).contains("Rune scimitar"));
        assertFalse(names(results).contains("Saw"));
        assertFalse(names(results).contains("Rope"));
        assertFalse(names(results).contains("Amulet of glory(6)"));
        assertFalse(names(results).contains("Stamina potion(4)"));
    }

    @Test
    public void questUtilityFindsCommonQuestTools()
    {
        StorageIndex index = new StorageIndex();
        index.record(330, "Ghostspeak amulet", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(331, "Rope", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(332, "Pickaxe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(333, "Shark", 5, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(334, "Lockpick", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(335, "Saw", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("quest utility", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Ghostspeak amulet"));
        assertTrue(names(results).contains("Rope"));
        assertTrue(names(results).contains("Pickaxe"));
        assertFalse(names(results).contains("Shark"));
        assertFalse(names(results).contains("Lockpick"));
        assertFalse(names(results).contains("Saw"));
    }

    @Test
    public void desertTeleportDoesNotReturnProtectionOnlyItems()
    {
        StorageIndex index = new StorageIndex();
        index.record(340, "Desert amulet 2", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(341, "Shantay pass", 5, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(342, "Waterskin(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(343, "Circlet of water", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(344, "Desert robes", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("desert teleport", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Desert amulet 2"));
        assertTrue(names(results).contains("Shantay pass"));
        assertTrue(names(results).contains("Waterskin(4)"));
        assertFalse(names(results).contains("Circlet of water"));
        assertFalse(names(results).contains("Desert robes"));
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
