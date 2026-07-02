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
                "Useful in dark caves and clue steps.",
                SemanticLibrary.aliases("light source", "light", "dark cave"),
                SemanticLibrary.patterns("lantern", "candle", "torch", "bruma torch", "bullseye lantern"),
                SemanticLibrary.scores(),
                100),
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
                100));
    }
}
