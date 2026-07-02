package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.List;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

final class ObservedStorageScanner
{
    interface ContainerProvider
    {
        ItemContainer getItemContainer(InventoryID inventoryId);
    }

    interface WidgetVisibilityProvider
    {
        boolean isVisible(int packedComponentId);
    }

    interface ItemCanonicalizer
    {
        int canonicalize(int itemId);
    }

    interface ItemNameResolver
    {
        String itemName(int canonicalItemId);
    }

    private final ContainerProvider containerProvider;
    private final WidgetVisibilityProvider widgetVisibilityProvider;
    private final ItemCanonicalizer itemCanonicalizer;
    private final ItemNameResolver itemNameResolver;

    ObservedStorageScanner(
        ContainerProvider containerProvider,
        WidgetVisibilityProvider widgetVisibilityProvider,
        ItemCanonicalizer itemCanonicalizer,
        ItemNameResolver itemNameResolver)
    {
        this.containerProvider = containerProvider;
        this.widgetVisibilityProvider = widgetVisibilityProvider;
        this.itemCanonicalizer = itemCanonicalizer;
        this.itemNameResolver = itemNameResolver;
    }

    List<ObservedStorageSnapshot> scan(List<ObservedStorageSource> sources, long now)
    {
        List<ObservedStorageSnapshot> snapshots = new ArrayList<>();
        if (sources == null)
        {
            return snapshots;
        }

        for (ObservedStorageSource source : sources)
        {
            if (source == null || !widgetVisibilityProvider.isVisible(source.getVisibleComponentId()))
            {
                continue;
            }

            ItemContainer container = containerProvider.getItemContainer(source.getInventoryId());
            if (container == null)
            {
                continue;
            }

            snapshots.add(new ObservedStorageSnapshot(source, scanItems(container, source, now)));
        }
        return snapshots;
    }

    private List<ObservedItem> scanItems(ItemContainer container, ObservedStorageSource source, long now)
    {
        List<ObservedItem> observedItems = new ArrayList<>();
        Item[] items = container.getItems();
        if (items == null)
        {
            return observedItems;
        }

        for (Item item : items)
        {
            ObservedItem observedItem = observeItem(item, source, now);
            if (observedItem != null)
            {
                observedItems.add(observedItem);
            }
        }
        return observedItems;
    }

    private ObservedItem observeItem(Item item, ObservedStorageSource source, long now)
    {
        if (item == null || item.getId() <= 0 || item.getQuantity() <= 0)
        {
            return null;
        }

        int canonicalItemId = itemCanonicalizer.canonicalize(item.getId());
        if (canonicalItemId <= 0)
        {
            return null;
        }

        String name = itemNameResolver.itemName(canonicalItemId);
        if (name == null || name.trim().isEmpty() || "null".equalsIgnoreCase(name.trim()))
        {
            return null;
        }

        return new ObservedItem(
            canonicalItemId,
            name,
            item.getQuantity(),
            source.getSourceType(),
            source.getSourceName(),
            true,
            now);
    }
}
