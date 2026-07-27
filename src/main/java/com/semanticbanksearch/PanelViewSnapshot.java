package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class PanelViewSnapshot
{
    enum Kind
    {
        CLEAR,
        SEARCH,
        ALL_INDEXED,
        READINESS,
        COVERAGE_AUDIT
    }

    private final long revision;
    private final Kind kind;
    private final String query;
    private final String status;
    private final List<SemanticSearchResult> searchResults;
    private final List<ObservedItem> indexedItems;
    private final ReadinessResult readinessResult;
    private final List<SemanticCoverageResult> coverageResults;

    private PanelViewSnapshot(
        long revision,
        Kind kind,
        String query,
        String status,
        List<SemanticSearchResult> searchResults,
        List<ObservedItem> indexedItems,
        ReadinessResult readinessResult,
        List<SemanticCoverageResult> coverageResults)
    {
        this.revision = revision;
        this.kind = kind;
        this.query = clean(query);
        this.status = clean(status);
        this.searchResults = immutableCopy(searchResults);
        this.indexedItems = immutableObservedItemCopy(indexedItems);
        this.readinessResult = readinessResult;
        this.coverageResults = immutableCopy(coverageResults);
    }

    static PanelViewSnapshot clear(long revision)
    {
        return clear(revision, "");
    }

    static PanelViewSnapshot clear(long revision, String status)
    {
        return new PanelViewSnapshot(
            revision,
            Kind.CLEAR,
            "",
            status,
            Collections.emptyList(),
            Collections.emptyList(),
            null,
            Collections.emptyList());
    }

    static PanelViewSnapshot search(
        long revision,
        String query,
        List<SemanticSearchResult> results,
        String status)
    {
        return new PanelViewSnapshot(
            revision,
            Kind.SEARCH,
            query,
            status,
            results,
            Collections.emptyList(),
            null,
            Collections.emptyList());
    }

    static PanelViewSnapshot allIndexed(long revision, List<ObservedItem> items, String status)
    {
        return new PanelViewSnapshot(
            revision,
            Kind.ALL_INDEXED,
            "",
            status,
            Collections.emptyList(),
            items,
            null,
            Collections.emptyList());
    }

    static PanelViewSnapshot readiness(
        long revision,
        String query,
        ReadinessResult result,
        String status)
    {
        return new PanelViewSnapshot(
            revision,
            Kind.READINESS,
            query,
            status,
            Collections.emptyList(),
            Collections.emptyList(),
            result == null ? ReadinessResult.unmatched(query) : result,
            Collections.emptyList());
    }

    static PanelViewSnapshot coverageAudit(
        long revision,
        List<SemanticCoverageResult> results,
        String status)
    {
        return new PanelViewSnapshot(
            revision,
            Kind.COVERAGE_AUDIT,
            "",
            status,
            Collections.emptyList(),
            Collections.emptyList(),
            null,
            results);
    }

    long getRevision()
    {
        return revision;
    }

    Kind getKind()
    {
        return kind;
    }

    String getQuery()
    {
        return query;
    }

    String getStatus()
    {
        return status;
    }

    List<SemanticSearchResult> getSearchResults()
    {
        return searchResults;
    }

    List<ObservedItem> getIndexedItems()
    {
        return indexedItems;
    }

    ReadinessResult getReadinessResult()
    {
        return readinessResult;
    }

    List<SemanticCoverageResult> getCoverageResults()
    {
        return coverageResults;
    }

    private static <T> List<T> immutableCopy(List<T> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }

    private static List<ObservedItem> immutableObservedItemCopy(List<ObservedItem> items)
    {
        if (items == null || items.isEmpty())
        {
            return Collections.emptyList();
        }

        List<ObservedItem> copiedItems = new ArrayList<>();
        for (ObservedItem item : items)
        {
            if (item != null)
            {
                copiedItems.add(new ObservedItem(item));
            }
        }
        return copiedItems.isEmpty()
            ? Collections.emptyList()
            : Collections.unmodifiableList(copiedItems);
    }

    private static String clean(String value)
    {
        return value == null ? "" : value.trim();
    }
}
