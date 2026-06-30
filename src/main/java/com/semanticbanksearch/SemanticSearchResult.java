package com.semanticbanksearch;

public class SemanticSearchResult
{
    private final int itemId;
    private final String itemName;
    private final int quantity;
    private final StorageSourceType sourceType;
    private final String sourceName;
    private final boolean currentlyVisible;
    private final String category;
    private final String reason;
    private final int score;

    public SemanticSearchResult(
        int itemId,
        String itemName,
        int quantity,
        StorageSourceType sourceType,
        String sourceName,
        boolean currentlyVisible,
        String category,
        String reason,
        int score)
    {
        this.itemId = itemId;
        this.itemName = normalizeString(itemName);
        this.quantity = quantity;
        this.sourceType = sourceType == null ? StorageSourceType.OTHER_STORAGE : sourceType;
        this.sourceName = normalizeString(sourceName);
        this.currentlyVisible = currentlyVisible;
        this.category = normalizeString(category);
        this.reason = normalizeString(reason);
        this.score = score;
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

    public String getCategory()
    {
        return category;
    }

    public String getReason()
    {
        return reason;
    }

    public int getScore()
    {
        return score;
    }

    public boolean isHighlightable()
    {
        return currentlyVisible && sourceType == StorageSourceType.BANK;
    }

    private static String normalizeString(String value)
    {
        return value == null ? "" : value.trim();
    }
}
