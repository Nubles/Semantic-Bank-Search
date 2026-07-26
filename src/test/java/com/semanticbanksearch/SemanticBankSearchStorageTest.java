package com.semanticbanksearch;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

public class SemanticBankSearchStorageTest
{
    @Test public void documentConversionOmitsObservedItemNames()
    {
        StorageIndex index = new StorageIndex(); index.record(2434, "Prayer potion(4)", 2, StorageSourceType.BANK, "Bank", true, 1000L);
        String json = new Gson().toJson(SemanticBankSearchStorage.toDocument(index)); JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        assertFalse(json.contains("Prayer potion")); assertFalse(root.getAsJsonArray("items").get(0).getAsJsonObject().has("name"));
    }

    @Test public void documentConversionRehydratesNamesFromResolver()
    {
        AccountStorageDocument document = new Gson().fromJson("{\"schemaVersion\":1,\"items\":[{\"itemId\":2434,\"quantity\":2,\"sourceType\":\"BANK\",\"sourceName\":\"Bank\",\"currentlyVisible\":true,\"lastSeenMillis\":1000}]}", AccountStorageDocument.class);
        ObservedItem item = SemanticBankSearchStorage.toIndex(document, id -> "Prayer potion(4)").items().get(0);
        assertEquals("Prayer potion(4)", item.getName()); assertEquals(2, item.getQuantity()); assertEquals(StorageSourceType.BANK, item.getSourceType());
    }

    @Test public void documentConversionNormalizesInvalidStoredValues()
    {
        AccountStorageDocument document = new Gson().fromJson("{\"schemaVersion\":1,\"items\":[{\"itemId\":2434,\"quantity\":-2,\"sourceType\":null,\"sourceName\":null,\"lastSeenMillis\":-1000},{\"itemId\":0}]}", AccountStorageDocument.class);
        ObservedItem item = SemanticBankSearchStorage.toIndex(document, id -> " ").items().get(0);
        assertEquals("Item 2434", item.getName()); assertEquals(0, item.getQuantity()); assertEquals(StorageSourceType.OTHER_STORAGE, item.getSourceType()); assertEquals("", item.getSourceName()); assertEquals(0L, item.getLastSeenMillis());
    }

    @Test public void resolverFailureUsesStableFallbackName()
    {
        AccountStorageDocument document = new Gson().fromJson("{\"schemaVersion\":1,\"items\":[{\"itemId\":4151}]}", AccountStorageDocument.class);
        assertEquals("Item 4151", SemanticBankSearchStorage.toIndex(document, id -> { throw new IllegalStateException(); }).items().get(0).getName());
    }
}