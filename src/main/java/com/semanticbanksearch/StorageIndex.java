package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StorageIndex
{
    private static final Comparator<ObservedItem> RETENTION_ORDER = Comparator
        .comparing(StorageIndex::isProtectedVisibleBankEntry)
        .thenComparingLong(ObservedItem::getLastSeenMillis)
        .thenComparingInt(ObservedItem::getItemId)
        .thenComparing(ObservedItem::getSourceType)
        .thenComparing(item -> item.getSourceName().toLowerCase(Locale.ROOT));

    private List<ObservedItem> items = new ArrayList<>();

    public List<ObservedItem> items()
    {
        List<ObservedItem> orderedItems = new ArrayList<>(items);
        orderedItems.sort(Comparator.comparingLong(ObservedItem::getLastSeenMillis));
        List<ObservedItem> snapshots = new ArrayList<>();
        for (ObservedItem item : orderedItems)
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

    void trimToLimits(int maximumAccountEntries, int maximumEntriesPerSource)
    {
        Map<String, List<ObservedItem>> itemsBySource = new LinkedHashMap<>();
        for (ObservedItem item : items)
        {
            String sourceKey = item.getSourceType() + "|" + item.getSourceName().toLowerCase(Locale.ROOT);
            List<ObservedItem> sourceItems = itemsBySource.get(sourceKey);
            if (sourceItems == null)
            {
                sourceItems = new ArrayList<>();
                itemsBySource.put(sourceKey, sourceItems);
            }
            sourceItems.add(item);
        }

        for (List<ObservedItem> sourceItems : itemsBySource.values())
        {
            trim(sourceItems, maximumEntriesPerSource);
        }

        trim(new ArrayList<>(items), maximumAccountEntries);
    }

    private void trim(List<ObservedItem> candidates, int maximumEntries)
    {
        int remainingEntriesToRemove = candidates.size() - maximumEntries;
        if (remainingEntriesToRemove <= 0)
        {
            return;
        }

        candidates.sort(RETENTION_ORDER);
        for (ObservedItem candidate : candidates)
        {
            if (isProtectedVisibleBankEntry(candidate))
            {
                continue;
            }

            items.remove(candidate);
            remainingEntriesToRemove--;
            if (remainingEntriesToRemove == 0)
            {
                return;
            }
        }
    }

    private static boolean isProtectedVisibleBankEntry(ObservedItem item)
    {
        return item.getSourceType() == StorageSourceType.BANK && item.isCurrentlyVisible();
    }
}
