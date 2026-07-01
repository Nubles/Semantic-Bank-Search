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
                SemanticLibrary.aliases("clue stash", "clue stashes", "stash", "items used for clue stashes", "clue tools"),
                SemanticLibrary.patterns("saw", "hammer", "nails", "plank", "clue hunter", "spade", "rope", "sextant", "watch", "chart", "bullseye lantern", "light source", "lantern"),
                SemanticLibrary.scores(),
                100));
    }
}
