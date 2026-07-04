# Semantic Coverage Guide

Semantic Bank Search uses a local static semantic database to map player-style searches to observed owned items. The database is intentionally approximate: it helps players find likely useful items, but it does not claim complete OSRS item coverage.

The most important rule is simple: add examples before adding rules. A useful semantic change should start with a player query, expected matches, and expected non-matches.

## What This Database Is

- Static Java rule data bundled with the plugin.
- Local-only and read-only.
- Built from curated OSRS item knowledge, not runtime web lookups.
- Approximate by design, because many item purposes are context-dependent.

The database should never require external AI, network calls, downloaded data, account data, or bank contents leaving the client.

## Wiki-Guided Curation

OSRS Wiki strategy and activity pages are good offline references for turning common player recommendations into purpose-based search behavior. Use them as curation input, then paraphrase the useful item purposes into local static rules and tests.

Good wiki-guided additions look like:

- `vorkath trip` finds dragonfire protection, salve items, ranged weapons, enchanted bolts, and sustain.
- `zulrah trip` finds venom protection, magic/ranged switch items, Zulrah travel, and sustain.
- `fight caves trip` finds prayer restoration, ranged weapons/ammunition, brews, and long-trip sustain.
- `wildy boss trip` finds risk-light supplies, escape teleports, blighted supplies, and freeze support.

Do not copy full guide tables into the plugin. Do not add runtime wiki/API lookups. The goal is not to become a boss guide; it is to answer "which observed items I already own match this purpose?"

## Safe Coverage Workflow

1. Pick a real player query, such as `barrows trip` or `herb run`.
2. Add or update a curated query-pack case with expected positive items.
3. Add expected negative items that are nearby but wrong.
4. Run the query-pack test and confirm it fails for the missing coverage.
5. Add the smallest phrase-specific rule or alias needed.
6. Run focused semantic tests and the full test suite.
7. Check that the change did not add unsafe APIs or behavior.

Prefer one narrow improvement over a broad rule that looks useful but catches unrelated items.


## Readiness Packs

Readiness packs are curated task checklists built on top of semantic search. A pack has aliases such as `barrows trip`, then several slots such as required teleports, food, tools, or optional upgrades. Each slot runs a normal local semantic search against observed storage.

Use readiness packs when a query needs a grouped answer:

- What do I already own for this trip?
- Which required slots are missing?
- Which observed items are acceptable substitutes?

Keep packs compact. They should help a player prepare from owned items, not become full activity guides. Missing slots mean "not found in observed storage," not "the account definitely does not own this item."

Current starter packs cover Barrows trip, herb run, Wildy escape, and clue step readiness.

## Coverage Audit View

Use the plugin's `Coverage` view before expanding a category. It shows every observed item that currently has semantic coverage and every observed item that is still uncovered.

Covered rows show the matched categories and reasons. Uncovered rows are the best source of future coverage work, because they come from real observed bank/storage contents instead of guesses.

The audit view is passive. It does not highlight bank items, infer unobserved storage, or change item/menu behavior.

## Semantic QA Scorecard

Use the developer QA scorecard when changing query behavior. It evaluates curated player-style queries against expected positives and expected negatives, then reports scenario counts, missing positives, and false positives.

See [Semantic QA Scorecard](SEMANTIC_QA_SCORECARD.md) for the current scorecard snapshot and contributor workflow.
## Coverage Benchmarks

Coverage benchmarks live in `src/test/java/com/semanticbanksearch/SemanticCoverageBenchmarkTest.java`.

They use small fixture banks that look like real account tabs: midgame utility, ironman skilling, PvM/slayer, and clue utility. Each fixture protects a minimum coverage count and a few intentionally uncovered items. Some useful uncovered items are kept explicit so future category work has a concrete target list.

Use benchmark failures as a triage list. If a useful item appears uncovered, add a player query, expected positives, and expected negatives before widening rules. If an intentionally uncovered item starts matching, check for a broad pattern that may be causing category bleed.

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

## Fuzzy Search

Search tolerates common spelling issues in two conservative places: semantic query aliases and item-name fallback. It does not fuzzy-match every item pattern inside every semantic rule.

Fuzzy matching should remain cautious:

- Short tokens should stay exact or prefix-based.
- Medium and long tokens may allow small edit-distance mistakes.
- Abbreviations such as `ppot`, `dhcb`, `dwh`, and `bgs` should be handled as aliases, not guessed through fuzzy distance.
- Add expected negatives for risky near-matches before widening fuzzy behavior.

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
.\gradlew.bat test --tests com.semanticbanksearch.ReadinessAnalyzerTest
.\gradlew.bat test
```

Before pushing, also run:

```powershell
rg "http|https|Socket|Robot|MenuEntry|setMenuEntries|invokeMenuAction|withdraw|deposit|Runtime|getRuntime|ProcessBuilder|reflection|ClassLoader"
git diff --check
git status --short --branch
```

The safety scan should return no matches from new source changes. If it returns matches from documentation, build files, or unchanged code, explain why they are not new runtime safety risks.
