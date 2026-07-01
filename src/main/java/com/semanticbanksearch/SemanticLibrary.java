package com.semanticbanksearch;

import java.util.ArrayList;
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
        List<SemanticRule> rules = new ArrayList<>();
        rules.addAll(PotionRules.create());
        rules.addAll(FoodRules.create());
        rules.addAll(TeleportRules.create());
        rules.addAll(CombatRules.create());
        rules.addAll(ProtectionRules.create());
        rules.addAll(ToolRules.create());
        rules.addAll(SkillingRules.create());
        rules.addAll(ClueRules.create());
        return rules;
    }

    static SemanticRule rule(
        String category,
        String reason,
        List<String> queryAliases,
        List<String> itemNamePatterns,
        Map<String, Integer> foodScores,
        int baseScore)
    {
        return new SemanticRule(category, reason, queryAliases, itemNamePatterns, foodScores, baseScore);
    }

    static List<String> aliases(String... values)
    {
        return Arrays.asList(values);
    }

    static List<String> patterns(String... values)
    {
        return Arrays.asList(values);
    }

    static Map<String, Integer> scores(Object... values)
    {
        Map<String, Integer> scores = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2)
        {
            scores.put((String) values[i], (Integer) values[i + 1]);
        }
        return scores;
    }
}
