package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class StorageIndex
{
    private List<ObservedItem> items = new ArrayList<>();

    public List<ObservedItem> items()
    {
        items.sort(Comparator.comparingLong(ObservedItem::getLastSeenMillis));
        List<ObservedItem> snapshots = new ArrayList<>();
        for (ObservedItem item : items)
        {
            snapshots.add(new ObservedItem(item));
        }
        return snapshots;
    }

    public void record(
        int itemId,
        String name,
        int quantity,
        StorageSourceType sourceType,
        String sourceName,
        boolean currentlyVisible,
        long lastSeenMillis)
    {
        if (itemId <= 0 || name == null || name.trim().isEmpty())
        {
            return;
        }

        ObservedItem observed = new ObservedItem(
            itemId,
            name,
            quantity,
            sourceType,
            sourceName,
            currentlyVisible,
            lastSeenMillis);

        for (ObservedItem existing : items)
        {
            if (existing.key().equals(observed.key()))
            {
                existing.updateFrom(observed);
                return;
            }
        }

        items.add(observed);
    }

    public void replaceVisibleSourceItems(
        StorageSourceType sourceType,
        String sourceName,
        List<ObservedItem> visibleItems)
    {
        markSourceNotVisible(sourceType, sourceName);
        if (visibleItems == null)
        {
            return;
        }

        for (ObservedItem item : visibleItems)
        {
            if (item == null)
            {
                continue;
            }

            record(
                item.getItemId(),
                item.getName(),
                item.getQuantity(),
                sourceType,
                sourceName,
                true,
                item.getLastSeenMillis());
        }
    }

    public void markSourceNotVisible(StorageSourceType sourceType, String sourceName)
    {
        StorageSourceType normalizedSourceType = ObservedItem.normalizeSourceType(sourceType);
        String normalizedSourceName = sourceName == null ? "" : sourceName.trim();

        for (ObservedItem item : items)
        {
            if (item.getSourceType() == normalizedSourceType && item.getSourceName().equals(normalizedSourceName))
            {
                item.setCurrentlyVisible(false);
            }
        }
    }

    public void trimToMaximumEntries(int maximumEntries)
    {
        if (maximumEntries <= 0)
        {
            items.clear();
            return;
        }

        items.sort(Comparator.comparingLong(ObservedItem::getLastSeenMillis));
        while (items.size() > maximumEntries)
        {
            Iterator<ObservedItem> iterator = items.iterator();
            iterator.next();
            iterator.remove();
        }
    }
}
