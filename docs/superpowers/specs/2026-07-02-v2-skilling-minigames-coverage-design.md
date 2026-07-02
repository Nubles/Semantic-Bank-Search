# v2 Skilling and Minigames Coverage Design

## Goal

Expand Semantic Bank Search with a broad local skilling and minigames coverage pack. The plugin should answer everyday activity queries like `wintertodt supplies`, `birdhouse run`, `mahogany homes`, `gotr pouch`, `herb run`, and `blast furnace` by showing useful observed owned items.

The v2 pack optimizes for coverage first. It should cover many common activities with simple, useful rules, then leave deeper minigame-specific ranking for a later pass.

## Non-Goals

- No runtime network calls, external AI, web lookups, or remote item databases.
- No clicking, withdrawing, depositing, moving items, menu mutation, or game-state-changing behavior.
- No inference about storage the client has not observed.
- No UI redesign in this pack.
- No personalized learning or feedback system yet.

## Coverage Scope

### Minigame Prep

Add local semantic rules for common skilling minigames:

- Wintertodt: warm clothing, axe, knife, tinderbox, hammer, food, rejuvenation supplies.
- Tempoross: harpoon, rope, hammer, buckets, cooking/fishing utility, angler outfit.
- Guardians of the Rift: essence pouches, chisel, binding necklace, runes, graceful, stamina/energy.
- Giants' Foundry: metal bars, smithing tools, ice gloves, stamina, smithing boosts.
- Mahogany Homes: saw, hammer, planks, steel bars, teleports, graceful/stamina.
- Forestry: axes, forestry kit, anima bark-related gear, logs, secateurs, rations where practical.
- Tithe Farm: watering cans, seed dibber, spade, graceful, farming boosts.
- Pyramid Plunder: anti-poison, lockpick, sceptre/camulet/desert travel, stamina, food.
- Blast Furnace: ores/bars, coal bag, ice gloves, goldsmith gauntlets, stamina/energy.

### Skilling Workflows

Add rules for recurring skilling runs and preparation:

- Herb runs: seed dibber, spade, rake, secateurs, compost, herb seeds, teleport jewellery, stamina.
- Tree runs: saplings, spade, payment baskets/sacks, teleports, farming outfit/boosts.
- Birdhouse runs: bird houses, clockworks, logs, seeds, digsite pendant, chisel, hammer.
- Seaweed runs: seaweed spores, diving/fossil island travel, compost, ultracompost.
- Farm contracts: farming tools, seed vault-style supplies, common seeds, boosts.
- Glass/crafting prep: buckets of sand, seaweed, soda ash, giant seaweed, glassblowing pipe, astral/cosmic utility where relevant.
- Fletching prep: logs, knife, bow string, feathers, darts, arrowtips, headless arrows.
- Smithing prep: ores, bars, coal bag, hammer, ice gloves, goldsmith gauntlets.
- Cooking prep: raw food, cooking gauntlets, chef's delight, pies, jugs/pots where useful.
- Fishing methods: harpoons, rods, bait, feathers, nets, lobster pots, karambwan vessels.
- Mining methods: pickaxes, prospector, celestial ring, varrock armour, waterskins/desert utility where relevant.

## Matching Design

Rules remain static Java data using the existing `SemanticRule` model. Each new rule should have:

- A focused display category.
- Activity-specific aliases, such as `wintertodt supplies`, `birdhouse run`, `gotr`, `mahogany homes`, and `blast furnace`.
- Item-name patterns that avoid broad substring traps.
- Score boosts for core items that should rank first.
- A short reason string that explains why the item is useful.

Avoid broad single-token aliases like `minigame`, `skilling`, `run`, or `supplies` unless tests prove they do not cause leakage. Prefer phrase aliases and activity names.

## Rule Organization

Add one or two new rule-family classes rather than overloading existing files:

- `MinigameRules` for Wintertodt, Tempoross, Guardians of the Rift, Giants' Foundry, Mahogany Homes, Forestry, Tithe Farm, Pyramid Plunder, and Blast Furnace.
- `SkillingWorkflowRules` for herb runs, tree runs, birdhouse runs, seaweed runs, farm contracts, and training-prep workflows.

Register both in `SemanticLibrary.create()` near the existing skilling and tool rules. Existing `SkillingRules`, `ToolRules`, `TeleportRules`, `PotionRules`, and `FoodRules` may receive small supporting additions only when a shared rule is the right home.

## Ranking Approach

Coverage-first ranking should still keep obvious core items near the top:

- Minigame required tools score highest.
- Activity-specific teleports and containers score next.
- Generic support items like stamina, graceful, food, and utility tools score lower unless the activity strongly depends on them.
- Avoid letting general-purpose items outrank the activity's defining item.

Examples:

- `birdhouse run` should rank Digsite pendant / bird houses / clockwork / logs ahead of generic tools.
- `gotr pouch` should rank essence pouches and chisel ahead of generic runes.
- `wintertodt supplies` should rank warm clothing, axe, knife, tinderbox, and hammer ahead of generic food.

## Testing Strategy

Each new rule group needs tests with useful items, distractors, and exact result counts where practical.

Required regression themes:

- Activity aliases do not activate unrelated minigame rules.
- Broad workflow words like `run`, `supplies`, `tools`, and `skilling` do not cause cross-rule leakage.
- Item substring traps are guarded, especially short patterns like `bar`, `log`, `axe`, `ring`, `kit`, and `pouch`.
- Existing v1 guard tests remain green.

Suggested initial tests:

- `wintertodtSuppliesFindsWarmClothingAndTools`
- `temporossSuppliesFindsFishingTools`
- `gotrFindsPouchesAndChisel`
- `mahoganyHomesFindsConstructionSuppliesAndTeleports`
- `birdhouseRunFindsBirdhousesLogsAndDigsiteTravel`
- `herbRunFindsFarmingToolsCompostAndTeleports`
- `blastFurnaceFindsSmithingUtility`
- `skillingMinigameQueriesDoNotBleedAcrossActivities`

## Documentation

Update README coverage text after implementation to mention skilling workflows and minigames explicitly. Keep the local-only statement prominent.

## Acceptance Criteria

- The plugin remains local-only and read-only.
- New rule families cover the listed minigames and skilling workflows.
- Tests prove at least one useful query per major activity group.
- Cross-rule false positives are guarded with distractor items.
- `gradlew test`, `compileJava`, and `shadowJar` pass.
- The built jar remains available at `build/libs/semantic-bank-search-1.0.0-all.jar`.