# All Indexed Items and Expanded Semantics Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an All Indexed panel view and broaden the bundled local semantic database for common OSRS bank item purposes.

**Architecture:** Keep the observed storage model local and unchanged, add a plugin/panel display mode for all indexed item snapshots, and split semantic rule data into focused package-private helper classes. Search remains pure Java and local-only; RuneLite wiring only passes sorted snapshots and refreshes the current panel mode.

**Tech Stack:** Java 11, Gradle, RuneLite client APIs, Swing, JUnit 4.

---

## File Structure

- Modify `src/main/java/com/semanticbanksearch/SemanticBankSearchPanel.java`: add All Indexed control and item-list rendering API.
- Modify `src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java`: track panel mode, sort observed items, wire `showIndexedItems()`, refresh all-indexed mode on bank changes.
- Modify `src/main/java/com/semanticbanksearch/SemanticLibrary.java`: replace monolithic rule construction with concatenation of focused rule helper classes.
- Create `src/main/java/com/semanticbanksearch/TeleportRules.java`: teleport and jewellery rules.
- Create `src/main/java/com/semanticbanksearch/CombatRules.java`: melee style, ranged, ammo, magic, defensive gear rules.
- Create `src/main/java/com/semanticbanksearch/PotionRules.java`: potions, boosts, restorations, poison/venom, antifire rules.
- Create `src/main/java/com/semanticbanksearch/FoodRules.java`: expanded food patterns and ranking scores.
- Create `src/main/java/com/semanticbanksearch/ToolRules.java`: tools, light sources, traversal utility rules.
- Create `src/main/java/com/semanticbanksearch/SkillingRules.java`: skilling supply/tool categories.
- Create `src/main/java/com/semanticbanksearch/ClueRules.java`: clue and stash utility rules.
- Create `src/main/java/com/semanticbanksearch/ProtectionRules.java`: defensive/protection utility rules not already owned by potions.
- Modify `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`: add expanded search regression tests.
- Modify `src/test/java/com/semanticbanksearch/StorageIndexTest.java` only if an extracted sort helper is added.
- Modify `README.md`: mention All Indexed and the broader bundled local rule database.

## Task 1: All Indexed Panel Mode

**Files:**
- Modify: `src/main/java/com/semanticbanksearch/SemanticBankSearchPanel.java`
- Modify: `src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java`

- [ ] **Step 1: Write compile-facing API change in the panel**

In `SemanticBankSearchPanel`, change the constructor signature from:

```java
public SemanticBankSearchPanel(Consumer<String> searchConsumer, Runnable clearConsumer)
```

to:

```java
public SemanticBankSearchPanel(
	Consumer<String> searchConsumer,
	Runnable allIndexedConsumer,
	Runnable clearConsumer)
```

Add a field:

```java
private final Runnable allIndexedConsumer;
```

Initialize it null-safely:

```java
this.allIndexedConsumer = allIndexedConsumer == null ? () -> { } : allIndexedConsumer;
```

- [ ] **Step 2: Add the All Indexed button**

In `searchControls()`, replace the single clear button area with a compact button row containing `Search`, `All Indexed`, and `Clear`.

Required behavior:

```java
JButton searchButton = new JButton("Search");
searchButton.setFocusable(false);
searchButton.addActionListener(event -> runSearch(searchField.getText()));

JButton allIndexedButton = new JButton("All Indexed");
allIndexedButton.setFocusable(false);
allIndexedButton.addActionListener(event -> allIndexedConsumer.run());

JButton clearButton = new JButton("Clear");
clearButton.setFocusable(false);
clearButton.addActionListener(event -> {
	searchField.setText("");
	clearConsumer.run();
	clearResults();
});
```

Keep Enter-to-search behavior unchanged.

- [ ] **Step 3: Add indexed item rendering API**

Add this public method to `SemanticBankSearchPanel`:

```java
public void updateIndexedItems(List<ObservedItem> items, String status)
{
	List<ObservedItem> safeItems = items == null ? Collections.emptyList() : new ArrayList<>(items);
	resultsContainer.removeAll();
	statusLabel.setText(status == null || status.trim().isEmpty() ? " " : status.trim());

	if (safeItems.isEmpty())
	{
		resultsContainer.add(textBlock(
			"No indexed items yet",
			"Open your bank so Semantic Bank Search can observe and index visible bank items."));
	}
	else
	{
		for (ObservedItem item : safeItems)
		{
			resultsContainer.add(indexedItemCard(item));
		}
	}

	revalidate();
	repaint();
}
```

Add helper `indexedItemCard(ObservedItem item)` that mirrors result card styling and shows item name plus details from `indexedDetails(item)`.

Add helper:

```java
private static String indexedDetails(ObservedItem item)
{
	String source = item.getSourceName().isEmpty()
		? item.getSourceType().name()
		: item.getSourceName();
	String highlightState = item.isCurrentlyVisible() && item.getSourceType() == StorageSourceType.BANK
		? "visible in bank"
		: "remembered";
	return "Source " + source
		+ " | Quantity " + item.getQuantity()
		+ " | " + highlightState;
}
```

- [ ] **Step 4: Wire panel mode in plugin**

In `SemanticBankSearchPlugin`, add:

```java
private enum PanelMode
{
	SEARCH,
	ALL_INDEXED
}
```

Add field:

```java
private PanelMode panelMode = PanelMode.SEARCH;
```

Update panel construction:

```java
panel = new SemanticBankSearchPanel(this::runSearch, this::showIndexedItems, this::clearSearch);
```

In `runSearch`, set `panelMode = PanelMode.SEARCH` before refreshing.

In `clearSearch`, set `panelMode = PanelMode.SEARCH`.

- [ ] **Step 5: Add sorted indexed item display in plugin**

Add:

```java
private void showIndexedItems()
{
	currentQuery = "";
	panelMode = PanelMode.ALL_INDEXED;
	if (overlay != null)
	{
		overlay.setHighlightedItemIds(new ArrayList<>());
	}
	refreshIndexedItems();
}
```

Add:

```java
private void refreshIndexedItems()
{
	if (panel == null || index == null)
	{
		return;
	}

	List<ObservedItem> items = index.items();
	items.sort((left, right) -> {
		int visibility = Boolean.compare(isVisibleBankItem(right), isVisibleBankItem(left));
		if (visibility != 0)
		{
			return visibility;
		}
		int name = String.CASE_INSENSITIVE_ORDER.compare(left.getName(), right.getName());
		if (name != 0)
		{
			return name;
		}
		return left.getSourceName().compareToIgnoreCase(right.getSourceName());
	});

	panel.updateIndexedItems(items, indexedStatus(items));
}
```

Add:

```java
private static boolean isVisibleBankItem(ObservedItem item)
{
	return item.isCurrentlyVisible() && item.getSourceType() == StorageSourceType.BANK;
}
```

Add:

```java
private String indexedStatus(List<ObservedItem> items)
{
	if (items == null || items.isEmpty())
	{
		return "";
	}
	if (!bankOpen)
	{
		return "Open the bank to refresh visible item status.";
	}
	return "Showing " + items.size() + " observed items.";
}
```

- [ ] **Step 6: Refresh the correct mode on bank changes**

Replace direct `refreshCurrentSearch()` calls after bank observation/close with:

```java
refreshActivePanelMode();
```

Add:

```java
private void refreshActivePanelMode()
{
	if (panelMode == PanelMode.ALL_INDEXED)
	{
		refreshIndexedItems();
	}
	else
	{
		refreshCurrentSearch();
	}
}
```

- [ ] **Step 7: Verify compile and tests**

Run:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat test
```

Expected: both commands complete with `BUILD SUCCESSFUL`.

- [ ] **Step 8: Commit All Indexed mode**

Run:

```powershell
git add src/main/java/com/semanticbanksearch/SemanticBankSearchPanel.java src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java
git commit -m "Add all indexed items view"
```

Expected: commit succeeds.

## Task 2: Split and Expand Semantic Rule Database

**Files:**
- Modify: `src/main/java/com/semanticbanksearch/SemanticLibrary.java`
- Create: `src/main/java/com/semanticbanksearch/TeleportRules.java`
- Create: `src/main/java/com/semanticbanksearch/CombatRules.java`
- Create: `src/main/java/com/semanticbanksearch/PotionRules.java`
- Create: `src/main/java/com/semanticbanksearch/FoodRules.java`
- Create: `src/main/java/com/semanticbanksearch/ToolRules.java`
- Create: `src/main/java/com/semanticbanksearch/SkillingRules.java`
- Create: `src/main/java/com/semanticbanksearch/ClueRules.java`
- Create: `src/main/java/com/semanticbanksearch/ProtectionRules.java`
- Modify: `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`

- [ ] **Step 1: Write failing expanded semantic tests**

Add tests to `SemanticSearchEngineTest`:

```java
@Test
public void teleportJewelleryFindsOwnedTeleportJewellery()
{
	StorageIndex index = new StorageIndex();
	index.record(100, "Games necklace(8)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
	index.record(101, "Hammer", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

	List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("teleport jewellery", index);

	assertEquals(1, results.size());
	assertEquals("Games necklace(8)", results.get(0).getItemName());
}

@Test
public void staminaFindsRunEnergyPotion()
{
	StorageIndex index = new StorageIndex();
	index.record(110, "Stamina potion(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

	List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("stamina", index);

	assertEquals(1, results.size());
	assertEquals("Stamina potion(4)", results.get(0).getItemName());
}

@Test
public void antifireFindsDragonProtectionPotion()
{
	StorageIndex index = new StorageIndex();
	index.record(120, "Extended antifire(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

	List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("antifire", index);

	assertEquals(1, results.size());
	assertEquals("Extended antifire(4)", results.get(0).getItemName());
}

@Test
public void rangedAmmoFindsArrowsAndBolts()
{
	StorageIndex index = new StorageIndex();
	index.record(130, "Rune arrow", 100, StorageSourceType.BANK, "Bank", true, 1_000L);
	index.record(131, "Diamond bolts (e)", 50, StorageSourceType.BANK, "Bank", true, 1_000L);
	index.record(132, "Air rune", 500, StorageSourceType.BANK, "Bank", true, 1_000L);

	List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("ranged ammo", index);

	assertEquals(2, results.size());
	assertTrue(names(results).contains("Rune arrow"));
	assertTrue(names(results).contains("Diamond bolts (e)"));
	assertFalse(names(results).contains("Air rune"));
}

@Test
public void magicRunesFindsOwnedRunes()
{
	StorageIndex index = new StorageIndex();
	index.record(140, "Law rune", 500, StorageSourceType.BANK, "Bank", true, 1_000L);
	index.record(141, "Nature rune", 500, StorageSourceType.BANK, "Bank", true, 1_000L);
	index.record(142, "Rune platebody", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

	List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("magic runes", index);

	assertEquals(2, results.size());
	assertTrue(names(results).contains("Law rune"));
	assertTrue(names(results).contains("Nature rune"));
	assertFalse(names(results).contains("Rune platebody"));
}

@Test
public void farmingToolsFindsOwnedFarmingTools()
{
	StorageIndex index = new StorageIndex();
	index.record(150, "Seed dibber", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
	index.record(151, "Magic secateurs", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

	List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("farming tools", index);

	assertEquals(2, results.size());
	assertTrue(names(results).contains("Seed dibber"));
	assertTrue(names(results).contains("Magic secateurs"));
}

@Test
public void fishingToolsFindsOwnedFishingTools()
{
	StorageIndex index = new StorageIndex();
	index.record(160, "Harpoon", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
	index.record(161, "Lobster pot", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

	List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("fishing tools", index);

	assertEquals(2, results.size());
	assertTrue(names(results).contains("Harpoon"));
	assertTrue(names(results).contains("Lobster pot"));
}

@Test
public void clueToolsFindsLightSourceAndSpade()
{
	StorageIndex index = new StorageIndex();
	index.record(170, "Bullseye lantern", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
	index.record(171, "Spade", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

	List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("clue tools", index);

	assertEquals(2, results.size());
	assertTrue(names(results).contains("Bullseye lantern"));
	assertTrue(names(results).contains("Spade"));
}
```

- [ ] **Step 2: Run expanded semantic tests to verify failure**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: at least one of the new tests fails because the current library does not include all expanded rule coverage.

- [ ] **Step 3: Add shared rule factory methods to SemanticLibrary**

Change `SemanticLibrary` helper methods `rule`, `aliases`, `patterns`, and `scores` from `private static` to package-private `static` so helper classes can reuse them.

Change `create()` to:

```java
public static List<SemanticRule> create()
{
	List<SemanticRule> rules = new ArrayList<>();
	rules.addAll(PotionRules.create());
	rules.addAll(FoodRules.create());
	rules.addAll(TeleportRules.create());
	rules.addAll(CombatRules.create());
	rules.addAll(ProtectionRules.create());
	rules.addAll(ToolRules.create());
	rules.addAll(SkillingRules.create());
	rules.addAll(ClueRules.create());
	return rules;
}
```

Add imports for `ArrayList` and remove unused imports.

- [ ] **Step 4: Create PotionRules**

Create `PotionRules`:

```java
package com.semanticbanksearch;

import java.util.Arrays;
import java.util.List;

final class PotionRules
{
	private PotionRules()
	{
	}

	static List<SemanticRule> create()
	{
		return Arrays.asList(
			SemanticLibrary.rule(
				"Prayer restoration",
				"Restores prayer points.",
				SemanticLibrary.aliases("prayer", "restore prayer", "prayer restoration", "ppot"),
				SemanticLibrary.patterns("prayer potion", "super restore", "sanfew serum", "blighted super restore"),
				SemanticLibrary.scores(),
				100),
			SemanticLibrary.rule(
				"Run energy restoration",
				"Restores or preserves run energy.",
				SemanticLibrary.aliases("stamina", "run energy", "energy potion", "restore run"),
				SemanticLibrary.patterns("stamina potion", "energy potion", "super energy", "strange fruit"),
				SemanticLibrary.scores(),
				100),
			SemanticLibrary.rule(
				"Poison protection",
				"Protects against poison or venom.",
				SemanticLibrary.aliases("poison", "venom", "poison protection", "antipoison"),
				SemanticLibrary.patterns("antipoison", "anti-venom", "sanfew serum", "antidote"),
				SemanticLibrary.scores(),
				100),
			SemanticLibrary.rule(
				"Antifire protection",
				"Protects against dragonfire.",
				SemanticLibrary.aliases("antifire", "dragonfire", "anti dragon", "dragon protection"),
				SemanticLibrary.patterns("antifire", "super antifire", "extended antifire", "anti-dragon shield", "dragonfire shield"),
				SemanticLibrary.scores(),
				100),
			SemanticLibrary.rule(
				"Combat boosts",
				"Boosts combat stats.",
				SemanticLibrary.aliases("combat boost", "melee boost", "strength boost", "attack boost", "defence boost"),
				SemanticLibrary.patterns("combat potion", "super combat", "attack potion", "strength potion", "defence potion", "divine super combat"),
				SemanticLibrary.scores(),
				100),
			SemanticLibrary.rule(
				"Ranged and magic boosts",
				"Boosts ranged or magic.",
				SemanticLibrary.aliases("ranged boost", "range boost", "magic boost", "mage boost"),
				SemanticLibrary.patterns("ranging potion", "bastion potion", "magic potion", "forgotten brew", "imbued heart"),
				SemanticLibrary.scores(),
				100));
	}
}
```

- [ ] **Step 5: Create FoodRules**

Create `FoodRules` with the existing food rule plus expanded patterns and scores:

```java
SemanticLibrary.patterns("manta ray", "shark", "karambwan", "anglerfish", "sea turtle", "monkfish", "trout", "salmon", "lobster", "swordfish", "tuna potato", "dark crab", "summer pie", "cooked karambwan", "saradomin brew")
```

Use scores:

```java
SemanticLibrary.scores(
	"anglerfish", 22,
	"manta ray", 22,
	"dark crab", 22,
	"tuna potato", 22,
	"sea turtle", 21,
	"shark", 20,
	"cooked karambwan", 18,
	"karambwan", 18,
	"monkfish", 16,
	"swordfish", 14,
	"lobster", 12,
	"salmon", 9,
	"trout", 7,
	"saradomin brew", 16,
	"summer pie", 11)
```

- [ ] **Step 6: Create TeleportRules**

Create rules for:

- `Barrows teleports`, preserving current aliases and patterns.
- `Teleport jewellery`, aliases `teleport jewellery`, `jewellery teleport`, `charged jewellery`; patterns `games necklace`, `ring of dueling`, `amulet of glory`, `combat bracelet`, `skills necklace`, `necklace of passage`, `burning amulet`, `digsite pendant`, `slayer ring`, `ring of wealth`.
- `Teleport tablets`, aliases `teleport tablet`, `tele tabs`, `house tab`, `spell tablet`; patterns `teleport`, `tablet`, `redirected house tablet`.

Keep patterns broad enough for the tests but avoid including every item with the word `ring` unless paired with teleport context.

- [ ] **Step 7: Create CombatRules**

Create rules for:

- `Crush weapons`, preserving current aliases/patterns.
- `Slash weapons`, aliases `slash`, `slash weapon`, `cut webs`; patterns `scimitar`, `sword`, `dagger`, `machete`, `axe`, `spear`.
- `Stab weapons`, aliases `stab`, `stab weapon`; patterns `dagger`, `spear`, `hasta`, `rapier`, `shortsword`.
- `Ranged weapons`, aliases `ranged weapon`, `range weapon`, `bow`, `crossbow`; patterns `bow`, `crossbow`, `blowpipe`, `ballista`, `chinchompa`.
- `Ranged ammunition`, aliases `ranged ammo`, `range ammo`, `arrows`, `bolts`; patterns `arrow`, `arrows`, `bolt`, `bolts`, `dart`, `darts`, `javelin`, `knife`.
- `Magic runes`, aliases `magic runes`, `spell runes`, `runes`; patterns `air rune`, `water rune`, `earth rune`, `fire rune`, `mind rune`, `chaos rune`, `death rune`, `blood rune`, `soul rune`, `law rune`, `nature rune`, `cosmic rune`, `astral rune`.
- `Magic weapons`, aliases `magic weapon`, `mage weapon`, `staff`, `wand`; patterns `staff`, `wand`, `trident`, `sceptre`, `tome`.

- [ ] **Step 8: Create ToolRules, SkillingRules, ClueRules, ProtectionRules**

Create `ToolRules` with:

- `Web cutters`, preserving current rule.
- `Light sources`, aliases `light source`, `light`, `dark cave`; patterns `lantern`, `candle`, `torch`, `bruma torch`, `bullseye lantern`.
- `General tools`, aliases `tool`, `utility`, `rope`, `spade`, `lockpick`; patterns `spade`, `rope`, `lockpick`, `tinderbox`, `chisel`, `pestle and mortar`, `hammer`, `saw`, `knife`.

Create `SkillingRules` with:

- `Farming tools`, aliases `farming tools`, `farm tools`; patterns `seed dibber`, `rake`, `spade`, `secateurs`, `magic secateurs`, `watering can`, `plant cure`, `compost`.
- `Fishing tools`, aliases `fishing tools`, `fish tools`; patterns `harpoon`, `fishing rod`, `fly fishing rod`, `small fishing net`, `big fishing net`, `lobster pot`, `karambwan vessel`.
- `Mining tools`, aliases `mining tools`, `pickaxe`; patterns `pickaxe`.
- `Woodcutting tools`, aliases `woodcutting tools`, `axe`; patterns `axe`.
- `Crafting supplies`, aliases `crafting supplies`, `crafting tools`; patterns `chisel`, `needle`, `thread`, `mould`, `glassblowing pipe`.

Create `ClueRules` with:

- `Clue utility`, aliases `clue stash`, `clue stashes`, `stash`, `items used for clue stashes`, `clue tools`; patterns `saw`, `hammer`, `nails`, `plank`, `clue hunter`, `spade`, `rope`, `sextant`, `watch`, `chart`, `bullseye lantern`, `light source`, `lantern`.

Create `ProtectionRules` with:

- `Defensive shields`, aliases `shield`, `defensive gear`, `dragon protection`; patterns `shield`, `defender`, `ward`, `book of`.
- Keep `Antifire protection` in `PotionRules`; do not duplicate it here.

- [ ] **Step 9: Run tests**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest
.\gradlew.bat test
```

Expected: both commands complete with `BUILD SUCCESSFUL`.

- [ ] **Step 10: Commit expanded semantic database**

Run:

```powershell
git add src/main/java/com/semanticbanksearch/SemanticLibrary.java src/main/java/com/semanticbanksearch/TeleportRules.java src/main/java/com/semanticbanksearch/CombatRules.java src/main/java/com/semanticbanksearch/PotionRules.java src/main/java/com/semanticbanksearch/FoodRules.java src/main/java/com/semanticbanksearch/ToolRules.java src/main/java/com/semanticbanksearch/SkillingRules.java src/main/java/com/semanticbanksearch/ClueRules.java src/main/java/com/semanticbanksearch/ProtectionRules.java src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java
git commit -m "Expand local semantic rule database"
```

Expected: commit succeeds.

## Task 3: Documentation and Final Verification

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Update README**

Add a short section after the examples:

```markdown
## Views

- **Search** finds observed owned items by purpose or item-name fallback.
- **All Indexed** lists every item the plugin has observed locally, so players can check what the plugin currently knows about their bank/storage.

The bundled semantic database is local static data. It covers common teleports, potions, food, combat equipment, tools, skilling supplies, clue utility, and protection items. It is intentionally approximate and avoids runtime network calls.
```

- [ ] **Step 2: Run full verification**

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
- Safety scan finds only acceptable build metadata URLs, README/spec non-goal text, and defensive `RuntimeException` catches.
- Git status shows only intended README change before commit.

- [ ] **Step 3: Commit README**

Run:

```powershell
git add README.md
git commit -m "Document indexed view and expanded semantics"
```

Expected: commit succeeds.

- [ ] **Step 4: Push PR branch**

Run:

```powershell
git push
```

Expected: `semantic-bank-search-implementation` pushes to `origin/semantic-bank-search-implementation`.

## Self-Review

- Spec coverage: All Indexed UI, plugin mode wiring, no auto-highlight, expanded local rule helpers, representative tests, README update, safety posture, and final verification all map to tasks.
- Placeholder scan: this plan contains no unfinished markers or unresolved file references.
- Type consistency: new panel API is `updateIndexedItems(List<ObservedItem> items, String status)`; plugin callback is `showIndexedItems()`; plugin mode enum values are `SEARCH` and `ALL_INDEXED`; rule helper classes all expose `static List<SemanticRule> create()`.
