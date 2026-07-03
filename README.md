# Semantic Bank Search

A RuneLite external plugin that lets players search observed owned items by purpose instead of exact item name.

Example searches:

- teleport near barrows
- crush weapons
- poison protection
- prayer restoration
- items used for clue stashes
- warm clothing
- things that cut webs
- fastest food I own

## Views

- **Search** finds observed owned items by purpose or item-name fallback.
- **All Indexed** lists every item the plugin has observed locally, so players can check what the plugin currently knows about their bank/storage.
- **Coverage** audits observed items and shows which ones already have semantic categories versus items that still need coverage.

The bundled semantic database is local static data. It covers common teleports, potions, food, combat equipment, boss prep, slayer prep, tools, skilling supplies, skilling workflows, minigame prep, clue utility, quest/diary utility, travel, and protection items. The static semantic pack also covers regional travel and utility searches such as Kourend/Zeah travel, Fossil Island travel, fairy ring access, Fremennik travel, light sources, ghostspeak items, dig clue tools, and desert protection. It is intentionally approximate and avoids runtime network calls.

See [Semantic Coverage Guide](docs/SEMANTIC_COVERAGE.md) for how to add new semantic coverage safely.

See [Contributing](CONTRIBUTING.md) for development, testing, and safety expectations.

The plugin is local-only. It reads visible bank items plus safe observed storage containers such as Seed Vault and Group Storage, remembers those observations locally, and highlights matching visible bank items. Remembered storage only includes items the client has observed; it cannot infer items from tabs, accounts, or storage containers that have not been opened in RuneLite. POH storage is not inferred; only explicitly safe RuneLite-exposed inventories are remembered.

It does not click, withdraw, deposit, move, tag, modify menus, use external services, or send bank contents anywhere.
