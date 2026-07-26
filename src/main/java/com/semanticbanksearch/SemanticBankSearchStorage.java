package com.semanticbanksearch;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.function.IntFunction;

class SemanticBankSearchStorage
{
    private SemanticBankSearchStorage() { }

    static AccountStorageDocument toDocument(StorageIndex index)
    {
        AccountStorageDocument document = new AccountStorageDocument();
        document.schemaVersion = AccountStorageRepository.SCHEMA_VERSION;
        document.catalogueVersion = AccountStorageRepository.CURRENT_CATALOGUE_VERSION;
        if (index != null)
        {
            for (ObservedItem item : index.items())
            {
                document.items.add(StoredObservedItem.from(item));
            }
        }
        return document;
    }

    static StorageIndex toIndex(AccountStorageDocument document, IntFunction<String> itemNameResolver)
    {
        StorageIndex index = new StorageIndex();
        if (document == null || !document.isSupported()) return index;
        for (StoredObservedItem item : document.items)
        {
            if (item == null || item.itemId <= 0) continue;
            String name = resolveName(item.itemId, itemNameResolver);
            index.record(item.itemId, name, Math.max(0, item.quantity), ObservedItem.normalizeSourceType(item.sourceType), item.sourceName == null ? "" : item.sourceName, item.currentlyVisible, Math.max(0L, item.lastSeenMillis));
        }
        return index;
    }

    private static String resolveName(int itemId, IntFunction<String> itemNameResolver)
    {
        try
        {
            String name = itemNameResolver == null ? null : itemNameResolver.apply(itemId);
            if (name != null && !name.trim().isEmpty()) return name.trim();
        }
        catch (RuntimeException ex)
        {
            // The persisted observation remains usable while item metadata is unavailable.
        }
        return "Item " + itemId;
    }

    static String serialize(Gson gson, StorageIndex index)
    {
        return gson == null ? "" : gson.toJson(index == null ? new StorageIndex() : index);
    }

    static StorageIndex deserialize(Gson gson, String json)
    {
        if (gson == null || json == null || json.trim().isEmpty()) return new StorageIndex();
        try { return gson.fromJson(json, StorageIndex.class); }
        catch (JsonSyntaxException | IllegalStateException ex) { return new StorageIndex(); }
    }
}