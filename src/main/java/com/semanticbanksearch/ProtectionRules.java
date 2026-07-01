package com.semanticbanksearch;

import java.util.Arrays;
import java.util.List;

final class ProtectionRules
{
    private ProtectionRules()
    {
    }

    static List<SemanticRule> create()
    {
        return Arrays.asList(
            SemanticLibrary.rule(
                "Warm clothing",
                "Counts as warm clothing or cold protection.",
                SemanticLibrary.aliases("warm clothing", "wintertodt clothing", "cold protection"),
                SemanticLibrary.patterns("clue hunter", "pyromancer", "warm gloves", "fire cape", "infernal cape", "santa hat", "bomber jacket"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Defensive shields",
                "Defensive shields and off-hand protection items.",
                SemanticLibrary.aliases("shield", "defensive gear", "dragon protection"),
                SemanticLibrary.patterns("shield", "defender", "ward", "book of"),
                SemanticLibrary.scores(),
                100));
    }
}
