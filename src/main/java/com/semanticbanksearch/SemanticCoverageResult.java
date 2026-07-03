package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SemanticCoverageResult
{
    private final int itemId;
    private final String itemName;
    private final int quantity;
    private final StorageSourceType sourceType;
    private final String sourceName;
    private final boolean currentlyVisible;
    private final List<String> categories;
    private final List<String> reasons;
    private final int bestScore;

    public SemanticCoverageResult(
        ObservedItem item,
        List<String> categories,
        List<String> reasons,
        int bestScore)
    {
        this.itemId = item.getItemId();
        this.itemName = item.getName();
        this.quantity = item.getQuantity();
        this.sourceType = item.getSourceType();
        this.sourceName = item.getSourceName();
        this.currentlyVisible = item.isCurrentlyVisible();
        this.categories = immutableCopy(categories);
        this.reasons = immutableCopy(reasons);
        this.bestScore = bestScore;
    }

    public int getItemId()
    {
        return itemId;
    }

    public String getItemName()
    {
        return itemName;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public StorageSourceType getSourceType()
    {
        return sourceType;
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public boolean isCurrentlyVisible()
    {
        return currentlyVisible;
    }

    public List<String> getCategories()
    {
        return categories;
    }

    public List<String> getReasons()
    {
        return reasons;
    }

    public int getBestScore()
    {
        return bestScore;
    }

    public boolean isCovered()
    {
        return !categories.isEmpty();
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
