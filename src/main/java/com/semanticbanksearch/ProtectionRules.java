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
                100),
            SemanticLibrary.rule(
                "Dragonfire safeguards",
                "Items and consumables used for dragonfire protection.",
                SemanticLibrary.aliases("anti dragon", "anti-dragon", "dragonfire protection", "dragon shield", "anti dragon shield"),
                SemanticLibrary.patterns("anti-dragon shield", "dragonfire shield", "dragonfire ward", "extended antifire", "antifire potion", "super antifire"),
                SemanticLibrary.scores(
                    "anti-dragon shield", 35,
                    "dragonfire shield", 30,
                    "extended antifire", 25,
                    "super antifire", 25),
                115),
            SemanticLibrary.rule(
                "Desert protection",
                "Protects against desert heat and supports desert travel.",
                SemanticLibrary.aliases("desert protection", "desert survival", "heat protection", "desert gear", "desert utility"),
                SemanticLibrary.patterns("waterskin", "desert robes", "desert shirt", "desert boots", "desert amulet", "camulet", "circlet of water", "water tiara", "shantay pass"),
                SemanticLibrary.scores(
                    "waterskin", 30,
                    "circlet of water", 30,
                    "desert amulet", 20),
                110));
    }
}
