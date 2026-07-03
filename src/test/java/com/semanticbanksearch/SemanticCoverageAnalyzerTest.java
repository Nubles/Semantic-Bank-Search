package com.semanticbanksearch;

import java.util.Arrays;
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
}
