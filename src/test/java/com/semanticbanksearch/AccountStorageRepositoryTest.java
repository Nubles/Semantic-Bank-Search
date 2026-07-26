package com.semanticbanksearch;

import static org.junit.Assert.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AccountStorageRepositoryTest
{
    private static final long FIXED_MILLIS = 1720000000000L;
    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test public void savesUnderAccountSpecificRuneLiteDirectory() throws IOException { AccountKey k = key(123456789L); repo().save(k, index(2434, "Prayer potion", 2)); assertTrue(Files.exists(path(k))); }

    @Test public void persistedJsonOmitsNamesAndAccountIdentity() throws IOException
    {
        long hash = 123456789L; AccountKey k = key(hash); repo().save(k, index(2434, "Prayer potion", 2));
        String json = Files.readString(path(k), StandardCharsets.UTF_8); JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        assertFalse(json.contains("Prayer potion")); assertFalse(json.contains(Long.toString(hash))); assertFalse(root.has("accountHash")); assertFalse(root.has("displayName")); assertFalse(root.getAsJsonArray("items").get(0).getAsJsonObject().has("name"));
    }

    @Test public void roundTripRehydratesNamesFromResolver() throws IOException
    {
        AccountKey k = key(123456789L); AccountStorageRepository r = repo(); r.save(k, index(2434, "Prayer potion", 2));
        AccountStorageLoadResult loaded = r.load(k, id -> "Prayer potion"); assertEquals("Prayer potion", loaded.index().items().get(0).getName()); assertFalse(loaded.quarantinedPath().isPresent());
    }

    @Test public void accountsNeverReadEachOthersIndexes() throws IOException
    {
        AccountKey a = key(123456789L), b = key(987654321L); AccountStorageRepository r = repo(); r.save(a, index(2434, "Prayer potion", 2)); r.save(b, index(4151, "Abyssal whip", 1));
        assertEquals(2434, r.load(a, id -> "Item " + id).index().items().get(0).getItemId()); assertEquals(4151, r.load(b, id -> "Item " + id).index().items().get(0).getItemId());
    }

    @Test public void saveAtomicallyReplacesExistingDocument() throws IOException
    {
        AccountKey k = key(123456789L); AccountStorageRepository r = repo(); r.save(k, index(2434, "Prayer potion", 2)); r.save(k, index(4151, "Abyssal whip", 1));
        assertEquals(4151, r.load(k, id -> "Item " + id).index().items().get(0).getItemId()); assertFalse(Files.exists(path(k).resolveSibling("index.json.tmp")));
    }

    @Test public void malformedJsonIsQuarantinedAndReturnsEmptyIndex() throws IOException { malformedIsQuarantined("{bad json"); }
    @Test public void unsupportedSchemaIsQuarantinedAndReturnsEmptyIndex() throws IOException { malformedIsQuarantined("{\"schemaVersion\":2,\"items\":[]}"); }

    @Test public void missingOrBlankCatalogueVersionIsQuarantined() throws IOException
    {
        documentIsQuarantined(key(123456789L), "{\"schemaVersion\":1,\"items\":[]}");
        documentIsQuarantined(key(987654321L), "{\"schemaVersion\":1,\"catalogueVersion\":\" \",\"items\":[]}");
    }

    private void documentIsQuarantined(AccountKey key, String json) throws IOException
    {
        Files.createDirectories(path(key).getParent());
        Files.writeString(path(key), json, StandardCharsets.UTF_8);
        AccountStorageLoadResult loaded = repo().load(key, id -> "Item " + id);
        Path corrupt = path(key).resolveSibling("index.corrupt-" + FIXED_MILLIS + ".json");
        assertTrue(loaded.index().items().isEmpty());
        assertEquals(Optional.of(corrupt), loaded.quarantinedPath());
        assertTrue(Files.exists(corrupt));
    }
    @Test public void invalidEntriesAreDroppedDuringLoad() throws IOException
    {
        AccountKey k = key(123456789L); Files.createDirectories(path(k).getParent()); Files.writeString(path(k), "{\"schemaVersion\":1,\"catalogueVersion\":\"legacy-rules-v1\",\"items\":[{\"itemId\":0},{\"itemId\":2434,\"quantity\":-2,\"sourceType\":null,\"sourceName\":null,\"lastSeenMillis\":-1}]}", StandardCharsets.UTF_8);
        ObservedItem item = repo().load(k, id -> " ").index().items().get(0); assertEquals("Item 2434", item.getName()); assertEquals(0, item.getQuantity()); assertEquals(StorageSourceType.OTHER_STORAGE, item.getSourceType()); assertEquals("", item.getSourceName()); assertEquals(0L, item.getLastSeenMillis());
    }

    @Test public void clearRemovesOnlyTheSelectedAccountFile() throws IOException
    {
        AccountKey a = key(123456789L), b = key(987654321L); AccountStorageRepository r = repo(); r.save(a, index(2434, "Prayer potion", 2)); r.save(b, index(4151, "Abyssal whip", 1)); r.clear(a); assertFalse(Files.exists(path(a))); assertTrue(Files.exists(path(b)));
    }

    private void malformedIsQuarantined(String json) throws IOException
    {
        AccountKey k = key(123456789L); Files.createDirectories(path(k).getParent()); Files.writeString(path(k), json, StandardCharsets.UTF_8); AccountStorageLoadResult loaded = repo().load(k, id -> "Item " + id); Path corrupt = path(k).resolveSibling("index.corrupt-" + FIXED_MILLIS + ".json"); assertTrue(loaded.index().items().isEmpty()); assertFalse(Files.exists(path(k))); assertEquals(Optional.of(corrupt), loaded.quarantinedPath()); assertTrue(Files.exists(corrupt));
    }
    private AccountStorageRepository repo() { return new AccountStorageRepository(temporaryFolder.getRoot().toPath(), new Gson(), Clock.fixed(Instant.ofEpochMilli(FIXED_MILLIS), ZoneOffset.UTC)); }
    private Path path(AccountKey k) { return temporaryFolder.getRoot().toPath().resolve("semantic-bank-search").resolve("accounts").resolve(k.value()).resolve("index.json"); }
    private static AccountKey key(long hash) { return AccountKey.fromAccountHash(hash).get(); }
    private static StorageIndex index(int id, String name, int quantity) { StorageIndex index = new StorageIndex(); index.record(id, name, quantity, StorageSourceType.BANK, "Bank", true, 1000L); return index; }
}
