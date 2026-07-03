# Curated Query Pack Design

## Goal

Add a repeatable curated query pack that tests real player-style searches against expected positive and negative item matches.

This is a quality layer for the static semantic database. It should make future category expansion safer by proving useful queries keep finding the right owned items without drifting into broad false positives.

## Problem

Semantic Bank Search now has broad local coverage across teleports, combat, potions, food, skilling, minigames, slayer, clues, travel, and utility. The main risk is no longer whether the plugin can match something; it is whether new rules keep matching the right things as coverage grows.

Individual unit tests already protect specific rules and regressions, but the project also needs a higher-level test suite that reads like common player intent:

- barrows trip
- clue step
- herb run
- wildy escape
- birdhouse run
- wintertodt supplies
- tempoross supplies
- dragon slayer task
- desert travel
- fossil island travel

Each query should define items that must be returned and items that must not be returned.

## Approach

Create a dedicated `CuratedQueryPackTest` in `src/test/java/com/semanticbanksearch`.

The test class will use a compact helper that:

1. Builds a `StorageIndex` containing all positive and negative fixture items for a query.
2. Runs `new SemanticSearchEngine(SemanticLibrary.create()).search(query, index)`.
3. Asserts every expected positive item appears.
4. Asserts every expected negative item does not appear.

The helper should keep test rows readable. A query pack entry should be easy to scan without understanding the engine internals.

Example shape:

```java
assertQueryPack(
    "barrows trip",
    positives("Barrows teleport", "Spade", "Prayer potion(4)", "Trident of the seas"),
    negatives("Shantay pass", "Dragon scimitar", "Raw shark"));
```

## Initial Query Pack

The first pack should cover representative searches from existing major categories:

- `barrows trip`: nearby Barrows teleport, spade, prayer restoration, and magic gear; excludes unrelated desert/travel/melee/food fixtures.
- `clue step`: common clue utility such as spade, light source, teleport jewellery, and warm clothing where current rules support it; excludes combat-only fixtures.
- `herb run`: farming tools, compost, herb seeds, and relevant travel helpers; excludes unrelated combat and minigame fixtures.
- `wildy escape`: one-click or wilderness escape teleports; excludes regional travel and combat weapons that should not match escape intent.
- `birdhouse run`: birdhouses, clockwork, logs, seeds, and Fossil Island travel; excludes fletching-only fixtures where possible.
- `wintertodt supplies`: warm clothing, axe, tinderbox, hammer, and food; excludes other minigame supplies.
- `tempoross supplies`: fishing tools and Tempoross utility; excludes combat and unrelated skilling supplies.
- `dragon slayer task`: dragon protection, ranged/melee options, prayer restoration, and slayer helmet; excludes non-dragon slayer fixtures.
- `desert travel`: desert access and protection items; excludes Barrows and unrelated regional travel.
- `fossil island travel`: Digsite pendant and Fossil Island support items; excludes unrelated regional travel.

The initial implementation may adjust exact positives to match existing local coverage, but each query must include at least two positives and at least two negatives.

## Rule Changes

The first implementation should add tests before changing production rules.

If a query pack row fails because coverage is genuinely missing, make the smallest phrase-specific semantic rule update needed to satisfy that row.

Rules must avoid broad single-token aliases or patterns that commonly cause bleed, especially:

- `travel`
- `teleport`
- `ring`
- `staff`
- `axe`
- `bar`
- `tool`
- `utility`

Existing legacy rules may keep broad terms where already covered by regression tests, but new query-pack-driven changes should prefer explicit phrases.

## Data Flow

The query pack remains fully local and test-only:

1. Test creates an in-memory `StorageIndex`.
2. Test records synthetic observed items.
3. Semantic search runs against the static `SemanticLibrary`.
4. Assertions compare returned item names.

No runtime plugin storage behavior changes are required.

## Testing

Add `CuratedQueryPackTest` and run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.CuratedQueryPackTest
.\gradlew.bat test
```

Expected outcome:

- Query pack tests pass.
- Existing tests keep passing.
- No new unsafe behavior appears in safety scans.

## Out of Scope

- Importing OSRS Wiki data.
- Runtime network calls or external AI.
- UI changes.
- Ranking assertions beyond positive/negative inclusion.
- Claiming complete coverage for every item in the game.

## Future Expansion

After the first pack lands, grow coverage category by category:

1. Boss trips
2. Clues
3. Skilling runs
4. Slayer tasks
5. Minigames
6. Regional travel
7. Utility and protection

Each category should add curated query rows first, then semantic rule changes only where the tests expose useful missing coverage.
