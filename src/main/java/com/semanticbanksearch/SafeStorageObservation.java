package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class SafeStorageObservation
{
    private SafeStorageObservation()
    {
    }

    static boolean replaceVisibleSourceItemsIfChanged(
        StorageIndex index,
        ObservedStorageSource source,
        List<ObservedItem> visibleItems)
    {
        boolean changed = !visibleSourceState(index, source).equals(visibleSourceState(source, visibleItems));
        index.replaceVisibleSourceItems(source.getSourceType(), source.getSourceName(), visibleItems);
        return changed;
    }

    static boolean markAllSafeStorageSourcesNotVisible(StorageIndex index)
    {
        if (index == null)
        {
            return false;
        }

        boolean changed = false;
        for (ObservedStorageSource source : ObservedStorageSource.safeDirectInventorySources())
        {
            changed |= !visibleSourceState(index, source).isEmpty();
            index.markSourceNotVisible(source.getSourceType(), source.getSourceName());
        }
        return changed;
    }

    private static List<String> visibleSourceState(StorageIndex index, ObservedStorageSource source)
    {
        List<String> state = new ArrayList<>();
        if (index == null)
        {
            return state;
        }

        for (ObservedItem item : index.items())
        {
            if (item.isCurrentlyVisible()
                && item.getSourceType() == source.getSourceType()
                && item.getSourceName().equals(source.getSourceName()))
            {
                state.add(itemState(item));
            }
        }
        Collections.sort(state);
        return state;
    }

    private static List<String> visibleSourceState(ObservedStorageSource source, List<ObservedItem> visibleItems)
    {
        List<String> state = new ArrayList<>();
        if (visibleItems == null)
        {
            return state;
        }

        for (ObservedItem item : visibleItems)
        {
            if (item != null)
            {
                state.add(item.getItemId()
                    + "|" + item.getName()
                    + "|" + item.getQuantity()
                    + "|" + source.getSourceType()
                    + "|" + source.getSourceName());
            }
        }
        Collections.sort(state);
        return state;
    }

    private static String itemState(ObservedItem item)
    {
        return item.getItemId()
            + "|" + item.getName()
            + "|" + item.getQuantity()
            + "|" + item.getSourceType()
            + "|" + item.getSourceName();
    }
}
