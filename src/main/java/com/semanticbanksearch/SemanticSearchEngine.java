package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SemanticSearchEngine
{
    private final List<SemanticRule> rules;

    public SemanticSearchEngine(List<SemanticRule> rules)
    {
        this.rules = rules == null ? new ArrayList<>() : new ArrayList<>(rules);
    }

    public List<SemanticSearchResult> search(String query, StorageIndex index)
    {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isEmpty() || index == null)
        {
            return new ArrayList<>();
        }

        List<SemanticRule> matchingRules = matchingRules(normalizedQuery);
        List<SemanticSearchResult> results;
        if (matchingRules.isEmpty())
        {
            results = fallbackResults(normalizedQuery, index);
        }
        else
        {
            results = semanticResults(matchingRules, index);
        }

        results.sort(Comparator
            .comparingInt(SemanticSearchResult::getScore).reversed()
            .thenComparing(SemanticSearchResult::isCurrentlyVisible, Comparator.reverseOrder())
            .thenComparing(SemanticSearchResult::getItemName, String.CASE_INSENSITIVE_ORDER));
        return results;
    }

    private List<SemanticRule> matchingRules(String normalizedQuery)
    {
        List<SemanticRule> matches = new ArrayList<>();
        for (SemanticRule rule : rules)
        {
            if (rule.matchesQuery(normalizedQuery))
            {
                matches.add(rule);
            }
        }
        return matches;
    }

    private List<SemanticSearchResult> semanticResults(List<SemanticRule> matchingRules, StorageIndex index)
    {
        List<SemanticSearchResult> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ObservedItem item : index.items())
        {
            String normalizedItemName = normalize(item.getName());
            for (SemanticRule rule : matchingRules)
            {
                if (rule.matchesItem(normalizedItemName))
                {
                    addResult(results, seen, item, rule.getCategory(), rule.getReason(), rule.scoreFor(normalizedItemName));
                }
            }
        }
        return results;
    }

    private List<SemanticSearchResult> fallbackResults(String normalizedQuery, StorageIndex index)
    {
        List<SemanticSearchResult> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ObservedItem item : index.items())
        {
            if (allQueryTokensMatchItem(normalizedQuery, normalize(item.getName())))
            {
                addResult(results, seen, item, "Item name match", "Item name matches all query terms.", 0);
            }
        }
        return results;
    }

    private void addResult(
        List<SemanticSearchResult> results,
        Set<String> seen,
        ObservedItem item,
        String category,
        String reason,
        int score)
    {
        String key = item.getItemId() + "|" + item.getSourceType() + "|" + item.getSourceName() + "|" + category;
        if (!seen.add(key))
        {
            return;
        }

        results.add(new SemanticSearchResult(
            item.getItemId(),
            item.getName(),
            item.getQuantity(),
            item.getSourceType(),
            item.getSourceName(),
            item.isCurrentlyVisible(),
            category,
            reason,
            score));
    }

    private static boolean allQueryTokensMatchItem(String normalizedQuery, String normalizedItemName)
    {
        String[] itemTokens = normalizedItemName.split("\\s+");
        for (String queryToken : normalizedQuery.split("\\s+"))
        {
            if (!itemHasToken(itemTokens, queryToken))
            {
                return false;
            }
        }
        return true;
    }

    private static boolean itemHasToken(String[] itemTokens, String queryToken)
    {
        for (String itemToken : itemTokens)
        {
            if (itemToken.equals(queryToken) || itemToken.startsWith(queryToken))
            {
                return true;
            }
        }
        return false;
    }

    static String normalize(String value)
    {
        if (value == null)
        {
            return "";
        }

        String lower = value.toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); i++)
        {
            char current = lower.charAt(i);
            if (Character.isLetterOrDigit(current) || Character.isWhitespace(current))
            {
                builder.append(current);
            }
            else if (current == '\'' && isInnerWordApostrophe(lower, i))
            {
                builder.append(current);
            }
            else
            {
                builder.append(' ');
            }
        }

        return builder.toString().replaceAll("\\s+", " ").trim();
    }

    private static boolean isInnerWordApostrophe(String value, int index)
    {
        return index > 0
            && index < value.length() - 1
            && Character.isLetterOrDigit(value.charAt(index - 1))
            && Character.isLetterOrDigit(value.charAt(index + 1));
    }
}
