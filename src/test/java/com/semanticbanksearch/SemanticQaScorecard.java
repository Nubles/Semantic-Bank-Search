package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class SemanticQaScorecard
{
    private final List<SemanticRule> rules;

    SemanticQaScorecard(List<SemanticRule> rules)
    {
        this.rules = rules == null ? Collections.emptyList() : new ArrayList<>(rules);
    }

    Result evaluate(List<SemanticQueryCase> cases)
    {
        List<SemanticQueryCase> safeCases = cases == null ? Collections.emptyList() : cases;
        List<CaseResult> caseResults = new ArrayList<>();
        int foundExpectedPositives = 0;
        int totalExpectedPositives = 0;
        int avoidedExpectedNegatives = 0;
        int totalExpectedNegatives = 0;

        for (SemanticQueryCase queryCase : safeCases)
        {
            CaseResult caseResult = evaluateCase(queryCase);
            caseResults.add(caseResult);
            foundExpectedPositives += queryCase.getExpectedPositives().size() - caseResult.getMissingPositives().size();
            totalExpectedPositives += queryCase.getExpectedPositives().size();
            avoidedExpectedNegatives += queryCase.getExpectedNegatives().size() - caseResult.getFalsePositives().size();
            totalExpectedNegatives += queryCase.getExpectedNegatives().size();
        }

        return new Result(
            caseResults,
            foundExpectedPositives,
            totalExpectedPositives,
            avoidedExpectedNegatives,
            totalExpectedNegatives);
    }

    private CaseResult evaluateCase(SemanticQueryCase queryCase)
    {
        StorageIndex index = new StorageIndex();
        int itemId = 10_000;
        for (String itemName : queryCase.getExpectedPositives())
        {
            index.record(itemId++, itemName, 1, StorageSourceType.BANK, "QA fixture", true, 1_000L);
        }
        for (String itemName : queryCase.getExpectedNegatives())
        {
            index.record(itemId++, itemName, 1, StorageSourceType.BANK, "QA fixture", true, 1_000L);
        }

        Set<String> resultNames = names(new SemanticSearchEngine(rules).search(queryCase.getQuery(), index));
        List<String> missingPositives = new ArrayList<>();
        List<String> falsePositives = new ArrayList<>();

        for (String expectedPositive : queryCase.getExpectedPositives())
        {
            if (!resultNames.contains(expectedPositive))
            {
                missingPositives.add(expectedPositive);
            }
        }
        for (String expectedNegative : queryCase.getExpectedNegatives())
        {
            if (resultNames.contains(expectedNegative))
            {
                falsePositives.add(expectedNegative);
            }
        }

        return new CaseResult(queryCase, missingPositives, falsePositives);
    }

    private static Set<String> names(List<SemanticSearchResult> results)
    {
        Set<String> names = new LinkedHashSet<>();
        for (SemanticSearchResult result : results)
        {
            names.add(result.getItemName());
        }
        return names;
    }

    static final class Result
    {
        private final List<CaseResult> caseResults;
        private final int foundExpectedPositives;
        private final int totalExpectedPositives;
        private final int avoidedExpectedNegatives;
        private final int totalExpectedNegatives;

        private Result(
            List<CaseResult> caseResults,
            int foundExpectedPositives,
            int totalExpectedPositives,
            int avoidedExpectedNegatives,
            int totalExpectedNegatives)
        {
            this.caseResults = Collections.unmodifiableList(new ArrayList<>(caseResults));
            this.foundExpectedPositives = foundExpectedPositives;
            this.totalExpectedPositives = totalExpectedPositives;
            this.avoidedExpectedNegatives = avoidedExpectedNegatives;
            this.totalExpectedNegatives = totalExpectedNegatives;
        }

        int getTotalCases()
        {
            return caseResults.size();
        }

        int getPassedCases()
        {
            int passed = 0;
            for (CaseResult caseResult : caseResults)
            {
                if (caseResult.isPassing())
                {
                    passed++;
                }
            }
            return passed;
        }

        int getFoundExpectedPositives()
        {
            return foundExpectedPositives;
        }

        int getTotalExpectedPositives()
        {
            return totalExpectedPositives;
        }

        int getAvoidedExpectedNegatives()
        {
            return avoidedExpectedNegatives;
        }

        int getTotalExpectedNegatives()
        {
            return totalExpectedNegatives;
        }

        List<CaseResult> getFailures()
        {
            List<CaseResult> failures = new ArrayList<>();
            for (CaseResult caseResult : caseResults)
            {
                if (!caseResult.isPassing())
                {
                    failures.add(caseResult);
                }
            }
            return Collections.unmodifiableList(failures);
        }

        boolean isPassing()
        {
            return getPassedCases() == getTotalCases();
        }

        String toMarkdown()
        {
            StringBuilder builder = new StringBuilder();
            builder.append("# Semantic QA Scorecard\n\n");
            builder.append("- Scenarios: ").append(getPassedCases()).append("/").append(getTotalCases()).append(" passing\n");
            builder.append("- Expected positives found: ").append(foundExpectedPositives).append("/").append(totalExpectedPositives).append("\n");
            builder.append("- Expected negatives avoided: ").append(avoidedExpectedNegatives).append("/").append(totalExpectedNegatives).append("\n");

            List<CaseResult> failures = getFailures();
            if (failures.isEmpty())
            {
                builder.append("- Failures: 0\n");
                return builder.toString();
            }

            builder.append("\n## Failures\n");
            for (CaseResult failure : failures)
            {
                builder.append("\n### ").append(failure.getQueryCase().getQuery()).append("\n");
                builder.append("- Scenario: ").append(failure.getQueryCase().getScenario()).append("\n");
                if (!failure.getMissingPositives().isEmpty())
                {
                    builder.append("- Missing positives: ").append(String.join(", ", failure.getMissingPositives())).append("\n");
                }
                if (!failure.getFalsePositives().isEmpty())
                {
                    builder.append("- False positives: ").append(String.join(", ", failure.getFalsePositives())).append("\n");
                }
            }
            return builder.toString();
        }
    }

    static final class CaseResult
    {
        private final SemanticQueryCase queryCase;
        private final List<String> missingPositives;
        private final List<String> falsePositives;

        private CaseResult(SemanticQueryCase queryCase, List<String> missingPositives, List<String> falsePositives)
        {
            this.queryCase = queryCase;
            this.missingPositives = Collections.unmodifiableList(new ArrayList<>(missingPositives));
            this.falsePositives = Collections.unmodifiableList(new ArrayList<>(falsePositives));
        }

        SemanticQueryCase getQueryCase()
        {
            return queryCase;
        }

        List<String> getMissingPositives()
        {
            return missingPositives;
        }

        List<String> getFalsePositives()
        {
            return falsePositives;
        }

        boolean isPassing()
        {
            return missingPositives.isEmpty() && falsePositives.isEmpty();
        }
    }
}