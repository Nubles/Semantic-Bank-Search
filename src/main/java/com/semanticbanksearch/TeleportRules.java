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
