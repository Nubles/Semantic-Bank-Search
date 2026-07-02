package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.List;

final class ObservedStorageSnapshot
{
    private final ObservedStorageSource source;
    private final List<ObservedItem> items;

    ObservedStorageSnapshot(ObservedStorageSource source, List<ObservedItem> items)
    {
        this.source = source;
        this.items = copyItems(items);
    }

    ObservedStorageSource getSource()
    {
        return source;
    }

    List<ObservedItem> getItems()
    {
        return copyItems(items);
    }

    private static List<ObservedItem> copyItems(List<ObservedItem> sourceItems)
    {
        List<ObservedItem> snapshots = new ArrayList<>();
        if (sourceItems == null)
        {
            return snapshots;
        }

        for (ObservedItem item : sourceItems)
        {
            snapshots.add(new ObservedItem(item));
        }
        return snapshots;
    }
}
