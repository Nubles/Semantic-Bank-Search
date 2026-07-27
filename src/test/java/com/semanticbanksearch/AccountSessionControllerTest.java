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
}
