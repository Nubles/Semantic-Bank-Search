# Semantic Bank Search

Semantic Bank Search is a **beta** RuneLite external plugin that lets players search observed owned items by purpose instead of exact item name.

It is local-only and passive. It searches items the client has observed, shows matching results, and highlights matching visible bank items. It does not use external AI, call web services, click items, withdraw items, deposit items, or modify menus.

## Beta Status

This beta is suitable for early users who understand that semantic coverage is curated and still growing. The plugin is designed to be safe first, useful second, and comprehensive over time.

Known limitations:

- Search and Readiness only know about items observed in the bank or safe remembered storage.
- Missing Readiness slots mean "not observed," not "definitely not owned."
- Purpose coverage is broad enough for common utility, travel, skilling, Slayer, clue, and boss-prep searches, but it is not complete OSRS item coverage.
- Readiness packs are compact prep checklists, not full boss/activity guides.
- POH storage is not inferred.
- Screenshots and real-bank QA should be completed before calling this a 1.0 release.

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
- prayr restoraton
- barows teleprt

## Views

- **Search** finds observed owned items by purpose or item-name fallback.
- **All** lists every item the plugin has observed locally, so players can check what the plugin currently knows about their bank/storage.
- **Readiness** checks a supported trip or task query and groups observed items into owned and missing preparation slots.
- **Coverage** audits observed items and groups them into semantic-covered, mechanically known, known unclassified, and unknown observed items.
- **Clear** resets the current panel results and bank highlights.

Result summaries show match counts, observed item counts, semantic coverage totals, or required Readiness slot coverage so players can quickly tell what the plugin found.

Search is typo-tolerant for common misspellings. Fuzzy matching is conservative: it helps with longer misspelled purpose words and item names, while short risky words such as `axe`, `bar`, `bow`, `law`, and `ring` stay mostly exact to avoid noisy results.

## Readiness Mode

Readiness mode turns a supported purpose query into a lightweight prep checklist. Instead of only listing matching items, it shows owned slots, missing slots, and the best observed substitutes for that task.

Supported beta readiness packs:

- barrows trip
- vorkath trip
- zulrah trip
- fight caves
- herb run
- birdhouse run
- farm contract
- wildy escape
- wildy boss
- dagannoth kings
- slayer task
- clue step
- quest tools
- wintertodt

Readiness uses the same observed-storage limits as search. If an item has not been observed in the bank or a safe remembered storage source, the plugin treats that slot as missing. It remains advisory only: it highlights owned visible bank items, but does not click, withdraw, deposit, or change menus.

## Semantic Coverage

The bundled semantic database is static data shipped with the plugin. It covers common teleports, potions, food, combat equipment, boss prep, slayer prep, tools, skilling supplies, skilling workflows, minigame prep, clue utility, quest/diary utility, travel, and protection items. Coverage also applies conservative mechanical tags to recognized observed items so non-semantic gaps are easier to triage.

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

## Contributing And QA

Contributor workflow, semantic quality checks, and beta release QA live in [Contributing](CONTRIBUTING.md). The detailed manual beta checklist lives in [Beta Release Checklist](docs/BETA_RELEASE_CHECKLIST.md).
