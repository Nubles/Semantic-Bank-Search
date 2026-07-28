package com.semanticbanksearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class AccountSessionController
{
    private static final Logger LOG = LoggerFactory.getLogger(AccountSessionController.class);

    private static final String CORRUPT_NOTICE = "A damaged local index was quarantined; this account will rebuild as storage is observed.";
    private static final String LOAD_FAILURE_NOTICE = "Local account storage could not be loaded; this session started with an empty index.";
    private static final String SAVE_FAILURE_NOTICE = "Local account storage could not be saved.";
    private static final String CLEARED_NOTICE = "Local observed storage for this account was cleared.";

    private final AccountStorageRepository repository;
    private final IntFunction<String> itemNameResolver;
    private final StorageRetentionPolicy retentionPolicy;

    private AccountKey activeAccountKey;
    private StorageIndex activeIndex;

    AccountSessionController(
        AccountStorageRepository repository,
        IntFunction<String> itemNameResolver,
        StorageRetentionPolicy retentionPolicy)
    {
        this.repository = Objects.requireNonNull(repository);
        this.itemNameResolver = Objects.requireNonNull(itemNameResolver);
        this.retentionPolicy = Objects.requireNonNull(retentionPolicy);
    }

    AccountSessionUpdate switchTo(long accountHash)
    {
        Optional<AccountKey> requestedAccountKey = AccountKey.fromAccountHash(accountHash);
        if (!requestedAccountKey.isPresent())
        {
            return deactivate();
        }

        AccountKey nextAccountKey = requestedAccountKey.get();
        if (nextAccountKey.equals(activeAccountKey))
        {
            return update(false, Collections.emptyList());
        }

        List<String> notices = new ArrayList<>();
        unloadActiveAccount(notices);
        loadAccount(nextAccountKey, notices);
        return update(true, notices);
    }

    AccountSessionUpdate deactivate()
    {
        if (activeAccountKey == null)
        {
            return update(false, Collections.emptyList());
        }

        List<String> notices = new ArrayList<>();
        unloadActiveAccount(notices);
        return update(true, notices);
    }

    Optional<String> persist()
    {
        if (activeAccountKey == null)
        {
            return Optional.empty();
        }

        return saveActiveIndex(activeAccountKey, activeIndex);
    }

    boolean trimActiveIndex()
    {
        if (activeIndex == null)
        {
            return false;
        }

        int entriesBeforeTrim = activeIndex.items().size();
        retentionPolicy.apply(activeIndex);
        return activeIndex.items().size() != entriesBeforeTrim;
    }

    Optional<String> clearActiveAccount()
    {
        if (activeAccountKey == null)
        {
            return Optional.empty();
        }

        try
        {
            repository.clear(activeAccountKey);
            activeIndex = new StorageIndex();
            return Optional.of(CLEARED_NOTICE);
        }
        catch (IOException ex)
        {
            LOG.warn("Could not clear local account storage", ex);
            return Optional.of(SAVE_FAILURE_NOTICE);
        }
    }

    StorageIndex activeIndexOrNull()
    {
        return activeIndex;
    }

    AccountKey activeAccountKeyOrNull()
    {
        return activeAccountKey;
    }

    private void unloadActiveAccount(List<String> notices)
    {
        if (activeAccountKey == null)
        {
            return;
        }

        try
        {
            Optional<String> notice = saveActiveIndex(activeAccountKey, activeIndex);
            notice.ifPresent(notices::add);
        }
        finally
        {
            activeAccountKey = null;
            activeIndex = null;
        }
    }

    private void loadAccount(AccountKey accountKey, List<String> notices)
    {
        try
        {
            AccountStorageLoadResult result = repository.load(accountKey, itemNameResolver);
            StorageIndex loadedIndex = result.index();
            retentionPolicy.apply(loadedIndex);
            activeAccountKey = accountKey;
            activeIndex = loadedIndex;
            if (result.quarantinedPath().isPresent())
            {
                notices.add(CORRUPT_NOTICE);
            }
        }
        catch (IOException ex)
        {
            LOG.warn("Could not load local account storage", ex);
            activeAccountKey = accountKey;
            activeIndex = new StorageIndex();
            notices.add(LOAD_FAILURE_NOTICE);
        }
    }

    private Optional<String> saveActiveIndex(AccountKey accountKey, StorageIndex index)
    {
        retentionPolicy.apply(index);
        try
        {
            repository.save(accountKey, index);
            return Optional.empty();
        }
        catch (IOException ex)
        {
            LOG.warn("Could not save local account storage", ex);
            return Optional.of(SAVE_FAILURE_NOTICE);
        }
    }

    private AccountSessionUpdate update(boolean changed, List<String> notices)
    {
        return new AccountSessionUpdate(changed, activeIndex, notices);
    }
}
