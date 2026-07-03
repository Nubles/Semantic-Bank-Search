package com.semanticbanksearch;

import java.util.Arrays;
import java.util.List;

final class ToolRules
{
    private ToolRules()
    {
    }

    static List<SemanticRule> create()
    {
        return Arrays.asList(
            SemanticLibrary.rule(
                "Web cutters",
                "Can cut webs or acts as a slash tool.",
                SemanticLibrary.aliases("web", "webs", "cut webs", "things that cut webs", "slash"),
                SemanticLibrary.patterns("knife", "scimitar", "sword", "slash", "dagger", "machete", "axe"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Light sources",
                "Useful in dark caves, clue steps, and travel routes that require light.",
                SemanticLibrary.aliases("light source", "light sources", "dark cave", "cave light", "lantern", "torch"),
                SemanticLibrary.patterns("bullseye lantern", "lantern", "candle lantern", "oil lantern", "torch", "bruma torch", "tinderbox"),
                SemanticLibrary.scores(
                    "bullseye lantern", 30,
                    "bruma torch", 25,
                    "lantern", 20),
                110),
            SemanticLibrary.rule(
                "Ghostspeak",
                "Items used to speak with ghosts or support undead quest interactions.",
                SemanticLibrary.aliases("ghostspeak", "speak to ghosts", "ghost speak", "ghost amulet", "undead quest utility"),
                SemanticLibrary.patterns("ghostspeak amulet", "cramulet", "morytania legs", "ectophial"),
                SemanticLibrary.scores(
                    "ghostspeak amulet", 35,
                    "cramulet", 30,
                    "ectophial", 15),
                115),
            SemanticLibrary.rule(
                "Quest utility",
                "Common tools used by quests and achievement diaries.",
                SemanticLibrary.aliases("quest tools", "quest utility", "diary tools", "achievement diary tools"),
                SemanticLibrary.patterns("ghostspeak amulet", "rope", "spade", "pickaxe", "axe", "knife", "bucket", "empty pot", "pestle and mortar", "vial", "needle", "thread", "hammer", "chisel", "light source", "lantern", "tinderbox"),
                SemanticLibrary.scores(
                    "ghostspeak amulet", 30,
                    "rope", 20,
                    "spade", 20,
                    "pickaxe", 15),
                105),
            SemanticLibrary.rule(
                "General tools",
                "General skilling or traversal tool.",
                SemanticLibrary.aliases("tool", "rope", "spade", "lockpick"),
                SemanticLibrary.patterns("spade", "rope", "lockpick", "tinderbox", "chisel", "pestle and mortar", "hammer", "saw", "knife"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Traversal tools",
                "Common tools used to unlock paths, cross obstacles, or access quest areas.",
                SemanticLibrary.aliases("access tools", "quest access tools", "dungeon tools", "lockpick tools", "machete tools"),
                SemanticLibrary.patterns("rope", "lockpick", "machete", "knife", "hammer", "saw", "pickaxe"),
                SemanticLibrary.scores(
                    "rope", 25,
                    "lockpick", 25,
                    "machete", 20,
                    "pickaxe", 15),
                105));
    }
}
