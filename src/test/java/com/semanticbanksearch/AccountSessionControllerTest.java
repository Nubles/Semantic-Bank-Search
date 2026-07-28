package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AccountSessionControllerTest
{
    private static final long FIXED_MILLIS = 1720000000000L;
    private static final String CORRUPT_NOTICE = "A damaged local index was quarantined; this account will rebuild as storage is observed.";
    private static final String LOAD_FAILURE_NOTICE = "Local account storage could not be loaded; this session started with an empty index.";
    private static final String SAVE_FAILURE_NOTICE = "Local account storage could not be saved.";
    private static final String CLEARED_NOTICE = "Local observed storage for this account was cleared.";

    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test public void zeroHashLeavesNoActiveIndex()
    {
        AccountSessionUpdate update = controller().switchTo(0L);

        assertFalse(update.isChanged());
        assertNull(update.getActiveIndex());
        assertNull(controller().activeIndexOrNull());
    }

    @Test public void activatingAccountLoadsOnlyItsIndex() throws IOException
    {
        AccountStorageRepository repository = repository();
        repository.save(key(101L), index(101, "Account A item", 1));
        repository.save(key(202L), index(202, "Account B item", 1));
        AccountSessionController controller = controller(repository);

        AccountSessionUpdate update = controller.switchTo(101L);

        assertTrue(update.isChanged());
        assertSame(update.getActiveIndex(), controller.activeIndexOrNull());
        assertTrue(containsItem(update.getActiveIndex(), 101));
        assertFalse(containsItem(update.getActiveIndex(), 202));
    }

    @Test public void switchingPersistsFirstAccountBeforeLoadingSecond() throws IOException
    {
        AccountSessionController controller = controller();
        StorageIndex firstIndex = controller.switchTo(101L).getActiveIndex();
        firstIndex.record(101, "Account A item", 1, StorageSourceType.BANK, "Bank", false, 1L);

        AccountSessionUpdate update = controller.switchTo(202L);

        assertTrue(update.isChanged());
        assertNotSame(firstIndex, update.getActiveIndex());
        assertFalse(containsItem(update.getActiveIndex(), 101));
        assertTrue(containsItem(repository().load(key(101L), this::itemName).index(), 101));
    }

    @Test public void repeatedHashDoesNotReloadOrReplaceIndex()
    {
        AccountSessionController controller = controller();
        StorageIndex activeIndex = controller.switchTo(101L).getActiveIndex();

        AccountSessionUpdate update = controller.switchTo(101L);

        assertFalse(update.isChanged());
        assertSame(activeIndex, update.getActiveIndex());
        assertTrue(update.getNotices().isEmpty());
    }

    @Test public void deactivationPersistsThenDropsAllAccountState() throws IOException
    {
        AccountSessionController controller = controller();
        StorageIndex activeIndex = controller.switchTo(101L).getActiveIndex();
        activeIndex.record(101, "Account A item", 1, StorageSourceType.BANK, "Bank", false, 1L);

        AccountSessionUpdate update = controller.deactivate();

        assertTrue(update.isChanged());
        assertNull(update.getActiveIndex());
        assertNull(controller.activeIndexOrNull());
        assertNull(controller.activeAccountKeyOrNull());
        assertTrue(containsItem(repository().load(key(101L), this::itemName).index(), 101));
    }

    @Test public void deactivationDropsStateAndReportsSaveFailure() throws IOException
    {
        AccountSessionController controller = controller();
        controller.switchTo(101L).getActiveIndex().record(101, "Account A item", 1, StorageSourceType.BANK, "Bank", false, 1L);
        assertEquals(Optional.empty(), controller.persist());
        replaceStorageDirectoryWithFile();

        AccountSessionUpdate update = controller.deactivate();

        assertTrue(update.isChanged());
        assertTrue(update.getNotices().contains(SAVE_FAILURE_NOTICE));
        assertNull(update.getActiveIndex());
        assertNull(controller.activeIndexOrNull());
        assertNull(controller.activeAccountKeyOrNull());
    }
    @Test public void failedSaveStillDropsPreviousAccountBeforeSwitch() throws IOException
    {
        AccountSessionController controller = controller();
        StorageIndex firstIndex = controller.switchTo(101L).getActiveIndex();
        firstIndex.record(101, "Account A item", 1, StorageSourceType.BANK, "Bank", false, 1L);
        assertEquals(Optional.empty(), controller.persist());
        replaceStorageDirectoryWithFile();

        AccountSessionUpdate update = controller.switchTo(202L);

        assertTrue(update.isChanged());
        assertTrue(update.getNotices().contains(SAVE_FAILURE_NOTICE));
        assertNotSame(firstIndex, update.getActiveIndex());
        assertFalse(containsItem(update.getActiveIndex(), 101));
    }

    @Test public void corruptLoadReturnsAUserFacingQuarantineNotice() throws IOException
    {
        Path indexPath = path(key(101L));
        Files.createDirectories(indexPath.getParent());
        Files.writeString(indexPath, "{damaged", StandardCharsets.UTF_8);

        AccountSessionUpdate update = controller().switchTo(101L);

        assertTrue(update.getNotices().contains(CORRUPT_NOTICE));
        assertTrue(update.getActiveIndex().items().isEmpty());
        assertTrue(Files.exists(indexPath.resolveSibling("index.corrupt-" + FIXED_MILLIS + ".json")));
    }

    @Test public void loadFailureStartsEmptyWithoutReusingPreviousIndex() throws IOException
    {
        AccountSessionController controller = controller();
        StorageIndex firstIndex = controller.switchTo(101L).getActiveIndex();
        firstIndex.record(101, "Account A item", 1, StorageSourceType.BANK, "Bank", false, 1L);
        assertEquals(Optional.empty(), controller.persist());
        replaceStorageDirectoryWithFile();

        AccountSessionUpdate update = controller.switchTo(202L);

        assertTrue(update.getNotices().contains(LOAD_FAILURE_NOTICE));
        assertNotSame(firstIndex, update.getActiveIndex());
        assertTrue(update.getActiveIndex().items().isEmpty());
        assertFalse(containsItem(update.getActiveIndex(), 101));
    }

    @Test public void transientReadFailureRecoversAndMergesNewestObservationsBeforeSave() throws IOException
    {
        StorageIndex diskIndex = new StorageIndex();
        diskIndex.record(101, "Disk only", 1, StorageSourceType.BANK, "Bank", false, 100L);
        diskIndex.record(102, "Disk newer", 12, StorageSourceType.BANK, "Bank", false, 300L);
        diskIndex.record(103, "Disk older", 13, StorageSourceType.BANK, "Bank", false, 100L);
        diskIndex.record(104, "Disk tie", 14, StorageSourceType.BANK, "Bank", false, 500L);
        repository().save(key(101L), diskIndex);
        ReadFailingFileOperations operations = new ReadFailingFileOperations(1);
        AccountSessionController controller = new AccountSessionController(
            repository(operations),
            this::itemName,
            new StorageRetentionPolicy(100, 100));

        AccountSessionUpdate update = controller.switchTo(101L);
        assertTrue(update.getNotices().contains(LOAD_FAILURE_NOTICE));
        StorageIndex sessionIndex = update.getActiveIndex();
        sessionIndex.record(102, "Session older", 22, StorageSourceType.BANK, "Bank", true, 200L);
        sessionIndex.record(103, "Session newer", 23, StorageSourceType.BANK, "Bank", true, 400L);
        sessionIndex.record(104, "Session tie", 24, StorageSourceType.BANK, "Bank", true, 500L);
        sessionIndex.record(105, "Session only", 25, StorageSourceType.BANK, "Bank", true, 600L);

        assertEquals(Optional.empty(), controller.persist());

        assertSame(sessionIndex, controller.activeIndexOrNull());
        StorageIndex recovered = repository().load(key(101L), this::itemName).index();
        assertEquals(1, itemById(recovered, 101).getQuantity());
        assertEquals(12, itemById(recovered, 102).getQuantity());
        assertEquals(23, itemById(recovered, 103).getQuantity());
        assertEquals(24, itemById(recovered, 104).getQuantity());
        assertEquals(25, itemById(recovered, 105).getQuantity());
        assertEquals(2, operations.readAttempts);
        assertEquals(1, operations.writeAttempts);

        assertEquals(Optional.empty(), controller.persist());
        assertEquals(2, operations.readAttempts);
        assertEquals(2, operations.writeAttempts);
    }

    @Test public void persistentReadFailureNeverOverwritesUnreadableIndex() throws IOException
    {
        repository().save(key(101L), index(101, "Valid disk item", 7));
        String originalJson = Files.readString(path(key(101L)), StandardCharsets.UTF_8);
        ReadFailingFileOperations operations = new ReadFailingFileOperations(Integer.MAX_VALUE);
        AccountSessionController controller = controller(repository(operations));
        StorageIndex sessionIndex = controller.switchTo(101L).getActiveIndex();
        sessionIndex.record(202, "Session item", 2, StorageSourceType.BANK, "Bank", true, 2L);

        assertEquals(Optional.of(SAVE_FAILURE_NOTICE), controller.persist());
        assertEquals(Optional.of(SAVE_FAILURE_NOTICE), controller.persist());

        assertEquals(originalJson, Files.readString(path(key(101L)), StandardCharsets.UTF_8));
        assertEquals(3, operations.readAttempts);
        assertEquals(0, operations.writeAttempts);
    }

    @Test public void deliberateClearResetsReadFailureSaveGuard() throws IOException
    {
        repository().save(key(101L), index(101, "Valid disk item", 7));
        ReadFailingFileOperations operations = new ReadFailingFileOperations(Integer.MAX_VALUE);
        AccountSessionController controller = controller(repository(operations));
        AccountSessionUpdate update = controller.switchTo(101L);
        assertTrue(update.getNotices().contains(LOAD_FAILURE_NOTICE));

        assertEquals(Optional.of(CLEARED_NOTICE), controller.clearActiveAccount());
        int readsAfterClear = operations.readAttempts;
        controller.activeIndexOrNull().record(
            202,
            "Fresh item",
            2,
            StorageSourceType.BANK,
            "Bank",
            true,
            2L);

        assertEquals(Optional.empty(), controller.persist());
        assertEquals(readsAfterClear, operations.readAttempts);
        assertEquals(1, operations.writeAttempts);
        assertTrue(containsItem(repository().load(key(101L), this::itemName).index(), 202));
    }

    @Test public void clearActiveAccountDeletesDiskAndMemoryState() throws IOException
    {
        AccountSessionController controller = controller();
        StorageIndex activeIndex = controller.switchTo(101L).getActiveIndex();
        activeIndex.record(101, "Account A item", 1, StorageSourceType.BANK, "Bank", false, 1L);
        assertEquals(Optional.empty(), controller.persist());
        assertTrue(Files.exists(path(key(101L))));

        Optional<String> notice = controller.clearActiveAccount();

        assertEquals(Optional.of(CLEARED_NOTICE), notice);
        assertFalse(Files.exists(path(key(101L))));
        assertNotSame(activeIndex, controller.activeIndexOrNull());
        assertTrue(controller.activeIndexOrNull().items().isEmpty());
    }

    @Test public void trimActiveIndexReportsOnlyActualRetentionChanges()
    {
        AccountSessionController controller = controller();
        StorageIndex activeIndex = controller.switchTo(101L).getActiveIndex();
        activeIndex.record(101, "First item", 1, StorageSourceType.BANK, "Bank", false, 1L);
        activeIndex.record(102, "Second item", 1, StorageSourceType.BANK, "Bank", false, 2L);
        activeIndex.record(103, "Newest item", 1, StorageSourceType.BANK, "Bank", false, 3L);

        assertTrue(controller.trimActiveIndex());
        assertEquals(2, activeIndex.items().size());
        assertFalse(controller.trimActiveIndex());
    }

    private AccountSessionController controller()
    {
        return controller(repository());
    }

    private AccountSessionController controller(AccountStorageRepository repository)
    {
        return new AccountSessionController(repository, this::itemName, new StorageRetentionPolicy(2, 2));
    }

    private AccountStorageRepository repository()
    {
        return new AccountStorageRepository(
            temporaryFolder.getRoot().toPath(),
            new Gson(),
            Clock.fixed(Instant.ofEpochMilli(FIXED_MILLIS), ZoneOffset.UTC));
    }

    private AccountStorageRepository repository(AccountStorageRepository.FileOperations operations)
    {
        return new AccountStorageRepository(
            temporaryFolder.getRoot().toPath(),
            new Gson(),
            Clock.fixed(Instant.ofEpochMilli(FIXED_MILLIS), ZoneOffset.UTC),
            operations);
    }

    private Path path(AccountKey accountKey)
    {
        return temporaryFolder.getRoot().toPath()
            .resolve("semantic-bank-search")
            .resolve("accounts")
            .resolve(accountKey.value())
            .resolve("index.json");
    }

    private void replaceStorageDirectoryWithFile() throws IOException
    {
        Path storageDirectory = temporaryFolder.getRoot().toPath().resolve("semantic-bank-search");
        Files.walk(storageDirectory)
            .sorted((left, right) -> right.getNameCount() - left.getNameCount())
            .forEach(path -> {
                try
                {
                    Files.delete(path);
                }
                catch (IOException ex)
                {
                    throw new IllegalStateException(ex);
                }
            });
        Files.writeString(storageDirectory, "not a directory", StandardCharsets.UTF_8);
    }

    private String itemName(int itemId)
    {
        return "Item " + itemId;
    }

    private static AccountKey key(long accountHash)
    {
        return AccountKey.fromAccountHash(accountHash).get();
    }

    private static StorageIndex index(int itemId, String name, int quantity)
    {
        StorageIndex index = new StorageIndex();
        index.record(itemId, name, quantity, StorageSourceType.BANK, "Bank", false, 1L);
        return index;
    }

    private static boolean containsItem(StorageIndex index, int itemId)
    {
        for (ObservedItem item : index.items())
        {
            if (item.getItemId() == itemId)
            {
                return true;
            }
        }
        return false;
    }

    private static ObservedItem itemById(StorageIndex index, int itemId)
    {
        for (ObservedItem item : index.items())
        {
            if (item.getItemId() == itemId)
            {
                return item;
            }
        }
        throw new AssertionError("Missing item " + itemId);
    }

    private static final class ReadFailingFileOperations implements AccountStorageRepository.FileOperations
    {
        private int remainingReadFailures;
        private int readAttempts;
        private int writeAttempts;

        private ReadFailingFileOperations(int remainingReadFailures)
        {
            this.remainingReadFailures = remainingReadFailures;
        }

        @Override
        public String readString(Path path) throws IOException
        {
            readAttempts++;
            if (remainingReadFailures > 0)
            {
                remainingReadFailures--;
                throw new IOException("read failed");
            }
            return Files.readString(path, StandardCharsets.UTF_8);
        }

        @Override
        public void writeString(Path path, String value) throws IOException
        {
            writeAttempts++;
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
}
