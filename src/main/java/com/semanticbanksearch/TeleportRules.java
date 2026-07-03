package com.semanticbanksearch;

import java.util.Arrays;
import java.util.List;

final class TeleportRules
{
    private TeleportRules()
    {
    }

    static List<SemanticRule> create()
    {
        return Arrays.asList(
            SemanticLibrary.rule(
                "Barrows",
                "Gets you near Barrows.",
                SemanticLibrary.aliases("teleport near barrows", "barrows teleport", "mortton teleport", "morytania teleport"),
                SemanticLibrary.patterns("barrows teleport", "mort'ton teleport", "morytania legs", "shades of mort'ton", "drakan's medallion"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Kharidian desert",
                "Gets you into or around the desert.",
                SemanticLibrary.aliases("desert travel", "desert teleport", "shantay", "alkharid", "pollnivneach", "nardah"),
                SemanticLibrary.patterns("desert amulet", "pharaoh's sceptre", "camulet", "slayer ring", "ring of dueling", "shantay pass", "waterskin"),
                SemanticLibrary.scores(
                    "desert amulet", 30,
                    "pharaoh's sceptre", 25,
                    "shantay pass", 20),
                110),
            SemanticLibrary.rule(
                "Morytania",
                "Gets you into or around Morytania.",
                SemanticLibrary.aliases("morytania travel", "morytania teleport", "canifis", "burgh de rott", "mortton"),
                SemanticLibrary.patterns("morytania legs", "ecto", "ectophial", "drakan's medallion", "barrows teleport", "mort'ton teleport", "fenkenstrain"),
                SemanticLibrary.scores(
                    "drakan's medallion", 30,
                    "morytania legs", 25,
                    "barrows teleport", 25),
                110),
            SemanticLibrary.rule(
                "Wilderness escape",
                "Fast teleports for leaving dangerous wilderness situations.",
                SemanticLibrary.aliases("wilderness escape", "wildy escape", "escape teleport", "teleport out", "wilderness travel", "wildy teleport"),
                SemanticLibrary.patterns("royal seed pod", "amulet of glory", "ring of wealth", "burning amulet", "games necklace", "one-click teleport", "teleport crystal", "wilderness sword"),
                SemanticLibrary.scores(
                    "royal seed pod", 35,
                    "amulet of glory", 25,
                    "ring of wealth", 20),
                110),
            SemanticLibrary.rule(
                "Kourend Zeah",
                "Gets you around Great Kourend and Zeah.",
                SemanticLibrary.aliases("kourend travel", "kourend teleport", "zeah travel", "zeah teleport", "xeric teleport"),
                SemanticLibrary.patterns("xeric's talisman", "kharedst's memoirs", "book of the dead", "skills necklace", "rada's blessing"),
                SemanticLibrary.scores(
                    "xeric's talisman", 35,
                    "kharedst's memoirs", 30,
                    "book of the dead", 25),
                115),
            SemanticLibrary.rule(
                "Fossil Island",
                "Gets you to or supports Fossil Island travel.",
                SemanticLibrary.aliases("fossil island travel", "fossil island teleport", "digsite pendant fossil", "mushroom meadow"),
                SemanticLibrary.patterns("digsite pendant", "numulite", "mushroom meadow", "volcanic mine"),
                SemanticLibrary.scores(
                    "digsite pendant", 35,
                    "numulite", 10),
                115),
            SemanticLibrary.rule(
                "Fairy network",
                "Items used to access fairy rings.",
                SemanticLibrary.aliases("fairy ring", "fairy rings", "fairy ring items", "fairy ring access"),
                SemanticLibrary.patterns("dramen staff", "lunar staff", "quest cape"),
                SemanticLibrary.scores(
                    "dramen staff", 35,
                    "lunar staff", 35,
                    "quest cape", 20),
                115),
            SemanticLibrary.rule(
                "Fremennik region",
                "Gets you around Fremennik areas, Lunar Isle, and Waterbirth.",
                SemanticLibrary.aliases("fremennik travel", "fremennik teleport", "rellekka teleport", "lunar isle", "waterbirth teleport"),
                SemanticLibrary.patterns("enchanted lyre", "games necklace", "lunar isle teleport", "waterbirth teleport", "fremennik sea boots"),
                SemanticLibrary.scores(
                    "enchanted lyre", 35,
                    "lunar isle teleport", 30,
                    "waterbirth teleport", 30,
                    "games necklace", 20),
                115),
            SemanticLibrary.rule(
                "Kandarin Ardougne",
                "Gets you around Ardougne, Camelot, Catherby, and Kandarin.",
                SemanticLibrary.aliases("kandarin travel", "ardougne travel", "ardougne teleport", "camelot travel", "catherby teleport"),
                SemanticLibrary.patterns("ardougne cloak", "camelot teleport", "combat bracelet", "skills necklace", "kandarin headgear"),
                SemanticLibrary.scores(
                    "ardougne cloak", 35,
                    "camelot teleport", 25,
                    "skills necklace", 20),
                110),
            SemanticLibrary.rule(
                "Charged jewellery",
                "Charged jewellery with teleport options.",
                SemanticLibrary.aliases("teleport jewellery", "jewellery teleport", "charged jewellery"),
                SemanticLibrary.patterns(
                    "games necklace",
                    "ring of dueling",
                    "amulet of glory",
                    "combat bracelet",
                    "skills necklace",
                    "necklace of passage",
                    "burning amulet",
                    "digsite pendant",
                    "slayer ring",
                    "ring of wealth"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Spell tablets",
                "Single-use teleport tablets.",
                SemanticLibrary.aliases("teleport tablet", "tele tabs", "house tab", "spell tablet"),
                SemanticLibrary.patterns("teleport", "tablet", "redirected house tablet"),
                SemanticLibrary.scores(),
                100));
    }
}
