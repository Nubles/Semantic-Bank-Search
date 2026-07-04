package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class SemanticQueryCase
{
    private final String scenario;
    private final String query;
    private final List<String> expectedPositives;
    private final List<String> expectedNegatives;

    private SemanticQueryCase(String scenario, String query, List<String> expectedPositives, List<String> expectedNegatives)
    {
        this.scenario = scenario;
        this.query = query;
        this.expectedPositives = immutableCopy(expectedPositives);
        this.expectedNegatives = immutableCopy(expectedNegatives);
    }

    static SemanticQueryCase of(String scenario, String query, List<String> expectedPositives, List<String> expectedNegatives)
    {
        return new SemanticQueryCase(scenario, query, expectedPositives, expectedNegatives);
    }

    String getScenario()
    {
        return scenario;
    }

    String getQuery()
    {
        return query;
    }

    List<String> getExpectedPositives()
    {
        return expectedPositives;
    }

    List<String> getExpectedNegatives()
    {
        return expectedNegatives;
    }

    private static List<String> immutableCopy(List<String> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }
}