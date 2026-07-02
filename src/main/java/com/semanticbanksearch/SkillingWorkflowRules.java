package com.semanticbanksearch;

import java.util.Arrays;
import java.util.List;

final class SkillingWorkflowRules
{
    private SkillingWorkflowRules()
    {
    }

    static List<SemanticRule> create()
    {
        return Arrays.asList(
            SemanticLibrary.rule(
                "Herb run supplies",
                "Useful for herb runs: farming tools, compost, teleports, and movement support.",
                SemanticLibrary.aliases("herb run", "herb run supplies", "herb run prep", "farming run"),
                SemanticLibrary.patterns("seed dibber", "spade", "rake", "secateurs", "magic secateurs", "compost", "supercompost", "ultracompost", "herb seed", "ranarr seed", "snapdragon seed", "torstol seed", "skills necklace", "explorer's ring", "stamina potion"),
                SemanticLibrary.scores(
                    "magic secateurs", 30,
                    "seed dibber", 25,
                    "ultracompost", 25,
                    "skills necklace", 20,
                    "stamina potion", 15),
                110),
            SemanticLibrary.rule(
                "Tree run supplies",
                "Useful for tree farming runs: saplings, tools, payments, teleports, and boosts.",
                SemanticLibrary.aliases("tree run", "tree run supplies", "fruit tree run", "tree farming run"),
                SemanticLibrary.patterns("sapling", "spade", "basket", "sack", "plant pot", "watering can", "garden pie", "botanical pie", "skills necklace", "ring of dueling", "spirit tree"),
                SemanticLibrary.scores(
                    "sapling", 30,
                    "spade", 20,
                    "garden pie", 15,
                    "botanical pie", 15),
                105),
            SemanticLibrary.rule(
                "Birdhouse run supplies",
                "Useful for birdhouse runs: houses, clockworks, logs, tools, seeds, and Fossil Island travel.",
                SemanticLibrary.aliases("birdhouse run", "bird house run", "birdhouse supplies", "bird houses"),
                SemanticLibrary.patterns("bird house", "birdhouse", "clockwork", "log", "logs", "hammer", "chisel", "seed", "hop seed", "digsite pendant", "fossil island", "mushroom meadow"),
                SemanticLibrary.scores(
                    "bird house", 35,
                    "birdhouse", 35,
                    "clockwork", 30,
                    "digsite pendant", 25,
                    "logs", 20),
                115),
            SemanticLibrary.rule(
                "Seaweed run supplies",
                "Useful for seaweed runs: spores, compost, diving gear, travel, and run energy.",
                SemanticLibrary.aliases("seaweed run", "seaweed supplies", "seaweed spores", "giant seaweed run"),
                SemanticLibrary.patterns("seaweed spore", "giant seaweed", "ultracompost", "compost", "diving apparatus", "fishbowl helmet", "digsite pendant", "fossil island", "stamina potion"),
                SemanticLibrary.scores(
                    "seaweed spore", 35,
                    "giant seaweed", 25,
                    "ultracompost", 20,
                    "digsite pendant", 20),
                105),
            SemanticLibrary.rule(
                "Farm contract supplies",
                "Useful for farming contracts: farming tools, compost, seeds, saplings, boosts, and teleports.",
                SemanticLibrary.aliases("farm contract", "farm contracts", "farming contract", "farming contracts"),
                SemanticLibrary.patterns("seed dibber", "spade", "rake", "secateurs", "compost", "ultracompost", "seed", "sapling", "garden pie", "botanical pie", "skills necklace"),
                SemanticLibrary.scores(
                    "seed dibber", 25,
                    "ultracompost", 20,
                    "botanical pie", 20,
                    "garden pie", 20),
                100),
            SemanticLibrary.rule(
                "Glass crafting prep",
                "Useful for glass crafting: sand, seaweed, soda ash, molten glass, tools, and runes.",
                SemanticLibrary.aliases("glass crafting", "glass crafting prep", "make glass", "crafting glass", "blow glass"),
                SemanticLibrary.patterns("bucket of sand", "seaweed", "giant seaweed", "soda ash", "molten glass", "glassblowing pipe", "astral rune", "cosmic rune"),
                SemanticLibrary.scores(
                    "bucket of sand", 30,
                    "giant seaweed", 30,
                    "glassblowing pipe", 25,
                    "soda ash", 20),
                105),
            SemanticLibrary.rule(
                "Fletching prep",
                "Useful for Fletching prep: logs, cutting tools, bow strings, feathers, shafts, tips, and bolt parts.",
                SemanticLibrary.aliases("fletching prep", "fletching supplies", "make bows", "make arrows", "make darts"),
                SemanticLibrary.patterns("log", "logs", "knife", "bow string", "feather", "headless arrow", "arrow shaft", "arrowtips", "dart tip", "dart tips", "unfinished broad bolts"),
                SemanticLibrary.scores(
                    "knife", 25,
                    "bow string", 20,
                    "feather", 20,
                    "logs", 15),
                105),
            SemanticLibrary.rule(
                "Smithing prep",
                "Useful for Smithing prep: ores, coal, bars, storage, hammers, and utility gloves.",
                SemanticLibrary.aliases("smithing prep", "smithing supplies", "make bars", "smith bars"),
                SemanticLibrary.patterns("copper ore", "tin ore", "iron ore", "silver ore", "gold ore", "mithril ore", "adamantite ore", "runite ore", "coal", "bronze bar", "iron bar", "steel bar", "mithril bar", "adamantite bar", "runite bar", "coal bag", "hammer", "ice gloves", "goldsmith gauntlets"),
                SemanticLibrary.scores(
                    "coal bag", 30,
                    "hammer", 20,
                    "goldsmith gauntlets", 20,
                    "ice gloves", 20),
                105),
            SemanticLibrary.rule(
                "Cooking prep",
                "Useful for Cooking prep: raw food, cooking boosts, pies, jugs, and flour.",
                SemanticLibrary.aliases("cooking prep", "cooking supplies", "cook food", "cooking training"),
                SemanticLibrary.patterns("raw shark", "raw monkfish", "raw karambwan", "raw lobster", "raw swordfish", "raw tuna", "raw salmon", "raw trout", "cooking gauntlets", "chef's delight", "garden pie", "botanical pie", "jug", "pot of flour"),
                SemanticLibrary.scores(
                    "cooking gauntlets", 30,
                    "raw shark", 20,
                    "raw karambwan", 20,
                    "chef's delight", 15),
                100),
            SemanticLibrary.rule(
                "Fishing method supplies",
                "Useful for Fishing methods: rods, harpoons, bait, feathers, nets, pots, and vessels.",
                SemanticLibrary.aliases("fishing method", "fishing methods", "fishing supplies", "fish training"),
                SemanticLibrary.patterns("harpoon", "fishing rod", "fly fishing rod", "barbarian rod", "bait", "feather", "small fishing net", "big fishing net", "lobster pot", "karambwan vessel"),
                SemanticLibrary.scores(
                    "dragon harpoon", 30,
                    "harpoon", 25,
                    "fly fishing rod", 20,
                    "karambwan vessel", 20),
                100),
            SemanticLibrary.rule(
                "Mining method supplies",
                "Useful for Mining methods: pickaxes, outfit pieces, mining boosts, and desert support.",
                SemanticLibrary.aliases("mining method", "mining methods", "mining supplies", "mining training"),
                SemanticLibrary.patterns("pickaxe", "prospector", "celestial ring", "varrock armour", "dwarven stout", "waterskin", "desert amulet"),
                SemanticLibrary.scores(
                    "crystal pickaxe", 35,
                    "dragon pickaxe", 30,
                    "rune pickaxe", 25,
                    "prospector", 20,
                    "celestial ring", 20),
                100));
    }
}
