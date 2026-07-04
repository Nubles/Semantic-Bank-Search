package com.semanticbanksearch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class CuratedQueryPackTest
{
    @Test
    public void curatedPlayerQueriesFindExpectedItemsWithoutBleed()
    {
        for (SemanticQueryCase queryCase : SemanticQueryCases.all())
        {
            assertQueryPack(queryCase);
        }
    }

    private static void assertQueryPack(SemanticQueryCase queryCase)
    {
        StorageIndex index = new StorageIndex();
        int itemId = 10_000;
        for (String itemName : queryCase.getExpectedPositives())
        {
            index.record(itemId++, itemName, 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        }
        for (String itemName : queryCase.getExpectedNegatives())
        {
            index.record(itemId++, itemName, 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        }

        String resultNames = names(new SemanticSearchEngine(SemanticLibrary.create()).search(queryCase.getQuery(), index));
        for (String expectedItem : queryCase.getExpectedPositives())
        {
            assertTrue(queryCase.getQuery() + " should include " + expectedItem + " in:\n" + resultNames, resultNames.contains(expectedItem));
        }
        for (String unexpectedItem : queryCase.getExpectedNegatives())
        {
            assertFalse(queryCase.getQuery() + " should exclude " + unexpectedItem + " from:\n" + resultNames, resultNames.contains(unexpectedItem));
        }
    }

    private static String names(List<SemanticSearchResult> results)
    {
        StringBuilder builder = new StringBuilder();
        for (SemanticSearchResult result : results)
        {
            builder.append(result.getItemName()).append("\n");
        }
        return builder.toString();
    }
}