# Curated Query Pack Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a repeatable curated query pack test suite for real player-style searches with explicit positive and negative item expectations.

**Architecture:** Keep production search logic unchanged unless the query pack exposes a phrase gap. Add a focused JUnit test class that builds in-memory `StorageIndex` fixtures and asserts inclusion/exclusion by item name. Make only phrase-specific rule updates needed to pass the pack.

**Tech Stack:** Java 11, JUnit 4, Gradle wrapper, existing static `SemanticLibrary` rule classes.

---

## File Structure

- Create `src/test/java/com/semanticbanksearch/CuratedQueryPackTest.java`: owns high-level player-query examples and reusable positive/negative assertion helpers.
- Modify `src/main/java/com/semanticbanksearch/BossRules.java`: add `barrows trip` as a phrase alias only if the red query pack proves it is missing.
- Modify `src/main/java/com/semanticbanksearch/ClueRules.java`: add `clue step` and `clue steps` phrase aliases only if the red query pack proves they are missing.

No UI, storage, overlay, or runtime plugin behavior files should change.

---

### Task 1: Add Curated Query Pack Harness

**Files:**
- Create: `src/test/java/com/semanticbanksearch/CuratedQueryPackTest.java`

- [ ] **Step 1: Write the failing query pack tests**

Create `CuratedQueryPackTest.java` with this content:

```java
package com.semanticbanksearch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class CuratedQueryPackTest
{
    @Test
    public void corePlayerQueriesFindExpectedItemsWithoutBleed()
    {
        assertQueryPack(
            "barrows trip",
            positives("Barrows teleport", "Spade", "Prayer potion(4)", "Trident of the seas"),
            negatives("Shantay pass", "Dragon scimitar", "Raw shark"));

        assertQueryPack(
            "clue step",
            positives("Spade", "Bullseye lantern", "Games necklace(8)"),
            negatives("Dragon scimitar", "Anti-dragon shield", "Raw shark"));

        assertQueryPack(
            "herb run",
            positives("Seed dibber", "Ultracompost", "Ranarr seed", "Skills necklace(6)"),
            negatives("Dragon harpoon", "Rune platebody", "Coal bag"));

        assertQueryPack(
            "wildy escape",
            positives("Royal seed pod", "Burning amulet", "Ring of wealth"),
            negatives("Xeric's talisman", "Dragon scimitar", "Shantay pass"));

        assertQueryPack(
            "birdhouse run",
            positives("Oak bird house", "Clockwork", "Teak logs", "Digsite pendant"),
            negatives("Bow string", "Knife", "Rune platebody"));

        assertQueryPack(
            "wintertodt supplies",
            positives("Clue hunter boots", "Steel axe", "Tinderbox", "Cake"),
            negatives("Dragon harpoon", "Large pouch", "Coal bag"));

        assertQueryPack(
            "tempoross supplies",
            positives("Dragon harpoon", "Rope", "Bucket", "Angler hat"),
            negatives("Rune scimitar", "Coal bag", "Large pouch"));

        assertQueryPack(
            "dragon slayer task",
            positives("Anti-dragon shield", "Extended antifire(4)", "Dragon hunter crossbow", "Zamorakian hasta"),
            negatives("Arclight", "Mirror shield", "Facemask"));

        assertQueryPack(
            "desert travel",
            positives("Desert amulet 4", "Shantay pass", "Waterskin(4)"),
            negatives("Barrows teleport", "Xeric's talisman", "Enchanted lyre"));

        assertQueryPack(
            "fossil island travel",
            positives("Digsite pendant", "Numulite"),
            negatives("Xeric's talisman", "Enchanted lyre", "Ardougne cloak"));
    }

    private static List<String> positives(String... itemNames)
    {
        return Arrays.asList(itemNames);
    }

    private static List<String> negatives(String... itemNames)
    {
        return Arrays.asList(itemNames);
    }

    private static void assertQueryPack(String query, List<String> expectedItems, List<String> unexpectedItems)
    {
        StorageIndex index = new StorageIndex();
        int itemId = 10_000;
        for (String itemName : expectedItems)
        {
            index.record(itemId++, itemName, 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        }
        for (String itemName : unexpectedItems)
        {
            index.record(itemId++, itemName, 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        }

        String resultNames = names(new SemanticSearchEngine(SemanticLibrary.create()).search(query, index));
        for (String expectedItem : expectedItems)
        {
            assertTrue(query + " should include " + expectedItem + " in:\n" + resultNames, resultNames.contains(expectedItem));
        }
        for (String unexpectedItem : unexpectedItems)
        {
            assertFalse(query + " should exclude " + unexpectedItem + " from:\n" + resultNames, resultNames.contains(unexpectedItem));
        }
    }

    private static String names(List<SemanticSearchResult> results)
    {
        StringBuilder builder = new StringBuilder();
        for (SemanticSearchResult result : results)
        {
            builder.append(result.getItemName()).append("\n");
        }
        return builder.toString();
    }
}
```

- [ ] **Step 2: Run the query pack and verify red**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.CuratedQueryPackTest
```

Expected: the test fails because `barrows trip` and/or `clue step` are not currently recognized aliases.

- [ ] **Step 3: Commit the red query pack**

If the new test fails for the expected missing-query reason, commit it:

```powershell
git add src/test/java/com/semanticbanksearch/CuratedQueryPackTest.java
git commit -m "Add curated query pack tests"
```

---

### Task 2: Add Minimal Phrase Aliases

**Files:**
- Modify: `src/main/java/com/semanticbanksearch/BossRules.java`
- Modify: `src/main/java/com/semanticbanksearch/ClueRules.java`

- [ ] **Step 1: Add Barrows trip phrase alias**

In `BossRules.java`, update the `Crypt prep` alias list from:

```java
SemanticLibrary.aliases("barrows gear", "barrows prep", "barrows setup", "barrows supplies"),
```

to:

```java
SemanticLibrary.aliases("barrows gear", "barrows prep", "barrows setup", "barrows supplies", "barrows trip"),
```

- [ ] **Step 2: Add clue step phrase aliases**

In `ClueRules.java`, update the `Clue utility` alias list from:

```java
SemanticLibrary.aliases("clue stash", "clue stashes", "stash", "items used for clue stashes", "clue tools", "dig clue", "emote clue", "clue supplies"),
```

to:

```java
SemanticLibrary.aliases("clue stash", "clue stashes", "stash", "items used for clue stashes", "clue tools", "clue step", "clue steps", "dig clue", "emote clue", "clue supplies"),
```

- [ ] **Step 3: Run the query pack and verify green**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.CuratedQueryPackTest
```

Expected: `CuratedQueryPackTest` passes.

- [ ] **Step 4: Run existing semantic tests**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: existing semantic tests pass.

- [ ] **Step 5: Commit the alias fixes**

Run:

```powershell
git add src/main/java/com/semanticbanksearch/BossRules.java src/main/java/com/semanticbanksearch/ClueRules.java
git commit -m "Support curated query phrases"
```

---

### Task 3: Final Verification and PR Update

**Files:**
- No file changes expected.

- [ ] **Step 1: Run full verification**

Run:

```powershell
.\gradlew.bat test
.\gradlew.bat compileJava
.\gradlew.bat shadowJar
rg "http|https|Socket|Robot|MenuEntry|setMenuEntries|invokeMenuAction|withdraw|deposit|Runtime|getRuntime|ProcessBuilder|reflection|ClassLoader"
git diff --check
git status --short --branch
```

Expected:

- Gradle commands complete with `BUILD SUCCESSFUL`.
- Safety scan returns no matches.
- `git diff --check` returns no whitespace errors.
- Git status is clean and ahead of origin.

- [ ] **Step 2: Push PR branch**

Run:

```powershell
git push
```

Expected: branch pushes to `origin/semantic-bank-search-implementation`, updating PR #2.
