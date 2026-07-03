package com.semanticbanksearch;

import java.util.Arrays;
import java.util.List;

final class ClueRules
{
    private ClueRules()
    {
    }

    static List<SemanticRule> create()
    {
        return Arrays.asList(
            SemanticLibrary.rule(
                "Clue utility",
                "Useful for clue stashes and clue steps.",
                SemanticLibrary.aliases("clue stash", "clue stashes", "stash", "items used for clue stashes", "clue tools", "dig clue", "emote clue", "clue supplies"),
                SemanticLibrary.patterns("saw", "hammer", "nails", "plank", "clue hunter", "spade", "rope", "sextant", "watch", "chart", "bullseye lantern", "light source", "lantern", "fairy ring", "teleport scroll", "teleport tablet", "amulet of glory", "games necklace", "ring of dueling", "stamina potion", "energy potion"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Coordinate clue tools",
                "Navigation tools for coordinate clue steps.",
                SemanticLibrary.aliases("coordinate clue", "coordinate clues", "sextant watch chart", "clue coordinates"),
                SemanticLibrary.patterns("sextant", "watch", "chart", "spade"),
                SemanticLibrary.scores(
                    "sextant", 30,
                    "watch", 30,
                    "chart", 30,
                    "spade", 15),
                110),
            SemanticLibrary.rule(
                "Dig clue",
                "Items used for dig and coordinate clue steps.",
                SemanticLibrary.aliases("dig clue items", "dig clue tools", "dig clue", "coordinate dig clue"),
                SemanticLibrary.patterns("spade", "sextant", "watch", "chart", "bullseye lantern", "lantern", "rope"),
                SemanticLibrary.scores(
                    "spade", 30,
                    "sextant", 25,
                    "watch", 25,
                    "chart", 25),
                115));
    }
}
