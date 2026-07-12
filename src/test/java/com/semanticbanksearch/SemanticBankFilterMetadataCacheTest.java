package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class SemanticBankFilterMetadataCacheTest
{
    @Test
    public void rankingResolvesEachOwnedItemOncePerQuery()
    {
        Map<Integer, BankItemMetadata> items = new HashMap<>();
        items.put(1, food("Shark", 20));
        items.put(2, food("Manta ray", 22));
        AtomicInteger resolutions = new AtomicInteger();
        SemanticBankFilter filter = new SemanticBankFilter(
            new SemanticSearchEngine(SemanticLibrary.create()),
            id ->
            {
                resolutions.incrementAndGet();
                return items.get(id);
            },
            () -> Arrays.asList(1, 2));

        filter.decision("best food", 1);
        filter.decision("best food", 2);

        assertEquals(2, resolutions.get());
    }

    private static BankItemMetadata food(String name, int healing)
    {
        return new BankItemMetadata(
            name, false, false, true, false, -1,
            0, 0, 0, 0, 0, 0, 0, 0f, 0, healing);
    }
}
