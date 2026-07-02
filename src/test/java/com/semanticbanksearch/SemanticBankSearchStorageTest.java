package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import org.junit.Test;

public class SemanticBankSearchStorageTest
{
    @Test
    public void badJsonReturnsEmptyStorageIndex()
    {
        StorageIndex loaded = SemanticBankSearchStorage.deserialize(new Gson(), "{bad json");

        assertTrue(loaded.items().isEmpty());
    }

    @Test
    public void roundTripPreservesStoredItem()
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "Prayer potion(4)", 2, StorageSourceType.BANK, "Bank", true, 1_000L);

        String json = SemanticBankSearchStorage.serialize(new Gson(), index);
        StorageIndex loaded = SemanticBankSearchStorage.deserialize(new Gson(), json);

        assertEquals("Prayer potion(4)", loaded.items().get(0).getName());
        assertEquals(2, loaded.items().get(0).getQuantity());
        assertEquals(StorageSourceType.BANK, loaded.items().get(0).getSourceType());
        assertTrue(loaded.items().get(0).isCurrentlyVisible());
    }

    @Test
    public void roundTripPreservesNonBankStorageSource()
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "Ranarr seed", 10, StorageSourceType.OTHER_STORAGE, "Seed Vault", true, 1_000L);

        String json = SemanticBankSearchStorage.serialize(new Gson(), index);
        StorageIndex loaded = SemanticBankSearchStorage.deserialize(new Gson(), json);

        assertEquals("Ranarr seed", loaded.items().get(0).getName());
        assertEquals(10, loaded.items().get(0).getQuantity());
        assertEquals(StorageSourceType.OTHER_STORAGE, loaded.items().get(0).getSourceType());
        assertEquals("Seed Vault", loaded.items().get(0).getSourceName());
        assertTrue(loaded.items().get(0).isCurrentlyVisible());
    }

    @Test
    public void malformedItemFieldsDeserializeIntoUsableStorageIndex()
    {
        String json = "{\"items\":[{\"itemId\":100,\"name\":\" Prayer potion(4) \",\"quantity\":2,"
            + "\"sourceType\":\"BANK\",\"sourceName\":null,\"currentlyVisible\":true,\"lastSeenMillis\":1000}]}";

        StorageIndex loaded = SemanticBankSearchStorage.deserialize(new Gson(), json);

        assertEquals("Prayer potion(4)", loaded.items().get(0).getName());
        assertEquals("", loaded.items().get(0).getSourceName());
        assertEquals(StorageSourceType.BANK, loaded.items().get(0).getSourceType());

        loaded.markSourceNotVisible(StorageSourceType.BANK, "");

        assertFalse(loaded.items().get(0).isCurrentlyVisible());
    }
}
