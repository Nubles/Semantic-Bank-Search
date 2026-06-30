package com.semanticbanksearch;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SemanticLibrary
{
    private SemanticLibrary()
    {
    }

    public static List<SemanticRule> create()
    {
        return Arrays.asList(
            rule(
                "Prayer restoration",
                "Restores prayer points.",
                aliases("prayer", "restore prayer", "prayer restoration", "ppot"),
                patterns("prayer potion", "super restore", "sanfew serum", "blighted super restore"),
                scores(),
                100),
            rule(
                "Warm clothing",
                "Counts as warm clothing or cold protection.",
                aliases("warm clothing", "wintertodt clothing", "cold protection"),
                patterns("clue hunter", "pyromancer", "warm gloves", "fire cape", "infernal cape", "santa hat", "bomber jacket"),
                scores(),
                100),
            rule(
                "Web cutters",
                "Can cut webs or acts as a slash tool.",
                aliases("web", "webs", "cut webs", "things that cut webs", "slash"),
                patterns("knife", "scimitar", "sword", "slash", "dagger", "machete", "axe"),
                scores(),
                100),
            rule(
                "Crush weapons",
                "Can attack with crush style.",
                aliases("crush", "crush weapon", "crush weapons"),
                patterns("mace", "warhammer", "maul", "anchor", "hasta", "godsword", "bludgeon"),
                scores(),
                100),
            rule(
                "Food",
                "Heals hitpoints.",
                aliases("food", "fastest food", "best food", "healing", "heal"),
                patterns("manta ray", "shark", "karambwan", "anglerfish", "sea turtle", "monkfish", "trout", "salmon", "lobster", "swordfish", "tuna potato"),
                scores(
                    "manta ray", 22,
                    "shark", 20,
                    "sea turtle", 21,
                    "anglerfish", 22,
                    "karambwan", 18,
                    "monkfish", 16,
                    "swordfish", 14,
                    "lobster", 12,
                    "salmon", 9,
                    "trout", 7),
                100),
            rule(
                "Poison protection",
                "Protects against poison or venom.",
                aliases("poison", "venom", "poison protection", "antipoison"),
                patterns("antipoison", "anti-venom", "sanfew serum", "antidote"),
                scores(),
                100),
            rule(
                "Barrows teleports",
                "Gets you near Barrows.",
                aliases("teleport near barrows", "barrows teleport", "mortton teleport", "morytania teleport"),
                patterns("barrows teleport", "mort'ton teleport", "morytania legs", "shades of mort'ton", "drakan's medallion"),
                scores(),
                100),
            rule(
                "Clue utility",
                "Useful for clue stashes and clue steps.",
                aliases("clue stash", "clue stashes", "stash", "items used for clue stashes"),
                patterns("saw", "hammer", "nails", "plank", "clue hunter", "spade", "rope", "light source", "lantern"),
                scores(),
                100),
            rule(
                "Utility tools",
                "General skilling or traversal tool.",
                aliases("tool", "utility", "light", "rope", "spade", "lockpick"),
                patterns("spade", "rope", "lockpick", "lantern", "candle", "tinderbox", "chisel", "pestle and mortar"),
                scores(),
                100));
    }

    private static SemanticRule rule(
        String category,
        String reason,
        List<String> queryAliases,
        List<String> itemNamePatterns,
        Map<String, Integer> foodScores,
        int baseScore)
    {
        return new SemanticRule(category, reason, queryAliases, itemNamePatterns, foodScores, baseScore);
    }

    private static List<String> aliases(String... values)
    {
        return Arrays.asList(values);
    }

    private static List<String> patterns(String... values)
    {
        return Arrays.asList(values);
    }

    private static Map<String, Integer> scores(Object... values)
    {
        Map<String, Integer> scores = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2)
        {
            scores.put((String) values[i], (Integer) values[i + 1]);
        }
        return scores;
    }
}
