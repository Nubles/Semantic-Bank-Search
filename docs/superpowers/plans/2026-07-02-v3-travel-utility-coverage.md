# v3 Travel and Utility Coverage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a curated local semantic coverage pack for regional travel and utility searches.

**Architecture:** Keep the existing static Java rule-family architecture. Add phrase-specific regional travel rules to `TeleportRules`, utility rules to `ToolRules` and `ClueRules`, and environmental/protection support to `ProtectionRules`; verify behavior through `SemanticSearchEngineTest` and keep README coverage wording current.

**Tech Stack:** Java, RuneLite plugin code, JUnit 4, Gradle wrapper.

---

## File Structure

- Modify `src/main/java/com/semanticbanksearch/TeleportRules.java`: add regional travel rules for Kourend/Zeah, Fossil Island, Fremennik, Kandarin/Ardougne, fairy rings, and wilderness travel refinements.
- Modify `src/main/java/com/semanticbanksearch/ToolRules.java`: add access/tool utility rules for light sources, ghostspeak, rope/lockpick/machete, and travel support where it fits existing categories.
- Modify `src/main/java/com/semanticbanksearch/ClueRules.java`: add a focused `Dig clue items` rule so clue utility queries do not depend only on broad clue-stash matching.
- Modify `src/main/java/com/semanticbanksearch/ProtectionRules.java`: extend desert/environmental protection and add anti-dragon utility if not already covered well enough by existing potion/shield rules.
- Modify `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`: add coverage tests and false-positive guard tests near the existing semantic coverage tests, before the `names(...)` helper.
- Modify `README.md`: mention travel and utility coverage in the semantic coverage description.

---

### Task 1: Regional Travel Coverage

**Files:**
- Modify: `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`
- Modify: `src/main/java/com/semanticbanksearch/TeleportRules.java`

- [ ] **Step 1: Add failing regional travel tests**

Add these tests immediately before `private static String names(List<SemanticSearchResult> results)` in `SemanticSearchEngineTest.java`:

```java
@Test
public void kourendTravelFindsXericsAndMemoirs()
{
    StorageIndex index = new StorageIndex();
    index.record(910, "Xeric's talisman", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(911, "Kharedst's memoirs", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(912, "Games necklace(8)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("kourend teleport", index);

    assertTrue(names(results).contains("Xeric's talisman"));
    assertTrue(names(results).contains("Kharedst's memoirs"));
    assertFalse(names(results).contains("Games necklace(8)"));
}

@Test
public void fossilIslandTravelFindsDigsitePendant()
{
    StorageIndex index = new StorageIndex();
    index.record(920, "Digsite pendant", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(921, "Numulite", 250, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(922, "Varrock teleport", 3, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("fossil island travel", index);

    assertTrue(names(results).contains("Digsite pendant"));
    assertTrue(names(results).contains("Numulite"));
    assertFalse(names(results).contains("Varrock teleport"));
}

@Test
public void fairyRingItemsFindsDramenStaff()
{
    StorageIndex index = new StorageIndex();
    index.record(930, "Dramen staff", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(931, "Lunar staff", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(932, "Staff of air", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("fairy ring items", index);

    assertTrue(names(results).contains("Dramen staff"));
    assertTrue(names(results).contains("Lunar staff"));
    assertFalse(names(results).contains("Staff of air"));
}

@Test
public void fremennikTravelFindsLyreAndGamesNecklace()
{
    StorageIndex index = new StorageIndex();
    index.record(940, "Enchanted lyre", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(941, "Games necklace(8)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(942, "Lunar isle teleport", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(943, "Shantay pass", 5, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("fremennik travel", index);

    assertTrue(names(results).contains("Enchanted lyre"));
    assertTrue(names(results).contains("Games necklace(8)"));
    assertTrue(names(results).contains("Lunar isle teleport"));
    assertFalse(names(results).contains("Shantay pass"));
}
```

- [ ] **Step 2: Run the regional travel tests to verify they fail**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: the newly added tests fail because the categories do not exist or do not match enough items yet.

- [ ] **Step 3: Add regional travel rules**

In `TeleportRules.java`, add these rules before the generic `Teleport jewellery` rule so destination-specific searches score before broad jewellery matching:

```java
SemanticLibrary.rule(
    "Kourend travel",
    "Gets you around Great Kourend and Zeah.",
    SemanticLibrary.aliases("kourend travel", "kourend teleport", "zeah travel", "zeah teleport", "xeric teleport"),
    SemanticLibrary.patterns("xeric's talisman", "kharedst's memoirs", "book of the dead", "skills necklace", "rada's blessing"),
    SemanticLibrary.scores(
        "xeric's talisman", 35,
        "kharedst's memoirs", 30,
        "book of the dead", 25),
    115),
SemanticLibrary.rule(
    "Fossil Island travel",
    "Gets you to or supports Fossil Island travel.",
    SemanticLibrary.aliases("fossil island travel", "fossil island teleport", "digsite pendant fossil", "mushroom meadow"),
    SemanticLibrary.patterns("digsite pendant", "numulite", "mushroom meadow", "volcanic mine"),
    SemanticLibrary.scores(
        "digsite pendant", 35,
        "numulite", 10),
    115),
SemanticLibrary.rule(
    "Fairy ring access",
    "Items used to access fairy rings.",
    SemanticLibrary.aliases("fairy ring", "fairy rings", "fairy ring items", "fairy ring access"),
    SemanticLibrary.patterns("dramen staff", "lunar staff", "quest cape"),
    SemanticLibrary.scores(
        "dramen staff", 35,
        "lunar staff", 35,
        "quest cape", 20),
    115),
SemanticLibrary.rule(
    "Fremennik travel",
    "Gets you around Fremennik areas, Lunar Isle, and Waterbirth.",
    SemanticLibrary.aliases("fremennik travel", "fremennik teleport", "rellekka teleport", "lunar isle", "waterbirth teleport"),
    SemanticLibrary.patterns("enchanted lyre", "games necklace", "lunar isle teleport", "waterbirth teleport", "fremennik sea boots"),
    SemanticLibrary.scores(
        "enchanted lyre", 35,
        "lunar isle teleport", 30,
        "waterbirth teleport", 30,
        "games necklace", 20),
    115),
SemanticLibrary.rule(
    "Kandarin and Ardougne travel",
    "Gets you around Ardougne, Camelot, Catherby, and Kandarin.",
    SemanticLibrary.aliases("kandarin travel", "ardougne travel", "ardougne teleport", "camelot travel", "catherby teleport"),
    SemanticLibrary.patterns("ardougne cloak", "camelot teleport", "combat bracelet", "skills necklace", "kandarin headgear"),
    SemanticLibrary.scores(
        "ardougne cloak", 35,
        "camelot teleport", 25,
        "skills necklace", 20),
    110),
```

Replace the existing wilderness escape aliases with this expanded alias list:

```java
SemanticLibrary.aliases("wilderness escape", "wildy escape", "escape teleport", "teleport out", "wilderness travel", "wildy teleport")
```

Replace the existing wilderness escape patterns with this expanded pattern list:

```java
SemanticLibrary.patterns("royal seed pod", "amulet of glory", "ring of wealth", "burning amulet", "games necklace", "one-click teleport", "teleport crystal", "wilderness sword")
```

- [ ] **Step 4: Run regional travel tests to verify they pass**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: all `SemanticSearchEngineTest` tests pass.

- [ ] **Step 5: Commit regional travel coverage**

Run:

```powershell
git add src/main/java/com/semanticbanksearch/TeleportRules.java src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java
git commit -m "Add v3 regional travel semantics"
```

---

### Task 2: Utility and Environmental Coverage

**Files:**
- Modify: `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`
- Modify: `src/main/java/com/semanticbanksearch/ToolRules.java`
- Modify: `src/main/java/com/semanticbanksearch/ClueRules.java`
- Modify: `src/main/java/com/semanticbanksearch/ProtectionRules.java`

- [ ] **Step 1: Add failing utility tests**

Add these tests immediately before `private static String names(List<SemanticSearchResult> results)` in `SemanticSearchEngineTest.java`:

```java
@Test
public void lightSourceFindsLanternsAndTorches()
{
    StorageIndex index = new StorageIndex();
    index.record(950, "Bullseye lantern", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(951, "Bruma torch", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(952, "Rune sword", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("light source", index);

    assertTrue(names(results).contains("Bullseye lantern"));
    assertTrue(names(results).contains("Bruma torch"));
    assertFalse(names(results).contains("Rune sword"));
}

@Test
public void ghostspeakFindsGhostspeakItems()
{
    StorageIndex index = new StorageIndex();
    index.record(960, "Ghostspeak amulet", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(961, "Cramulet", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(962, "Amulet of strength", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("ghostspeak", index);

    assertTrue(names(results).contains("Ghostspeak amulet"));
    assertTrue(names(results).contains("Cramulet"));
    assertFalse(names(results).contains("Amulet of strength"));
}

@Test
public void digClueItemsFindsSpadeAndNavigationTools()
{
    StorageIndex index = new StorageIndex();
    index.record(970, "Spade", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(971, "Sextant", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(972, "Watch", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(973, "Chart", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(974, "Dragon scimitar", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("dig clue items", index);

    assertTrue(names(results).contains("Spade"));
    assertTrue(names(results).contains("Sextant"));
    assertTrue(names(results).contains("Watch"));
    assertTrue(names(results).contains("Chart"));
    assertFalse(names(results).contains("Dragon scimitar"));
}

@Test
public void desertProtectionFindsWaterskinsAndPasses()
{
    StorageIndex index = new StorageIndex();
    index.record(980, "Waterskin(4)", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(981, "Shantay pass", 5, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(982, "Desert amulet 4", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(983, "Barrows teleport", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("desert protection", index);

    assertTrue(names(results).contains("Waterskin(4)"));
    assertTrue(names(results).contains("Shantay pass"));
    assertTrue(names(results).contains("Desert amulet 4"));
    assertFalse(names(results).contains("Barrows teleport"));
}
```

- [ ] **Step 2: Run the utility tests to verify they fail where coverage is missing**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: at least `ghostspeakFindsGhostspeakItems` and `digClueItemsFindsSpadeAndNavigationTools` fail before the new utility-specific rules are added. Existing light-source/desert tests may already partially pass; that is acceptable if the new tests still guard behavior.

- [ ] **Step 3: Expand utility rules**

In `ToolRules.java`, replace the current `Light sources` rule with this expanded version:

```java
SemanticLibrary.rule(
    "Light sources",
    "Useful in dark caves, clue steps, and travel routes that require light.",
    SemanticLibrary.aliases("light source", "light sources", "dark cave", "cave light", "lantern", "torch"),
    SemanticLibrary.patterns("bullseye lantern", "lantern", "candle lantern", "oil lantern", "torch", "bruma torch", "tinderbox"),
    SemanticLibrary.scores(
        "bullseye lantern", 30,
        "bruma torch", 25,
        "lantern", 20),
    110),
```

Add this new rule after `Light sources`:

```java
SemanticLibrary.rule(
    "Ghostspeak utility",
    "Items used to speak with ghosts or support undead quest interactions.",
    SemanticLibrary.aliases("ghostspeak", "speak to ghosts", "ghost speak", "ghost amulet", "undead quest utility"),
    SemanticLibrary.patterns("ghostspeak amulet", "cramulet", "morytania legs", "ectophial"),
    SemanticLibrary.scores(
        "ghostspeak amulet", 35,
        "cramulet", 30,
        "ectophial", 15),
    115),
```

Add this new rule after `General tools`:

```java
SemanticLibrary.rule(
    "Access tools",
    "Common tools used to unlock paths, cross obstacles, or access quest areas.",
    SemanticLibrary.aliases("access tools", "quest access tools", "dungeon tools", "lockpick tools", "machete tools"),
    SemanticLibrary.patterns("rope", "lockpick", "machete", "knife", "hammer", "saw", "pickaxe", "axe"),
    SemanticLibrary.scores(
        "rope", 25,
        "lockpick", 25,
        "machete", 20,
        "pickaxe", 15,
        "axe", 15),
    105),
```

In `ClueRules.java`, add this focused clue rule after `Coordinate clue tools`:

```java
SemanticLibrary.rule(
    "Dig clue items",
    "Items used for dig and coordinate clue steps.",
    SemanticLibrary.aliases("dig clue items", "dig clue tools", "dig clue", "coordinate dig clue"),
    SemanticLibrary.patterns("spade", "sextant", "watch", "chart", "bullseye lantern", "lantern", "rope"),
    SemanticLibrary.scores(
        "spade", 30,
        "sextant", 25,
        "watch", 25,
        "chart", 25),
    115),
```

In `ProtectionRules.java`, keep the existing `Desert protection` category but ensure its aliases and patterns include these values:

```java
SemanticLibrary.aliases("desert protection", "desert survival", "heat protection", "desert gear", "desert utility")
```

```java
SemanticLibrary.patterns("waterskin", "desert robes", "desert shirt", "desert boots", "desert amulet", "camulet", "circlet of water", "water tiara", "shantay pass")
```

Add this anti-dragon utility rule after `Defensive shields`:

```java
SemanticLibrary.rule(
    "Anti-dragon utility",
    "Items and consumables used for dragonfire protection.",
    SemanticLibrary.aliases("anti dragon", "anti-dragon", "dragonfire protection", "dragon shield", "anti dragon shield"),
    SemanticLibrary.patterns("anti-dragon shield", "dragonfire shield", "dragonfire ward", "extended antifire", "antifire potion", "super antifire"),
    SemanticLibrary.scores(
        "anti-dragon shield", 35,
        "dragonfire shield", 30,
        "extended antifire", 25,
        "super antifire", 25),
    115),
```

- [ ] **Step 4: Run utility tests to verify they pass**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: all semantic search engine tests pass.

- [ ] **Step 5: Commit utility coverage**

Run:

```powershell
git add src/main/java/com/semanticbanksearch/ToolRules.java src/main/java/com/semanticbanksearch/ClueRules.java src/main/java/com/semanticbanksearch/ProtectionRules.java src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java
git commit -m "Add v3 utility semantics"
```

---

### Task 3: False-Positive Guards and Ranking Checks

**Files:**
- Modify: `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`
- Modify: `src/main/java/com/semanticbanksearch/TeleportRules.java`, `ToolRules.java`, `ClueRules.java`, `ProtectionRules.java`

- [ ] **Step 1: Add guard tests**

Add these tests immediately before `private static String names(List<SemanticSearchResult> results)` in `SemanticSearchEngineTest.java`:

```java
@Test
public void travelUtilityQueriesDoNotBleedAcrossCategories()
{
    StorageIndex index = new StorageIndex();
    index.record(990, "Xeric's talisman", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(991, "Dramen staff", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(992, "Ghostspeak amulet", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(993, "Bullseye lantern", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(994, "Dragon scimitar", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> kourendResults = new SemanticSearchEngine(SemanticLibrary.create()).search("kourend teleport", index);
    assertTrue(names(kourendResults).contains("Xeric's talisman"));
    assertFalse(names(kourendResults).contains("Ghostspeak amulet"));
    assertFalse(names(kourendResults).contains("Bullseye lantern"));

    List<SemanticSearchResult> fairyRingResults = new SemanticSearchEngine(SemanticLibrary.create()).search("fairy ring items", index);
    assertTrue(names(fairyRingResults).contains("Dramen staff"));
    assertFalse(names(fairyRingResults).contains("Xeric's talisman"));
    assertFalse(names(fairyRingResults).contains("Dragon scimitar"));

    List<SemanticSearchResult> ghostspeakResults = new SemanticSearchEngine(SemanticLibrary.create()).search("ghostspeak", index);
    assertTrue(names(ghostspeakResults).contains("Ghostspeak amulet"));
    assertFalse(names(ghostspeakResults).contains("Dramen staff"));
}

@Test
public void genericTravelWordDoesNotActivateEveryRegionalRule()
{
    StorageIndex index = new StorageIndex();
    index.record(995, "Xeric's talisman", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(996, "Digsite pendant", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(997, "Enchanted lyre", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("travel", index);

    assertTrue(results.isEmpty());
}
```

- [ ] **Step 2: Run guard tests**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: the tests pass. If `genericTravelWordDoesNotActivateEveryRegionalRule` fails, remove any single-word `travel` alias from new rules and use phrases like `kourend travel`, `fossil island travel`, and `fremennik travel` only.

- [ ] **Step 3: Keep aliases and patterns phrase-specific**

If guard tests fail, make these exact fixes:

- Remove single-token aliases `travel`, `utility`, `tool`, `teleport`, and `ring` from new rules.
- Replace broad item patterns like `staff`, `ring`, `teleport`, `axe`, and `bar` with exact phrases such as `dramen staff`, `ring of wealth`, `lunar isle teleport`, `woodcutting axe`, or `steel bar`.
- Keep `General tools` as the only rule that uses a direct `tool` alias, because it existed before this pack.

- [ ] **Step 4: Commit guard coverage**

Run:

```powershell
git add src/main/java/com/semanticbanksearch/TeleportRules.java src/main/java/com/semanticbanksearch/ToolRules.java src/main/java/com/semanticbanksearch/ClueRules.java src/main/java/com/semanticbanksearch/ProtectionRules.java src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java
git commit -m "Guard v3 travel utility semantics"
```

---

### Task 4: README and Final Verification

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Update README coverage wording**

In `README.md`, find the semantic coverage section or feature list and add this sentence near the existing skilling/minigame and safe storage notes:

```markdown
The static semantic pack also covers regional travel and utility searches such as Kourend/Zeah travel, Fossil Island travel, fairy ring access, Fremennik travel, light sources, ghostspeak items, dig clue tools, and desert protection.
```

Keep the local-only paragraph intact. Do not weaken the statement that the plugin only uses observed local storage and visible bank highlighting.

- [ ] **Step 2: Run targeted semantic tests**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: build successful.

- [ ] **Step 3: Run full verification**

Run:

```powershell
.\gradlew.bat test
.\gradlew.bat compileJava
.\gradlew.bat shadowJar
rg "http|https|Socket|Robot|MenuEntry|setMenuEntries|invokeMenuAction|withdraw|deposit|Runtime|getRuntime|ProcessBuilder|reflection|ClassLoader"
git diff --check
git status --short --branch
```

Expected:

- Gradle test, compile, and jar build succeed.
- The safety scan returns no matches.
- `git diff --check` returns no whitespace errors.
- Git status shows only intended README/rule/test changes before commit, then clean after commit.

- [ ] **Step 4: Commit README and final verification notes**

Run:

```powershell
git add README.md
git commit -m "Document v3 travel utility coverage"
```

- [ ] **Step 5: Final review handoff**

Request a final code review over the range from `887f2e0` to `HEAD`, checking:

- The new pack remains local-only and read-only.
- New rules are phrase-specific and do not create broad category bleed.
- Tests cover the required travel/utility categories and guardrails.
- README accurately describes coverage without implying unsafe storage inference.