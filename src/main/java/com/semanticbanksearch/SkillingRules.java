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
                100),
            SemanticLibrary.rule(
                "Skilling outfits",
                "Experience and utility outfits used while skilling.",
                SemanticLibrary.aliases("skilling outfit", "skilling outfits", "skill outfit", "xp outfit", "graceful"),
                SemanticLibrary.patterns("graceful", "prospector", "angler hat", "angler top", "angler waders", "angler boots", "lumberjack", "farmer", "rogue", "pyromancer", "carpenter", "smiths", "goldsmith", "varrock armour", "desert amulet"),
                SemanticLibrary.scores(
                    "graceful", 30,
                    "prospector", 25,
                    "angler", 25,
                    "lumberjack", 25),
                105),
            SemanticLibrary.rule(
                "Herblore supplies",
                "Tools, herbs, and secondaries used to make potions.",
                SemanticLibrary.aliases("herblore supplies", "herblore tools", "make potions"),
                SemanticLibrary.patterns("vial", "vial of water", "pestle and mortar", "grimy", "clean", "unf potion", "secondary", "snape grass", "limpwurt", "red spiders' eggs"),
                SemanticLibrary.scores(
                    "vial of water", 20,
                    "pestle and mortar", 20),
                100),
            SemanticLibrary.rule(
                "Construction supplies",
                "Tools and materials used for Construction.",
                SemanticLibrary.aliases("construction supplies", "construction tools", "house building"),
                SemanticLibrary.patterns("saw", "hammer", "plank", "oak plank", "teak plank", "mahogany plank", "nails", "bolt of cloth", "house teleport"),
                SemanticLibrary.scores(
                    "saw", 25,
                    "hammer", 25,
                    "plank", 15),
                100),
            SemanticLibrary.rule(
                "Hunter supplies",
                "Tools used for Hunter traps and catches.",
                SemanticLibrary.aliases("hunter supplies", "hunter tools", "trapping"),
                SemanticLibrary.patterns("box trap", "bird snare", "butterfly net", "impling jar", "noose wand", "teasing stick", "magic box"),
                SemanticLibrary.scores(
                    "box trap", 25,
                    "bird snare", 25),
                100));
    }
}
