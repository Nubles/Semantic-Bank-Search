package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class PanelViewSnapshotTest
{
    @Test
    public void factoriesDefensivelyCopyLists()
    {
        List<SemanticSearchResult> searchResults = new ArrayList<>();
        searchResults.add(result("Prayer potion(4)"));
        List<ObservedItem> indexedItems = new ArrayList<>();
        ObservedItem indexedItem = observedItem("Coins");
        indexedItems.add(indexedItem);
        List<SemanticCoverageResult> coverageResults = new ArrayList<>();
        coverageResults.add(coverage("Coins"));

        PanelViewSnapshot search = PanelViewSnapshot.search(1L, "prayer", searchResults, "Search status");
        PanelViewSnapshot indexed = PanelViewSnapshot.allIndexed(2L, indexedItems, "Index status");
        PanelViewSnapshot coverage = PanelViewSnapshot.coverageAudit(3L, coverageResults, "Coverage status");

        searchResults.clear();
        indexedItems.clear();
        coverageResults.clear();
        indexedItem.setCurrentlyVisible(false);

        assertEquals(1, search.getSearchResults().size());
        assertEquals(1, indexed.getIndexedItems().size());
        assertEquals(1, coverage.getCoverageResults().size());
        assertNotSame(indexedItem, indexed.getIndexedItems().get(0));
        assertTrue(indexed.getIndexedItems().get(0).isCurrentlyVisible());
    }

    @Test
    public void listGettersAreUnmodifiable()
    {
        PanelViewSnapshot search = PanelViewSnapshot.search(
            1L,
            "prayer",
            Collections.singletonList(result("Prayer potion(4)")),
            "");
        PanelViewSnapshot indexed = PanelViewSnapshot.allIndexed(
            2L,
            Collections.singletonList(observedItem("Coins")),
            "");
        PanelViewSnapshot coverage = PanelViewSnapshot.coverageAudit(
            3L,
            Collections.singletonList(coverage("Coins")),
            "");

        assertThrows(
            UnsupportedOperationException.class,
            () -> search.getSearchResults().add(result("Super restore(4)")));
        assertThrows(
            UnsupportedOperationException.class,
            () -> indexed.getIndexedItems().add(observedItem("Platinum token")));
        assertThrows(
            UnsupportedOperationException.class,
            () -> coverage.getCoverageResults().add(coverage("Platinum token")));
    }

    private static SemanticSearchResult result(String itemName)
    {
        return new SemanticSearchResult(
            100,
            itemName,
            1,
            StorageSourceType.BANK,
            "Bank",
            true,
            "Prayer restoration",
            "Useful item.",
            100);
    }

    private static ObservedItem observedItem(String itemName)
    {
        return new ObservedItem(100, itemName, 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    }

    private static SemanticCoverageResult coverage(String itemName)
    {
        return new SemanticCoverageResult(
            observedItem(itemName),
            Collections.emptyList(),
            Collections.emptyList(),
            0);
    }
}
