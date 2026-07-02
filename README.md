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

The bundled semantic database is local static data. It covers common teleports, potions, food, combat equipment, boss prep, slayer prep, tools, skilling supplies, clue utility, quest/diary utility, travel, and protection items. It is intentionally approximate and avoids runtime network calls.

The plugin is local-only. It reads visible bank items, remembers observed storage locally, and highlights matching visible bank items. Remembered storage only includes items the client has observed; it cannot infer items from tabs, accounts, or storage containers that have not been opened in RuneLite.

It does not click, withdraw, deposit, move, tag, modify menus, use external services, or send bank contents anywhere.
