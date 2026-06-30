# Semantic Bank Search Design

## Summary

Semantic Bank Search is a standalone RuneLite external plugin that lets players search owned items by purpose instead of exact item name. It is local-only, advisory, and non-automating. The first version searches the currently observed bank plus a local index of storage locations the plugin has seen, then highlights matching visible bank items and lists remembered matches with their last observed location.

Example searches the MVP should handle include:

- teleport near barrows
- crush weapons
- poison protection
- prayer restoration
- items used for clue stashes
- warm clothing
- things that cut webs
- fastest food I own

## Goals

- Provide useful purpose-based search without an external AI service or network calls.
- Keep all bank and storage observations local to RuneLite config storage.
- Highlight matching visible bank items without clicking, withdrawing, depositing, tagging, or changing menu entries.
- Build a testable semantic engine that can grow through curated local categories and aliases.
- Represent storage honestly: visible bank matches can be highlighted; remembered storage matches are shown with where and when they were last observed.

## Non-Goals

- No gameplay automation.
- No remote semantic search, model calls, or API keys.
- No automatic item movement, withdrawal, deposit, tagging, or bank search text insertion.
- No claim to know items in storage interfaces that have never been observed by the client.
- No custom player-defined categories in the MVP. The code should leave room for them later.

## User Experience

The plugin adds a "Semantic Bank Search" side panel. The panel contains a search field, starter examples, and a results area. When the player searches, the plugin returns owned matching items grouped by reason, such as "Prayer restoration", "Warm clothing", or "Web cutters".

Each result shows the item name, quantity if known, category reason, confidence label, and source location. Bank results are marked as highlightable when the bank is open. Previously observed storage results are listed with labels such as "Last seen in POH storage" or "Last seen in bank", but are only highlighted if the relevant visible widget item is currently present.

The plugin uses a widget item overlay to draw a subtle fill and border around matching bank items. Highlighting is read-only and can be cleared by clearing the search or selecting a different result set.

## Architecture

### SemanticBankSearchPlugin

The RuneLite plugin entry point wires together config, storage persistence, panel events, bank observation, and overlays. It observes visible bank item widgets when the bank is open, resolves item names through RuneLite item metadata, updates the local storage index, runs searches from the panel, and sends matching item IDs to the overlay.

### StorageIndex

`StorageIndex` stores observed items by canonical item ID and source location. Each entry records item ID, item name, quantity when known, source type, source display name, and last seen timestamp. Bank observation is required for MVP. The model should allow later POH and other storage observers without changing the search engine API.

### SemanticSearchEngine

`SemanticSearchEngine` accepts a query and a `StorageIndex`, normalizes the query, maps it to semantic categories, and ranks owned item matches. The engine is pure Java with no RuneLite dependencies so it can be tested quickly.

Ranking should prefer:

- exact category or alias hits over broad fallback matches
- items currently visible in the bank over older remembered entries
- stronger utility matches for comparative phrases such as "fastest food I own"
- exact or partial item-name matches as a fallback, so normal searches still work

### SemanticLibrary

`SemanticLibrary` provides the starter local rules. Each rule defines category names, query aliases, item-name patterns, optional exact item IDs where practical, and explanatory match text.

MVP categories:

- Teleports and location hints, including a Barrows-focused starter rule.
- Combat styles: crush, slash, stab, ranged, magic.
- Poison and venom protection.
- Prayer, run, health, and stat restoration.
- Food, including a simple healing-per-bite priority list for "fastest food I own".
- Clue and stash utility items.
- Warm clothing.
- Web cutters.
- Common utility tools such as light sources, ropes, spades, and lockpicks.

The library should be deliberately curated rather than exhaustive. Adding rules should be straightforward and covered by tests.

### SemanticSearchResult

Results contain item ID, item name, quantity, source location, category, reason text, score, and whether the item is currently visible and highlightable. The panel uses this object directly, while the overlay receives only the visible matching item IDs.

### SemanticBankSearchPanel

The Swing side panel owns the search text field, example buttons, result rendering, empty states, and clear action. It delegates search execution to the plugin through callbacks and does not know RuneLite widget details.

### SemanticBankSearchOverlay

The overlay extends `WidgetItemOverlay`, shows on the bank, and highlights matching item IDs when highlighting is enabled in config. It must tolerate empty results and missing widget bounds.

## Data Flow

1. Plugin starts and loads the serialized `StorageIndex` from RuneLite config storage.
2. When the bank is open, the plugin observes visible bank items and updates the index.
3. The player enters a purpose-based query in the panel.
4. The plugin asks `SemanticSearchEngine` for matches from the current index.
5. The panel renders grouped result cards.
6. The overlay receives visible bank item IDs for highlighting.
7. The index is periodically saved locally and saved during shutdown.

## Error Handling

- Empty queries clear highlights and show starter examples.
- No matches show a helpful empty state without modifying the bank UI.
- Missing item metadata falls back to the widget item name if available, or skips the item if no useful name can be resolved.
- Corrupt stored index data is ignored and replaced with an empty index.
- If the bank is closed, searches still run against remembered storage but the panel explains that opening the bank enables highlights.

## Configuration

Initial config options:

- Enable item highlights: default on.
- Remember observed storage: default on.
- Maximum remembered entries or age: conservative default to avoid unbounded config growth.

## Privacy and Rule Posture

The plugin is intentionally local-only. It does not send bank contents, queries, item lists, or storage observations anywhere. It does not use reflection, native code, subprocesses, runtime-downloaded code, external services, input injection, menu modification, or gameplay automation.

## Testing

Unit tests should cover the pure semantic engine and storage model before RuneLite wiring:

- Query normalization maps aliases and loose phrasing to expected categories.
- "prayer restoration" finds prayer potions and similar owned items.
- "warm clothing" finds owned warm items and excludes unrelated clothing.
- "things that cut webs" finds knives and slash-capable tools.
- "crush weapons" finds owned crush weapons and excludes non-crush items.
- "fastest food I own" ranks stronger food ahead of weaker food.
- Exact and partial item-name fallback works when no semantic category matches.
- Search only returns items present in the observed storage index.
- Results preserve source labels for bank and remembered storage.
- Corrupt or empty stored index data falls back safely.

Manual verification should include running the RuneLite dev task, opening a bank, observing item highlights, searching with the example queries, closing the bank, and confirming remembered results still appear without visible highlights.

## MVP Acceptance Criteria

- A standalone RuneLite plugin project exists for Semantic Bank Search.
- The plugin builds and tests pass.
- The panel supports local purpose-based searches over observed owned items.
- Opening the bank records visible bank items into the local storage index.
- Matching visible bank items can be highlighted.
- Remembered storage matches are labeled by source and do not claim to be visible unless currently observed.
- No network calls or automation behavior are present.
