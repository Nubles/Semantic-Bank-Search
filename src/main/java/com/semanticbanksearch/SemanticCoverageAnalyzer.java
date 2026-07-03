package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SemanticCoverageAnalyzer
{
    private final List<SemanticRule> rules;

    public SemanticCoverageAnalyzer(List<SemanticRule> rules)
    {
        this.rules = rules == null ? new ArrayList<>() : new ArrayList<>(rules);
    }

    public List<SemanticCoverageResult> analyze(List<ObservedItem> items)
    {
        List<SemanticCoverageResult> results = new ArrayList<>();
        if (items == null)
        {
            return results;
        }

        for (ObservedItem item : items)
        {
            if (item != null)
            {
                results.add(analyzeItem(item));
            }
        }

        results.sort(Comparator
            .comparing(SemanticCoverageResult::isCovered)
            .thenComparing(SemanticCoverageResult::getItemName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(SemanticCoverageResult::getSourceName, String.CASE_INSENSITIVE_ORDER));
        return results;
    }

    private SemanticCoverageResult analyzeItem(ObservedItem item)
    {
        Set<String> categories = new LinkedHashSet<>();
        Set<String> reasons = new LinkedHashSet<>();
        String normalizedName = SemanticSearchEngine.normalize(item.getName());
        int bestScore = 0;

        for (SemanticRule rule : rules)
        {
            if (rule.matchesObservedItem(item))
            {
                categories.add(rule.getCategory());
                reasons.add(rule.getReason());
                bestScore = Math.max(bestScore, rule.scoreFor(normalizedName));
            }
        }

        return new SemanticCoverageResult(
            item,
            new ArrayList<>(categories),
            new ArrayList<>(reasons),
            bestScore);
    }
}