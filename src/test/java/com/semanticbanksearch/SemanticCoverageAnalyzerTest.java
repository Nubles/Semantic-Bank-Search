package com.semanticbanksearch;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SemanticCoverageAnalyzerTest
{
    @Test
    public void coverageResultReportsCoveredState()
    {
        ObservedItem item = new ObservedItem(1001, "Barrows teleport", 3, StorageSourceType.BANK, "Bank", true, 1_000L);

        SemanticCoverageResult covered = new SemanticCoverageResult(
            item,
            Arrays.asList("Barrows travel"),
            Arrays.asList("Teleports near Barrows."),
            100);
        SemanticCoverageResult uncovered = new SemanticCoverageResult(
            item,
            Arrays.asList(),
            Arrays.asList(),
            0);

        assertTrue(covered.isCovered());
        assertFalse(uncovered.isCovered());
        assertEquals("Barrows teleport", covered.getItemName());
        assertEquals(3, covered.getQuantity());
    }

    @Test
    public void analyzerMarksCoveredAndUncoveredItems()
    {
        StorageIndex index = new StorageIndex();
        index.record(1001, "Barrows teleport", 3, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(1002, "Uncut sapphire", 12, StorageSourceType.BANK, "Bank", true, 1_001L);

        SemanticCoverageAnalyzer analyzer = new SemanticCoverageAnalyzer(SemanticLibrary.create());
        List<SemanticCoverageResult> results = analyzer.analyze(index.items());

        assertTrue(find(results, "Barrows teleport").isCovered());
        assertFalse(find(results, "Uncut sapphire").isCovered());
    }

    @Test
    public void analyzerKeepsDuplicateItemsFromDifferentSources()
    {
        StorageIndex index = new StorageIndex();
        index.record(2001, "Spade", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(2001, "Spade", 1, StorageSourceType.POH_STORAGE, "Costume room", false, 1_001L);

        List<SemanticCoverageResult> results = new SemanticCoverageAnalyzer(SemanticLibrary.create()).analyze(index.items());

        assertEquals(2, results.size());
        assertTrue(results.get(0).isCovered());
        assertTrue(results.get(1).isCovered());
    }

    @Test
    public void analyzerSortsUncoveredItemsFirst()
    {
        StorageIndex index = new StorageIndex();
        index.record(3001, "Barrows teleport", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(3002, "Uncut sapphire", 1, StorageSourceType.BANK, "Bank", true, 1_001L);

        List<SemanticCoverageResult> results = new SemanticCoverageAnalyzer(SemanticLibrary.create()).analyze(index.items());

        assertEquals("Uncut sapphire", results.get(0).getItemName());
        assertFalse(results.get(0).isCovered());
        assertEquals("Barrows teleport", results.get(1).getItemName());
        assertTrue(results.get(1).isCovered());
    }


    @Test
    public void analyzerDistinguishesSemanticMechanicalKnownAndUnknownItems()
    {
        List<ObservedItem> items = Arrays.asList(
            new ObservedItem(4001, "Barrows teleport", 1, StorageSourceType.BANK, "Bank", true, 1_000L),
            new ObservedItem(4002, "Uncut sapphire", 12, StorageSourceType.BANK, "Bank", true, 1_001L),
            new ObservedItem(4003, "Coins", 995, StorageSourceType.BANK, "Bank", true, 1_002L),
            new ObservedItem(4004, "", 1, StorageSourceType.BANK, "Bank", true, 1_003L));

        List<SemanticCoverageResult> results = new SemanticCoverageAnalyzer(SemanticLibrary.create()).analyze(items);

        SemanticCoverageResult semantic = find(results, "Barrows teleport");
        assertEquals(ItemAwarenessStatus.SEMANTIC_COVERED, semantic.getAwarenessStatus());
        assertTrue(semantic.getMechanicalTags().isEmpty());

        SemanticCoverageResult mechanical = find(results, "Uncut sapphire");
        assertEquals(ItemAwarenessStatus.MECHANICALLY_TAGGED, mechanical.getAwarenessStatus());
        assertEquals(Arrays.asList("Gem"), mechanical.getMechanicalTags());
        assertFalse(mechanical.isCovered());

        SemanticCoverageResult unclassified = find(results, "Coins");
        assertEquals(ItemAwarenessStatus.KNOWN_UNCLASSIFIED, unclassified.getAwarenessStatus());
        assertTrue(unclassified.getMechanicalTags().isEmpty());
        assertFalse(unclassified.isCovered());

        SemanticCoverageResult unknown = find(results, "");
        assertEquals(ItemAwarenessStatus.UNKNOWN_OBSERVED, unknown.getAwarenessStatus());
        assertTrue(unknown.getMechanicalTags().isEmpty());
    }
    private static SemanticCoverageResult find(List<SemanticCoverageResult> results, String itemName)
    {
        for (SemanticCoverageResult result : results)
        {
            if (result.getItemName().equals(itemName))
            {
                return result;
            }
        }
        throw new AssertionError("Missing " + itemName);
    }
}
