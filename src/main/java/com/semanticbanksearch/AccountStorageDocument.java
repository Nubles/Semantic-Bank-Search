package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.List;

class AccountStorageDocument
{
    int schemaVersion;
    String catalogueVersion;
    List<StoredObservedItem> items = new ArrayList<>();

    AccountStorageDocument()
    {
    }

    boolean isSupported()
    {
        return schemaVersion == AccountStorageRepository.SCHEMA_VERSION && items != null;
    }
}