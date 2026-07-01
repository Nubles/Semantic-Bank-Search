package com.semanticbanksearch;

import java.util.Arrays;
import java.util.List;

final class SkillingRules
{
    private SkillingRules()
    {
    }

    static List<SemanticRule> create()
    {
        return Arrays.asList(
            SemanticLibrary.rule(
                "Farming tools",
                "Tools and supplies for farming runs.",
                SemanticLibrary.aliases("farming tools", "farm tools"),
                SemanticLibrary.patterns("seed dibber", "rake", "spade", "secateurs", "magic secateurs", "watering can", "plant cure", "compost"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Fishing tools",
                "Tools used to catch fish.",
                SemanticLibrary.aliases("fishing tools", "fish tools"),
                SemanticLibrary.patterns("harpoon", "fishing rod", "fly fishing rod", "small fishing net", "big fishing net", "lobster pot", "karambwan vessel"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Mining tools",
                "Tools used for mining.",
                SemanticLibrary.aliases("mining tools", "pickaxe"),
                SemanticLibrary.patterns("pickaxe"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Woodcutting tools",
                "Tools used for woodcutting.",
                SemanticLibrary.aliases("woodcutting tools", "axe"),
                SemanticLibrary.patterns("axe"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Crafting supplies",
                "Tools and supplies used for crafting.",
                SemanticLibrary.aliases("crafting supplies", "crafting tools"),
                SemanticLibrary.patterns("chisel", "needle", "thread", "mould", "glassblowing pipe"),
                SemanticLibrary.scores(),
                100));
    }
}
