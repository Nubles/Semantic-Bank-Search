package com.semanticbanksearch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class NumericSemanticClassifier
{
    private enum Metric
    {
        NONE,
        HEALING,
        PRAYER
    }

    private static final Pattern NUMBER_COMPARISON = Pattern.compile(
        "\\b(over|above|greater than|at least|minimum|min) (?:plus )?(\\d+)\\b");

    boolean recognizes(String query)
    {
        return metric(query) != Metric.NONE && comparison(query) != null;
    }

    boolean matches(String query, BankItemMetadata item)
    {
        if (item == null)
        {
            return false;
        }

        Comparison comparison = comparison(query);
        if (comparison == null)
        {
            return false;
        }

        switch (metric(query))
        {
            case HEALING:
                return item.isEdible() && item.getHealing() >= 0
                    && comparison.matches(item.getHealing());
            case PRAYER:
                return item.isEquipable() && comparison.matches(item.getPrayer());
            default:
                return false;
        }
    }

    private static Metric metric(String query)
    {
        String normalized = SemanticSearchEngine.normalize(query);
        if (containsAny(normalized, "food", "foods", "heal", "heals", "healing"))
        {
            return Metric.HEALING;
        }
        if (normalized.contains("prayer")
            && containsAny(normalized, "gear", "equipment", "bonus", "items"))
        {
            return Metric.PRAYER;
        }
        return Metric.NONE;
    }

    private static Comparison comparison(String query)
    {
        Matcher matcher = NUMBER_COMPARISON.matcher(SemanticSearchEngine.normalize(query));
        if (!matcher.find())
        {
            return null;
        }

        int threshold = Integer.parseInt(matcher.group(2));
        boolean inclusive = matcher.group(1).equals("at least")
            || matcher.group(1).equals("minimum")
            || matcher.group(1).equals("min");
        return new Comparison(threshold, inclusive);
    }

    private static boolean containsAny(String query, String... words)
    {
        String padded = " " + query + " ";
        for (String word : words)
        {
            if (padded.contains(" " + word + " "))
            {
                return true;
            }
        }
        return false;
    }

    private static final class Comparison
    {
        private final int threshold;
        private final boolean inclusive;

        private Comparison(int threshold, boolean inclusive)
        {
            this.threshold = threshold;
            this.inclusive = inclusive;
        }

        private boolean matches(int value)
        {
            return inclusive ? value >= threshold : value > threshold;
        }
    }
}
