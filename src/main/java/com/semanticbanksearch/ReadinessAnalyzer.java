package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReadinessAnalyzer
{
    private static final int MAX_ITEMS_PER_SLOT = 5;

    private final SemanticSearchEngine searchEngine;
    private final List<ReadinessPack> packs;

    public ReadinessAnalyzer(List<SemanticRule> rules, List<ReadinessPack> packs)
    {
        this.searchEngine = new SemanticSearchEngine(rules);
        this.packs = packs == null ? Collections.emptyList() : new ArrayList<>(packs);
    }

    public ReadinessResult analyze(String query, StorageIndex index)
    {
        String normalizedQuery = SemanticSearchEngine.normalize(query);
        if (normalizedQuery.isEmpty() || index == null)
        {
            return ReadinessResult.unmatched(query);
        }

        ReadinessPack pack = matchingPack(normalizedQuery);
        if (pack == null)
        {
            return ReadinessResult.unmatched(query);
        }

        List<ReadinessSlotResult> slotResults = new ArrayList<>();
        for (ReadinessSlot slot : pack.getSlots())
        {
            slotResults.add(new ReadinessSlotResult(slot, bestOwnedItems(slot, index)));
        }
        return new ReadinessResult(pack.getName(), pack.getDescription(), slotResults, true);
    }

    private ReadinessPack matchingPack(String normalizedQuery)
    {
        for (ReadinessPack pack : packs)
        {
            if (pack.matches(normalizedQuery))
            {
                return pack;
            }
        }
        return null;
    }

    private List<SemanticSearchResult> bestOwnedItems(ReadinessSlot slot, StorageIndex index)
    {
        List<SemanticSearchResult> results = searchEngine.search(slot.getQuery(), index);
        if (results.size() <= MAX_ITEMS_PER_SLOT)
        {
            return results;
        }
        return new ArrayList<>(results.subList(0, MAX_ITEMS_PER_SLOT));
    }
}
