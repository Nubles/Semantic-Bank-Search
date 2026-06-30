package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
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
}
