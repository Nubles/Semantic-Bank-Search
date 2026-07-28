package com.semanticbanksearch;

import java.util.Objects;
import java.util.function.BiConsumer;

final class LegacyStorageCleanup
{
    private static final String SHARED_INDEX_KEY = "index";

    private LegacyStorageCleanup()
    {
    }

    static void remove(BiConsumer<String, String> unsetConfiguration)
    {
        Objects.requireNonNull(unsetConfiguration, "unsetConfiguration")
            .accept(SemanticBankSearchConfig.GROUP, SHARED_INDEX_KEY);
    }
}
