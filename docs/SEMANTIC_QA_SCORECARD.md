# Semantic QA Scorecard

Semantic Bank Search uses a developer-only QA scorecard to keep semantic search quality measurable as the static rule database grows.

The scorecard is not part of the RuneLite runtime UI. It is a test/reporting tool for contributors, and it keeps the plugin's runtime promise intact: local-only, passive, and based on bundled static rules.

## Current Scorecard

- Scenarios: 16/16 passing
- Expected positives found: 62/62
- Expected negatives avoided: 44/44
- Runtime network calls: none
- Native bank beta queries: 13 player-facing query scenarios passing.
- Native compatibility/performance: pass-through and 1,000-item workload gates passing.

## What It Measures

Each scorecard case contains:

- A player-style query, such as `vorkath trip` or `herb run`.
- Expected positives that should appear in results.
- Expected negatives that should not appear in results.
- A scenario label for grouping related query coverage.

The scorecard fails when a useful expected item is missing or when a nearby-but-wrong item appears. False positives matter as much as missing positives because players need the search to feel predictable.

## Covered Scenario Groups

The first scorecard includes:

- Core trips and utility: Barrows, clues, herb runs, birdhouse runs.
- Minigames: Wintertodt and Tempoross supplies.
- Travel: wilderness escape, desert travel, Fossil Island travel.
- Slayer: dragon Slayer task prep.
- Wiki-guided boss trips: Vorkath, Zulrah, Fight Caves, and Wilderness boss prep.
- Fuzzy spelling: misspelled semantic purpose queries such as prayer restoration and Vorkath trip prep.

## How To Use It

Run the focused scorecard test before and after semantic rule changes:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticQaScorecardTest
.\gradlew.bat test --tests com.semanticbanksearch.BetaNativeBankQueryPackTest
```
For broader semantic changes, also run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.CuratedQueryPackTest
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
.\gradlew.bat test
```

## Adding New Cases

Add new cases in `src/test/java/com/semanticbanksearch/SemanticQueryCases.java`.

A good case should include:

- One real player query.
- Several expected positives.
- Several expected negatives that are tempting false matches.
- Specific item names that resemble real bank contents.

Prefer adding a failing scorecard case before widening any item pattern or alias. Then make the smallest static rule change that passes the case without weakening nearby negatives.