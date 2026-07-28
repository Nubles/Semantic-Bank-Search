package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class LegacyStorageCleanupTest
{
    @Test
    public void legacyCleanupUnsetsOnlySharedIndexKey()
    {
        List<String> removedKeys = new ArrayList<>();

        LegacyStorageCleanup.remove((group, key) -> removedKeys.add(group + "." + key));

        assertEquals(
            Collections.singletonList("semanticbanksearch.index"),
            removedKeys);
    }
}
