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
                "Barrows teleports",
                "Gets you near Barrows.",
                SemanticLibrary.aliases("teleport near barrows", "barrows teleport", "mortton teleport", "morytania teleport"),
                SemanticLibrary.patterns("barrows teleport", "mort'ton teleport", "morytania legs", "shades of mort'ton", "drakan's medallion"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Desert travel",
                "Gets you into or around the desert.",
                SemanticLibrary.aliases("desert travel", "desert teleport", "shantay", "alkharid", "pollnivneach", "nardah"),
                SemanticLibrary.patterns("desert amulet", "pharaoh's sceptre", "camulet", "slayer ring", "ring of dueling", "shantay pass", "waterskin"),
                SemanticLibrary.scores(
                    "desert amulet", 30,
                    "pharaoh's sceptre", 25,
                    "shantay pass", 20),
                110),
            SemanticLibrary.rule(
                "Morytania travel",
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
                SemanticLibrary.aliases("wilderness escape", "wildy escape", "escape teleport", "teleport out"),
                SemanticLibrary.patterns("royal seed pod", "amulet of glory", "ring of wealth", "burning amulet", "games necklace", "one-click teleport", "teleport crystal"),
                SemanticLibrary.scores(
                    "royal seed pod", 35,
                    "amulet of glory", 25,
                    "ring of wealth", 20),
                110),
            SemanticLibrary.rule(
                "Teleport jewellery",
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
                "Teleport tablets",
                "Single-use teleport tablets.",
                SemanticLibrary.aliases("teleport tablet", "tele tabs", "house tab", "spell tablet"),
                SemanticLibrary.patterns("teleport", "tablet", "redirected house tablet"),
                SemanticLibrary.scores(),
                100));
    }
}
