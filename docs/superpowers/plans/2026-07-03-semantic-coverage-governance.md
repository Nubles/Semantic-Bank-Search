# Semantic Coverage Governance Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add public contributor guidance for safely expanding the local static semantic database.

**Architecture:** This is a documentation-only change. Add a public `docs/SEMANTIC_COVERAGE.md` guide and link it from `README.md`; do not change Java source, runtime behavior, storage behavior, UI, or semantic rules.

**Tech Stack:** Markdown documentation, Git, existing Gradle/JUnit verification where available.

---

## File Structure

- Create `docs/SEMANTIC_COVERAGE.md`: public contributor guide for semantic coverage changes.
- Modify `README.md`: add one link to the new semantic coverage guide near the semantic database description.

---

### Task 1: Add Semantic Coverage Guide

**Files:**
- Create: `docs/SEMANTIC_COVERAGE.md`

- [ ] **Step 1: Create the contributor guide**

Create `docs/SEMANTIC_COVERAGE.md` with this content:

```markdown
# Semantic Coverage Guide

Semantic Bank Search uses a local static semantic database to map player-style searches to observed owned items. The database is intentionally approximate: it helps players find likely useful items, but it does not claim complete OSRS item coverage.

The most important rule is simple: add examples before adding rules. A useful semantic change should start with a player query, expected matches, and expected non-matches.

## What This Database Is

- Static Java rule data bundled with the plugin.
- Local-only and read-only.
- Built from curated OSRS item knowledge, not runtime web lookups.
- Approximate by design, because many item purposes are context-dependent.

The database should never require external AI, network calls, downloaded data, account data, or bank contents leaving the client.

## Safe Coverage Workflow

1. Pick a real player query, such as `barrows trip` or `herb run`.
2. Add or update a curated query-pack case with expected positive items.
3. Add expected negative items that are nearby but wrong.
4. Run the query-pack test and confirm it fails for the missing coverage.
5. Add the smallest phrase-specific rule or alias needed.
6. Run focused semantic tests and the full test suite.
7. Check that the change did not add unsafe APIs or behavior.

Prefer one narrow improvement over a broad rule that looks useful but catches unrelated items.

## Writing Query Pack Cases

Curated query-pack cases live in `src/test/java/com/semanticbanksearch/CuratedQueryPackTest.java`.

Each query should include:

- At least two positive items that should appear.
- At least two negative items that should not appear.
- Item names that resemble real bank contents.
- Near-miss negatives where possible, not only obviously unrelated items.

Example:

```java
assertQueryPack(
    "barrows trip",
    positives("Barrows teleport", "Spade", "Prayer potion(4)", "Trident of the seas"),
    negatives("Shantay pass", "Dragon scimitar", "Raw shark"));
```

Positive examples define the user promise. Negative examples protect that promise from broad matching drift.

## Adding Rule Aliases

Aliases should sound like things a player would type.

Prefer:

```java
SemanticLibrary.aliases("barrows trip", "barrows prep", "barrows supplies")
```

Avoid broad single-word aliases unless they are already covered by regression tests and clearly intentional.

Good aliases are usually short phrases:

- `fossil island travel`
- `fairy ring access`
- `clue step`
- `dragon slayer task`
- `wintertodt supplies`

## Adding Item Patterns

Item patterns should be specific enough to avoid accidental matches.

Prefer exact or near-exact item phrases:

- `dramen staff`
- `ring of wealth`
- `anti-dragon shield`
- `steel bar`
- `barrows teleport`

Avoid generic substrings unless they are deliberately broad and protected by tests:

- `staff`
- `ring`
- `axe`
- `bar`
- `teleport`
- `shield`

If a broad pattern is necessary, add negative tests for common false positives.

## Terms To Treat Carefully

These terms often create category bleed:

- `travel`
- `teleport`
- `ring`
- `staff`
- `axe`
- `bar`
- `tool`
- `utility`
- `food`
- `shield`

Before adding one of these as an alias or item pattern, add a negative test showing what must not match.

## Safety Boundaries

Semantic coverage changes must not add:

- Network calls.
- External AI or API calls.
- Runtime downloads.
- Menu modification.
- Item clicking, moving, withdrawing, or depositing.
- Reflection or classloader behavior.
- Process execution.
- Storage inference for containers the client has not observed.

The plugin should remain advisory: it lists and highlights matching observed items, but never acts on them.

## Verification

For semantic coverage changes, run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.CuratedQueryPackTest
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
.\gradlew.bat test
```

Before pushing, also run:

```powershell
rg "http|https|Socket|Robot|MenuEntry|setMenuEntries|invokeMenuAction|withdraw|deposit|Runtime|getRuntime|ProcessBuilder|reflection|ClassLoader"
git diff --check
git status --short --branch
```

The safety scan should return no matches from new source changes. If it returns matches from documentation, build files, or unchanged code, explain why they are not new runtime safety risks.
```

- [ ] **Step 2: Review the guide for public tone**

Read `docs/SEMANTIC_COVERAGE.md` and confirm:

- It speaks to contributors, not internal agents.
- It keeps local-only/read-only safety prominent.
- It does not claim complete OSRS coverage.
- It includes examples for query-pack cases and phrase aliases.

- [ ] **Step 3: Commit the guide**

Run:

```powershell
git add docs/SEMANTIC_COVERAGE.md
git commit -m "Add semantic coverage guide"
```

---

### Task 2: Link Guide From README

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Add README link**

In `README.md`, after the paragraph that starts `The bundled semantic database is local static data.`, add:

```markdown
See [Semantic Coverage Guide](docs/SEMANTIC_COVERAGE.md) for how to add new semantic coverage safely.
```

- [ ] **Step 2: Verify README wording**

Confirm the README still says:

```markdown
The plugin is local-only.
```

and still says:

```markdown
It does not click, withdraw, deposit, move, tag, modify menus, use external services, or send bank contents anywhere.
```

- [ ] **Step 3: Commit README link**

Run:

```powershell
git add README.md
git commit -m "Link semantic coverage guide"
```

---

### Task 3: Verification and PR Update

**Files:**
- No file changes expected.

- [ ] **Step 1: Run documentation checks**

Run:

```powershell
git diff --check
rg "http|https|Socket|Robot|MenuEntry|setMenuEntries|invokeMenuAction|withdraw|deposit|Runtime|getRuntime|ProcessBuilder|reflection|ClassLoader"
git status --short --branch
```

Expected:

- `git diff --check` exits successfully.
- Safety scan reports only expected documentation mentions, or no matches.
- Git status is clean and ahead of origin.

- [ ] **Step 2: Run focused query-pack test if execution is available**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.CuratedQueryPackTest
```

Expected: `BUILD SUCCESSFUL`.

If the local usage limit blocks Gradle execution, do not work around it. Report that the change is documentation-only and explain which non-Gradle checks passed.

- [ ] **Step 3: Push PR branch**

Run:

```powershell
git push
```

Expected: `origin/semantic-bank-search-implementation` updates PR #2.
