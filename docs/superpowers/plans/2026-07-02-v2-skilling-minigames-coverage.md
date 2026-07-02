# v2 Skilling and Minigames Coverage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a broad local skilling and minigames semantic coverage pack so activity queries surface useful observed owned items.

**Architecture:** Keep the existing local static rule model. Add two focused rule-family classes, register them in `SemanticLibrary.create()`, and add regression-heavy tests that guard activity aliases against broad false positives. Existing rule files only receive small support changes when they are the correct shared home.

**Tech Stack:** Java 11, Gradle, RuneLite client APIs, Swing, JUnit 4.

---

## File Structure

- Create `src/main/java/com/semanticbanksearch/MinigameRules.java`: skilling minigame prep rules.
- Create `src/main/java/com/semanticbanksearch/SkillingWorkflowRules.java`: recurring skilling run and training-prep rules.
- Modify `src/main/java/com/semanticbanksearch/SemanticLibrary.java`: register the two new rule families near existing skilling/tool rules.
- Modify `src/main/java/com/semanticbanksearch/SemanticRule.java`: only if tests prove an additional generic category word is needed to avoid category-word bleed.
- Modify `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`: add activity coverage and false-positive regression tests.
- Modify `README.md`: document v2 skilling workflow and minigame coverage after implementation.

## Task 1: Minigame Prep Rules

**Files:**
- Create: `src/main/java/com/semanticbanksearch/MinigameRules.java`
- Modify: `src/main/java/com/semanticbanksearch/SemanticLibrary.java`
- Modify: `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`

- [ ] **Step 1: Write failing minigame tests**

Add these tests above the `names(...)` helper in `SemanticSearchEngineTest`:

```java
@Test
public void wintertodtSuppliesFindsWarmClothingAndTools()
{
    StorageIndex index = new StorageIndex();
    index.record(700, "Clue hunter garb", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(701, "Steel axe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(702, "Tinderbox", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(703, "Hammer", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(704, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("wintertodt supplies", index);

    assertEquals(4, results.size());
    assertTrue(names(results).contains("Clue hunter garb"));
    assertTrue(names(results).contains("Steel axe"));
    assertTrue(names(results).contains("Tinderbox"));
    assertTrue(names(results).contains("Hammer"));
    assertFalse(names(results).contains("Rune platebody"));
}

@Test
public void temporossSuppliesFindsFishingTools()
{
    StorageIndex index = new StorageIndex();
    index.record(710, "Dragon harpoon", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(711, "Rope", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(712, "Bucket", 5, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(713, "Angler hat", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(714, "Rune scimitar", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("tempoross supplies", index);

    assertEquals(4, results.size());
    assertTrue(names(results).contains("Dragon harpoon"));
    assertTrue(names(results).contains("Rope"));
    assertTrue(names(results).contains("Bucket"));
    assertTrue(names(results).contains("Angler hat"));
    assertFalse(names(results).contains("Rune scimitar"));
}

@Test
public void gotrFindsPouchesAndChisel()
{
    StorageIndex index = new StorageIndex();
    index.record(720, "Large pouch", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(721, "Chisel", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(722, "Binding necklace", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(723, "Pure essence", 250, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(724, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("gotr pouch", index);

    assertEquals(4, results.size());
    assertTrue(names(results).contains("Large pouch"));
    assertTrue(names(results).contains("Chisel"));
    assertTrue(names(results).contains("Binding necklace"));
    assertTrue(names(results).contains("Pure essence"));
    assertFalse(names(results).contains("Rune platebody"));
}
```

Add these two tests in the same location:

```java
@Test
public void mahoganyHomesFindsConstructionSuppliesAndTeleports()
{
    StorageIndex index = new StorageIndex();
    index.record(730, "Saw", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(731, "Hammer", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(732, "Mahogany plank", 20, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(733, "Steel bar", 5, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(734, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("mahogany homes", index);

    assertEquals(4, results.size());
    assertTrue(names(results).contains("Saw"));
    assertTrue(names(results).contains("Hammer"));
    assertTrue(names(results).contains("Mahogany plank"));
    assertTrue(names(results).contains("Steel bar"));
    assertFalse(names(results).contains("Rune platebody"));
}

@Test
public void blastFurnaceFindsSmithingUtility()
{
    StorageIndex index = new StorageIndex();
    index.record(740, "Coal bag", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(741, "Ice gloves", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(742, "Goldsmith gauntlets", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(743, "Stamina potion(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(744, "Spade", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("blast furnace", index);

    assertEquals(4, results.size());
    assertTrue(names(results).contains("Coal bag"));
    assertTrue(names(results).contains("Ice gloves"));
    assertTrue(names(results).contains("Goldsmith gauntlets"));
    assertTrue(names(results).contains("Stamina potion(4)"));
    assertFalse(names(results).contains("Spade"));
}
```

- [ ] **Step 2: Run minigame tests to verify red state**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: at least one new minigame test fails because `MinigameRules` does not exist or is not registered.
- [ ] **Step 3: Create `MinigameRules`**

Create `src/main/java/com/semanticbanksearch/MinigameRules.java` with this class shape and rule set:

```java
package com.semanticbanksearch;

import java.util.Arrays;
import java.util.List;

final class MinigameRules
{
    private MinigameRules()
    {
    }

    static List<SemanticRule> create()
    {
        return Arrays.asList(
            SemanticLibrary.rule(
                "Wintertodt supplies",
                "Useful for Wintertodt: warm clothing, firemaking tools, repair tools, and food.",
                SemanticLibrary.aliases("wintertodt", "wintertodt supplies", "wintertodt prep", "todt supplies"),
                SemanticLibrary.patterns("clue hunter", "pyromancer", "warm gloves", "bomber jacket", "axe", "knife", "tinderbox", "hammer", "rejuvenation potion", "cake", "jug of wine", "karambwan", "shark"),
                SemanticLibrary.scores("clue hunter", 30, "pyromancer", 30, "tinderbox", 25, "axe", 20, "hammer", 15),
                115),
            SemanticLibrary.rule(
                "Tempoross supplies",
                "Useful for Tempoross: fishing tools, repair tools, buckets, and fishing outfit pieces.",
                SemanticLibrary.aliases("tempoross", "tempoross supplies", "tempoross prep", "tempoross fishing"),
                SemanticLibrary.patterns("harpoon", "dragon harpoon", "crystal harpoon", "infernal harpoon", "rope", "hammer", "bucket", "angler hat", "angler top", "angler waders", "angler boots", "small fishing net"),
                SemanticLibrary.scores("dragon harpoon", 35, "crystal harpoon", 35, "infernal harpoon", 35, "harpoon", 25, "bucket", 15),
                115),
            SemanticLibrary.rule(
                "Guardians of the Rift supplies",
                "Useful for Guardians of the Rift: essence pouches, chisel, binding necklace, essence, and runecrafting support.",
                SemanticLibrary.aliases("gotr", "guardians of the rift", "guardians of the rift supplies", "gotr pouch", "gotr supplies"),
                SemanticLibrary.patterns("small pouch", "medium pouch", "large pouch", "giant pouch", "colossal pouch", "chisel", "binding necklace", "pure essence", "rune essence", "graceful", "stamina potion", "energy potion"),
                SemanticLibrary.scores("colossal pouch", 40, "giant pouch", 35, "large pouch", 30, "medium pouch", 25, "small pouch", 20, "chisel", 20, "binding necklace", 20),
                115),
            SemanticLibrary.rule(
                "Giants' Foundry supplies",
                "Useful for Giants' Foundry: bars, smithing tools, gloves, and stamina.",
                SemanticLibrary.aliases("giants foundry", "giants' foundry", "foundry supplies", "giants foundry supplies"),
                SemanticLibrary.patterns("bronze bar", "iron bar", "steel bar", "mithril bar", "adamantite bar", "runite bar", "hammer", "ice gloves", "smiths gloves", "stamina potion", "dwarven stout"),
                SemanticLibrary.scores("runite bar", 30, "adamantite bar", 25, "mithril bar", 20, "steel bar", 15, "ice gloves", 20),
                110),
            SemanticLibrary.rule(
                "Mahogany Homes supplies",
                "Useful for Mahogany Homes: construction tools, planks, steel bars, and movement support.",
                SemanticLibrary.aliases("mahogany homes", "mahogany homes supplies", "mahogany homes prep", "construction contracts"),
                SemanticLibrary.patterns("saw", "hammer", "plank", "oak plank", "teak plank", "mahogany plank", "steel bar", "house teleport", "varrock teleport", "falador teleport", "ardy teleport", "stamina potion", "graceful"),
                SemanticLibrary.scores("saw", 30, "hammer", 30, "mahogany plank", 25, "teak plank", 20, "oak plank", 15, "steel bar", 15),
                115),
            SemanticLibrary.rule(
                "Forestry supplies",
                "Useful for Forestry: axes, forestry kit, logs, rations, and woodcutting support.",
                SemanticLibrary.aliases("forestry", "forestry supplies", "forestry kit", "forestry prep"),
                SemanticLibrary.patterns("forestry kit", "felling axe", "rune axe", "dragon axe", "crystal axe", "log", "logs", "secateurs", "stamina potion", "graceful", "forester's ration", "anima bark"),
                SemanticLibrary.scores("forestry kit", 35, "crystal axe", 30, "dragon axe", 25, "rune axe", 20, "forester's ration", 20),
                105),
            SemanticLibrary.rule(
                "Tithe Farm supplies",
                "Useful for Tithe Farm: watering cans, farming tools, graceful, and farming boosts.",
                SemanticLibrary.aliases("tithe farm", "tithe farm supplies", "tithe supplies", "tithe prep"),
                SemanticLibrary.patterns("watering can", "seed dibber", "spade", "graceful", "stamina potion", "botanical pie", "garden pie", "farmer"),
                SemanticLibrary.scores("watering can", 35, "seed dibber", 25, "spade", 20, "graceful", 15),
                105),
            SemanticLibrary.rule(
                "Pyramid Plunder supplies",
                "Useful for Pyramid Plunder: desert travel, anti-poison, lockpicks, stamina, and food.",
                SemanticLibrary.aliases("pyramid plunder", "pyramid plunder supplies", "plunder supplies", "sceptre run"),
                SemanticLibrary.patterns("pharaoh's sceptre", "camulet", "desert amulet", "antipoison", "anti-venom", "lockpick", "stamina potion", "energy potion", "shark", "karambwan"),
                SemanticLibrary.scores("pharaoh's sceptre", 35, "lockpick", 25, "antipoison", 20, "stamina potion", 20),
                105),
            SemanticLibrary.rule(
                "Blast Furnace supplies",
                "Useful for Blast Furnace: coal bag, gloves, smithing support, and stamina.",
                SemanticLibrary.aliases("blast furnace", "blast furnace supplies", "blast furnace prep", "bf supplies"),
                SemanticLibrary.patterns("coal bag", "ice gloves", "goldsmith gauntlets", "smiths gloves", "stamina potion", "energy potion", "coal", "iron ore", "gold ore", "mithril ore", "adamantite ore", "runite ore"),
                SemanticLibrary.scores("coal bag", 35, "ice gloves", 30, "goldsmith gauntlets", 25, "stamina potion", 15),
                115));
    }
}
```

- [ ] **Step 4: Register `MinigameRules`**

In `SemanticLibrary.create()`, register the new rules near skilling rules:

```java
rules.addAll(ToolRules.create());
rules.addAll(SkillingRules.create());
rules.addAll(MinigameRules.create());
rules.addAll(ClueRules.create());
```

- [ ] **Step 5: Run and commit Task 1**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
.\gradlew.bat test
```

Expected: both commands complete with `BUILD SUCCESSFUL`.

Commit:

```powershell
git add src/main/java/com/semanticbanksearch/MinigameRules.java src/main/java/com/semanticbanksearch/SemanticLibrary.java src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java
git commit -m "Add skilling minigame semantic rules"
```
## Task 2: Skilling Workflow Rules

**Files:**
- Create: `src/main/java/com/semanticbanksearch/SkillingWorkflowRules.java`
- Modify: `src/main/java/com/semanticbanksearch/SemanticLibrary.java`
- Modify: `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`

- [ ] **Step 1: Write failing skilling workflow tests**

Add these tests above the `names(...)` helper in `SemanticSearchEngineTest`:

```java
@Test
public void birdhouseRunFindsBirdhousesLogsAndDigsiteTravel()
{
    StorageIndex index = new StorageIndex();
    index.record(800, "Oak bird house", 4, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(801, "Clockwork", 4, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(802, "Teak logs", 4, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(803, "Digsite pendant", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(804, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("birdhouse run", index);

    assertEquals(4, results.size());
    assertTrue(names(results).contains("Oak bird house"));
    assertTrue(names(results).contains("Clockwork"));
    assertTrue(names(results).contains("Teak logs"));
    assertTrue(names(results).contains("Digsite pendant"));
    assertFalse(names(results).contains("Rune platebody"));
}

@Test
public void herbRunFindsFarmingToolsCompostAndTeleports()
{
    StorageIndex index = new StorageIndex();
    index.record(810, "Seed dibber", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(811, "Magic secateurs", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(812, "Ultracompost", 50, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(813, "Skills necklace(6)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(814, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("herb run", index);

    assertEquals(4, results.size());
    assertTrue(names(results).contains("Seed dibber"));
    assertTrue(names(results).contains("Magic secateurs"));
    assertTrue(names(results).contains("Ultracompost"));
    assertTrue(names(results).contains("Skills necklace(6)"));
    assertFalse(names(results).contains("Rune platebody"));
}

@Test
public void glassCraftingFindsSandSeaweedAndPipe()
{
    StorageIndex index = new StorageIndex();
    index.record(820, "Bucket of sand", 50, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(821, "Giant seaweed", 20, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(822, "Glassblowing pipe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(823, "Rune scimitar", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("glass crafting prep", index);

    assertEquals(3, results.size());
    assertTrue(names(results).contains("Bucket of sand"));
    assertTrue(names(results).contains("Giant seaweed"));
    assertTrue(names(results).contains("Glassblowing pipe"));
    assertFalse(names(results).contains("Rune scimitar"));
}

@Test
public void fletchingPrepFindsLogsStringAndFeathers()
{
    StorageIndex index = new StorageIndex();
    index.record(830, "Maple logs", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(831, "Knife", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(832, "Bow string", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(833, "Feather", 1000, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(834, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("fletching prep", index);

    assertEquals(4, results.size());
    assertTrue(names(results).contains("Maple logs"));
    assertTrue(names(results).contains("Knife"));
    assertTrue(names(results).contains("Bow string"));
    assertTrue(names(results).contains("Feather"));
    assertFalse(names(results).contains("Rune platebody"));
}
```

- [ ] **Step 2: Run workflow tests to verify red state**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: at least one new workflow test fails because `SkillingWorkflowRules` does not exist or is not registered.

- [ ] **Step 3: Create `SkillingWorkflowRules`**

Create `src/main/java/com/semanticbanksearch/SkillingWorkflowRules.java` with `static List<SemanticRule> create()` returning these rules:

```java
SemanticLibrary.rule("Herb run supplies", "Useful for herb runs: farming tools, compost, teleports, and movement support.", SemanticLibrary.aliases("herb run", "herb run supplies", "herb run prep", "farming run"), SemanticLibrary.patterns("seed dibber", "spade", "rake", "secateurs", "magic secateurs", "compost", "supercompost", "ultracompost", "herb seed", "ranarr seed", "snapdragon seed", "torstol seed", "skills necklace", "explorer's ring", "stamina potion"), SemanticLibrary.scores("magic secateurs", 30, "seed dibber", 25, "ultracompost", 25, "skills necklace", 20, "stamina potion", 15), 110)
SemanticLibrary.rule("Tree run supplies", "Useful for tree runs: saplings, protection payments, spade, teleports, and boosts.", SemanticLibrary.aliases("tree run", "tree run supplies", "fruit tree run", "tree farming run"), SemanticLibrary.patterns("sapling", "spade", "basket", "sack", "plant pot", "watering can", "garden pie", "botanical pie", "teleport", "skills necklace", "ring of dueling", "spirit tree"), SemanticLibrary.scores("sapling", 30, "spade", 20, "garden pie", 15, "botanical pie", 15), 105)
SemanticLibrary.rule("Birdhouse run supplies", "Useful for birdhouse runs: bird houses, clockworks, logs, seeds, tools, and Fossil Island travel.", SemanticLibrary.aliases("birdhouse run", "bird house run", "birdhouse supplies", "bird houses"), SemanticLibrary.patterns("bird house", "birdhouse", "clockwork", "log", "logs", "hammer", "chisel", "seed", "hop seed", "digsite pendant", "fossil island", "mushroom meadow"), SemanticLibrary.scores("bird house", 35, "birdhouse", 35, "clockwork", 30, "digsite pendant", 25, "logs", 20), 115)
SemanticLibrary.rule("Seaweed run supplies", "Useful for seaweed runs: spores, compost, diving/Fossil Island travel, and farming support.", SemanticLibrary.aliases("seaweed run", "seaweed supplies", "seaweed spores", "giant seaweed run"), SemanticLibrary.patterns("seaweed spore", "giant seaweed", "ultracompost", "compost", "diving apparatus", "fishbowl helmet", "digsite pendant", "fossil island", "stamina potion"), SemanticLibrary.scores("seaweed spore", 35, "giant seaweed", 25, "ultracompost", 20, "digsite pendant", 20), 105)
SemanticLibrary.rule("Farm contract supplies", "Useful for farm contracts: farming tools, common seeds, compost, and farming boosts.", SemanticLibrary.aliases("farm contract", "farm contracts", "farming contract", "farming contracts"), SemanticLibrary.patterns("seed dibber", "spade", "rake", "secateurs", "compost", "ultracompost", "seed", "sapling", "garden pie", "botanical pie", "skills necklace"), SemanticLibrary.scores("seed dibber", 25, "ultracompost", 20, "botanical pie", 20, "garden pie", 20), 100)
SemanticLibrary.rule("Glass crafting prep", "Useful for glass crafting: sand, seaweed, soda ash, and glassblowing tools.", SemanticLibrary.aliases("glass crafting", "glass crafting prep", "make glass", "crafting glass", "blow glass"), SemanticLibrary.patterns("bucket of sand", "sand", "seaweed", "giant seaweed", "soda ash", "molten glass", "glassblowing pipe", "astral rune", "cosmic rune"), SemanticLibrary.scores("bucket of sand", 30, "giant seaweed", 30, "glassblowing pipe", 25, "soda ash", 20), 105)
SemanticLibrary.rule("Fletching prep", "Useful for Fletching training: logs, knives, strings, feathers, arrows, and dart supplies.", SemanticLibrary.aliases("fletching prep", "fletching supplies", "make bows", "make arrows", "make darts"), SemanticLibrary.patterns("log", "logs", "knife", "bow string", "feather", "headless arrow", "arrow shaft", "arrowtips", "dart tip", "dart tips", "unfinished broad bolts"), SemanticLibrary.scores("knife", 25, "bow string", 20, "feather", 20, "logs", 15), 105)
SemanticLibrary.rule("Smithing prep", "Useful for Smithing training: ores, bars, coal bag, hammer, and smithing gloves.", SemanticLibrary.aliases("smithing prep", "smithing supplies", "make bars", "smith bars"), SemanticLibrary.patterns("ore", "coal", "bar", "bronze bar", "iron bar", "steel bar", "mithril bar", "adamantite bar", "runite bar", "coal bag", "hammer", "ice gloves", "goldsmith gauntlets"), SemanticLibrary.scores("coal bag", 30, "hammer", 20, "goldsmith gauntlets", 20, "ice gloves", 20), 105)
SemanticLibrary.rule("Cooking prep", "Useful for Cooking training: raw food, cooking gauntlets, pies, and cooking boosts.", SemanticLibrary.aliases("cooking prep", "cooking supplies", "cook food", "cooking training"), SemanticLibrary.patterns("raw", "raw shark", "raw monkfish", "raw karambwan", "raw lobster", "raw swordfish", "cooking gauntlets", "chef's delight", "pie", "jug", "pot of flour"), SemanticLibrary.scores("cooking gauntlets", 30, "raw shark", 20, "raw karambwan", 20, "chef's delight", 15), 100)
SemanticLibrary.rule("Fishing method supplies", "Useful for Fishing methods: rods, bait, feathers, nets, harpoons, pots, and vessels.", SemanticLibrary.aliases("fishing method", "fishing methods", "fishing supplies", "fish training"), SemanticLibrary.patterns("harpoon", "fishing rod", "fly fishing rod", "barbarian rod", "bait", "feather", "small fishing net", "big fishing net", "lobster pot", "karambwan vessel"), SemanticLibrary.scores("dragon harpoon", 30, "harpoon", 25, "fly fishing rod", 20, "karambwan vessel", 20), 100)
SemanticLibrary.rule("Mining method supplies", "Useful for Mining methods: pickaxes, prospector, mining boosts, and environment support.", SemanticLibrary.aliases("mining method", "mining methods", "mining supplies", "mining training"), SemanticLibrary.patterns("pickaxe", "prospector", "celestial ring", "varrock armour", "dwarven stout", "waterskin", "desert amulet"), SemanticLibrary.scores("crystal pickaxe", 35, "dragon pickaxe", 30, "rune pickaxe", 25, "prospector", 20, "celestial ring", 20), 100)
```

Use the same package, imports, private constructor, and `return Arrays.asList(...)` class structure shown in Task 1 for `MinigameRules`; the complete rule expressions above are the contents of that `Arrays.asList(...)` call.

- [ ] **Step 4: Register `SkillingWorkflowRules`**

In `SemanticLibrary.create()`, register the new rules near `SkillingRules` and `MinigameRules`:

```java
rules.addAll(ToolRules.create());
rules.addAll(SkillingRules.create());
rules.addAll(MinigameRules.create());
rules.addAll(SkillingWorkflowRules.create());
rules.addAll(ClueRules.create());
```

- [ ] **Step 5: Run and commit Task 2**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
.\gradlew.bat test
```

Expected: both commands complete with `BUILD SUCCESSFUL`.

Commit:

```powershell
git add src/main/java/com/semanticbanksearch/SkillingWorkflowRules.java src/main/java/com/semanticbanksearch/SemanticLibrary.java src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java
git commit -m "Add skilling workflow semantic rules"
```
## Task 3: Cross-Activity Regression Guards and Documentation

**Files:**
- Modify: `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`
- Modify: `README.md`

- [ ] **Step 1: Add cross-activity leakage tests**

Add these tests above the `names(...)` helper in `SemanticSearchEngineTest`:

```java
@Test
public void skillingMinigameQueriesDoNotBleedAcrossActivities()
{
    StorageIndex index = new StorageIndex();
    index.record(900, "Steel axe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(901, "Tinderbox", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(902, "Dragon harpoon", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(903, "Large pouch", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(904, "Coal bag", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("wintertodt supplies", index);

    assertEquals(2, results.size());
    assertTrue(names(results).contains("Steel axe"));
    assertTrue(names(results).contains("Tinderbox"));
    assertFalse(names(results).contains("Dragon harpoon"));
    assertFalse(names(results).contains("Large pouch"));
    assertFalse(names(results).contains("Coal bag"));
}

@Test
public void birdhouseRunDoesNotMatchGenericLogsEverywhere()
{
    StorageIndex index = new StorageIndex();
    index.record(910, "Oak bird house", 4, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(911, "Clockwork", 4, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(912, "Maple logs", 50, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(913, "Bow string", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(914, "Knife", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("birdhouse run", index);

    assertEquals(3, results.size());
    assertTrue(names(results).contains("Oak bird house"));
    assertTrue(names(results).contains("Clockwork"));
    assertTrue(names(results).contains("Maple logs"));
    assertFalse(names(results).contains("Bow string"));
    assertFalse(names(results).contains("Knife"));
}

@Test
public void blastFurnaceDoesNotMatchGenericSmithingPrepOnlyItems()
{
    StorageIndex index = new StorageIndex();
    index.record(920, "Coal bag", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(921, "Ice gloves", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(922, "Gold ore", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(923, "Hammer", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("blast furnace", index);

    assertEquals(3, results.size());
    assertTrue(names(results).contains("Coal bag"));
    assertTrue(names(results).contains("Ice gloves"));
    assertTrue(names(results).contains("Gold ore"));
    assertFalse(names(results).contains("Hammer"));
}
```

- [ ] **Step 2: Run regression tests**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: `BUILD SUCCESSFUL`. If a test fails because a broad category word activates unrelated rules, add the broad word to `GENERIC_CATEGORY_WORDS` or remove the risky alias/pattern from the broader rule.

- [ ] **Step 3: Update README coverage sentence**

Replace the existing bundled semantic database sentence in `README.md` with:

```markdown
The bundled semantic database is local static data. It covers common teleports, potions, food, combat equipment, boss prep, slayer prep, tools, skilling supplies, skilling workflows, minigame prep, clue utility, quest/diary utility, travel, and protection items. It is intentionally approximate and avoids runtime network calls.
```

- [ ] **Step 4: Run final verification**

Run:

```powershell
.\gradlew.bat test
.\gradlew.bat compileJava
.\gradlew.bat shadowJar
rg "http|https|Socket|Robot|MenuEntry|setMenuEntries|invokeMenuAction|withdraw|deposit|Runtime|getRuntime|ProcessBuilder|reflection|ClassLoader"
git status --short --branch
```

Expected:

- Gradle commands complete with `BUILD SUCCESSFUL`.
- `build/libs/semantic-bank-search-1.0.0-all.jar` exists.
- Safety scan finds no production networking, menu/actioning, withdrawal, deposit, process execution, reflection, or classloader behavior. Documentation-only matches are acceptable if reviewed and understood.
- Git status shows only intended README/test changes before commit.

- [ ] **Step 5: Commit final docs and regression guards**

Run:

```powershell
git add README.md src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java
git commit -m "Document v2 skilling minigame coverage"
```

## Self-Review

- Spec coverage: Minigame prep, skilling workflows, matching guardrails, ranking guidance, documentation, local-only safety, and build verification all map to tasks.
- Placeholder scan: this plan contains no unfinished markers or unresolved file references.
- Type consistency: new rule-family classes expose `static List<SemanticRule> create()`, are package-private `final`, and are registered through `SemanticLibrary.create()`.
- Scope control: this plan adds static local Java rule data and tests only. It does not add UI changes, networking, external AI, storage inference, or game-state-changing behavior.