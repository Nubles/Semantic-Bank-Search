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

## Coverage Audit View

Use the plugin's `Coverage` view before expanding a category. It shows every observed item that currently has semantic coverage and every observed item that is still uncovered.

Covered rows show the matched categories and reasons. Uncovered rows are the best source of future coverage work, because they come from real observed bank/storage contents instead of guesses.

The audit view is passive. It does not highlight bank items, infer unobserved storage, or change item/menu behavior.

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
