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
    private final List<String> mechanicalTags;
    private final ItemAwarenessStatus awarenessStatus;
    private final int bestScore;

    public SemanticCoverageResult(
        ObservedItem item,
        List<String> categories,
        List<String> reasons,
        int bestScore)
    {
        this(item, categories, reasons, Collections.emptyList(), null, bestScore);
    }

    public SemanticCoverageResult(
        ObservedItem item,
        List<String> categories,
        List<String> reasons,
        List<String> mechanicalTags,
        ItemAwarenessStatus awarenessStatus,
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
        this.mechanicalTags = immutableCopy(mechanicalTags);
        this.awarenessStatus = awarenessStatus == null
            ? inferAwarenessStatus(this.categories, this.mechanicalTags)
            : awarenessStatus;
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

    public List<String> getMechanicalTags()
    {
        return mechanicalTags;
    }

    public ItemAwarenessStatus getAwarenessStatus()
    {
        return awarenessStatus;
    }

    public int getBestScore()
    {
        return bestScore;
    }

    public boolean isCovered()
    {
        return !categories.isEmpty();
    }

    private static ItemAwarenessStatus inferAwarenessStatus(List<String> categories, List<String> mechanicalTags)
    {
        if (categories != null && !categories.isEmpty())
        {
            return ItemAwarenessStatus.SEMANTIC_COVERED;
        }
        if (mechanicalTags != null && !mechanicalTags.isEmpty())
        {
            return ItemAwarenessStatus.MECHANICALLY_TAGGED;
        }
        return ItemAwarenessStatus.KNOWN_UNCLASSIFIED;
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
