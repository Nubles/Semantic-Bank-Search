package com.semanticbanksearch;

public class ReadinessSlot
{
    private final String name;
    private final ReadinessSlotKind kind;
    private final String query;
    private final String reason;

    public ReadinessSlot(String name, ReadinessSlotKind kind, String query, String reason)
    {
        this.name = clean(name);
        this.kind = kind == null ? ReadinessSlotKind.RECOMMENDED : kind;
        this.query = clean(query);
        this.reason = clean(reason);
    }

    public String getName()
    {
        return name;
    }

    public ReadinessSlotKind getKind()
    {
        return kind;
    }

    public String getQuery()
    {
        return query;
    }

    public String getReason()
    {
        return reason;
    }

    private static String clean(String value)
    {
        return value == null ? "" : value.trim();
    }
}
