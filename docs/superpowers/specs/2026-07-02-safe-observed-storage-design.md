# Safe Observed Storage Design

## Goal

Expand Semantic Bank Search from bank-only live observation to safe, read-only observation of clearly identifiable storage containers. Searches and the All Indexed view should show remembered items from sources such as Seed Vault, Group Storage, and supported POH storage without clicking, withdrawing, depositing, modifying menus, inferring contents, or sending data anywhere.

## Current State

The plugin already has a storage-aware data model:

- `StorageIndex` stores items by canonical item ID, source type, source name, visibility, quantity, and last-seen time.
- `StorageSourceType` already includes `BANK`, `POH_STORAGE`, and `OTHER_STORAGE`.
- Search results can represent non-bank sources, and only visible bank results are highlightable.
- The plugin currently populates the index only from `InventoryID.BANK`.

The user-facing setting already says "Remember observed storage", so this feature closes an existing promise gap.

## Chosen Approach

Use tiered safe observed storage.

The plugin should observe only sources that can be named confidently and treated as storage. It should skip temporary or transactional containers even if their item containers are visible. Ambiguous sources should not be guessed.

### Tier 1: Direct Safe Item Containers

These sources are exposed through `InventoryID` and can be read without widget scraping:

- Bank: `InventoryID.BANK`, source type `BANK`, source name `Bank`.
- Seed Vault: `InventoryID.SEED_VAULT`, source type `OTHER_STORAGE`, source name `Seed Vault`.
- Group Storage: `InventoryID.GROUP_STORAGE`, source type `OTHER_STORAGE`, source name `Group Storage`.

Group Storage is shared storage, so results must label it clearly as `Group Storage` rather than implying the item is personally banked.

### Tier 2: Supported POH Storage

POH support should start only where RuneLite exposes a clear storage interface. The local API includes `InterfaceID.POH_TREASURE_CHEST_INV`, so the first POH target should be treasure chest-style storage if its visible widgets expose item IDs and quantities reliably.

Source label:

- `POH Treasure Chest`, source type `POH_STORAGE`.

If the widget item extraction is not reliable in tests or local inspection, POH support should be deferred rather than guessed.

### Explicitly Ignored Sources

Do not index:

- Trade screens.
- Shops.
- Inventory or equipment.
- Reward chests.
- Loot chests.
- Temporary minigame containers.
- Deposit box inventory.
- Any container whose source cannot be identified confidently.

This avoids polluting the index with items the player may not actually own or may not be able to retrieve later.

## Architecture

Add a small observation layer instead of growing `SemanticBankSearchPlugin` directly.

### `ObservedStorageSource`

A small value object describing a source:

- source type
- source name
- optional `InventoryID`
- optional widget/interface descriptor

This keeps source naming and safety classification out of the main plugin loop.

### `ObservedStorageScanner`

A focused scanner that turns visible storage sources into `ObservedItem` lists.

Responsibilities:

- Read direct `InventoryID` containers.
- Canonicalize item IDs with `ItemManager`.
- Resolve item names with the same item-name logic currently used by bank observation.
- Ignore invalid item IDs, empty names, and non-positive quantities.
- Return visible items grouped by source.

It must not:

- Click widgets.
- Open containers.
- Infer unavailable contents.
- Read trade/shop/reward containers.
- Change menus or game state.

### Plugin Integration

`SemanticBankSearchPlugin` should keep ownership of lifecycle, persistence, panel refresh, and overlay updates.

On each game tick, when `rememberObservedStorage` is enabled:

1. Observe the bank if open.
2. Observe safe non-bank storage sources when their interfaces or item containers are visible.
3. Mark a source not visible when it was visible in the previous scan but no longer appears.
4. Persist on the existing interval.
5. Refresh the active search or All Indexed view.

The current bank behavior must remain unchanged.

## Visibility And Highlighting

Bank items:

- Visible while the bank is open.
- Highlightable when the current search matches them and highlights are enabled.

Non-bank storage items:

- Searchable after observation.
- Listed in Search and All Indexed with source and quantity.
- Not highlightable, even if the storage UI is visible.

This avoids promising overlays on widgets the plugin has not been designed to highlight safely.

## User Experience

Search results should continue to show source and quantity. Non-bank remembered items should naturally appear as:

- `Source Seed Vault | Quantity 42 | remembered`
- `Source Group Storage | Quantity 1 | remembered`
- `Source POH Treasure Chest | Quantity 1 | remembered`

All Indexed should sort visible bank items first, then remembered storage items by item name and source, as it does today.

No new settings are required for the first version. The existing `Remember observed storage` setting should control bank and non-bank observation together.

## Testing

Add focused unit tests for the new scanner and existing index behavior:

- Direct storage container scanning records valid items with the correct source name and source type.
- Invalid item IDs, empty names, and zero quantities are ignored.
- Bank observation remains highlightable only for visible bank items.
- Non-bank observed items are searchable but not highlightable.
- Closing or losing visibility for a non-bank source marks that source's items remembered, not visible.
- Safe source allow-list excludes trade, shops, reward chests, loot chests, inventory, and equipment.
- Serialization round trips non-bank sources.

Plugin-level tests should cover source visibility transitions without requiring live game actions.

## Safety

This feature remains local-only and passive:

- No runtime network calls.
- No external AI calls.
- No clicks.
- No withdraws or deposits.
- No item moves.
- No menu modification.
- No inference from unopened storage.
- No storage writes beyond the existing local RuneLite config serialization.

## Out Of Scope

- Highlighting non-bank storage widgets.
- Full POH costume room support if widget item extraction is not reliable.
- Searching unopened storage.
- Importing external item databases.
- Manual source editing.
- Syncing storage between accounts or machines.

## Open Risk

RuneLite may expose some storage UIs as widgets rather than `InventoryID` containers. The implementation should verify widget extraction before claiming support for each POH source. If POH treasure chest support is uncertain, ship Seed Vault and Group Storage first and leave POH as a documented follow-up.
