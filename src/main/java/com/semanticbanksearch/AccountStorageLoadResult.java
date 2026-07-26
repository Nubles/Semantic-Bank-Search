package com.semanticbanksearch;

import java.nio.file.Path;
import java.util.Optional;

final class AccountStorageLoadResult
{
    private final StorageIndex index;
    private final Optional<Path> quarantinedPath;

    AccountStorageLoadResult(StorageIndex index, Path quarantinedPath)
    {
        this.index = index == null ? new StorageIndex() : index;
        this.quarantinedPath = Optional.ofNullable(quarantinedPath);
    }

    StorageIndex index()
    {
        return index;
    }

    Optional<Path> quarantinedPath()
    {
        return quarantinedPath;
    }
}