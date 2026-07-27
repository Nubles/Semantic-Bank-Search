package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class AccountSessionUpdate
{
    private final boolean changed;
    private final StorageIndex activeIndex;
    private final List<String> notices;

    AccountSessionUpdate(boolean changed, StorageIndex activeIndex, List<String> notices)
    {
        this.changed = changed;
        this.activeIndex = activeIndex;
        this.notices = Collections.unmodifiableList(new ArrayList<>(notices));
    }

    boolean isChanged()
    {
        return changed;
    }

    StorageIndex getActiveIndex()
    {
        return activeIndex;
    }

    List<String> getNotices()
    {
        return notices;
    }
}
