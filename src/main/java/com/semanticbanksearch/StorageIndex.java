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
        return new ArrayList<>(items);
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
