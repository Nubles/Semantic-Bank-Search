package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReadinessSlotResult
{
    private final ReadinessSlot slot;
    private final List<SemanticSearchResult> ownedItems;

    public ReadinessSlotResult(ReadinessSlot slot, List<SemanticSearchResult> ownedItems)
    {
        this.slot = slot;
        this.ownedItems = copy(ownedItems);
    }

    public String getSlotName()
    {
        return slot == null ? "" : slot.getName();
    }

    public ReadinessSlotKind getKind()
    {
        return slot == null ? ReadinessSlotKind.RECOMMENDED : slot.getKind();
    }

    public String getQuery()
    {
        return slot == null ? "" : slot.getQuery();
    }

    public String getReason()
    {
        return slot == null ? "" : slot.getReason();
    }

    public List<SemanticSearchResult> getOwnedItems()
    {
        return ownedItems;
    }

    public boolean isMissing()
    {
        return ownedItems.isEmpty();
    }

    private static List<SemanticSearchResult> copy(List<SemanticSearchResult> items)
    {
        if (items == null || items.isEmpty())
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(items));
    }
}
