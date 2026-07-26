package com.semanticbanksearch;

class StoredObservedItem
{
    int itemId;
    int quantity;
    StorageSourceType sourceType;
    String sourceName;
    boolean currentlyVisible;
    long lastSeenMillis;

    StoredObservedItem()
    {
    }

    static StoredObservedItem from(ObservedItem item)
    {
        StoredObservedItem stored = new StoredObservedItem();
        stored.itemId = item.getItemId();
        stored.quantity = Math.max(0, item.getQuantity());
        stored.sourceType = ObservedItem.normalizeSourceType(item.getSourceType());
        stored.sourceName = item.getSourceName() == null ? "" : item.getSourceName();
        stored.currentlyVisible = item.isCurrentlyVisible();
        stored.lastSeenMillis = Math.max(0L, item.getLastSeenMillis());
        return stored;
    }
}