package com.semanticbanksearch;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.util.Objects;
import java.util.function.IntFunction;

final class AccountStorageRepository
{
    static final int SCHEMA_VERSION = 1;
    static final String CURRENT_CATALOGUE_VERSION = "legacy-rules-v1";
    private final Path runeLiteDirectory;
    private final Gson gson;
    private final Clock clock;

    AccountStorageRepository(Path runeLiteDirectory, Gson gson, Clock clock)
    {
        this.runeLiteDirectory = Objects.requireNonNull(runeLiteDirectory);
        this.gson = Objects.requireNonNull(gson);
        this.clock = Objects.requireNonNull(clock);
    }

    AccountStorageLoadResult load(AccountKey accountKey, IntFunction<String> itemNameResolver) throws IOException
    {
        Path indexPath = indexPath(accountKey);
        if (!Files.exists(indexPath))
        {
            return new AccountStorageLoadResult(new StorageIndex(), null);
        }
        try
        {
            JsonElement root = JsonParser.parseString(Files.readString(indexPath, StandardCharsets.UTF_8));
            if (!root.isJsonObject())
            {
                return quarantined(indexPath);
            }
            AccountStorageDocument document = gson.fromJson(root, AccountStorageDocument.class);
            if (document == null || !document.isSupported())
            {
                return quarantined(indexPath);
            }
            return new AccountStorageLoadResult(SemanticBankSearchStorage.toIndex(document, itemNameResolver), null);
        }
        catch (RuntimeException ex)
        {
            return quarantined(indexPath);
        }
    }

    void save(AccountKey accountKey, StorageIndex index) throws IOException
    {
        Path indexPath = indexPath(accountKey);
        Path directory = indexPath.getParent();
        Path temporaryPath = indexPath.resolveSibling("index.json.tmp");
        Files.createDirectories(directory);
        try
        {
            Files.writeString(temporaryPath, gson.toJson(SemanticBankSearchStorage.toDocument(index)), StandardCharsets.UTF_8);
            try
            {
                Files.move(temporaryPath, indexPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (AtomicMoveNotSupportedException ex)
            {
                Files.move(temporaryPath, indexPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        finally
        {
            Files.deleteIfExists(temporaryPath);
        }
    }

    void clear(AccountKey accountKey) throws IOException
    {
        Files.deleteIfExists(indexPath(accountKey));
    }

    private AccountStorageLoadResult quarantined(Path indexPath) throws IOException
    {
        Path quarantinePath = indexPath.resolveSibling("index.corrupt-" + clock.millis() + ".json");
        int suffix = 1;
        while (Files.exists(quarantinePath))
        {
            quarantinePath = indexPath.resolveSibling("index.corrupt-" + clock.millis() + "-" + suffix++ + ".json");
        }
        Files.move(indexPath, quarantinePath);
        return new AccountStorageLoadResult(new StorageIndex(), quarantinePath);
    }

    private Path indexPath(AccountKey accountKey)
    {
        return runeLiteDirectory.resolve("semantic-bank-search").resolve("accounts").resolve(accountKey.value()).resolve("index.json");
    }
}