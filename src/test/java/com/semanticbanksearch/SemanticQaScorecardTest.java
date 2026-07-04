package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class SemanticQaScorecardTest
{
    @Test
    public void scorecardCountsPassingAndFailingExpectations()
    {
        SemanticQaScorecard.Result result = new SemanticQaScorecard(SemanticLibrary.create()).evaluate(Arrays.asList(
            SemanticQueryCase.of(
                "Boss prep",
                "vorkath trip",
                Arrays.asList("Anti-dragon shield", "Dragon hunter crossbow"),
                Arrays.asList("Arclight")),
            SemanticQueryCase.of(
                "Intentional failure",
                "vorkath trip",
                Arrays.asList("Missing example item"),
                Arrays.asList("Anti-dragon shield"))));

        assertEquals(2, result.getTotalCases());
        assertEquals(1, result.getPassedCases());
        assertEquals(2, result.getFoundExpectedPositives());
        assertEquals(3, result.getTotalExpectedPositives());
        assertEquals(1, result.getAvoidedExpectedNegatives());
        assertEquals(2, result.getTotalExpectedNegatives());
        assertEquals(1, result.getFailures().size());
        assertFalse(result.isPassing());
        assertTrue(result.toMarkdown().contains("Missing example item"));
        assertTrue(result.toMarkdown().contains("Anti-dragon shield"));
    }

    @Test
    public void curatedScorecardCurrentlyPassesAllSharedCases()
    {
        SemanticQaScorecard.Result result = new SemanticQaScorecard(SemanticLibrary.create()).evaluate(SemanticQueryCases.all());

        assertTrue(result.toMarkdown(), result.isPassing());
        assertEquals(result.getTotalCases(), result.getPassedCases());
        assertEquals(result.getTotalExpectedPositives(), result.getFoundExpectedPositives());
        assertEquals(result.getTotalExpectedNegatives(), result.getAvoidedExpectedNegatives());
    }

    @Test
    public void emptyScorecardHasZeroTotalsAndPasses()
    {
        SemanticQaScorecard.Result result = new SemanticQaScorecard(SemanticLibrary.create()).evaluate(Collections.emptyList());

        assertTrue(result.isPassing());
        assertEquals(0, result.getTotalCases());
        assertEquals(0, result.getTotalExpectedPositives());
        assertEquals(0, result.getTotalExpectedNegatives());
    }
}