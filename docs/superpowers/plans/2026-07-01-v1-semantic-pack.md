# v1 Semantic Pack Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expand Semantic Bank Search into a curated v1 local semantic pack with stronger OSRS-aware coverage, ranking, and explanations.

**Architecture:** Keep the search engine local and static. Add focused rule-family classes for boss and slayer prep, expand existing rule families, and guard behavior with search regression tests. No runtime network calls, external AI, menu actions, withdrawals, deposits, or game-state-changing behavior are introduced.

**Tech Stack:** Java 11, Gradle, RuneLite client APIs, Swing, JUnit 4.

---

## File Structure

- Modify `src/main/java/com/semanticbanksearch/SemanticLibrary.java`: register new rule-family classes.
- Create `src/main/java/com/semanticbanksearch/BossRules.java`: boss-prep semantic rules.
- Create `src/main/java/com/semanticbanksearch/SlayerRules.java`: slayer-task semantic rules.
- Modify `src/main/java/com/semanticbanksearch/TeleportRules.java`: destination and transportation expansions.
- Modify `src/main/java/com/semanticbanksearch/CombatRules.java`: combat style, spell utility, and weapon expansions.
- Modify `src/main/java/com/semanticbanksearch/PotionRules.java`: boost and protection potion expansions.
- Modify `src/main/java/com/semanticbanksearch/FoodRules.java`: healing, combo-food, brew, and pie ranking.
- Modify `src/main/java/com/semanticbanksearch/ToolRules.java`: quest and diary utility tools.
- Modify `src/main/java/com/semanticbanksearch/SkillingRules.java`: outfit, tool, supply, and boost expansions.
- Modify `src/main/java/com/semanticbanksearch/ClueRules.java`: clue navigation and stash expansions.
- Modify `src/main/java/com/semanticbanksearch/ProtectionRules.java`: survival, environmental protection, and defensive gear expansions.
- Modify `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`: add regression tests for each rule group and ranking behavior.
- Modify `README.md`: document v1 semantic coverage after implementation.

## Task 1: Boss Prep Semantic Rules

**Files:**
- Create: `src/main/java/com/semanticbanksearch/BossRules.java`
- Modify: `src/main/java/com/semanticbanksearch/SemanticLibrary.java`
- Modify: `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`

- [ ] **Step 1: Write failing boss-prep tests**

Add these tests above the `names(...)` helper in `SemanticSearchEngineTest`:

```java
@Test
public void vorkathPrepFindsDragonfireAndRangedSupplies()
{
    StorageIndex index = new StorageIndex();
    index.record(200, "Extended super antifire(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(201, "Dragon crossbow", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(202, "Diamond dragon bolts (e)", 50, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(203, "Lobster pot", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("vorkath gear", index);

    assertEquals(3, results.size());
    assertTrue(names(results).contains("Extended super antifire(4)"));
    assertTrue(names(results).contains("Dragon crossbow"));
    assertTrue(names(results).contains("Diamond dragon bolts (e)"));
    assertFalse(names(results).contains("Lobster pot"));
}

@Test
public void fightCavesPrepFindsPrayerRangedAndSustains()
{
    StorageIndex index = new StorageIndex();
    index.record(210, "Prayer potion(4)", 4, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(211, "Toxic blowpipe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(212, "Saradomin brew(4)", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(213, "Hammer", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("fight caves supplies", index);

    assertEquals(3, results.size());
    assertTrue(names(results).contains("Prayer potion(4)"));
    assertTrue(names(results).contains("Toxic blowpipe"));
    assertTrue(names(results).contains("Saradomin brew(4)"));
    assertFalse(names(results).contains("Hammer"));
}

@Test
public void zulrahPrepFindsAntipoisonAndSwitchGear()
{
    StorageIndex index = new StorageIndex();
    index.record(220, "Anti-venom+(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(221, "Trident of the swamp", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(222, "Magic shortbow", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(223, "Spade", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("zulrah setup", index);

    assertEquals(3, results.size());
    assertTrue(names(results).contains("Anti-venom+(4)"));
    assertTrue(names(results).contains("Trident of the swamp"));
    assertTrue(names(results).contains("Magic shortbow"));
    assertFalse(names(results).contains("Spade"));
}
```

- [ ] **Step 2: Run boss tests to verify red state**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: at least one new boss-prep test fails because `BossRules` is not registered yet.

- [ ] **Step 3: Create `BossRules`**

Create `BossRules` with `static List<SemanticRule> create()` returning these exact categories: `Vorkath prep`, `Zulrah prep`, `Fight Caves prep`, `Barrows prep`, `God Wars prep`, `Dagannoth Kings prep`, `Corporeal Beast prep`, and `Wilderness boss prep`.

Use these aliases and patterns:

```java
SemanticLibrary.rule("Vorkath prep", "Useful for Vorkath trips: dragonfire protection, ranged gear, bolts, and sustain.", SemanticLibrary.aliases("vorkath", "vorkath gear", "vorkath prep", "vorkath setup"), SemanticLibrary.patterns("antifire", "super antifire", "extended super antifire", "dragonfire shield", "anti-dragon shield", "crossbow", "dragon hunter crossbow", "dragon crossbow", "ruby dragon bolts", "diamond dragon bolts", "salve amulet", "void", "ranging potion", "prayer potion", "super restore", "shark", "manta ray", "saradomin brew"), SemanticLibrary.scores("extended super antifire", 35, "super antifire", 30, "antifire", 25, "dragon hunter crossbow", 25, "dragon crossbow", 18, "diamond dragon bolts", 16, "ruby dragon bolts", 16), 120)
SemanticLibrary.rule("Zulrah prep", "Useful for Zulrah trips: venom protection, magic/ranged switches, and sustain.", SemanticLibrary.aliases("zulrah", "zulrah gear", "zulrah prep", "zulrah setup"), SemanticLibrary.patterns("anti-venom", "anti-venom+", "trident", "toxic trident", "sanguinesti", "magic shortbow", "blowpipe", "crystal bow", "ranging potion", "magic potion", "saradomin brew", "super restore", "shark", "manta ray"), SemanticLibrary.scores("anti-venom+", 35, "anti-venom", 30, "trident", 25, "blowpipe", 20), 120)
SemanticLibrary.rule("Fight Caves prep", "Useful for Fight Caves: prayer restoration, ranged weapons, and long-trip sustain.", SemanticLibrary.aliases("fight caves", "jad", "jad supplies", "fire cape", "fight caves supplies"), SemanticLibrary.patterns("prayer potion", "super restore", "saradomin brew", "ranging potion", "blowpipe", "crossbow", "crystal bow", "karil", "black d'hide", "manta ray", "shark"), SemanticLibrary.scores("prayer potion", 25, "super restore", 25, "saradomin brew", 20, "blowpipe", 20), 120)
```

Add the remaining five boss categories with these exact aliases, patterns, scores, and base scores:

- `Barrows prep`: aliases `barrows gear`, `barrows prep`, `barrows setup`, `barrows supplies`; patterns `spade`, `prayer potion`, `super restore`, `trident`, `iban`, `wind wave`, `barrows teleport`, `mort'ton teleport`, `morytania legs`, `drakan's medallion`; scores `barrows teleport` 30, `spade` 25, `prayer potion` 20; base 115.
- `God Wars prep`: aliases `god wars`, `gwd`, `god wars gear`, `god wars prep`; patterns `rope`, `prayer potion`, `super restore`, `saradomin item`, `zamorak item`, `bandos item`, `armadyl item`, `crossbow`, `godsword`, `stamina potion`; scores `rope` 20, `prayer potion` 20, `super restore` 20; base 110.
- `Dagannoth Kings prep`: aliases `dagannoth kings`, `dks`, `rex prime supreme`, `dk prep`; patterns `antipoison`, `super antipoison`, `prayer potion`, `super restore`, `trident`, `crossbow`, `rune thrownaxe`, `fremennik sea boots`, `waterbirth teleport`; scores `rune thrownaxe` 30, `antipoison` 20, `prayer potion` 20; base 110.
- `Corporeal Beast prep`: aliases `corp`, `corporeal beast`, `corp prep`, `corp gear`; patterns `spear`, `hasta`, `dragon warhammer`, `bandos godsword`, `arclight`, `prayer potion`, `super restore`, `stamina potion`, `karambwan`; scores `dragon warhammer` 30, `bandos godsword` 25, `spear` 20, `hasta` 20; base 110.
- `Wilderness boss prep`: aliases `wilderness boss`, `wildy boss`, `wildy bosses`, `wilderness pvm`; patterns `royal seed pod`, `burning amulet`, `glory`, `ice barrage`, `blood rune`, `death rune`, `water rune`, `prayer potion`, `blighted super restore`, `blighted manta ray`, `black d'hide`; scores `royal seed pod` 30, `burning amulet` 25, `blighted super restore` 20; base 110.

- [ ] **Step 4: Register `BossRules`**

In `SemanticLibrary.create()`, add:

```java
rules.addAll(TeleportRules.create());
rules.addAll(BossRules.create());
rules.addAll(CombatRules.create());
```

- [ ] **Step 5: Run and commit**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: `BUILD SUCCESSFUL`.

Commit:

```powershell
git add src/main/java/com/semanticbanksearch/BossRules.java src/main/java/com/semanticbanksearch/SemanticLibrary.java src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java
git commit -m "Add boss prep semantic rules"
```

## Task 2: Slayer Prep Semantic Rules

**Files:**
- Create: `src/main/java/com/semanticbanksearch/SlayerRules.java`
- Modify: `src/main/java/com/semanticbanksearch/SemanticLibrary.java`
- Modify: `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`

- [ ] **Step 1: Write failing slayer tests**

Add these tests above the `names(...)` helper:

```java
@Test
public void dragonSlayerTaskFindsAntifireAndDragonWeapons()
{
    StorageIndex index = new StorageIndex();
    index.record(300, "Extended antifire(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(301, "Dragon hunter lance", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(302, "Anti-dragon shield", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(303, "Lobster pot", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("dragon slayer task", index);

    assertEquals(3, results.size());
    assertTrue(names(results).contains("Extended antifire(4)"));
    assertTrue(names(results).contains("Dragon hunter lance"));
    assertTrue(names(results).contains("Anti-dragon shield"));
    assertFalse(names(results).contains("Lobster pot"));
}

@Test
public void dustDevilTaskFindsMaskAndBurstRunes()
{
    StorageIndex index = new StorageIndex();
    index.record(310, "Slayer helmet", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(311, "Facemask", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(312, "Death rune", 1000, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(313, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("dust devil task", index);

    assertEquals(3, results.size());
    assertTrue(names(results).contains("Slayer helmet"));
    assertTrue(names(results).contains("Facemask"));
    assertTrue(names(results).contains("Death rune"));
    assertFalse(names(results).contains("Rune platebody"));
}

@Test
public void basiliskTaskFindsMirrorShield()
{
    StorageIndex index = new StorageIndex();
    index.record(320, "Mirror shield", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(321, "V's shield", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(322, "Bronze shield", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("basilisk task", index);

    assertEquals(2, results.size());
    assertTrue(names(results).contains("Mirror shield"));
    assertTrue(names(results).contains("V's shield"));
    assertFalse(names(results).contains("Bronze shield"));
}
```

- [ ] **Step 2: Run slayer tests to verify red state**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: at least one new slayer test fails because `SlayerRules` is not registered yet.

- [ ] **Step 3: Create `SlayerRules`**

Create `src/main/java/com/semanticbanksearch/SlayerRules.java` with `static List<SemanticRule> create()` returning these exact 10 categories:

- `Dragon slayer task`: aliases `dragon task`, `dragon slayer task`, `slayer dragons`, `metal dragons`, `blue dragons`, `black dragons`; patterns `antifire`, `super antifire`, `extended antifire`, `anti-dragon shield`, `dragonfire shield`, `dragon hunter lance`, `dragon hunter crossbow`, `stab weapon`, `hasta`; scores `dragon hunter lance` 35, `dragon hunter crossbow` 35, `extended antifire` 25, `anti-dragon shield` 20; base 120.
- `Demon slayer task`: aliases `demon task`, `demon slayer task`, `black demons`, `greater demons`, `abyssal demons`; patterns `arclight`, `darklight`, `silverlight`, `emberlight`, `prayer potion`, `super restore`, `slayer helmet`; scores `arclight` 35, `emberlight` 35, `darklight` 25; base 115.
- `Undead slayer task`: aliases `undead task`, `undead slayer`, `ankou task`, `zombie task`, `skeleton task`; patterns `salve amulet`, `crumble undead`, `slayer helmet`, `prayer potion`, `super restore`; score `salve amulet` 35; base 110.
- `Kalphite slayer task`: aliases `kalphite task`, `kalphite slayer`, `kalphites`; patterns `rope`, `desert amulet`, `shantay pass`, `waterskin`, `keris`, `hasta`, `mace`, `slayer helmet`; scores `keris` 30, `rope` 20; base 105.
- `Lizardman slayer task`: aliases `lizardman task`, `lizardmen`, `shamans`, `lizardman shaman`; patterns `shayzien`, `slayer helmet`, `ranging potion`, `crossbow`, `blowpipe`, `antidote`, `prayer potion`; score `shayzien` 35; base 110.
- `Wyvern slayer task`: aliases `wyvern task`, `skeletal wyverns`, `fossil island wyverns`, `wyvern slayer`; patterns `elemental shield`, `mind shield`, `dragonfire shield`, `ancient wyvern shield`, `wyvern shield`, `ranging potion`, `crossbow`, `prayer potion`; scores `ancient wyvern shield` 35, `elemental shield` 25, `mind shield` 25; base 115.
- `Dust devil slayer task`: aliases `dust devil`, `dust devils`, `dust devil task`; patterns `facemask`, `slayer helmet`, `death rune`, `blood rune`, `chaos rune`, `ice burst`, `ice barrage`, `prayer potion`; scores `facemask` 30, `slayer helmet` 30, `death rune` 15; base 115.
- `Smoke devil slayer task`: aliases `smoke devil`, `smoke devils`, `smoke devil task`; patterns `facemask`, `slayer helmet`, `death rune`, `blood rune`, `ice barrage`, `occult necklace`, `prayer potion`; scores `slayer helmet` 30, `facemask` 25; base 115.
- `Gargoyle slayer task`: aliases `gargoyle`, `gargoyles`, `gargoyle task`; patterns `rock hammer`, `granite hammer`, `slayer helmet`, `guthan`, `prayer potion`; scores `rock hammer` 35, `granite hammer` 30; base 115.
- `Basilisk slayer task`: aliases `basilisk`, `basilisks`, `basilisk task`, `basilisk knight`; patterns `mirror shield`, `v's shield`, `slayer helmet`, `prayer potion`; scores `mirror shield` 35, `v's shield` 35; base 115.

- [ ] **Step 4: Register `SlayerRules`**

In `SemanticLibrary.create()`, add:

```java
rules.addAll(BossRules.create());
rules.addAll(SlayerRules.create());
rules.addAll(CombatRules.create());
```

- [ ] **Step 5: Run and commit**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: `BUILD SUCCESSFUL`.

Commit:

```powershell
git add src/main/java/com/semanticbanksearch/SlayerRules.java src/main/java/com/semanticbanksearch/SemanticLibrary.java src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java
git commit -m "Add slayer prep semantic rules"
```

## Task 3: Travel, Quest, Diary, and Clue Utility Expansion

**Files:**
- Modify: `src/main/java/com/semanticbanksearch/TeleportRules.java`
- Modify: `src/main/java/com/semanticbanksearch/ToolRules.java`
- Modify: `src/main/java/com/semanticbanksearch/ClueRules.java`
- Modify: `src/main/java/com/semanticbanksearch/ProtectionRules.java`
- Modify: `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`

- [ ] **Step 1: Write failing travel and utility tests**

Add tests named `desertTravelFindsWaterskinsAndDesertAccess`, `coordinateClueFindsNavigationTools`, and `questUtilityFindsCommonQuestTools`. Use these owned items:

```java
// desert travel
"Waterskin(4)", "Desert amulet 2", "Shantay pass", exclude "Air rune"
// coordinate clue
"Sextant", "Watch", "Chart", exclude "Rune scimitar"
// quest tools
"Ghostspeak amulet", "Rope", "Pickaxe", exclude "Shark"
```

Each test must assert the exact result count, assert each intended item is present through `names(results).contains(...)`, and assert the excluded item is absent.

- [ ] **Step 2: Run tests to verify red state**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: at least one new travel or utility test fails.

- [ ] **Step 3: Expand travel and utility rules**

Add these categories:

- `Desert travel` in `TeleportRules`: aliases `desert travel`, `desert teleport`, `shantay`, `alkharid`, `pollnivneach`, `nardah`; patterns `desert amulet`, `pharaoh's sceptre`, `camulet`, `slayer ring`, `ring of dueling`, `shantay pass`, `waterskin`; scores `desert amulet` 30, `pharaoh's sceptre` 25, `shantay pass` 20; base 110.
- `Morytania travel` in `TeleportRules`: aliases `morytania travel`, `morytania teleport`, `canifis`, `burgh de rott`, `mortton`; patterns `morytania legs`, `ecto`, `ectophial`, `drakan's medallion`, `barrows teleport`, `mort'ton teleport`, `fenkenstrain`; scores `drakan's medallion` 30, `morytania legs` 25, `barrows teleport` 25; base 110.
- `Wilderness escape` in `TeleportRules`: aliases `wilderness escape`, `wildy escape`, `escape teleport`, `teleport out`; patterns `royal seed pod`, `amulet of glory`, `ring of wealth`, `burning amulet`, `games necklace`, `one-click teleport`, `teleport crystal`; scores `royal seed pod` 35, `amulet of glory` 25, `ring of wealth` 20; base 110.
- `Quest utility` in `ToolRules`: aliases `quest tools`, `quest utility`, `diary tools`, `achievement diary tools`; patterns `ghostspeak amulet`, `rope`, `spade`, `pickaxe`, `axe`, `knife`, `bucket`, `empty pot`, `pestle and mortar`, `vial`, `needle`, `thread`, `hammer`, `chisel`, `light source`, `lantern`, `tinderbox`; scores `ghostspeak amulet` 30, `rope` 20, `spade` 20, `pickaxe` 15; base 105.
- `Coordinate clue tools` in `ClueRules`: aliases `coordinate clue`, `coordinate clues`, `sextant watch chart`, `clue coordinates`; patterns `sextant`, `watch`, `chart`, `spade`; scores `sextant` 30, `watch` 30, `chart` 30, `spade` 15; base 110.
- `Desert protection` in `ProtectionRules`: aliases `desert protection`, `desert survival`, `heat protection`, `desert travel`; patterns `waterskin`, `desert robes`, `desert shirt`, `desert boots`, `desert amulet`, `circlet of water`, `water tiara`, `shantay pass`; scores `waterskin` 30, `circlet of water` 30, `desert amulet` 20; base 110.

Also expand the existing `Clue utility` aliases with `coordinate clue`, `dig clue`, `emote clue`, `clue supplies` and patterns with `fairy ring`, `teleport scroll`, `teleport tablet`, `amulet of glory`, `games necklace`, `ring of dueling`, `stamina potion`, `energy potion`.

- [ ] **Step 4: Run and commit**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: `BUILD SUCCESSFUL`.

Commit:

```powershell
git add src/main/java/com/semanticbanksearch/TeleportRules.java src/main/java/com/semanticbanksearch/ToolRules.java src/main/java/com/semanticbanksearch/ClueRules.java src/main/java/com/semanticbanksearch/ProtectionRules.java src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java
git commit -m "Expand travel clue and quest semantics"
```

## Task 4: Skilling, Food, Potion, and Combat Quality Expansion

**Files:**
- Modify: `src/main/java/com/semanticbanksearch/SkillingRules.java`
- Modify: `src/main/java/com/semanticbanksearch/FoodRules.java`
- Modify: `src/main/java/com/semanticbanksearch/PotionRules.java`
- Modify: `src/main/java/com/semanticbanksearch/CombatRules.java`
- Modify: `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`

- [ ] **Step 1: Write failing skilling and ranking tests**

Add tests named `gracefulAndSkillingOutfitsAreFoundBySkillingOutfitQuery`, `comboFoodRanksBeforeSlowFoodForFastFood`, and `alchemyRunesFindsNatureFireAndStaff`. Use these owned items:

```java
// skilling outfits
"Graceful hood", "Prospector jacket", "Angler hat", exclude "Rune platebody"
// fastest food ranking
"Shark", "Cooked karambwan", "Saradomin brew(4)"; expected order: brew, karambwan, shark
// alchemy runes
"Nature rune", "Fire rune", "Staff of fire", exclude "Rune platebody"
```

The ranking test must assert:

```java
assertEquals("Saradomin brew(4)", results.get(0).getItemName());
assertEquals("Cooked karambwan", results.get(1).getItemName());
assertEquals("Shark", results.get(2).getItemName());
```

- [ ] **Step 2: Run tests to verify red state**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: at least one new test fails.

- [ ] **Step 3: Expand skilling rules**

Add these categories to `SkillingRules`:

- `Skilling outfits`: aliases `skilling outfit`, `skilling outfits`, `skill outfit`, `xp outfit`, `graceful`; patterns `graceful`, `prospector`, `angler`, `lumberjack`, `farmer`, `rogue`, `pyromancer`, `carpenter`, `smiths`, `goldsmith`, `varrock armour`, `desert amulet`; scores `graceful` 30, `prospector` 25, `angler` 25, `lumberjack` 25; base 105.
- `Herblore supplies`: aliases `herblore supplies`, `herblore tools`, `make potions`; patterns `vial`, `vial of water`, `pestle and mortar`, `grimy`, `clean`, `unf potion`, `secondary`, `snape grass`, `limpwurt`, `red spiders' eggs`; scores `vial of water` 20, `pestle and mortar` 20; base 100.
- `Construction supplies`: aliases `construction supplies`, `construction tools`, `house building`; patterns `saw`, `hammer`, `plank`, `oak plank`, `teak plank`, `mahogany plank`, `nails`, `bolt of cloth`, `house teleport`; scores `saw` 25, `hammer` 25, `plank` 15; base 100.
- `Hunter supplies`: aliases `hunter supplies`, `hunter tools`, `trapping`; patterns `box trap`, `bird snare`, `butterfly net`, `impling jar`, `noose wand`, `teasing stick`, `magic box`; scores `box trap` 25, `bird snare` 25; base 100.

- [ ] **Step 4: Expand food, potion, and combat rules**

In `FoodRules`, add patterns `blighted manta ray`, `blighted anglerfish`, `pineapple pizza`, `meat pie`, `apple pie`, `redberry pie`, and update scores to:

```java
SemanticLibrary.scores(
    "saradomin brew", 40,
    "cooked karambwan", 35,
    "karambwan", 35,
    "anglerfish", 30,
    "manta ray", 28,
    "dark crab", 28,
    "tuna potato", 26,
    "sea turtle", 24,
    "shark", 20,
    "monkfish", 16,
    "swordfish", 14,
    "lobster", 12,
    "summer pie", 11,
    "salmon", 9,
    "trout", 7)
```

In `PotionRules`, add patterns:

- `Run energy restoration`: `stamina mix`, `ring of endurance`.
- `Combat boosts`: `super attack`, `super strength`, `super defence`, `divine super attack`, `divine super strength`, `divine super defence`.
- `Ranged and magic boosts`: `divine ranging potion`, `divine magic potion`, `saturated heart`.

Add `Skilling boosts` to `PotionRules`: aliases `skilling boost`, `skill boost`, `boost farming`, `boost mining`, `boost woodcutting`, `boost crafting`; patterns `botanical pie`, `garden pie`, `mushroom pie`, `admiral pie`, `wild pie`, `spicy stew`, `dwarven stout`, `mature dwarven stout`, `chef's delight`, `wizard's mind bomb`; scores `spicy stew` 30, `botanical pie` 20, `garden pie` 20, `mushroom pie` 20; base 100.

Add these categories to `CombatRules`:

- `Alchemy runes`: aliases `alchemy`, `alchemy runes`, `high alch`, `low alch`, `alching`; patterns `nature rune`, `fire rune`, `staff of fire`, `fire staff`, `tome of fire`, `smoke battlestaff`; scores `nature rune` 30, `staff of fire` 20, `fire rune` 15; base 105.
- `Binding and freezing runes`: aliases `freeze runes`, `binding runes`, `ice barrage`, `ice burst`, `entangle`; patterns `water rune`, `death rune`, `blood rune`, `chaos rune`, `nature rune`, `ancient staff`, `kodai`, `water staff`; scores `death rune` 20, `blood rune` 20, `ancient staff` 20; base 105.

- [ ] **Step 5: Run and commit**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
.\gradlew.bat test
```

Expected: both commands complete with `BUILD SUCCESSFUL`.

Commit:

```powershell
git add src/main/java/com/semanticbanksearch/SkillingRules.java src/main/java/com/semanticbanksearch/FoodRules.java src/main/java/com/semanticbanksearch/PotionRules.java src/main/java/com/semanticbanksearch/CombatRules.java src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java
git commit -m "Expand skilling food potion and magic semantics"
```

## Task 5: Regression Guards, Documentation, and Final Verification

**Files:**
- Modify: `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`
- Modify: `README.md`

- [ ] **Step 1: Add category-bleed regression tests**

Add these tests above the `names(...)` helper:

```java
@Test
public void genericWeaponWordDoesNotMatchEveryWeaponRule()
{
    StorageIndex index = new StorageIndex();
    index.record(600, "Dragon mace", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(601, "Rune scimitar", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(602, "Magic shortbow", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("crush weapons", index);

    assertEquals(1, results.size());
    assertEquals("Dragon mace", results.get(0).getItemName());
}

@Test
public void genericToolWordDoesNotMatchEveryToolRule()
{
    StorageIndex index = new StorageIndex();
    index.record(610, "Sextant", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(611, "Harpoon", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
    index.record(612, "Seed dibber", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("coordinate clue", index);

    assertEquals(1, results.size());
    assertEquals("Sextant", results.get(0).getItemName());
}
```

- [ ] **Step 2: Run regression tests**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: `BUILD SUCCESSFUL`. If `genericWeaponWordDoesNotMatchEveryWeaponRule` fails because extra weapon categories match, add the extra generic word to `GENERIC_CATEGORY_WORDS` in `SemanticRule`. If `genericToolWordDoesNotMatchEveryToolRule` fails because extra tool categories match, add the extra generic word to `GENERIC_CATEGORY_WORDS` in `SemanticRule` or remove the risky implicit category word from the new rule category.

- [ ] **Step 3: Update README semantic coverage**

Replace the existing semantic database sentence in `README.md` with:

```markdown
The bundled semantic database is local static data. It covers common teleports, potions, food, combat equipment, boss prep, slayer prep, tools, skilling supplies, clue utility, quest/diary utility, travel, and protection items. It is intentionally approximate and avoids runtime network calls.
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
- Safety scan finds only acceptable build metadata URLs, README/spec non-goal text, and defensive `RuntimeException` catches. If it finds production networking/action code, stop and remove it.
- Git status shows only intended README/test changes before commit.

- [ ] **Step 5: Commit final docs and regression guards**

Run:

```powershell
git add README.md src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java
git commit -m "Document v1 semantic pack coverage"
```

- [ ] **Step 6: Push PR branch**

Run:

```powershell
git push
```

Expected: `semantic-bank-search-implementation` pushes to `origin/semantic-bank-search-implementation` and updates the existing draft PR.

## Self-Review

- Spec coverage: Boss prep, slayer prep, quest/diary utility, clue solving, skilling outfits, magic utility, protection/survival, food ranking, explanations, local-only safety, and tests all map to tasks.
- Placeholder scan: this plan contains no unfinished markers or unresolved file references.
- Type consistency: new rule-family classes all expose `static List<SemanticRule> create()`, are package-private `final`, and are registered through `SemanticLibrary.create()`.
- Scope control: this plan keeps data in Java rule families and does not add runtime network access, external AI, storage-state parsing, or UI changes.
