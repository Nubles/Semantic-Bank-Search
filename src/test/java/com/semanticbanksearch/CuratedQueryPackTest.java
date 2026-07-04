package com.semanticbanksearch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class CuratedQueryPackTest
{
    @Test
    public void corePlayerQueriesFindExpectedItemsWithoutBleed()
    {
        assertQueryPack(
            "barrows trip",
            positives("Barrows teleport", "Spade", "Prayer potion(4)", "Trident of the seas"),
            negatives("Shantay pass", "Dragon scimitar", "Raw shark"));

        assertQueryPack(
            "clue step",
            positives("Spade", "Bullseye lantern", "Games necklace(8)"),
            negatives("Dragon scimitar", "Anti-dragon shield", "Raw shark"));

        assertQueryPack(
            "herb run",
            positives("Seed dibber", "Ultracompost", "Ranarr seed", "Skills necklace(6)"),
            negatives("Dragon harpoon", "Rune platebody", "Coal bag"));

        assertQueryPack(
            "wildy escape",
            positives("Royal seed pod", "Burning amulet", "Ring of wealth"),
            negatives("Xeric's talisman", "Dragon scimitar", "Shantay pass"));

        assertQueryPack(
            "birdhouse run",
            positives("Oak bird house", "Clockwork", "Teak logs", "Digsite pendant"),
            negatives("Bow string", "Knife", "Rune platebody"));

        assertQueryPack(
            "wintertodt supplies",
            positives("Clue hunter boots", "Steel axe", "Tinderbox", "Cake"),
            negatives("Dragon harpoon", "Large pouch", "Coal bag"));

        assertQueryPack(
            "tempoross supplies",
            positives("Dragon harpoon", "Rope", "Bucket", "Angler hat"),
            negatives("Rune scimitar", "Coal bag", "Large pouch"));

        assertQueryPack(
            "dragon slayer task",
            positives("Anti-dragon shield", "Extended antifire(4)", "Dragon hunter crossbow", "Zamorakian hasta"),
            negatives("Arclight", "Mirror shield", "Facemask"));

        assertQueryPack(
            "desert travel",
            positives("Desert amulet 4", "Shantay pass", "Waterskin(4)"),
            negatives("Barrows teleport", "Xeric's talisman", "Enchanted lyre"));

        assertQueryPack(
            "fossil island travel",
            positives("Digsite pendant", "Numulite"),
            negatives("Xeric's talisman", "Enchanted lyre", "Ardougne cloak"));
    }

    @Test
    public void wikiGuidedBossTripQueriesFindCommonPrepItemsWithoutBleed()
    {
        assertQueryPack(
            "vorkath trip",
            positives("Extended super antifire(4)", "Anti-dragon shield", "Dragon hunter crossbow", "Salve amulet(ei)", "Ruby bolts (e)", "Diamond bolts (e)"),
            negatives("Arclight", "Dramen staff", "Raw shark"));

        assertQueryPack(
            "zulrah trip",
            positives("Anti-venom+(4)", "Trident of the seas", "Toxic blowpipe", "Ring of suffering", "Serpentine helm", "Zul-andra teleport"),
            negatives("Antipoison(4)", "Anti-dragon shield", "Barrows teleport"));

        assertQueryPack(
            "fight caves trip",
            positives("Prayer potion(4)", "Saradomin brew(4)", "Ranging potion(4)", "Toxic blowpipe", "Diamond bolts (e)", "Purple sweets"),
            negatives("Burning amulet", "Spade", "Shantay pass"));

        assertQueryPack(
            "wildy boss trip",
            positives("Royal seed pod", "Burning amulet", "Blighted super restore(4)", "Blighted manta ray", "Ring of dueling(8)", "Escape crystal"),
            negatives("Xeric's talisman", "Barrows teleport", "Dragon scimitar"));
    }

    private static List<String> positives(String... itemNames)
    {
        return Arrays.asList(itemNames);
    }

    private static List<String> negatives(String... itemNames)
    {
        return Arrays.asList(itemNames);
    }

    private static void assertQueryPack(String query, List<String> expectedItems, List<String> unexpectedItems)
    {
        StorageIndex index = new StorageIndex();
        int itemId = 10_000;
        for (String itemName : expectedItems)
        {
            index.record(itemId++, itemName, 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        }
        for (String itemName : unexpectedItems)
        {
            index.record(itemId++, itemName, 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        }

        String resultNames = names(new SemanticSearchEngine(SemanticLibrary.create()).search(query, index));
        for (String expectedItem : expectedItems)
        {
            assertTrue(query + " should include " + expectedItem + " in:\n" + resultNames, resultNames.contains(expectedItem));
        }
        for (String unexpectedItem : unexpectedItems)
        {
            assertFalse(query + " should exclude " + unexpectedItem + " from:\n" + resultNames, resultNames.contains(unexpectedItem));
        }
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
