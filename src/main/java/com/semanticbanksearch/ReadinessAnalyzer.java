package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /** Every trip in the library, checked against what the account owns, in library order. */
    public List<ReadinessResult> analyzeAll(StorageIndex index)
    {
        List<ReadinessResult> results = new ArrayList<>();
        if (index == null)
        {
            return results;
        }
        for (ReadinessPack pack : packs)
        {
            List<ReadinessSlotResult> slotResults = new ArrayList<>();
            for (ReadinessSlot slot : pack.getSlots())
            {
                slotResults.add(new ReadinessSlotResult(slot, bestOwnedItems(slot, index)));
            }
            results.add(new ReadinessResult(pack.getName(), pack.getDescription(), slotResults, true));
        }
        return results;
    }

    /**
     * A slot that names an item ("dragon hunter crossbow") is filled by that item when it is
     * owned. Otherwise, of the purposes its query touches, only those that share a word with the
     * slot itself count, so a "Ranged weapon" slot is never filled by antifire potions just
     * because both mention dragons.
     */
    private static List<SemanticSearchResult> relevant(ReadinessSlot slot, List<SemanticSearchResult> results)
    {
        List<SemanticSearchResult> named = new ArrayList<>();
        for (SemanticSearchResult result : results)
        {
            if (SemanticSearchEngine.nameMatches(slot.getQuery(), result.getItemName()))
            {
                named.add(result);
            }
        }
        if (!named.isEmpty())
        {
            return named;
        }

        String slotWords = SemanticSearchEngine.normalize(slot.getName() + " " + slot.getQuery());
        int best = 0;
        Map<String, Integer> overlap = new HashMap<>();
        for (SemanticSearchResult result : results)
        {
            int shared = overlap.computeIfAbsent(result.getCategory(), category -> sharedWords(slotWords, category));
            best = Math.max(best, shared);
        }
        if (best == 0)
        {
            return results;
        }
        List<SemanticSearchResult> kept = new ArrayList<>();
        for (SemanticSearchResult result : results)
        {
            if (overlap.get(result.getCategory()) == best)
            {
                kept.add(result);
            }
        }
        return kept;
    }

    private static int sharedWords(String normalizedSlot, String category)
    {
        int shared = 0;
        String[] slotTokens = normalizedSlot.split(" ");
        for (String word : SemanticSearchEngine.normalize(category).split(" "))
        {
            if (word.length() < 4)
            {
                continue;
            }
            for (String token : slotTokens)
            {
                if (token.equals(word))
                {
                    shared += 2;
                    break;
                }
                if (token.length() >= 4 && (token.startsWith(word) || word.startsWith(token)))
                {
                    shared++;
                    break;
                }
            }
        }
        return shared;
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
        List<SemanticSearchResult> results = relevant(slot, searchEngine.search(slot.getQuery(), index));
        if (results.size() <= MAX_ITEMS_PER_SLOT)
        {
            return results;
        }
        return new ArrayList<>(results.subList(0, MAX_ITEMS_PER_SLOT));
    }
}
