package com.semanticbanksearch;

public final class StorageRetentionPolicy
{
    static final int DEFAULT_MAXIMUM_ENTRIES = 5_000;
    static final int HARD_MAXIMUM_ENTRIES = 20_000;
    private static final int MINIMUM_ENTRIES = 100;

    private final int maximumEntries;

    public StorageRetentionPolicy(int configuredMaximumEntries)
    {
        this(configuredMaximumEntries, HARD_MAXIMUM_ENTRIES);
    }

    StorageRetentionPolicy(int configuredMaximumEntries, int hardMaximumEntries)
    {
        int maximumAllowedEntries = Math.max(1, hardMaximumEntries);
        int minimumAllowedEntries = Math.min(MINIMUM_ENTRIES, maximumAllowedEntries);
        maximumEntries = Math.max(minimumAllowedEntries, Math.min(configuredMaximumEntries, maximumAllowedEntries));
    }

    void apply(StorageIndex index)
    {
        if (index == null)
        {
            return;
        }

        index.trimToLimits(maximumEntries, maximumEntries);
    }
}
