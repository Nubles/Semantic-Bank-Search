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
    public void bossReadinessPacksCoverCommonOwnedTripItems()
    {
        ReadinessAnalyzer analyzer = new ReadinessAnalyzer(SemanticLibrary.create(), ReadinessPackLibrary.create());

        StorageIndex vorkath = new StorageIndex();
        vorkath.record(300, "Extended super antifire(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        vorkath.record(301, "Dragon hunter crossbow", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        vorkath.record(302, "Diamond dragon bolts (e)", 200, StorageSourceType.BANK, "Bank", true, 1_000L);
        vorkath.record(303, "Prayer potion(4)", 4, StorageSourceType.BANK, "Bank", true, 1_000L);
        ReadinessResult vorkathResult = analyzer.analyze("vorkath trip", vorkath);
        assertEquals("Vorkath trip", vorkathResult.getPackName());
        assertTrue(hasOwnedSlot(vorkathResult, "Dragonfire protection", "Extended super antifire(4)"));
        assertTrue(hasOwnedSlot(vorkathResult, "Ranged weapon", "Dragon hunter crossbow"));
        assertTrue(hasOwnedSlot(vorkathResult, "Bolts", "Diamond dragon bolts (e)"));
        assertTrue(hasOwnedSlot(vorkathResult, "Prayer restoration", "Prayer potion(4)"));

        StorageIndex zulrah = new StorageIndex();
        zulrah.record(310, "Anti-venom+(4)", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
        zulrah.record(311, "Trident of the seas", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        zulrah.record(312, "Toxic blowpipe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        zulrah.record(313, "Zul-andra teleport", 5, StorageSourceType.BANK, "Bank", true, 1_000L);
        ReadinessResult zulrahResult = analyzer.analyze("zulrah trip", zulrah);
        assertEquals("Zulrah trip", zulrahResult.getPackName());
        assertTrue(hasOwnedSlot(zulrahResult, "Venom protection", "Anti-venom+(4)"));
        assertTrue(hasOwnedSlot(zulrahResult, "Magic weapon", "Trident of the seas"));
        assertTrue(hasOwnedSlot(zulrahResult, "Ranged weapon", "Toxic blowpipe"));
        assertTrue(hasOwnedSlot(zulrahResult, "Zulrah travel", "Zul-andra teleport"));
    }

    @Test
    public void activityReadinessPacksCoverCommonSkillingAndClueItems()
    {
        ReadinessAnalyzer analyzer = new ReadinessAnalyzer(SemanticLibrary.create(), ReadinessPackLibrary.create());

        StorageIndex birdhouse = new StorageIndex();
        birdhouse.record(400, "Oak bird house", 4, StorageSourceType.BANK, "Bank", true, 1_000L);
        birdhouse.record(401, "Clockwork", 4, StorageSourceType.BANK, "Bank", true, 1_000L);
        birdhouse.record(402, "Hammer", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        birdhouse.record(403, "Digsite pendant", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        ReadinessResult birdhouseResult = analyzer.analyze("birdhouse run", birdhouse);
        assertEquals("Birdhouse run", birdhouseResult.getPackName());
        assertTrue(hasOwnedSlot(birdhouseResult, "Bird houses", "Oak bird house"));
        assertTrue(hasOwnedSlot(birdhouseResult, "Clockworks", "Clockwork"));
        assertTrue(hasOwnedSlot(birdhouseResult, "Tools", "Hammer"));
        assertTrue(hasOwnedSlot(birdhouseResult, "Fossil Island travel", "Digsite pendant"));

        StorageIndex contract = new StorageIndex();
        contract.record(410, "Seed dibber", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        contract.record(411, "Ultracompost", 50, StorageSourceType.BANK, "Bank", true, 1_000L);
        contract.record(412, "Garden pie", 3, StorageSourceType.BANK, "Bank", true, 1_000L);
        ReadinessResult contractResult = analyzer.analyze("farm contract", contract);
        assertEquals("Farm contract", contractResult.getPackName());
        assertTrue(hasOwnedSlot(contractResult, "Farming tools", "Seed dibber"));
        assertTrue(hasOwnedSlot(contractResult, "Compost", "Ultracompost"));
        assertTrue(hasOwnedSlot(contractResult, "Boost", "Garden pie"));
    }

    @Test
    public void combatAndUtilityReadinessPacksCoverCommonOwnedItems()
    {
        ReadinessAnalyzer analyzer = new ReadinessAnalyzer(SemanticLibrary.create(), ReadinessPackLibrary.create());

        StorageIndex fightCaves = new StorageIndex();
        fightCaves.record(500, "Prayer potion(4)", 12, StorageSourceType.BANK, "Bank", true, 1_000L);
        fightCaves.record(501, "Toxic blowpipe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        fightCaves.record(502, "Saradomin brew(4)", 8, StorageSourceType.BANK, "Bank", true, 1_000L);
        ReadinessResult fightCavesResult = analyzer.analyze("fight caves", fightCaves);
        assertEquals("Fight Caves", fightCavesResult.getPackName());
        assertTrue(hasOwnedSlot(fightCavesResult, "Prayer restoration", "Prayer potion(4)"));
        assertTrue(hasOwnedSlot(fightCavesResult, "Ranged weapon", "Toxic blowpipe"));
        assertTrue(hasOwnedSlot(fightCavesResult, "Long-trip sustain", "Saradomin brew(4)"));

        StorageIndex slayer = new StorageIndex();
        slayer.record(510, "Slayer helmet", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        slayer.record(511, "Super combat potion(4)", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
        slayer.record(512, "Prayer potion(4)", 4, StorageSourceType.BANK, "Bank", true, 1_000L);
        ReadinessResult slayerResult = analyzer.analyze("slayer task", slayer);
        assertEquals("Slayer task", slayerResult.getPackName());
        assertTrue(hasOwnedSlot(slayerResult, "Slayer item", "Slayer helmet"));
        assertTrue(hasOwnedSlot(slayerResult, "Combat boost", "Super combat potion(4)"));
        assertTrue(hasOwnedSlot(slayerResult, "Prayer restoration", "Prayer potion(4)"));
    }

    @Test
    public void wildyBossDksQuestAndWintertodtPacksCoverOwnedItems()
    {
        ReadinessAnalyzer analyzer = new ReadinessAnalyzer(SemanticLibrary.create(), ReadinessPackLibrary.create());

        StorageIndex wildyBoss = new StorageIndex();
        wildyBoss.record(600, "Royal seed pod", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        wildyBoss.record(601, "Blighted super restore(4)", 3, StorageSourceType.BANK, "Bank", true, 1_000L);
        wildyBoss.record(602, "Black d'hide body", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        ReadinessResult wildyBossResult = analyzer.analyze("wildy boss", wildyBoss);
        assertEquals("Wildy boss", wildyBossResult.getPackName());
        assertTrue(hasOwnedSlot(wildyBossResult, "Escape teleport", "Royal seed pod"));
        assertTrue(hasOwnedSlot(wildyBossResult, "Blighted supplies", "Blighted super restore(4)"));
        assertTrue(hasOwnedSlot(wildyBossResult, "Risk-light gear", "Black d'hide body"));

        StorageIndex dks = new StorageIndex();
        dks.record(610, "Rune thrownaxe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        dks.record(611, "Prayer potion(4)", 5, StorageSourceType.BANK, "Bank", true, 1_000L);
        dks.record(612, "Antipoison(4)", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
        ReadinessResult dksResult = analyzer.analyze("dagannoth kings", dks);
        assertEquals("Dagannoth Kings", dksResult.getPackName());
        assertTrue(hasOwnedSlot(dksResult, "Door tool", "Rune thrownaxe"));
        assertTrue(hasOwnedSlot(dksResult, "Prayer restoration", "Prayer potion(4)"));
        assertTrue(hasOwnedSlot(dksResult, "Poison protection", "Antipoison(4)"));

        StorageIndex questTools = new StorageIndex();
        questTools.record(620, "Rope", 3, StorageSourceType.BANK, "Bank", true, 1_000L);
        questTools.record(621, "Spade", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        questTools.record(622, "Bullseye lantern", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        ReadinessResult questResult = analyzer.analyze("quest tools", questTools);
        assertEquals("Quest tools", questResult.getPackName());
        assertTrue(hasOwnedSlot(questResult, "General tools", "Rope"));
        assertTrue(hasOwnedSlot(questResult, "Digging", "Spade"));
        assertTrue(hasOwnedSlot(questResult, "Light source", "Bullseye lantern"));

        StorageIndex wintertodt = new StorageIndex();
        wintertodt.record(630, "Clue hunter garb", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        wintertodt.record(631, "Knife", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        wintertodt.record(632, "Shark", 5, StorageSourceType.BANK, "Bank", true, 1_000L);
        ReadinessResult wintertodtResult = analyzer.analyze("wintertodt", wintertodt);
        assertEquals("Wintertodt", wintertodtResult.getPackName());
        assertTrue(hasOwnedSlot(wintertodtResult, "Warm clothing", "Clue hunter garb"));
        assertTrue(hasOwnedSlot(wintertodtResult, "Tool", "Knife"));
        assertTrue(hasOwnedSlot(wintertodtResult, "Food", "Shark"));
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
