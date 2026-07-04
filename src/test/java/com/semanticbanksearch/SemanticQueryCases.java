package com.semanticbanksearch;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class SemanticQueryCases
{
    private SemanticQueryCases()
    {
    }

    static List<SemanticQueryCase> all()
    {
        return Collections.unmodifiableList(Arrays.asList(
            SemanticQueryCase.of(
                "Core trips",
                "barrows trip",
                positives("Barrows teleport", "Spade", "Prayer potion(4)", "Trident of the seas"),
                negatives("Shantay pass", "Dragon scimitar", "Raw shark")),
            SemanticQueryCase.of(
                "Core utility",
                "clue step",
                positives("Spade", "Bullseye lantern", "Games necklace(8)"),
                negatives("Dragon scimitar", "Anti-dragon shield", "Raw shark")),
            SemanticQueryCase.of(
                "Skilling runs",
                "herb run",
                positives("Seed dibber", "Ultracompost", "Ranarr seed", "Skills necklace(6)"),
                negatives("Dragon harpoon", "Rune platebody", "Coal bag")),
            SemanticQueryCase.of(
                "Wilderness escape",
                "wildy escape",
                positives("Royal seed pod", "Burning amulet", "Ring of wealth"),
                negatives("Xeric's talisman", "Dragon scimitar", "Shantay pass")),
            SemanticQueryCase.of(
                "Skilling runs",
                "birdhouse run",
                positives("Oak bird house", "Clockwork", "Teak logs", "Digsite pendant"),
                negatives("Bow string", "Knife", "Rune platebody")),
            SemanticQueryCase.of(
                "Minigames",
                "wintertodt supplies",
                positives("Clue hunter boots", "Steel axe", "Tinderbox", "Cake"),
                negatives("Dragon harpoon", "Large pouch", "Coal bag")),
            SemanticQueryCase.of(
                "Minigames",
                "tempoross supplies",
                positives("Dragon harpoon", "Rope", "Bucket", "Angler hat"),
                negatives("Rune scimitar", "Coal bag", "Large pouch")),
            SemanticQueryCase.of(
                "Slayer",
                "dragon slayer task",
                positives("Anti-dragon shield", "Extended antifire(4)", "Dragon hunter crossbow", "Zamorakian hasta"),
                negatives("Arclight", "Mirror shield", "Facemask")),
            SemanticQueryCase.of(
                "Travel",
                "desert travel",
                positives("Desert amulet 4", "Shantay pass", "Waterskin(4)"),
                negatives("Barrows teleport", "Xeric's talisman", "Enchanted lyre")),
            SemanticQueryCase.of(
                "Travel",
                "fossil island travel",
                positives("Digsite pendant", "Numulite"),
                negatives("Xeric's talisman", "Enchanted lyre", "Ardougne cloak")),
            SemanticQueryCase.of(
                "Wiki-guided boss trips",
                "vorkath trip",
                positives("Extended super antifire(4)", "Anti-dragon shield", "Dragon hunter crossbow", "Salve amulet(ei)", "Ruby bolts (e)", "Diamond bolts (e)"),
                negatives("Arclight", "Dramen staff", "Raw shark")),
            SemanticQueryCase.of(
                "Wiki-guided boss trips",
                "zulrah trip",
                positives("Anti-venom+(4)", "Trident of the seas", "Toxic blowpipe", "Ring of suffering", "Serpentine helm", "Zul-andra teleport"),
                negatives("Antipoison(4)", "Anti-dragon shield", "Barrows teleport")),
            SemanticQueryCase.of(
                "Wiki-guided boss trips",
                "fight caves trip",
                positives("Prayer potion(4)", "Saradomin brew(4)", "Ranging potion(4)", "Toxic blowpipe", "Diamond bolts (e)", "Purple sweets"),
                negatives("Burning amulet", "Spade", "Shantay pass")),
            SemanticQueryCase.of(
                "Wiki-guided boss trips",
                "wildy boss trip",
                positives("Royal seed pod", "Burning amulet", "Blighted super restore(4)", "Blighted manta ray", "Ring of dueling(8)", "Escape crystal"),
                negatives("Xeric's talisman", "Barrows teleport", "Dragon scimitar"))));
    }

    private static List<String> positives(String... itemNames)
    {
        return Arrays.asList(itemNames);
    }

    private static List<String> negatives(String... itemNames)
    {
        return Arrays.asList(itemNames);
    }
}