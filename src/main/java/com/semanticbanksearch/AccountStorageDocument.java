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
        return schemaVersion == AccountStorageRepository.SCHEMA_VERSION
            && AccountStorageRepository.CURRENT_CATALOGUE_VERSION.equals(catalogueVersion)
            && items != null;
    }
}