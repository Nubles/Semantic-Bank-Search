package com.semanticbanksearch;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
    private final FileOperations fileOperations;

    interface FileOperations
    {
        String readString(Path path) throws IOException;
        void writeString(Path path, String value) throws IOException;
        void move(Path source, Path target, CopyOption... options) throws IOException;
        void deleteIfExists(Path path) throws IOException;
    }

    private static final class NioFileOperations implements FileOperations
    {
        @Override
        public String readString(Path path) throws IOException
        {
            return Files.readString(path, StandardCharsets.UTF_8);
        }

        @Override
        public void writeString(Path path, String value) throws IOException
        {
            Files.writeString(path, value, StandardCharsets.UTF_8);
        }

        @Override
        public void move(Path source, Path target, CopyOption... options) throws IOException
        {
            Files.move(source, target, options);
        }

        @Override
        public void deleteIfExists(Path path) throws IOException
        {
            Files.deleteIfExists(path);
        }
    }

    AccountStorageRepository(Path runeLiteDirectory, Gson gson, Clock clock)
    {
        this(runeLiteDirectory, gson, clock, new NioFileOperations());
    }

    AccountStorageRepository(Path runeLiteDirectory, Gson gson, Clock clock, FileOperations fileOperations)
    {
        this.runeLiteDirectory = Objects.requireNonNull(runeLiteDirectory);
        this.gson = Objects.requireNonNull(gson);
        this.clock = Objects.requireNonNull(clock);
        this.fileOperations = Objects.requireNonNull(fileOperations);
    }

    AccountStorageLoadResult load(AccountKey accountKey, IntFunction<String> itemNameResolver) throws IOException
    {
        Path storageDirectory = runeLiteDirectory.resolve("semantic-bank-search");
        if (Files.exists(storageDirectory) && !Files.isDirectory(storageDirectory))
        {
            throw new IOException("Semantic bank search storage path is not a directory");
        }

        Path indexPath = indexPath(accountKey);
        if (Files.notExists(indexPath))
        {
            return new AccountStorageLoadResult(new StorageIndex(), null);
        }
        try
        {
            JsonElement root = JsonParser.parseString(fileOperations.readString(indexPath));
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
            fileOperations.writeString(temporaryPath, gson.toJson(SemanticBankSearchStorage.toDocument(index)));
            try
            {
                fileOperations.move(temporaryPath, indexPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (AtomicMoveNotSupportedException ex)
            {
                fileOperations.move(temporaryPath, indexPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        finally
        {
            fileOperations.deleteIfExists(temporaryPath);
        }
    }

    void clear(AccountKey accountKey) throws IOException
    {
        Path accountDirectory = accountDirectory(accountKey);
        if (Files.notExists(accountDirectory))
        {
            return;
        }

        Files.walkFileTree(accountDirectory, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException
            {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException failure) throws IOException
            {
                if (failure != null)
                {
                    throw failure;
                }
                Files.delete(directory);
                return FileVisitResult.CONTINUE;
            }
        });
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
        return accountDirectory(accountKey).resolve("index.json");
    }

    private Path accountDirectory(AccountKey accountKey)
    {
        return runeLiteDirectory.resolve("semantic-bank-search").resolve("accounts").resolve(accountKey.value()).normalize();
    }
}