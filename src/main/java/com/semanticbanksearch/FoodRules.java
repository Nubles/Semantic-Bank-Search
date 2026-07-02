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
                    "saradomin brew",
                    "blighted manta ray",
                    "blighted anglerfish",
                    "pineapple pizza",
                    "meat pie",
                    "apple pie",
                    "redberry pie"),
                SemanticLibrary.scores(
                    "saradomin brew", 40,
                    "cooked karambwan", 35,
                    "karambwan", 35,
                    "anglerfish", 30,
                    "manta ray", 28,
                    "dark crab", 28,
                    "tuna potato", 26,
                    "sea turtle", 24,
                    "shark", 20,
                    "monkfish", 16,
                    "swordfish", 14,
                    "lobster", 12,
                    "summer pie", 11,
                    "salmon", 9,
                    "trout", 7),
                100));
    }
}
