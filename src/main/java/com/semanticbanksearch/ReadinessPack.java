package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReadinessPack
{
    private final String name;
    private final String description;
    private final List<String> aliases;
    private final List<ReadinessSlot> slots;

    public ReadinessPack(String name, String description, List<String> aliases, List<ReadinessSlot> slots)
    {
        this.name = clean(name);
        this.description = clean(description);
        this.aliases = normalizeAliases(aliases);
        this.slots = copySlots(slots);
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public List<ReadinessSlot> getSlots()
    {
        return slots;
    }

    public boolean matches(String normalizedQuery)
    {
        if (normalizedQuery == null || normalizedQuery.isEmpty())
        {
            return false;
        }
        for (String alias : aliases)
        {
            if (FuzzyMatcher.phraseMatches(normalizedQuery, alias))
            {
                return true;
            }
        }
        return false;
    }

    private static List<String> normalizeAliases(List<String> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }
        List<String> aliases = new ArrayList<>();
        for (String value : values)
        {
            String normalized = SemanticSearchEngine.normalize(value);
            if (!normalized.isEmpty())
            {
                aliases.add(normalized);
            }
        }
        return Collections.unmodifiableList(aliases);
    }

    private static List<ReadinessSlot> copySlots(List<ReadinessSlot> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    private static String clean(String value)
    {
        return value == null ? "" : value.trim();
    }
}
