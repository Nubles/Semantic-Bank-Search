package com.semanticbanksearch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class SemanticCoverageBenchmarkTest
{
    @Test
    public void midgameUtilityBankKeepsExpectedCoverage()
    {
        CoverageSummary summary = summarize(fixture(
            "Prayer potion(4)",
            "Super restore(4)",
            "Stamina potion(4)",
            "Antipoison(4)",
            "Extended antifire(4)",
            "Shark",
            "Cooked karambwan",
            "Barrows teleport",
            "Games necklace(8)",
            "Ring of dueling(8)",
            "Amulet of glory(6)",
            "Digsite pendant",
            "Spade",
            "Rope",
            "Bullseye lantern",
            "Ghostspeak amulet",
            "Dragon scimitar",
            "Law rune",
            "Uncut sapphire",
            "Coins"));

        assertCoverageAtLeast("midgame utility", summary, 18, 20);
        assertCovered(summary, "Prayer potion(4)");
        assertCovered(summary, "Digsite pendant");
        assertCovered(summary, "Ghostspeak amulet");
        assertUncovered(summary, "Uncut sapphire");
        assertUncovered(summary, "Coins");
    }

    @Test
    public void ironmanSkillingBankKeepsExpectedCoverage()
    {
        CoverageSummary summary = summarize(fixture(
            "Seed dibber",
            "Rake",
            "Ultracompost",
            "Ranarr seed",
            "Oak bird house",
            "Clockwork",
            "Teak logs",
            "Dragon harpoon",
            "Angler hat",
            "Coal bag",
            "Large pouch",
            "Steel axe",
            "Tinderbox",
            "Bucket of sand",
            "Giant seaweed",
            "Glassblowing pipe",
            "Bow string",
            "Feather",
            "Uncut emerald",
            "Clue scroll (medium)"));

        assertCoverageAtLeast("ironman skilling", summary, 18, 20);
        assertCovered(summary, "Seed dibber");
        assertCovered(summary, "Coal bag");
        assertCovered(summary, "Glassblowing pipe");
        assertUncovered(summary, "Uncut emerald");
        assertUncovered(summary, "Clue scroll (medium)");
    }

    @Test
    public void pvmAndSlayerTabKeepsExpectedCoverage()
    {
        CoverageSummary summary = summarize(fixture(
            "Super combat potion(4)",
            "Ranging potion(4)",
            "Magic potion(4)",
            "Saradomin brew(4)",
            "Manta ray",
            "Anti-dragon shield",
            "Dragonfire shield",
            "Dragon hunter crossbow",
            "Zamorakian hasta",
            "Arclight",
            "Black mask",
            "Slayer helmet",
            "Mirror shield",
            "Facemask",
            "Rune crossbow",
            "Diamond bolts (e)",
            "Trident of the seas",
            "Ancient staff",
            "Rune platebody",
            "Ensouled goblin head"));

        assertCoverageAtLeast("pvm and slayer", summary, 17, 20);
        assertCovered(summary, "Dragon hunter crossbow");
        assertCovered(summary, "Slayer helmet");
        assertCovered(summary, "Ancient staff");
        assertUncovered(summary, "Black mask");
        assertUncovered(summary, "Rune platebody");
        assertUncovered(summary, "Ensouled goblin head");
    }

    @Test
    public void clueAndUtilityTabKeepsExpectedCoverage()
    {
        CoverageSummary summary = summarize(fixture(
            "Spade",
            "Sextant",
            "Watch",
            "Chart",
            "Bullseye lantern",
            "Light box",
            "Lockpick",
            "Machete",
            "Dramen staff",
            "Lunar staff",
            "Kharedst's memoirs",
            "Xeric's talisman",
            "Ardougne cloak",
            "Desert amulet 4",
            "Shantay pass",
            "Waterskin(4)",
            "Rope",
            "Hammer",
            "Casket",
            "Clue scroll (hard)"));

        assertCoverageAtLeast("clue and utility", summary, 17, 20);
        assertCovered(summary, "Sextant");
        assertCovered(summary, "Dramen staff");
        assertCovered(summary, "Waterskin(4)");
        assertUncovered(summary, "Light box");
        assertUncovered(summary, "Casket");
        assertUncovered(summary, "Clue scroll (hard)");
    }

    private static StorageIndex fixture(String... itemNames)
    {
        StorageIndex index = new StorageIndex();
        int itemId = 50_000;
        long lastSeenMillis = 1_000L;
        for (String itemName : itemNames)
        {
            index.record(itemId++, itemName, 1, StorageSourceType.BANK, "Benchmark bank", true, lastSeenMillis++);
        }
        return index;
    }

    private static CoverageSummary summarize(StorageIndex index)
    {
        return new CoverageSummary(new SemanticCoverageAnalyzer(SemanticLibrary.create()).analyze(index.items()));
    }

    private static void assertCoverageAtLeast(String fixtureName, CoverageSummary summary, int expectedCovered, int expectedTotal)
    {
        assertTrue(
            fixtureName + " coverage was " + summary.coveredCount + " of " + summary.totalCount + "\nUncovered:\n" + summary.uncoveredNames(),
            summary.coveredCount >= expectedCovered);
        assertTrue(
            fixtureName + " should have at least " + expectedTotal + " items",
            summary.totalCount >= expectedTotal);
    }

    private static void assertCovered(CoverageSummary summary, String itemName)
    {
        assertTrue(itemName + " should be covered", summary.isCovered(itemName));
    }

    private static void assertUncovered(CoverageSummary summary, String itemName)
    {
        assertFalse(itemName + " should stay uncovered until explicitly categorized", summary.isCovered(itemName));
    }

    private static final class CoverageSummary
    {
        private final List<SemanticCoverageResult> results;
        private final int coveredCount;
        private final int totalCount;

        private CoverageSummary(List<SemanticCoverageResult> results)
        {
            this.results = new ArrayList<>(results);
            this.totalCount = results.size();

            int covered = 0;
            for (SemanticCoverageResult result : results)
            {
                if (result.isCovered())
                {
                    covered++;
                }
            }
            this.coveredCount = covered;
        }

        private boolean isCovered(String itemName)
        {
            for (SemanticCoverageResult result : results)
            {
                if (result.getItemName().equals(itemName))
                {
                    return result.isCovered();
                }
            }
            throw new AssertionError("Missing fixture item " + itemName);
        }

        private String uncoveredNames()
        {
            StringBuilder builder = new StringBuilder();
            for (SemanticCoverageResult result : results)
            {
                if (!result.isCovered())
                {
                    builder.append(result.getItemName()).append("\n");
                }
            }
            return builder.toString();
        }
    }
}