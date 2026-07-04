package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReadinessResult
{
    private final String packName;
    private final String description;
    private final List<ReadinessSlotResult> slotResults;
    private final boolean matched;

    public ReadinessResult(String packName, String description, List<ReadinessSlotResult> slotResults, boolean matched)
    {
        this.packName = clean(packName);
        this.description = clean(description);
        this.slotResults = copy(slotResults);
        this.matched = matched;
    }

    public static ReadinessResult unmatched(String query)
    {
        return new ReadinessResult(clean(query), "", Collections.emptyList(), false);
    }

    public String getPackName()
    {
        return packName;
    }

    public String getDescription()
    {
        return description;
    }

    public List<ReadinessSlotResult> getSlotResults()
    {
        return slotResults;
    }

    public boolean isMatched()
    {
        return matched;
    }

    public int getRequiredSlotCount()
    {
        int count = 0;
        for (ReadinessSlotResult slotResult : slotResults)
        {
            if (slotResult.getKind() == ReadinessSlotKind.REQUIRED)
            {
                count++;
            }
        }
        return count;
    }

    public int getCoveredRequiredSlotCount()
    {
        int count = 0;
        for (ReadinessSlotResult slotResult : slotResults)
        {
            if (slotResult.getKind() == ReadinessSlotKind.REQUIRED && !slotResult.isMissing())
            {
                count++;
            }
        }
        return count;
    }

    private static List<ReadinessSlotResult> copy(List<ReadinessSlotResult> results)
    {
        if (results == null || results.isEmpty())
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(results));
    }

    private static String clean(String value)
    {
        return value == null ? "" : value.trim();
    }
}
