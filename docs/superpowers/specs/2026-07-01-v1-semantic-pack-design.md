# v1 Semantic Pack Design

## Goal

Make Semantic Bank Search feel substantially more OSRS-aware while staying local-only and safe. The next phase should expand semantic coverage from a starter rule set into a curated v1 pack covering common bank-search intents across combat, bosses, quests, clues, skilling, travel, boosts, protection, and utility.

This phase should improve search quality without adding network calls, external AI, menu actions, withdrawals, deposits, or any behavior that changes game state.

## Recommended Approach

Use a curated static semantic pack in Java for this phase, building on the existing rule-family classes:

- `TeleportRules`
- `CombatRules`
- `PotionRules`
- `FoodRules`
- `ToolRules`
- `SkillingRules`
- `ClueRules`
- `ProtectionRules`

This keeps the implementation easy to review and close to the current code. A later phase can move the pack to checked-in JSON or YAML if the Java rule files become too large.

## Scope

### Add High-Value Semantic Coverage

Add representative rules for these areas:

- Boss prep: Barrows, Zulrah, Vorkath, God Wars, Fight Caves, Dagannoth Kings, Corporeal Beast, wilderness bosses.
- Slayer prep: undead, demons, dragons, kalphites, lizardmen, wyverns, dust devils, smoke devils, gargoyles, basilisks.
- Quest and diary utility: common transportation, light sources, rope, spade, ghostspeak amulet, pickaxes, axes, anti-poison, heat/cold/desert protection.
- Clue solving: stash materials, coordinate tools, dig tools, light sources, teleports, emote gear families where feasible through broad patterns.
- Skilling boosts and outfits: graceful, graceful alternatives, skilling outfits, farming, herblore, crafting, smithing, mining, woodcutting, fishing, cooking, hunter, construction.
- Magic and spell utility: rune groups, teleport tablets, spellbook support items, elemental staves, alchemy, enchantment, binding/freezing utility.
- Protection and survival: poison, venom, dragonfire, prayer, antifire, antipoison, waterskins/desert, warm clothing, light sources.
- Food and healing: high-heal food, combo food, brews, pies, low-tier food, with better ranking for "best" or "fastest" food queries.

### Improve Ranking

Keep the existing scoring model, but add more explicit score maps where order matters:

- Food: prioritize high-heal/combo food.
- Teleports: prioritize direct destination items over generic transportation.
- Protection: prioritize exact protection items over broad defensive gear.
- Boss/slayer prep: prioritize mandatory counters first, then optional support items.

Do not overfit to a single account type. The goal is useful ordering, not perfect gameplay advice.

### Improve Explanations

Keep each rule reason specific enough to build trust. Examples:

- "Useful for Vorkath trips: dragonfire protection, ranged gear, and supplies."
- "Common clue utility: digging, lighting, navigation, and stash materials."
- "Helps with desert travel or heat/desert damage mitigation."

No new UI is required in this phase, but clearer categories and reasons should make existing result cards more understandable.

## Out of Scope

- Runtime wiki lookup or internet access.
- AI embeddings or hosted model calls.
- Automatic item actions, menu modification, withdrawing, depositing, tagging, or moving items.
- Perfect exhaustive OSRS item intelligence.
- Parsing live quest/diary/slayer state.
- Replacing Java rules with external data files in this phase.

## Architecture

The current architecture remains:

1. `StorageIndex` supplies observed local items.
2. `SemanticSearchEngine` normalizes the query and chooses semantic rules or item-name fallback.
3. Rule-family classes provide local static `SemanticRule` entries.
4. `SemanticBankSearchPanel` displays results and explanations.
5. `SemanticBankSearchOverlay` only highlights visible bank matches.

The main design rule is to grow data inside rule families without making a single giant file. If a family becomes too broad, split it by domain in a later phase, such as `BossRules`, `SlayerRules`, or `QuestRules`.

## Testing Strategy

Add behavior tests before expanding each group. Tests should use owned-item examples and verify both inclusion and exclusion.

Required test themes:

- Boss intent finds key owned supplies and excludes unrelated items.
- Slayer intent finds correct counters and excludes lookalikes.
- Quest/diary utility finds common tools.
- Food ranking still orders high-value food first.
- Teleport queries prioritize exact destination items when owned.
- Generic words such as "weapon", "tool", and "protection" do not cause broad category bleed.

Run targeted `SemanticSearchEngineTest` after each rule group and the full test suite before committing.

## Success Criteria

- At least 40-60 new semantic categories or meaningful expansions land in the v1 pack.
- Existing MVP examples continue to pass.
- New tests cover the highest-risk categories and ranking behavior.
- Search remains local-only and does not introduce unsafe APIs.
- Rule files remain readable enough for future contributors to add entries safely.

## Risks and Mitigations

- False positives from broad patterns: prefer specific item phrases where possible and add exclusion-focused tests for risky categories.
- Category bleed from generic terms: keep generic category words out of implicit category matching and rely on explicit aliases.
- Maintenance burden: keep categories grouped by rule family and avoid duplicating the same rule across multiple files unless the intent differs.
- Incomplete OSRS coverage: ship curated breadth first, then iterate from real search examples and user feedback.