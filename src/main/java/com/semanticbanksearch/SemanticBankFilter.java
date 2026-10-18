package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.Supplier;

class SemanticBankFilter
{
    static final String FORCE_PREFIX = "sem ";

    private final SemanticSearchEngine engine;
    private final IntFunction<BankItemMetadata> itemMetadataProvider;
    private final Supplier<List<Integer>> ownedItemIdsProvider;
    private final EquipmentSemanticClassifier equipmentClassifier = new EquipmentSemanticClassifier();
    private final ConsumableSemanticClassifier consumableClassifier = new ConsumableSemanticClassifier();
    private final NumericSemanticClassifier numericClassifier = new NumericSemanticClassifier();
    private final RelativeRankingClassifier rankingClassifier = new RelativeRankingClassifier();
    private final SemanticBankQueryParser queryParser = new SemanticBankQueryParser();
    private final Map<Integer, Boolean> decisions = new HashMap<>();
    private final Map<Integer, BankItemMetadata> metadataCache = new HashMap<>();
    private String cachedInput = "";
    private String cachedQuery = "";
    private List<String> cachedClauses = Collections.emptyList();
    private List<BankItemMetadata> cachedOwnedItems = Collections.emptyList();
    private boolean cachedSemanticSearch;

    SemanticBankFilter(SemanticSearchEngine engine, IntFunction<BankItemMetadata> itemMetadataProvider)
    {
        this(engine, itemMetadataProvider, Collections::emptyList);
    }

    SemanticBankFilter(
        SemanticSearchEngine engine,
        IntFunction<BankItemMetadata> itemMetadataProvider,
        Supplier<List<Integer>> ownedItemIdsProvider)
    {
        this.engine = engine;
        this.itemMetadataProvider = itemMetadataProvider;
        this.ownedItemIdsProvider = ownedItemIdsProvider == null ? Collections::emptyList : ownedItemIdsProvider;
    }

    Boolean decision(String input, int itemId)
    {
        String normalizedInput = SemanticSearchEngine.normalize(input);
        if (!normalizedInput.equals(cachedInput))
        {
            prepare(normalizedInput);
        }

        if (!cachedSemanticSearch)
        {
            return null;
        }
        if (itemId == -1)
        {
            return null;
        }

        return decisions.computeIfAbsent(itemId, id ->
        {
            BankItemMetadata item = metadataFor(id);
            if (!cachedClauses.isEmpty())
            {
                for (String clause : cachedClauses)
                {
                    if (!matchesClause(clause, item))
                    {
                        return false;
                    }
                }
                return true;
            }
            return matchesClause(cachedQuery, item);
        });
    }

    String semanticQuery(String input)
    {
        String normalizedInput = SemanticSearchEngine.normalize(input);
        if (normalizedInput.startsWith(FORCE_PREFIX))
        {
            return normalizedInput.substring(FORCE_PREFIX.length()).trim();
        }
        if (!queryParser.parse(normalizedInput).isEmpty())
        {
            return normalizedInput;
        }
        return equipmentClassifier.recognizes(normalizedInput)
            || consumableClassifier.recognizes(normalizedInput)
            || numericClassifier.recognizes(normalizedInput)
            || rankingClassifier.recognizes(normalizedInput)
            || engine.hasSemanticMatch(normalizedInput)
            ? normalizedInput
            : "";
    }

    /** Whether the bank would read this input as a purpose rather than a name. */
    boolean isSemantic(String input)
    {
        String normalizedInput = SemanticSearchEngine.normalize(input);
        if (!normalizedInput.equals(cachedInput))
        {
            prepare(normalizedInput);
        }
        return cachedSemanticSearch;
    }

    /** Whether the game's own item data answers this query, rather than the purpose library. */
    boolean isKindQuery(String input)
    {
        String query = semanticQuery(input);
        return equipmentClassifier.recognizes(query)
            || consumableClassifier.recognizes(query)
            || numericClassifier.recognizes(query)
            || rankingClassifier.recognizes(query);
    }

    /** The separate constraints of a compound query, or empty for a single purpose. */
    List<String> clauses(String input)
    {
        return queryParser.parse(semanticQuery(input));
    }

    private void prepare(String normalizedInput)
    {
        metadataCache.clear();
        cachedInput = normalizedInput;
        cachedQuery = semanticQuery(normalizedInput);
        cachedClauses = queryParser.parse(cachedQuery);
        cachedOwnedItems = rankingClassifier.recognizes(cachedQuery) ? loadOwnedItems() : Collections.emptyList();
        cachedSemanticSearch = !cachedQuery.isEmpty()
            && (normalizedInput.startsWith(FORCE_PREFIX)
                || !cachedClauses.isEmpty()
                || equipmentClassifier.recognizes(cachedQuery)
                || consumableClassifier.recognizes(cachedQuery)
                || numericClassifier.recognizes(cachedQuery)
                || rankingClassifier.recognizes(cachedQuery)
                || engine.hasSemanticMatch(cachedQuery));
        decisions.clear();
    }

    private boolean matchesClause(String clause, BankItemMetadata item)
    {
        if (numericClassifier.recognizes(clause))
        {
            return numericClassifier.matches(clause, item);
        }
        if (rankingClassifier.recognizes(clause))
        {
            return rankingClassifier.matches(clause, item, cachedOwnedItems);
        }

        // A kind of item the game itself knows ("prayer gear", "food") is answered from the
        // item's own stats and actions alone, so a prayer potion is never "prayer gear".
        if (equipmentClassifier.recognizes(clause))
        {
            return equipmentClassifier.matches(clause, item);
        }
        if (consumableClassifier.recognizes(clause))
        {
            return consumableClassifier.matches(clause, item);
        }
        return engine.matchesPurpose(clause, item == null ? "" : item.getName());
    }

    private List<BankItemMetadata> loadOwnedItems()
    {
        List<BankItemMetadata> items = new ArrayList<>();
        for (Integer itemId : ownedItemIdsProvider.get())
        {
            if (itemId != null && itemId > 0)
            {
                BankItemMetadata item = metadataFor(itemId);
                if (item != null)
                {
                    items.add(item);
                }
            }
        }
        return items;
    }

    private BankItemMetadata metadataFor(int itemId)
    {
        return metadataCache.computeIfAbsent(itemId, itemMetadataProvider::apply);
    }
}
