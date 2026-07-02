# v3 Travel and Utility Coverage Design

## Goal

Expand Semantic Bank Search with a focused local coverage pack for travel and utility searches. The plugin should answer practical queries like `fossil island travel`, `kourend teleport`, `fairy ring items`, `light source`, `ghostspeak`, `dig clue items`, and `desert protection` by showing useful owned observed items.

This phase should improve everyday bank-search coverage without changing the safety model: local-only, read-only, no game actions, and no inferred storage beyond observed items.

## Non-Goals

- No runtime wiki lookup, internet access, hosted AI, or embeddings.
- No clicking, withdrawing, depositing, moving items, menu mutation, or game-state-changing behavior.
- No new storage inference.
- No UI redesign in this pack.
- No perfect exhaustive OSRS destination intelligence.
- No POH storage inference.

## Recommended Approach

Use the existing static Java rule system and add a curated hybrid pack across current rule families:

- Expand `TeleportRules` for regional travel and destination phrases.
- Expand `ToolRules`, `ProtectionRules`, and `ClueRules` for utility searches.
- Add a small new rule family only if the existing files become unclear, such as `UtilityRules` for quest/diary utility.

This keeps the change reviewable and consistent with the existing v1/v2 semantic packs.

## Coverage Scope

### Regional Travel

Add phrase-specific travel rules for common regions and routes:

- Kourend/Zeah: Xeric's talisman, skills necklace, book of the dead, Kharedst's memoirs / Book of the dead style travel names where item patterns are safe.
- Fossil Island: Digsite pendant, mushroom meadow, volcanic mine, numulite-adjacent travel/support where useful.
- Fremennik: enchanted lyre, lunar isle teleport, waterbirth teleport, games necklace, Fremennik sea boots.
- Kandarin/Ardougne: Ardougne cloak, combat bracelet, skills necklace, Camelot teleport, Kandarin headgear where useful.
- Wilderness travel and escape: royal seed pod, burning amulet, games necklace, glory, ring of wealth, teleport crystal where appropriate.
- Fairy ring support: dramen staff, lunar staff, quest cape only if safe as a phrase pattern, plus common fairy-ring-adjacent transport items.

### Utility Searches

Add rules for common non-combat utility intents:

- Light sources: bullseye lantern, lantern, candle lantern, torch, bruma torch, firemaking light-source items.
- Dig/clue utility: spade, sextant, watch, chart, light source, rope.
- Ghostspeak / undead interaction: ghostspeak amulet, cramulet, morytania legs, ectophial where useful.
- Access tools: rope, lockpick, pickaxe, axe, machete, knife, hammer, saw.
- Desert and environmental utility: waterskins, desert amulet, camulet, shantay pass, ice gloves, warm clothing where query-specific.
- Anti-dragon utility: anti-dragon shield, dragonfire shield, antifire potions where query-specific.

### Travel Support

Support intent queries that imply movement help but should not match everything:

- `run energy`, `travel support`, `stamina`, and `movement` should find stamina/energy potions, graceful, ring of endurance, explorer's ring, and similar support items.
- Avoid broad aliases like just `travel`, `utility`, `tool`, or `teleport` unless guarded by tests.

## Matching Design

Rules remain static and phrase-driven:

- Prefer exact multi-word aliases such as `fossil island travel`, `fairy ring items`, `kourend teleport`, `ghostspeak`, `light source`, `dig clue items`, and `desert protection`.
- Prefer specific item-name patterns over broad single words.
- Use score maps for direct destination items so they rank above generic support items.
- Keep categories readable and user-facing, such as `Fossil Island travel`, `Fairy ring access`, and `Light sources`.
- Keep reasons concrete enough to explain why the item matched.

## False-Positive Guardrails

Add tests for risky broad terms and substring traps:

- `travel` should not activate every regional travel rule.
- `utility` should not return every tool.
- `teleport` should not drown out destination-specific searches.
- `ring` should not match every ring-related travel rule unless the query asks for teleport jewellery or a specific destination.
- `axe`, `pickaxe`, `machete`, and `rope` should be query-specific enough to avoid unrelated results.

## Testing Strategy

Add behavior tests before expanding each group. Tests should use owned items plus distractors.

Required tests:

- `kourendTravelFindsXericsAndMemoirs`
- `fossilIslandTravelFindsDigsitePendant`
- `fairyRingItemsFindsDramenStaff`
- `fremennikTravelFindsLyreAndGamesNecklace`
- `lightSourceFindsLanternsAndTorches`
- `ghostspeakFindsGhostspeakItems`
- `digClueItemsFindsSpadeAndNavigationTools`
- `desertProtectionFindsWaterskinsAndPasses`
- `travelUtilityQueriesDoNotBleedAcrossCategories`

Existing v1/v2 tests must remain green.

## Documentation

Update README coverage text after implementation to mention travel and utility coverage explicitly. Keep the local-only and read-only safety statement prominent.

## Acceptance Criteria

- The plugin remains local-only and read-only.
- New rules cover major regional travel and utility intents listed above.
- Tests prove useful matches and false-positive guardrails.
- Existing semantic examples continue to pass.
- No network, menu actions, withdrawals, deposits, or storage inference are introduced.
- `gradlew test`, `compileJava`, and `shadowJar` pass.