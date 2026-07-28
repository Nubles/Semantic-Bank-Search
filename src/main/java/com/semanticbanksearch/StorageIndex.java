package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StorageIndex
{
    private static final Comparator<ObservedItem> RETENTION_ORDER = Comparator
        .comparing(StorageIndex::isProtectedVisibleBankEntry)
        .thenComparingLong(ObservedItem::getLastSeenMillis)
        .thenComparingInt(ObservedItem::getItemId)
        .thenComparing(ObservedItem::getSourceType)
        .thenComparing(item -> item.getSourceName().toLowerCase(Locale.ROOT));

    private Map<String, ObservedItem> itemsByKey = new LinkedHashMap<>();

    public List<ObservedItem> items()
    {
        List<ObservedItem> orderedItems = new ArrayList<>(itemsByKey.values());
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

        String key = observed.key();
        ObservedItem existing = itemsByKey.get(key);
        if (existing != null)
        {
            existing.updateFrom(observed);
            return;
        }

        itemsByKey.put(key, observed);
    }

    void mergeRecovered(StorageIndex recovered)
    {
        if (recovered == null)
        {
            return;
        }

        Map<String, ObservedItem> sessionItems = itemsByKey;
        Map<String, ObservedItem> mergedItems = new LinkedHashMap<>();
        for (ObservedItem recoveredItem : recovered.itemsByKey.values())
        {
            ObservedItem recoveredCopy = new ObservedItem(recoveredItem);
            mergedItems.put(recoveredCopy.key(), recoveredCopy);
        }

        for (ObservedItem sessionItem : sessionItems.values())
        {
            String key = sessionItem.key();
            ObservedItem matchingItem = mergedItems.get(key);
            if (matchingItem == null)
            {
                mergedItems.put(key, new ObservedItem(sessionItem));
            }
            else if (sessionItem.getLastSeenMillis() >= matchingItem.getLastSeenMillis())
            {
                matchingItem.updateFrom(sessionItem);
            }
        }
        itemsByKey = mergedItems;
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

        for (ObservedItem item : itemsByKey.values())
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
        for (ObservedItem item : itemsByKey.values())
        {
            String sourceKey = item.getSourceType() + "|" + item.getSourceName().toLowerCase(Locale.ROOT);
            itemsBySource.computeIfAbsent(sourceKey, ignored -> new ArrayList<>()).add(item);
        }

        Set<String> removalKeys = new HashSet<>();
        for (List<ObservedItem> sourceItems : itemsBySource.values())
        {
            markForRemoval(sourceItems, maximumEntriesPerSource, removalKeys);
        }

        List<ObservedItem> accountCandidates = new ArrayList<>();
        for (Map.Entry<String, ObservedItem> entry : itemsByKey.entrySet())
        {
            if (!removalKeys.contains(entry.getKey()))
            {
                accountCandidates.add(entry.getValue());
            }
        }
        markForRemoval(accountCandidates, maximumAccountEntries, removalKeys);

        if (!removalKeys.isEmpty())
        {
            Map<String, ObservedItem> retainedItems = new LinkedHashMap<>();
            for (Map.Entry<String, ObservedItem> entry : itemsByKey.entrySet())
            {
                if (!removalKeys.contains(entry.getKey()))
                {
                    retainedItems.put(entry.getKey(), entry.getValue());
                }
            }
            itemsByKey = retainedItems;
        }
    }

    private static void markForRemoval(
        List<ObservedItem> candidates,
        int maximumEntries,
        Set<String> removalKeys)
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

            removalKeys.add(candidate.key());
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
