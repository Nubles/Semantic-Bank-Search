# All Indexed Items and Expanded Semantics Design

## Summary

This feature extends Semantic Bank Search with an "All Indexed" panel view and a broader bundled semantic database. The goal is to make it clear what the plugin has observed in the player's bank/storage, then improve purpose-based searches across more common OSRS item functions while remaining local-only and Plugin Hub-friendly.

## Goals

- Add an "All Indexed" view that lists every observed item in `StorageIndex`.
- Show item name, quantity, source, visibility/highlightability state, and last-observed ordering where useful.
- Keep all indexed item data local to RuneLite config storage.
- Expand the static semantic library beyond the starter rules into broader maintainable rule groups.
- Preserve the current search/highlight behavior: searches can highlight matching currently visible bank items; the all-items view is informational and does not auto-highlight the entire bank.
- Keep the implementation testable with pure Java tests for index listing, rule coverage, and representative search behavior.

## Non-Goals

- No runtime OSRS Wiki/API calls.
- No external AI/model calls.
- No claim to perfectly classify every OSRS item in the first expansion.
- No auto-clicking, withdrawing, depositing, tagging, menu mutation, or bank search text insertion.
- No live POH/storage scraping beyond the existing observed-storage model.

## User Experience

The side panel gains a small mode row near the search controls:

- `Search`
- `All Indexed`
- `Clear`

`Search` is the existing behavior. The player types a purpose or item name, presses Enter, and sees matching result cards.

`All Indexed` renders every item currently known to the local storage index. Items are grouped by source where practical, sorted with visible bank items first and then alphabetically by item name. Each item card shows:

- item name
- quantity
- source name, such as `Bank`
- whether it is currently visible/highlightable

If no items have been observed yet, the view explains that opening the bank lets the plugin index visible bank contents.

The all-items view does not highlight every item by default. This avoids turning the bank into a wall of highlights and keeps highlighting tied to intent searches.

## Architecture

### StorageIndex

`StorageIndex` already exposes immutable snapshots through `items()`. Add a convenience query such as `itemsByDisplayOrder()` only if it meaningfully keeps sorting logic out of the panel/plugin. Otherwise the plugin can sort the snapshots before passing them to the panel.

The storage model remains unchanged: entries are observed item/source snapshots, and bank visibility remains transient.

### SemanticBankSearchPanel

Add a mode/action for `All Indexed`. The panel should stay independent of RuneLite widgets and should not access `StorageIndex` directly.

New panel API:

- `updateIndexedItems(List<ObservedItem> items, String status)`

The plugin remains responsible for reading `StorageIndex`, sorting items, and passing snapshots to the panel.

### SemanticBankSearchPlugin

Add callback wiring for the all-indexed mode:

- `showIndexedItems()`

When invoked, it clears search highlights, reads `index.items()`, sorts visible bank items first and then item name, and calls `panel.updateIndexedItems(...)`.

When the bank is observed or closed, if the panel is currently in all-indexed mode, refresh the list so visible/highlightable labels stay current. This can be tracked with a simple enum or string state in the plugin, such as `PanelMode.SEARCH` and `PanelMode.ALL_INDEXED`.

### Semantic Library

Keep `SemanticLibrary.create()` as the single public entry point, but split the expanded local rules into focused package-private helpers:

- `TeleportRules`
- `CombatRules`
- `PotionRules`
- `FoodRules`
- `ToolRules`
- `SkillingRules`
- `ClueRules`
- `ProtectionRules`

Each helper returns `List<SemanticRule>`. `SemanticLibrary.create()` concatenates the lists in a deliberate priority order. This keeps the data readable and prevents one large class from becoming hard to review.

## Initial Expanded Rule Coverage

### Teleports

Add name-pattern coverage for:

- teleport tablets and scrolls
- enchanted jewellery teleports
- charged jewellery variants by base name
- common destination phrases such as Varrock, Falador, Camelot, Ardougne, Watchtower, House, Barrows, Morytania, Wilderness, Revenant cave, Slayer ring

### Combat

Add broader patterns for:

- slash, stab, crush style weapons
- ranged weapons and ammunition
- magic staves, wands, tridents, tomes, and runes
- shields, defenders, and basic defensive gear names

### Potions and Protection

Add broader patterns for:

- prayer restoration
- stat restoration
- combat boosts
- ranged/magic boosts
- stamina/run restoration
- poison/venom protection
- antifire and anti-dragon protection

### Food

Expand food tiers with common fish, cooked foods, combo foods, and high-heal foods. Ranking remains approximate and local; it should be good enough to answer "fastest food I own" with stronger foods first.

### Tools and Utility

Add broader patterns for:

- axes, pickaxes, harpoons, fishing rods, nets, cages
- knife, chisel, hammer, saw, spade, rake, seed dibber, secateurs
- rope, light sources, tinderbox, lockpick, pestle and mortar

### Skilling

Add query aliases and patterns for:

- farming supplies
- herblore supplies
- crafting supplies
- smithing supplies
- fishing supplies
- cooking supplies
- firemaking supplies
- mining and woodcutting tools

### Clues

Expand clue utility patterns for:

- stash-building tools
- spade, rope, light source, sextant/watch/chart style clue tools
- clue hunter outfit names and other reliable name-based clue utility items

## Data Quality Rules

- Prefer stable name patterns over brittle exact item IDs unless item ID use is clearly warranted.
- Avoid overclaiming semantics for ambiguous names.
- Keep category reasons human-readable and short.
- Add tests for representative searches instead of trying to test every pattern.
- If a rule is likely to cause noisy false positives, leave it out for this pass.

## Error Handling

- All Indexed with no items shows an empty state.
- All Indexed with many items should remain usable in the existing scroll pane.
- Null item lists are treated as empty.
- Search behavior remains unchanged for blank queries and no results.

## Testing

Add tests for:

- `StorageIndex` or plugin-facing sorting helper, if extracted.
- Panel-level behavior only if practical without brittle Swing assertions; otherwise compile verification is acceptable for UI rendering changes.
- `SemanticLibrary.create()` includes expanded rule groups and does not return an empty list.
- Representative semantic searches:
  - `teleport jewellery`
  - `stamina`
  - `antifire`
  - `ranged ammo`
  - `magic runes`
  - `farming tools`
  - `fishing tools`
  - `light source`
  - `clue tools`
- All searches only return observed/indexed owned items.
- Existing MVP searches continue to pass.

Manual verification should include opening a bank, checking that All Indexed lists observed bank items, closing the bank, confirming bank entries remain listed but are no longer marked highlightable, and running several expanded semantic searches.

## Acceptance Criteria

- The panel has an All Indexed view.
- The All Indexed view lists every observed item snapshot passed by the plugin.
- All Indexed does not auto-highlight all bank items.
- The plugin refreshes All Indexed when bank visibility changes.
- The semantic database is split into focused local rule helpers.
- Expanded semantic searches work for representative teleport, potion, combat, tool, skilling, clue, and protection queries.
- Tests and build pass.
- No runtime network calls, automation, menu mutation, or external services are added.
