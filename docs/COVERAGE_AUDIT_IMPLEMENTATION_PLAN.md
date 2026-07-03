# Coverage Audit Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a local coverage audit view that shows which observed bank/storage items are semantically covered and which remain uncovered.

**Architecture:** Add a small analyzer layer beside `SemanticSearchEngine` that evaluates observed items against existing semantic rules without needing a query. Add a panel mode that renders analyzer results and never updates bank highlights.

**Tech Stack:** Java, RuneLite plugin APIs, Swing UI, JUnit 4, existing static semantic rule classes.

---

## File Structure

- Create `src/main/java/com/semanticbanksearch/SemanticCoverageResult.java`
  - Immutable row model for one observed item and its matched semantic categories.
- Create `src/main/java/com/semanticbanksearch/SemanticCoverageAnalyzer.java`
  - Local analyzer that converts `List<ObservedItem>` plus rules into sorted coverage rows.
- Create `src/test/java/com/semanticbanksearch/SemanticCoverageAnalyzerTest.java`
  - Unit tests for covered rows, uncovered rows, source separation, and sorting.
- Modify `src/main/java/com/semanticbanksearch/SemanticRule.java`
  - Add read-only accessors/helpers needed by the analyzer.
- Modify `src/main/java/com/semanticbanksearch/SemanticBankSearchPanel.java`
  - Add a `Coverage` button and renderer for coverage rows.
- Modify `src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java`
  - Add `COVERAGE_AUDIT` panel mode and refresh path.
- Modify `src/test/java/com/semanticbanksearch/SemanticBankSearchPluginTest.java`
  - Verify coverage mode does not update overlay highlights.
- Modify `README.md` and `docs/SEMANTIC_COVERAGE.md`
  - Mention the new coverage audit workflow after implementation.

---

### Task 1: Coverage Row Model

**Files:**
- Create: `src/main/java/com/semanticbanksearch/SemanticCoverageResult.java`
- Test: `src/test/java/com/semanticbanksearch/SemanticCoverageAnalyzerTest.java`

- [ ] **Step 1: Write the failing row model test**

```java
@Test
public void coverageResultReportsCoveredState()
{
    ObservedItem item = new ObservedItem(1001, "Barrows teleport", 3, StorageSourceType.BANK, "Bank", true, 1_000L);

    SemanticCoverageResult covered = new SemanticCoverageResult(
        item,
        Arrays.asList("Barrows travel"),
        Arrays.asList("Teleports near Barrows."),
        100);
    SemanticCoverageResult uncovered = new SemanticCoverageResult(
        item,
        Arrays.asList(),
        Arrays.asList(),
        0);

    assertTrue(covered.isCovered());
    assertFalse(uncovered.isCovered());
    assertEquals("Barrows teleport", covered.getItemName());
    assertEquals(3, covered.getQuantity());
}
```

- [ ] **Step 2: Run the focused test and verify it fails**

Run: `.\gradlew.bat test --tests com.semanticbanksearch.SemanticCoverageAnalyzerTest`

Expected: FAIL because `SemanticCoverageResult` does not exist.

- [ ] **Step 3: Add `SemanticCoverageResult`**

```java
package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SemanticCoverageResult
{
    private final int itemId;
    private final String itemName;
    private final int quantity;
    private final StorageSourceType sourceType;
    private final String sourceName;
    private final boolean currentlyVisible;
    private final List<String> categories;
    private final List<String> reasons;
    private final int bestScore;

    public SemanticCoverageResult(
        ObservedItem item,
        List<String> categories,
        List<String> reasons,
        int bestScore)
    {
        this.itemId = item.getItemId();
        this.itemName = item.getName();
        this.quantity = item.getQuantity();
        this.sourceType = item.getSourceType();
        this.sourceName = item.getSourceName();
        this.currentlyVisible = item.isCurrentlyVisible();
        this.categories = immutableCopy(categories);
        this.reasons = immutableCopy(reasons);
        this.bestScore = bestScore;
    }

    public int getItemId()
    {
        return itemId;
    }

    public String getItemName()
    {
        return itemName;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public StorageSourceType getSourceType()
    {
        return sourceType;
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public boolean isCurrentlyVisible()
    {
        return currentlyVisible;
    }

    public List<String> getCategories()
    {
        return categories;
    }

    public List<String> getReasons()
    {
        return reasons;
    }

    public int getBestScore()
    {
        return bestScore;
    }

    public boolean isCovered()
    {
        return !categories.isEmpty();
    }

    private static List<String> immutableCopy(List<String> values)
    {
        if (values == null || values.isEmpty())
        {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(values));
    }
}
```

- [ ] **Step 4: Commit**

Run:

```bash
git add src/main/java/com/semanticbanksearch/SemanticCoverageResult.java src/test/java/com/semanticbanksearch/SemanticCoverageAnalyzerTest.java
git commit -m "Add semantic coverage row model"
```

---

### Task 2: Coverage Analyzer

**Files:**
- Create: `src/main/java/com/semanticbanksearch/SemanticCoverageAnalyzer.java`
- Modify: `src/main/java/com/semanticbanksearch/SemanticRule.java`
- Test: `src/test/java/com/semanticbanksearch/SemanticCoverageAnalyzerTest.java`

- [ ] **Step 1: Add the failing analyzer smoke test**

```java
@Test
public void analyzerMarksCoveredAndUncoveredItems()
{
    StorageIndex index = new StorageIndex();
    index.record(1001, "Barrows teleport", 3, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(1002, "Uncut sapphire", 12, StorageSourceType.BANK, "Bank", true, 1_001L);

    SemanticCoverageAnalyzer analyzer = new SemanticCoverageAnalyzer(SemanticLibrary.create());
    List<SemanticCoverageResult> results = analyzer.analyze(index.items());

    assertTrue(find(results, "Barrows teleport").isCovered());
    assertFalse(find(results, "Uncut sapphire").isCovered());
}

private static SemanticCoverageResult find(List<SemanticCoverageResult> results, String itemName)
{
    for (SemanticCoverageResult result : results)
    {
        if (result.getItemName().equals(itemName))
        {
            return result;
        }
    }
    throw new AssertionError("Missing " + itemName);
}
```

- [ ] **Step 2: Run the focused test and verify it fails**

Run: `.\gradlew.bat test --tests com.semanticbanksearch.SemanticCoverageAnalyzerTest`

Expected: FAIL because `SemanticCoverageAnalyzer` does not exist.

- [ ] **Step 3: Expose rule coverage helpers**

Add this package-private method to `SemanticRule`:

```java
boolean matchesObservedItem(ObservedItem item)
{
    if (item == null)
    {
        return false;
    }
    return matchesItem(SemanticSearchEngine.normalize(item.getName()));
}
```

- [ ] **Step 4: Add `SemanticCoverageAnalyzer`**

```java
package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SemanticCoverageAnalyzer
{
    private final List<SemanticRule> rules;

    public SemanticCoverageAnalyzer(List<SemanticRule> rules)
    {
        this.rules = rules == null ? new ArrayList<>() : new ArrayList<>(rules);
    }

    public List<SemanticCoverageResult> analyze(List<ObservedItem> items)
    {
        List<SemanticCoverageResult> results = new ArrayList<>();
        if (items == null)
        {
            return results;
        }

        for (ObservedItem item : items)
        {
            if (item != null)
            {
                results.add(analyzeItem(item));
            }
        }

        results.sort(Comparator
            .comparing(SemanticCoverageResult::isCovered)
            .thenComparing(SemanticCoverageResult::getItemName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(SemanticCoverageResult::getSourceName, String.CASE_INSENSITIVE_ORDER));
        return results;
    }

    private SemanticCoverageResult analyzeItem(ObservedItem item)
    {
        Set<String> categories = new LinkedHashSet<>();
        Set<String> reasons = new LinkedHashSet<>();
        String normalizedName = SemanticSearchEngine.normalize(item.getName());
        int bestScore = 0;

        for (SemanticRule rule : rules)
        {
            if (rule.matchesObservedItem(item))
            {
                categories.add(rule.getCategory());
                reasons.add(rule.getReason());
                bestScore = Math.max(bestScore, rule.scoreFor(normalizedName));
            }
        }

        return new SemanticCoverageResult(
            item,
            new ArrayList<>(categories),
            new ArrayList<>(reasons),
            bestScore);
    }
}
```

- [ ] **Step 5: Run the focused analyzer test**

Run: `.\gradlew.bat test --tests com.semanticbanksearch.SemanticCoverageAnalyzerTest`

Expected: PASS.

- [ ] **Step 6: Extend tests for sorting and separate sources**

Add:

```java
@Test
public void analyzerKeepsDuplicateItemsFromDifferentSources()
{
    StorageIndex index = new StorageIndex();
    index.record(2001, "Spade", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(2001, "Spade", 1, StorageSourceType.POH_STORAGE, "Costume room", false, 1_001L);

    List<SemanticCoverageResult> results = new SemanticCoverageAnalyzer(SemanticLibrary.create()).analyze(index.items());

    assertEquals(2, results.size());
    assertTrue(results.get(0).isCovered());
    assertTrue(results.get(1).isCovered());
}

@Test
public void analyzerSortsUncoveredItemsFirst()
{
    StorageIndex index = new StorageIndex();
    index.record(3001, "Barrows teleport", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(3002, "Uncut sapphire", 1, StorageSourceType.BANK, "Bank", true, 1_001L);

    List<SemanticCoverageResult> results = new SemanticCoverageAnalyzer(SemanticLibrary.create()).analyze(index.items());

    assertEquals("Uncut sapphire", results.get(0).getItemName());
    assertFalse(results.get(0).isCovered());
    assertEquals("Barrows teleport", results.get(1).getItemName());
    assertTrue(results.get(1).isCovered());
}
```

- [ ] **Step 7: Run the focused analyzer test again**

Run: `.\gradlew.bat test --tests com.semanticbanksearch.SemanticCoverageAnalyzerTest`

Expected: PASS.

- [ ] **Step 8: Commit**

Run:

```bash
git add src/main/java/com/semanticbanksearch/SemanticCoverageAnalyzer.java src/main/java/com/semanticbanksearch/SemanticRule.java src/test/java/com/semanticbanksearch/SemanticCoverageAnalyzerTest.java
git commit -m "Add semantic coverage analyzer"
```

---

### Task 3: Coverage Panel View

**Files:**
- Modify: `src/main/java/com/semanticbanksearch/SemanticBankSearchPanel.java`
- Test manually through plugin runner if available.

- [ ] **Step 1: Add a coverage callback**

Change the panel constructor signature to:

```java
public SemanticBankSearchPanel(
    Consumer<String> searchConsumer,
    Runnable allIndexedConsumer,
    Runnable coverageAuditConsumer,
    Runnable clearConsumer)
```

Store `coverageAuditConsumer` in a new private final field, defaulting to no-op when null.

- [ ] **Step 2: Add a Coverage button**

In `searchControls()`, after the `All Indexed` button, add:

```java
JButton coverageButton = new JButton("Coverage");
coverageButton.setFocusable(false);
coverageButton.addActionListener(event -> coverageAuditConsumer.run());
buttons.add(coverageButton);
```

- [ ] **Step 3: Add panel rendering method**

Add:

```java
public void updateCoverageAudit(List<SemanticCoverageResult> results, String status)
{
    int sequence = nextRenderSequence();
    List<SemanticCoverageResult> safeResults = results == null ? Collections.emptyList() : new ArrayList<>(results);
    String safeStatus = status == null ? "" : status;
    if (!SwingUtilities.isEventDispatchThread())
    {
        SwingUtilities.invokeLater(() -> renderCoverageAudit(sequence, safeResults, safeStatus));
        return;
    }

    renderCoverageAudit(sequence, safeResults, safeStatus);
}
```

- [ ] **Step 4: Add coverage rendering helpers**

Add `renderCoverageAudit`, `coverageCard`, `coverageDetails`, and a small `joinLimited` helper. The uncovered card body must say `No semantic category yet.` Covered rows should show matched categories and at most two reasons, plus `+N more` if more reasons exist.

- [ ] **Step 5: Commit**

Run:

```bash
git add src/main/java/com/semanticbanksearch/SemanticBankSearchPanel.java
git commit -m "Add coverage audit panel view"
```

---

### Task 4: Plugin Coverage Mode

**Files:**
- Modify: `src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java`
- Test: `src/test/java/com/semanticbanksearch/SemanticBankSearchPluginTest.java`

- [ ] **Step 1: Add panel mode**

Change:

```java
private enum PanelMode
{
    SEARCH,
    ALL_INDEXED
}
```

to:

```java
private enum PanelMode
{
    SEARCH,
    ALL_INDEXED,
    COVERAGE_AUDIT
}
```

- [ ] **Step 2: Add analyzer field**

Add:

```java
private SemanticCoverageAnalyzer coverageAnalyzer;
```

Initialize it in `startUp()` after the search engine:

```java
List<SemanticRule> rules = SemanticLibrary.create();
engine = new SemanticSearchEngine(rules);
coverageAnalyzer = new SemanticCoverageAnalyzer(rules);
```

Set it to null in `shutDown()`.

- [ ] **Step 3: Wire the panel callback**

Change panel construction to:

```java
panel = new SemanticBankSearchPanel(this::runSearch, this::showIndexedItems, this::showCoverageAudit, this::clearSearch);
```

- [ ] **Step 4: Add coverage refresh methods**

Add `showCoverageAudit()` and `refreshCoverageAudit(long revision)`. `showCoverageAudit()` must clear overlay highlights. `refreshCoverageAudit()` must call `panel.updateCoverageAudit(results, coverageStatus(results))`.

- [ ] **Step 5: Keep active refresh working**

Update `refreshActivePanelMode()` so `COVERAGE_AUDIT` calls `refreshCoverageAudit(viewState.revision)`.

- [ ] **Step 6: Add status text**

Add:

```java
private String coverageStatus(List<SemanticCoverageResult> results)
{
    if (results == null || results.isEmpty())
    {
        return "";
    }

    int covered = 0;
    for (SemanticCoverageResult result : results)
    {
        if (result.isCovered())
        {
            covered++;
        }
    }
    return "Covered " + covered + " of " + results.size() + " observed items.";
}
```

- [ ] **Step 7: Add plugin test coverage**

Add a focused test verifying that entering coverage mode clears highlight ids and does not set new highlights. If direct private method access is awkward, prefer testing through a small package-private method rather than reflection.

- [ ] **Step 8: Run focused plugin tests**

Run:

```bash
.\gradlew.bat test --tests com.semanticbanksearch.SemanticBankSearchPluginTest
```

Expected: PASS.

- [ ] **Step 9: Commit**

Run:

```bash
git add src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java src/test/java/com/semanticbanksearch/SemanticBankSearchPluginTest.java
git commit -m "Add coverage audit plugin mode"
```

---

### Task 5: Documentation and Verification

**Files:**
- Modify: `README.md`
- Modify: `docs/SEMANTIC_COVERAGE.md`

- [ ] **Step 1: Update public docs**

Add a short note that the Coverage view shows uncovered observed items and should be used before expanding static semantic coverage.

- [ ] **Step 2: Run full verification**

Run:

```bash
.\gradlew.bat test
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Run safety scan**

Run:

```bash
rg "http|https|Socket|Robot|MenuEntry|setMenuEntries|invokeMenuAction|withdraw|deposit|Runtime|getRuntime|ProcessBuilder|reflection|ClassLoader"
```

Expected: no production-code matches introducing network calls, menu manipulation, item actions, process execution, reflection, or classloader behavior.

- [ ] **Step 4: Run whitespace check**

Run:

```bash
git diff --check
```

Expected: no output.

- [ ] **Step 5: Commit**

Run:

```bash
git add README.md docs/SEMANTIC_COVERAGE.md
git commit -m "Document coverage audit workflow"
```

---

## Self-Review

- The plan builds one feature: coverage audit for observed items.
- It keeps the plugin local-only and passive.
- It uses existing semantic rules instead of adding a second classification source.
- It does not infer unobserved items or storage contents.
- It does not alter bank menu entries, withdraw items, deposit items, or trigger item actions.
- It provides focused tests before UI/plugin wiring and full verification at the end.
