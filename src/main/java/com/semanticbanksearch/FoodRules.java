package com.semanticbanksearch;

import java.util.Arrays;
import java.util.List;

final class FoodRules
{
    private FoodRules()
    {
    }

    static List<SemanticRule> create()
    {
        return Arrays.asList(
            SemanticLibrary.rule(
                "Food",
                "Heals hitpoints.",
                SemanticLibrary.aliases("food", "fastest food", "best food", "healing", "heal"),
                SemanticLibrary.patterns(
                    "manta ray",
                    "shark",
                    "karambwan",
                    "anglerfish",
                    "sea turtle",
                    "monkfish",
                    "trout",
                    "salmon",
                    "lobster",
                    "swordfish",
                    "tuna potato",
                    "dark crab",
                    "summer pie",
                    "cooked karambwan",
                    "saradomin brew"),
                SemanticLibrary.scores(
                    "anglerfish", 22,
                    "manta ray", 22,
                    "dark crab", 22,
                    "tuna potato", 22,
                    "sea turtle", 21,
                    "shark", 20,
                    "cooked karambwan", 18,
                    "karambwan", 18,
                    "monkfish", 16,
                    "swordfish", 14,
                    "lobster", 12,
                    "salmon", 9,
                    "trout", 7,
                    "saradomin brew", 16,
                    "summer pie", 11),
                100));
    }
}
