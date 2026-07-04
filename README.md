# Semantic Bank Search

A RuneLite external plugin that lets players search observed owned items by purpose instead of exact item name.

Semantic Bank Search is local-only and passive. It searches items the client has observed, shows matching results, and highlights matching visible bank items. It does not use external AI, call web services, or act on items.

## Example Searches

- teleport near barrows
- crush weapons
- poison protection
- prayer restoration
- items used for clue stashes
- warm clothing
- things that cut webs
- fastest food I own
- vorkath trip
- zulrah trip
- wildy escape

## Views

- **Search** finds observed owned items by purpose or item-name fallback.
- **All** lists every item the plugin has observed locally, so players can check what the plugin currently knows about their bank/storage.
- **Coverage** audits observed items and groups them into uncovered and covered semantic items.
- **Clear** resets the current panel results and bank highlights.

Result summaries show match counts, observed item counts, or semantic coverage totals so players can quickly tell what the plugin found.

## Semantic Coverage

The bundled semantic database is static data shipped with the plugin. It covers common teleports, potions, food, combat equipment, boss prep, slayer prep, tools, skilling supplies, skilling workflows, minigame prep, clue utility, quest/diary utility, travel, and protection items.

The static semantic pack also covers regional travel and utility searches such as Kourend/Zeah travel, Fossil Island travel, fairy ring access, Fremennik travel, light sources, ghostspeak items, dig clue tools, and desert protection.

Boss-trip curation includes wiki-guided, locally bundled purpose coverage for examples such as Vorkath, Zulrah, Fight Caves, Barrows, and Wilderness boss prep. Wiki guidance is used only while developing the static rule database; the plugin does not fetch wiki content at runtime.

## Observed Storage

The plugin reads visible bank items plus safe observed storage containers such as Seed Vault and Group Storage. Remembered storage only includes items the client has observed.

It cannot infer items from tabs, accounts, or storage containers that have not been opened in RuneLite. POH storage is not inferred; only explicitly safe RuneLite-exposed inventories are remembered.

## Safety

Semantic Bank Search does not:

- click, withdraw, deposit, move, or tag items
- modify menus or invoke menu actions
- use external services or runtime downloads
- send bank contents anywhere
- infer unobserved storage contents

## Quality Workflow

Semantic coverage is protected by repeatable tests and scorecards:

- [Semantic Coverage Guide](docs/SEMANTIC_COVERAGE.md) explains how to add new purpose coverage safely.
- [Semantic QA Scorecard](docs/SEMANTIC_QA_SCORECARD.md) tracks curated player queries, expected positives, expected negatives, and current pass totals.
- [Contributing](CONTRIBUTING.md) covers development, testing, and safety expectations.

For semantic changes, run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticQaScorecardTest
.\gradlew.bat test --tests com.semanticbanksearch.CuratedQueryPackTest
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
.\gradlew.bat test
```
