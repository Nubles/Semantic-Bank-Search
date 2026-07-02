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
        assertEquals("Barrows", results.get(0).getCategory());
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

    @Test
    public void gracefulAndSkillingOutfitsAreFoundBySkillingOutfitQuery()
    {
        StorageIndex index = new StorageIndex();
        index.record(350, "Graceful hood", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(351, "Prospector jacket", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(352, "Angler hat", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(353, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("skilling outfit", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Graceful hood"));
        assertTrue(names(results).contains("Prospector jacket"));
        assertTrue(names(results).contains("Angler hat"));
        assertFalse(names(results).contains("Rune platebody"));
    }

    @Test
    public void comboFoodRanksBeforeSlowFoodForFastFood()
    {
        StorageIndex index = new StorageIndex();
        index.record(360, "Shark", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(361, "Cooked karambwan", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(362, "Saradomin brew(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("fastest food", index);

        assertEquals(3, results.size());
        assertEquals("Saradomin brew(4)", results.get(0).getItemName());
        assertEquals("Cooked karambwan", results.get(1).getItemName());
        assertEquals("Shark", results.get(2).getItemName());
    }

    @Test
    public void alchemyRunesFindsNatureFireAndStaff()
    {
        StorageIndex index = new StorageIndex();
        index.record(370, "Nature rune", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(371, "Fire rune", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(372, "Staff of fire", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(373, "Law rune", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(374, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("alchemy runes", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Nature rune"));
        assertTrue(names(results).contains("Fire rune"));
        assertTrue(names(results).contains("Staff of fire"));
        assertFalse(names(results).contains("Law rune"));
        assertFalse(names(results).contains("Rune platebody"));
    }

    @Test
    public void skillingBoostDoesNotReturnCombatBoosts()
    {
        StorageIndex index = new StorageIndex();
        index.record(380, "Botanical pie", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(381, "Super combat potion(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(382, "Ranging potion(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("skilling boost", index);

        assertEquals(1, results.size());
        assertEquals("Botanical pie", results.get(0).getItemName());
    }

    @Test
    public void bindingRunesFindsEarthWaterAndNatureRunes()
    {
        StorageIndex index = new StorageIndex();
        index.record(390, "Earth rune", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(391, "Water rune", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(392, "Nature rune", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(393, "Fire rune", 100, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("binding runes", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Earth rune"));
        assertTrue(names(results).contains("Water rune"));
        assertTrue(names(results).contains("Nature rune"));
        assertFalse(names(results).contains("Fire rune"));
    }

    @Test
    public void skillingOutfitDoesNotMatchAnglerfish()
    {
        StorageIndex index = new StorageIndex();
        index.record(400, "Angler hat", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(401, "Anglerfish", 5, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("skilling outfit", index);

        assertEquals(1, results.size());
        assertEquals("Angler hat", results.get(0).getItemName());
    }

    @Test
    public void genericWeaponWordDoesNotMatchEveryWeaponRule()
    {
        StorageIndex index = new StorageIndex();
        index.record(600, "Dragon mace", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(601, "Rune scimitar", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(602, "Magic shortbow", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("crush weapons", index);

        assertEquals(1, results.size());
        assertEquals("Dragon mace", results.get(0).getItemName());
    }

    @Test
    public void genericToolWordDoesNotMatchEveryToolRule()
    {
        StorageIndex index = new StorageIndex();
        index.record(610, "Sextant", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(611, "Harpoon", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(612, "Seed dibber", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("coordinate clue", index);

        assertEquals(1, results.size());
        assertEquals("Sextant", results.get(0).getItemName());
    }

    @Test
    public void vorkathPrepDoesNotMatchVoidwaker()
    {
        StorageIndex index = new StorageIndex();
        index.record(620, "Void knight top", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(621, "Voidwaker", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("vorkath gear", index);

        assertEquals(1, results.size());
        assertEquals("Void knight top", results.get(0).getItemName());
        assertFalse(names(results).contains("Voidwaker"));
    }

    @Test
    public void skillingOutfitDoesNotReturnDesertAmulet()
    {
        StorageIndex index = new StorageIndex();
        index.record(630, "Graceful hood", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(631, "Desert amulet 2", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("skilling outfit", index);

        assertEquals(1, results.size());
        assertEquals("Graceful hood", results.get(0).getItemName());
        assertFalse(names(results).contains("Desert amulet 2"));
    }

    @Test
    public void bareRunesQueryStillFindsMagicRunes()
    {
        StorageIndex index = new StorageIndex();
        index.record(640, "Law rune", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(641, "Nature rune", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(642, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("runes", index);

        assertEquals(2, results.size());
        assertTrue(names(results).contains("Law rune"));
        assertTrue(names(results).contains("Nature rune"));
        assertFalse(names(results).contains("Rune platebody"));
    }

    @Test
    public void wintertodtSuppliesFindsWarmClothingAndTools()
    {
        StorageIndex index = new StorageIndex();
        index.record(650, "Clue hunter garb", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(651, "Steel axe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(652, "Tinderbox", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(653, "Hammer", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(654, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(655, "Rune pickaxe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(656, "Rune battleaxe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("wintertodt supplies", index);

        assertEquals(4, results.size());
        assertTrue(names(results).contains("Clue hunter garb"));
        assertTrue(names(results).contains("Steel axe"));
        assertTrue(names(results).contains("Tinderbox"));
        assertTrue(names(results).contains("Hammer"));
        assertFalse(names(results).contains("Rune platebody"));
        assertFalse(names(results).contains("Rune pickaxe"));
        assertFalse(names(results).contains("Rune battleaxe"));
    }

    @Test
    public void temporossSuppliesFindsFishingTools()
    {
        StorageIndex index = new StorageIndex();
        index.record(660, "Dragon harpoon", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(661, "Rope", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(662, "Bucket", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(663, "Angler hat", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(664, "Rune scimitar", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(665, "Granite hammer", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("tempoross supplies", index);

        assertEquals(4, results.size());
        assertTrue(names(results).contains("Dragon harpoon"));
        assertTrue(names(results).contains("Rope"));
        assertTrue(names(results).contains("Bucket"));
        assertTrue(names(results).contains("Angler hat"));
        assertFalse(names(results).contains("Rune scimitar"));
        assertFalse(names(results).contains("Granite hammer"));
    }

    @Test
    public void gotrFindsPouchesAndChisel()
    {
        StorageIndex index = new StorageIndex();
        index.record(670, "Large pouch", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(671, "Chisel", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(672, "Binding necklace", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(673, "Pure essence", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(674, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("gotr pouch", index);

        assertEquals(4, results.size());
        assertTrue(names(results).contains("Large pouch"));
        assertTrue(names(results).contains("Chisel"));
        assertTrue(names(results).contains("Binding necklace"));
        assertTrue(names(results).contains("Pure essence"));
        assertFalse(names(results).contains("Rune platebody"));
    }

    @Test
    public void mahoganyHomesFindsConstructionSuppliesAndTeleports()
    {
        StorageIndex index = new StorageIndex();
        index.record(680, "Saw", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(681, "Hammer", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(682, "Mahogany plank", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(683, "Steel bar", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(684, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(685, "Teleport to house", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("mahogany homes", index);

        assertEquals(5, results.size());
        assertTrue(names(results).contains("Saw"));
        assertTrue(names(results).contains("Hammer"));
        assertTrue(names(results).contains("Mahogany plank"));
        assertTrue(names(results).contains("Steel bar"));
        assertTrue(names(results).contains("Teleport to house"));
        assertFalse(names(results).contains("Rune platebody"));
    }

    @Test
    public void blastFurnaceFindsSmithingUtility()
    {
        StorageIndex index = new StorageIndex();
        index.record(690, "Coal bag", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(691, "Ice gloves", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(692, "Goldsmith gauntlets", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(693, "Stamina potion(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(694, "Spade", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(695, "Charcoal", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("blast furnace", index);

        assertEquals(4, results.size());
        assertTrue(names(results).contains("Coal bag"));
        assertTrue(names(results).contains("Ice gloves"));
        assertTrue(names(results).contains("Goldsmith gauntlets"));
        assertTrue(names(results).contains("Stamina potion(4)"));
        assertFalse(names(results).contains("Spade"));
        assertFalse(names(results).contains("Charcoal"));
    }

    @Test
    public void birdhouseRunFindsBirdhousesLogsAndDigsiteTravel()
    {
        StorageIndex index = new StorageIndex();
        index.record(700, "Oak bird house", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(701, "Clockwork", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(702, "Teak logs", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(703, "Digsite pendant", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(704, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("birdhouse run", index);

        assertEquals(4, results.size());
        assertTrue(names(results).contains("Oak bird house"));
        assertTrue(names(results).contains("Clockwork"));
        assertTrue(names(results).contains("Teak logs"));
        assertTrue(names(results).contains("Digsite pendant"));
        assertFalse(names(results).contains("Rune platebody"));
    }

    @Test
    public void herbRunFindsFarmingToolsCompostAndTeleports()
    {
        StorageIndex index = new StorageIndex();
        index.record(710, "Seed dibber", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(711, "Magic secateurs", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(712, "Ultracompost", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(713, "Skills necklace(6)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(714, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("herb run", index);

        assertEquals(4, results.size());
        assertTrue(names(results).contains("Seed dibber"));
        assertTrue(names(results).contains("Magic secateurs"));
        assertTrue(names(results).contains("Ultracompost"));
        assertTrue(names(results).contains("Skills necklace(6)"));
        assertFalse(names(results).contains("Rune platebody"));
    }

    @Test
    public void glassCraftingFindsSandSeaweedAndPipe()
    {
        StorageIndex index = new StorageIndex();
        index.record(720, "Bucket of sand", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(721, "Giant seaweed", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(722, "Glassblowing pipe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(723, "Rune scimitar", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("glass crafting prep", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Bucket of sand"));
        assertTrue(names(results).contains("Giant seaweed"));
        assertTrue(names(results).contains("Glassblowing pipe"));
        assertFalse(names(results).contains("Rune scimitar"));
    }

    @Test
    public void fletchingPrepFindsLogsStringAndFeathers()
    {
        StorageIndex index = new StorageIndex();
        index.record(730, "Maple logs", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(731, "Knife", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(732, "Bow string", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(733, "Feather", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(734, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("fletching prep", index);

        assertEquals(4, results.size());
        assertTrue(names(results).contains("Maple logs"));
        assertTrue(names(results).contains("Knife"));
        assertTrue(names(results).contains("Bow string"));
        assertTrue(names(results).contains("Feather"));
        assertFalse(names(results).contains("Rune platebody"));
    }

    @Test
    public void smithingPrepDoesNotMatchPotionTeleportOrFishingSubstrings()
    {
        StorageIndex index = new StorageIndex();
        index.record(740, "Iron ore", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(741, "Steel bar", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(742, "Coal bag", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(743, "Hammer", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(744, "Super restore(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(745, "Barrows teleport", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(746, "Barbarian rod", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("smithing prep", index);

        assertEquals(4, results.size());
        assertTrue(names(results).contains("Iron ore"));
        assertTrue(names(results).contains("Steel bar"));
        assertTrue(names(results).contains("Coal bag"));
        assertTrue(names(results).contains("Hammer"));
        assertFalse(names(results).contains("Super restore(4)"));
        assertFalse(names(results).contains("Barrows teleport"));
        assertFalse(names(results).contains("Barbarian rod"));
    }

    @Test
    public void cookingPrepDoesNotMatchStrawberryOrPieceSubstrings()
    {
        StorageIndex index = new StorageIndex();
        index.record(750, "Raw shark", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(751, "Raw karambwan", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(752, "Cooking gauntlets", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(753, "Strawberry", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(754, "Piece of cake", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("cooking prep", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Raw shark"));
        assertTrue(names(results).contains("Raw karambwan"));
        assertTrue(names(results).contains("Cooking gauntlets"));
        assertFalse(names(results).contains("Strawberry"));
        assertFalse(names(results).contains("Piece of cake"));
    }

    @Test
    public void glassCraftingPrepDoesNotMatchSandwichSubstrings()
    {
        StorageIndex index = new StorageIndex();
        index.record(760, "Bucket of sand", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(761, "Giant seaweed", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(762, "Glassblowing pipe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(763, "Gnome sandwich", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("glass crafting prep", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Bucket of sand"));
        assertTrue(names(results).contains("Giant seaweed"));
        assertTrue(names(results).contains("Glassblowing pipe"));
        assertFalse(names(results).contains("Gnome sandwich"));
    }

    @Test
    public void treeRunDoesNotReturnGenericTeleports()
    {
        StorageIndex index = new StorageIndex();
        index.record(770, "Oak sapling", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(771, "Spade", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(772, "Skills necklace(6)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(773, "Ring of dueling(8)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(774, "Varrock teleport", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("tree run", index);

        assertEquals(4, results.size());
        assertTrue(names(results).contains("Oak sapling"));
        assertTrue(names(results).contains("Spade"));
        assertTrue(names(results).contains("Skills necklace(6)"));
        assertTrue(names(results).contains("Ring of dueling(8)"));
        assertFalse(names(results).contains("Varrock teleport"));
    }

    @Test
    public void skillingMinigameQueriesDoNotBleedAcrossActivities()
    {
        StorageIndex index = new StorageIndex();
        index.record(780, "Steel axe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(781, "Tinderbox", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(782, "Dragon harpoon", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(783, "Large pouch", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(784, "Coal bag", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("wintertodt supplies", index);

        assertEquals(2, results.size());
        assertTrue(names(results).contains("Steel axe"));
        assertTrue(names(results).contains("Tinderbox"));
        assertFalse(names(results).contains("Dragon harpoon"));
        assertFalse(names(results).contains("Large pouch"));
        assertFalse(names(results).contains("Coal bag"));
    }

    @Test
    public void birdhouseRunDoesNotMatchGenericLogsEverywhere()
    {
        StorageIndex index = new StorageIndex();
        index.record(790, "Oak bird house", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(791, "Clockwork", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(792, "Maple logs", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(793, "Bow string", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(794, "Knife", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("birdhouse run", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Oak bird house"));
        assertTrue(names(results).contains("Clockwork"));
        assertTrue(names(results).contains("Maple logs"));
        assertFalse(names(results).contains("Bow string"));
        assertFalse(names(results).contains("Knife"));
    }

    @Test
    public void blastFurnaceDoesNotMatchGenericSmithingPrepOnlyItems()
    {
        StorageIndex index = new StorageIndex();
        index.record(800, "Coal bag", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(801, "Ice gloves", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(802, "Gold ore", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(803, "Hammer", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("blast furnace", index);

        assertEquals(3, results.size());
        assertTrue(names(results).contains("Coal bag"));
        assertTrue(names(results).contains("Ice gloves"));
        assertTrue(names(results).contains("Gold ore"));
        assertFalse(names(results).contains("Hammer"));
    }

    @Test
    public void nonBankObservedStorageItemsAreSearchableButNotHighlightable()
    {
        StorageIndex index = new StorageIndex();
        index.record(900, "Ranarr seed", 10, StorageSourceType.OTHER_STORAGE, "Seed Vault", true, 1_000L);
        index.record(901, "Seed dibber", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("herb run", index);

        SemanticSearchResult ranarrSeed = results.stream()
            .filter(result -> result.getItemName().equals("Ranarr seed"))
            .findFirst()
            .orElseThrow(AssertionError::new);
        SemanticSearchResult seedDibber = results.stream()
            .filter(result -> result.getItemName().equals("Seed dibber"))
            .findFirst()
            .orElseThrow(AssertionError::new);

        assertEquals("Seed Vault", ranarrSeed.getSourceName());
        assertFalse(ranarrSeed.isHighlightable());
        assertTrue(seedDibber.isHighlightable());
    }

    @Test
    public void kourendTravelFindsXericsAndMemoirs()
    {
        StorageIndex index = new StorageIndex();
        index.record(910, "Xeric's talisman", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(911, "Kharedst's memoirs", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(912, "Games necklace(8)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("kourend teleport", index);

        assertTrue(names(results).contains("Xeric's talisman"));
        assertTrue(names(results).contains("Kharedst's memoirs"));
        assertFalse(names(results).contains("Games necklace(8)"));
    }

    @Test
    public void fossilIslandTravelFindsDigsitePendant()
    {
        StorageIndex index = new StorageIndex();
        index.record(920, "Digsite pendant", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(921, "Numulite", 250, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(922, "Varrock teleport", 3, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(923, "Xeric's talisman", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(924, "Enchanted lyre", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(925, "Ardougne cloak", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("fossil island travel", index);

        assertTrue(names(results).contains("Digsite pendant"));
        assertTrue(names(results).contains("Numulite"));
        assertFalse(names(results).contains("Varrock teleport"));
        assertFalse(names(results).contains("Xeric's talisman"));
        assertFalse(names(results).contains("Enchanted lyre"));
        assertFalse(names(results).contains("Ardougne cloak"));
    }

    @Test
    public void fairyRingItemsFindsDramenStaff()
    {
        StorageIndex index = new StorageIndex();
        index.record(930, "Dramen staff", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(931, "Lunar staff", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(932, "Staff of air", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(933, "Shantay pass", 5, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("fairy ring access", index);

        assertTrue(names(results).contains("Dramen staff"));
        assertTrue(names(results).contains("Lunar staff"));
        assertFalse(names(results).contains("Staff of air"));
        assertFalse(names(results).contains("Shantay pass"));
    }

    @Test
    public void wildyTeleportFindsWildernessEscapeItems()
    {
        StorageIndex index = new StorageIndex();
        index.record(934, "Royal seed pod", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(935, "Wilderness sword 4", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(936, "Shantay pass", 5, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("wildy teleport", index);

        assertTrue(names(results).contains("Royal seed pod"));
        assertTrue(names(results).contains("Wilderness sword 4"));
        assertFalse(names(results).contains("Shantay pass"));
    }

    @Test
    public void fremennikTravelFindsLyreAndGamesNecklace()
    {
        StorageIndex index = new StorageIndex();
        index.record(940, "Enchanted lyre", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(941, "Games necklace(8)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(942, "Lunar isle teleport", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(943, "Shantay pass", 5, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("fremennik travel", index);

        assertTrue(names(results).contains("Enchanted lyre"));
        assertTrue(names(results).contains("Games necklace(8)"));
        assertTrue(names(results).contains("Lunar isle teleport"));
        assertFalse(names(results).contains("Shantay pass"));
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
