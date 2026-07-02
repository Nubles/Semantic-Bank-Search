package com.semanticbanksearch;

import java.util.Locale;

public class ObservedItem
{
    private int itemId;
    private String name;
    private int quantity;
    private StorageSourceType sourceType;
    private String sourceName;
    private boolean currentlyVisible;
    private long lastSeenMillis;

    public ObservedItem()
    {
        this(0, "", 0, StorageSourceType.OTHER_STORAGE, "", false, 0L);
    }

    ObservedItem(ObservedItem source)
    {
        this(
            source == null ? 0 : source.itemId,
            source == null ? "" : source.name,
            source == null ? 0 : source.quantity,
            source == null ? StorageSourceType.OTHER_STORAGE : source.sourceType,
            source == null ? "" : source.sourceName,
            source != null && source.currentlyVisible,
            source == null ? 0L : source.lastSeenMillis);
    }

    ObservedItem(
        int itemId,
        String name,
        int quantity,
        StorageSourceType sourceType,
        String sourceName,
        boolean currentlyVisible,
        long lastSeenMillis)
    {
        this.itemId = itemId;
        this.name = normalizeString(name);
        this.quantity = Math.max(0, quantity);
        this.sourceType = normalizeSourceType(sourceType);
        this.sourceName = normalizeString(sourceName);
        this.currentlyVisible = currentlyVisible;
        this.lastSeenMillis = Math.max(0L, lastSeenMillis);
    }

    public int getItemId()
    {
        return itemId;
    }

    public String getName()
    {
        return name;
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

    public long getLastSeenMillis()
    {
        return lastSeenMillis;
    }

    String key()
    {
        return itemId + "|" + sourceType + "|" + sourceName.toLowerCase(Locale.ROOT);
    }

    void updateFrom(ObservedItem replacement)
    {
        name = replacement.name;
        quantity = replacement.quantity;
        sourceType = replacement.sourceType;
        sourceName = replacement.sourceName;
        currentlyVisible = replacement.currentlyVisible;
        lastSeenMillis = replacement.lastSeenMillis;
    }

    void setCurrentlyVisible(boolean currentlyVisible)
    {
        this.currentlyVisible = currentlyVisible;
    }

    static StorageSourceType normalizeSourceType(StorageSourceType sourceType)
    {
        return sourceType == null ? StorageSourceType.OTHER_STORAGE : sourceType;
    }

    private static String normalizeString(String value)
    {
        return value == null ? "" : value.trim();
    }
}
