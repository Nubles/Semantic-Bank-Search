package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SemanticRule
{
    private static final Set<String> GENERIC_CATEGORY_WORDS = Set.of(
        "weapon",
        "weapons",
        "tool",
        "tools",
        "utility",
        "supplies",
        "protection",
        "restoration");

    private final String category;
    private final String reason;
    private final List<String> queryAliases;
    private final List<String> itemNamePatterns;
    private final Map<String, Integer> foodScores;
    private final int baseScore;

    public SemanticRule(
        String category,
        String reason,
        List<String> queryAliases,
        List<String> itemNamePatterns,
        Map<String, Integer> foodScores,
        int baseScore)
    {
        this.category = normalizeString(category);
        this.reason = normalizeString(reason);
        this.queryAliases = normalizeList(queryAliases);
        this.itemNamePatterns = normalizeList(itemNamePatterns);
        this.foodScores = normalizeScores(foodScores);
        this.baseScore = baseScore;
    }

    public String getCategory()
    {
        return category;
    }

    public String getReason()
    {
        return reason;
    }

    public boolean matchesQuery(String normalizedQuery)
    {
        if (normalizedQuery == null || normalizedQuery.trim().isEmpty())
        {
            return false;
        }

        for (String alias : queryAliases)
        {
            if (containsPhrase(normalizedQuery, alias))
            {
                return true;
            }
        }

        for (String categoryWord : normalizedCategoryWords())
        {
            if (containsWord(normalizedQuery, categoryWord))
            {
                return true;
            }
        }

        return false;
    }

    public boolean matchesItem(String normalizedItemName)
    {
        if (normalizedItemName == null || normalizedItemName.trim().isEmpty())
        {
            return false;
        }

        for (String pattern : itemNamePatterns)
        {
            if (normalizedItemName.contains(pattern))
            {
                return true;
            }
        }

        return false;
    }

    public int scoreFor(String normalizedItemName)
    {
        int score = baseScore;
        for (Map.Entry<String, Integer> entry : foodScores.entrySet())
        {
            if (normalizedItemName.contains(entry.getKey()))
            {
                score = Math.max(score, baseScore + entry.getValue());
            }
        }
        return score;
    }

    private List<String> normalizedCategoryWords()
    {
        String normalizedCategory = category.toLowerCase(Locale.ROOT);
        List<String> words = new ArrayList<>();
        for (String word : normalizedCategory.split("\\s+"))
        {
            if (word.length() > 2 && !GENERIC_CATEGORY_WORDS.contains(word))
            {
                words.add(word);
            }
        }
        return words;
    }

    private static boolean containsPhrase(String normalizedText, String normalizedPhrase)
    {
        if (normalizedPhrase == null || normalizedPhrase.isEmpty())
        {
            return false;
        }
        return (" " + normalizedText + " ").contains(" " + normalizedPhrase + " ");
    }

    private static boolean containsWord(String normalizedText, String normalizedWord)
    {
        for (String token : normalizedText.split("\\s+"))
        {
            if (token.equals(normalizedWord) || token.startsWith(normalizedWord) || normalizedWord.startsWith(token))
            {
                return true;
            }
        }
        return false;
    }

    private static List<String> normalizeList(List<String> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }

        List<String> normalized = new ArrayList<>();
        for (String value : values)
        {
            String normalizedValue = SemanticSearchEngine.normalize(value);
            if (!normalizedValue.isEmpty())
            {
                normalized.add(normalizedValue);
            }
        }
        return Collections.unmodifiableList(normalized);
    }

    private static Map<String, Integer> normalizeScores(Map<String, Integer> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyMap();
        }

        Map<String, Integer> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : values.entrySet())
        {
            String key = SemanticSearchEngine.normalize(entry.getKey());
            if (!key.isEmpty() && entry.getValue() != null)
            {
                normalized.put(key, entry.getValue());
            }
        }
        return Collections.unmodifiableMap(normalized);
    }

    private static String normalizeString(String value)
    {
        return value == null ? "" : value.trim();
    }
}
