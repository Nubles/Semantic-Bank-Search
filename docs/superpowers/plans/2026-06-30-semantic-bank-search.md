# Semantic Bank Search Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a standalone local-only RuneLite plugin that searches observed owned items by purpose and highlights matching visible bank items.

**Architecture:** Create a standard RuneLite external plugin project, then build the pure Java semantic core first so it can be tested without RuneLite. Wire the tested core into a Swing side panel, persisted observed storage index, bank observer, and `WidgetItemOverlay`.

**Tech Stack:** Java 11, Gradle, RuneLite client APIs, Swing, Gson through RuneLite injection, JUnit 4.

---

## File Structure

- Create `settings.gradle`: Gradle root project name.
- Create `build.gradle`: Java/RuneLite external plugin build, dev runner, tests, and shadow jar task.
- Create `runelite-plugin.properties`: Plugin Hub metadata for `com.semanticbanksearch.SemanticBankSearchPlugin`.
- Create `.gitignore`: Ignore Gradle/build output.
- Create `README.md`: User-facing summary, safety posture, and MVP examples.
- Create `LICENSE`: MIT license matching the other local plugin projects.
- Create `src/dev/java/com/semanticbanksearch/SemanticBankSearchPluginRunner.java`: RuneLite dev launcher.
- Create `src/main/java/com/semanticbanksearch/StorageSourceType.java`: Enum for observed storage source kinds.
- Create `src/main/java/com/semanticbanksearch/ObservedItem.java`: Immutable-ish data object for one observed item/source entry.
- Create `src/main/java/com/semanticbanksearch/StorageIndex.java`: Local observed item index with add, trim, and query helpers.
- Create `src/main/java/com/semanticbanksearch/SemanticRule.java`: Rule definition for semantic categories.
- Create `src/main/java/com/semanticbanksearch/SemanticLibrary.java`: Curated local starter rules.
- Create `src/main/java/com/semanticbanksearch/SemanticSearchResult.java`: Result DTO used by panel and plugin.
- Create `src/main/java/com/semanticbanksearch/SemanticSearchEngine.java`: Pure Java query normalization, rule matching, fallback matching, and ranking.
- Create `src/main/java/com/semanticbanksearch/SemanticBankSearchStorage.java`: Gson serialization guard for the index.
- Create `src/main/java/com/semanticbanksearch/SemanticBankSearchConfig.java`: RuneLite config options.
- Create `src/main/java/com/semanticbanksearch/SemanticBankSearchOverlay.java`: Bank item highlight overlay.
- Create `src/main/java/com/semanticbanksearch/SemanticBankSearchPanel.java`: Swing side panel with search field, examples, results, and status text.
- Create `src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java`: RuneLite plugin wiring, bank observation, persistence, search callbacks, panel updates.
- Create `src/test/java/com/semanticbanksearch/StorageIndexTest.java`: Storage model tests.
- Create `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`: Semantic search tests.
- Create `src/test/java/com/semanticbanksearch/SemanticBankSearchStorageTest.java`: Serialization safety tests.

## Task 1: Project Scaffold

**Files:**
- Create: `settings.gradle`
- Create: `build.gradle`
- Create: `runelite-plugin.properties`
- Create: `.gitignore`
- Create: `README.md`
- Create: `LICENSE`
- Create: `src/dev/java/com/semanticbanksearch/SemanticBankSearchPluginRunner.java`

- [ ] **Step 1: Create Gradle settings**

Write `settings.gradle`:

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = 'semantic-bank-search'
```

- [ ] **Step 2: Create Gradle build**

Write `build.gradle`:

```groovy
plugins {
    id 'java'
}

repositories {
    mavenLocal()
    maven {
        url = 'https://repo.runelite.net'
        content {
            includeGroupByRegex("net\\.runelite.*")
        }
    }
    mavenCentral()
}

def runeLiteVersion = 'latest.release'
def pluginMainClass = 'com.semanticbanksearch.SemanticBankSearchPluginRunner'

sourceSets {
    dev {
        java.srcDir 'src/dev/java'
        compileClasspath += sourceSets.main.output
        runtimeClasspath += output + compileClasspath
    }
}

dependencies {
    compileOnly group: 'net.runelite', name: 'client', version: runeLiteVersion
    devImplementation group: 'net.runelite', name: 'client', version: runeLiteVersion
    devImplementation group: 'net.runelite', name: 'jshell', version: runeLiteVersion
    testImplementation 'junit:junit:4.13.2'
}

group = 'com.semanticbanksearch'
version = '1.0.0'

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
    options.release.set(11)
}

tasks.register('run', JavaExec) {
    classpath = sourceSets.dev.runtimeClasspath
    mainClass = pluginMainClass
    jvmArgs '-ea'
    args '--developer-mode', '--debug'
}

tasks.register('shadowJar', Jar) {
    dependsOn configurations.devRuntimeClasspath
    manifest {
        attributes('Main-Class': pluginMainClass, 'Multi-Release': true)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from sourceSets.main.output
    from sourceSets.dev.output
    from {
        configurations.devRuntimeClasspath.collect { file ->
            file.isDirectory() ? file : zipTree(file)
        }
    }
    exclude 'META-INF/INDEX.LIST'
    exclude 'META-INF/*.SF'
    exclude 'META-INF/*.DSA'
    exclude 'META-INF/*.RSA'
    exclude '**/module-info.class'
    group = BasePlugin.BUILD_GROUP
    archiveClassifier.set('shadow')
    archiveFileName.set("${rootProject.name}-${project.version}-all.jar")
}
```

- [ ] **Step 3: Create plugin metadata**

Write `runelite-plugin.properties`:

```properties
displayName=Semantic Bank Search
author=Nubles
description=Searches observed bank and storage items by purpose using local semantic rules.
tags=bank,search,items,storage,utility
plugins=com.semanticbanksearch.SemanticBankSearchPlugin
version=
build=standard
```

- [ ] **Step 4: Create ignore rules**

Write `.gitignore`:

```gitignore
.gradle/
build/
out/
*.iml
```

- [ ] **Step 5: Create README**

Write `README.md`:

```markdown
# Semantic Bank Search

A RuneLite external plugin that lets players search observed owned items by purpose instead of exact item name.

Example searches:

- teleport near barrows
- crush weapons
- poison protection
- prayer restoration
- items used for clue stashes
- warm clothing
- things that cut webs
- fastest food I own

The plugin is local-only. It reads visible bank items, remembers observed storage locally, and highlights matching visible bank items. It does not click, withdraw, deposit, move, tag, modify menus, use external services, or send bank contents anywhere.
```

- [ ] **Step 6: Create license**

Write `LICENSE`:

```text
MIT License

Copyright (c) 2026 Nubles

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

- [ ] **Step 7: Create dev runner**

Write `src/dev/java/com/semanticbanksearch/SemanticBankSearchPluginRunner.java`:

```java
package com.semanticbanksearch;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SemanticBankSearchPluginRunner
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(SemanticBankSearchPlugin.class);
        RuneLite.main(args);
    }
}
```

- [ ] **Step 8: Commit scaffold**

Run:

```bash
git add settings.gradle build.gradle runelite-plugin.properties .gitignore README.md LICENSE src/dev/java/com/semanticbanksearch/SemanticBankSearchPluginRunner.java
git commit -m "Add Semantic Bank Search scaffold"
```

Expected: commit succeeds.

## Task 2: Storage Index Model

**Files:**
- Create: `src/test/java/com/semanticbanksearch/StorageIndexTest.java`
- Create: `src/main/java/com/semanticbanksearch/StorageSourceType.java`
- Create: `src/main/java/com/semanticbanksearch/ObservedItem.java`
- Create: `src/main/java/com/semanticbanksearch/StorageIndex.java`

- [ ] **Step 1: Write failing storage tests**

Write `src/test/java/com/semanticbanksearch/StorageIndexTest.java`:

```java
package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class StorageIndexTest
{
    @Test
    public void recordsObservedBankItemsByCanonicalItemAndSource()
    {
        StorageIndex index = new StorageIndex();

        index.record(100, "Prayer potion(4)", 3, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<ObservedItem> items = index.items();
        assertEquals(1, items.size());
        assertEquals(100, items.get(0).getItemId());
        assertEquals("Prayer potion(4)", items.get(0).getName());
        assertEquals(3, items.get(0).getQuantity());
        assertEquals(StorageSourceType.BANK, items.get(0).getSourceType());
        assertTrue(items.get(0).isCurrentlyVisible());
    }

    @Test
    public void updatesExistingItemSourceInsteadOfDuplicatingIt()
    {
        StorageIndex index = new StorageIndex();

        index.record(100, "Prayer potion(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(100, "Prayer potion(4)", 5, StorageSourceType.BANK, "Bank", true, 2_000L);

        assertEquals(1, index.items().size());
        assertEquals(5, index.items().get(0).getQuantity());
        assertEquals(2_000L, index.items().get(0).getLastSeenMillis());
    }

    @Test
    public void clearsVisibilityForSourceWhenBankIsClosed()
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "Prayer potion(4)", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        index.markSourceNotVisible(StorageSourceType.BANK, "Bank");

        assertFalse(index.items().get(0).isCurrentlyVisible());
    }

    @Test
    public void trimsOldEntriesByMaximumCount()
    {
        StorageIndex index = new StorageIndex();
        index.record(100, "Old item", 1, StorageSourceType.BANK, "Bank", false, 1_000L);
        index.record(101, "Middle item", 1, StorageSourceType.BANK, "Bank", false, 2_000L);
        index.record(102, "New item", 1, StorageSourceType.BANK, "Bank", false, 3_000L);

        index.trimToMaximumEntries(2);

        assertEquals(2, index.items().size());
        assertEquals(101, index.items().get(0).getItemId());
        assertEquals(102, index.items().get(1).getItemId());
    }
}
```

- [ ] **Step 2: Run storage tests to verify they fail**

Run:

```bash
./gradlew test --tests com.semanticbanksearch.StorageIndexTest
```

Expected: compilation fails because `StorageIndex`, `ObservedItem`, and `StorageSourceType` do not exist.

- [ ] **Step 3: Implement storage source enum**

Write `src/main/java/com/semanticbanksearch/StorageSourceType.java`:

```java
package com.semanticbanksearch;

public enum StorageSourceType
{
    BANK,
    POH_STORAGE,
    OTHER_STORAGE
}
```

- [ ] **Step 4: Implement observed item DTO**

Write `src/main/java/com/semanticbanksearch/ObservedItem.java`:

```java
package com.semanticbanksearch;

public class ObservedItem
{
    private int itemId;
    private String name;
    private int quantity;
    private StorageSourceType sourceType;
    private String sourceName;
    private boolean currentlyVisible;
    private long lastSeenMillis;

    public ObservedItem()
    {
        this(0, "", 0, StorageSourceType.OTHER_STORAGE, "", false, 0L);
    }

    ObservedItem(
        int itemId,
        String name,
        int quantity,
        StorageSourceType sourceType,
        String sourceName,
        boolean currentlyVisible,
        long lastSeenMillis)
    {
        this.itemId = itemId;
        this.name = name == null ? "" : name.trim();
        this.quantity = Math.max(0, quantity);
        this.sourceType = sourceType == null ? StorageSourceType.OTHER_STORAGE : sourceType;
        this.sourceName = sourceName == null ? "" : sourceName.trim();
        this.currentlyVisible = currentlyVisible;
        this.lastSeenMillis = Math.max(0L, lastSeenMillis);
    }

    public int getItemId()
    {
        return itemId;
    }

    public String getName()
    {
        return name;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public StorageSourceType getSourceType()
    {
        return sourceType;
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public boolean isCurrentlyVisible()
    {
        return currentlyVisible;
    }

    public long getLastSeenMillis()
    {
        return lastSeenMillis;
    }

    String key()
    {
        return itemId + "|" + sourceType + "|" + sourceName.toLowerCase();
    }

    void updateFrom(ObservedItem replacement)
    {
        name = replacement.name;
        quantity = replacement.quantity;
        sourceType = replacement.sourceType;
        sourceName = replacement.sourceName;
        currentlyVisible = replacement.currentlyVisible;
        lastSeenMillis = replacement.lastSeenMillis;
    }

    void setCurrentlyVisible(boolean currentlyVisible)
    {
        this.currentlyVisible = currentlyVisible;
    }
}
```

- [ ] **Step 5: Implement storage index**

Write `src/main/java/com/semanticbanksearch/StorageIndex.java`:

```java
package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class StorageIndex
{
    private List<ObservedItem> items = new ArrayList<>();

    public List<ObservedItem> items()
    {
        items.sort(Comparator.comparingLong(ObservedItem::getLastSeenMillis));
        return new ArrayList<>(items);
    }

    public void record(
        int itemId,
        String name,
        int quantity,
        StorageSourceType sourceType,
        String sourceName,
        boolean currentlyVisible,
        long lastSeenMillis)
    {
        if (itemId <= 0 || name == null || name.trim().isEmpty())
        {
            return;
        }

        ObservedItem observed = new ObservedItem(
            itemId,
            name,
            quantity,
            sourceType,
            sourceName,
            currentlyVisible,
            lastSeenMillis);

        for (ObservedItem existing : items)
        {
            if (existing.key().equals(observed.key()))
            {
                existing.updateFrom(observed);
                return;
            }
        }

        items.add(observed);
    }

    public void markSourceNotVisible(StorageSourceType sourceType, String sourceName)
    {
        String normalizedSource = sourceName == null ? "" : sourceName.trim();
        for (ObservedItem item : items)
        {
            if (item.getSourceType() == sourceType && item.getSourceName().equals(normalizedSource))
            {
                item.setCurrentlyVisible(false);
            }
        }
    }

    public void trimToMaximumEntries(int maximumEntries)
    {
        if (maximumEntries <= 0)
        {
            items.clear();
            return;
        }

        items.sort(Comparator.comparingLong(ObservedItem::getLastSeenMillis));
        while (items.size() > maximumEntries)
        {
            Iterator<ObservedItem> iterator = items.iterator();
            iterator.next();
            iterator.remove();
        }
    }
}
```

- [ ] **Step 6: Run storage tests to verify they pass**

Run:

```bash
./gradlew test --tests com.semanticbanksearch.StorageIndexTest
```

Expected: tests pass.

- [ ] **Step 7: Commit storage model**

Run:

```bash
git add src/main/java/com/semanticbanksearch/StorageSourceType.java src/main/java/com/semanticbanksearch/ObservedItem.java src/main/java/com/semanticbanksearch/StorageIndex.java src/test/java/com/semanticbanksearch/StorageIndexTest.java
git commit -m "Add observed storage index"
```

Expected: commit succeeds.

## Task 3: Semantic Search Core

**Files:**
- Create: `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`
- Create: `src/main/java/com/semanticbanksearch/SemanticRule.java`
- Create: `src/main/java/com/semanticbanksearch/SemanticLibrary.java`
- Create: `src/main/java/com/semanticbanksearch/SemanticSearchResult.java`
- Create: `src/main/java/com/semanticbanksearch/SemanticSearchEngine.java`

- [ ] **Step 1: Write failing semantic search tests**

Write `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java` with tests named:

```java
package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class SemanticSearchEngineTest
{
    @Test
    public void prayerRestorationFindsOwnedPrayerPotions()
    {
        StorageIndex index = new StorageIndex();
        index.record(1, "Prayer potion(4)", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(2, "Shark", 5, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("prayer restoration", index);

        assertEquals(1, results.size());
        assertEquals("Prayer potion(4)", results.get(0).getItemName());
        assertEquals("Prayer restoration", results.get(0).getCategory());
    }

    @Test
    public void warmClothingFindsOwnedWarmItems()
    {
        StorageIndex index = new StorageIndex();
        index.record(10, "Clue hunter garb", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(11, "Monk's robe", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("warm clothing", index);

        assertEquals(1, results.size());
        assertEquals("Clue hunter garb", results.get(0).getItemName());
    }

    @Test
    public void webCuttersFindKnivesAndSlashTools()
    {
        StorageIndex index = new StorageIndex();
        index.record(20, "Knife", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(21, "Dragon scimitar", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(22, "Air staff", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("things that cut webs", index);

        assertEquals(2, results.size());
        assertTrue(names(results).contains("Knife"));
        assertTrue(names(results).contains("Dragon scimitar"));
        assertFalse(names(results).contains("Air staff"));
    }

    @Test
    public void crushWeaponsFindOwnedCrushItems()
    {
        StorageIndex index = new StorageIndex();
        index.record(30, "Dragon mace", 1, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(31, "Rune scimitar", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("crush weapons", index);

        assertEquals(1, results.size());
        assertEquals("Dragon mace", results.get(0).getItemName());
    }

    @Test
    public void fastestFoodRanksHigherHealingFoodFirst()
    {
        StorageIndex index = new StorageIndex();
        index.record(40, "Trout", 10, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(41, "Shark", 10, StorageSourceType.BANK, "Bank", true, 1_000L);
        index.record(42, "Manta ray", 10, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("fastest food I own", index);

        assertEquals("Manta ray", results.get(0).getItemName());
        assertEquals("Shark", results.get(1).getItemName());
        assertEquals("Trout", results.get(2).getItemName());
    }

    @Test
    public void itemNameFallbackStillWorks()
    {
        StorageIndex index = new StorageIndex();
        index.record(50, "Barrows teleport", 2, StorageSourceType.BANK, "Bank", true, 1_000L);

        List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("barrows tele", index);

        assertEquals(1, results.size());
        assertEquals("Barrows teleport", results.get(0).getItemName());
    }

    private static String names(List<SemanticSearchResult> results)
    {
        StringBuilder builder = new StringBuilder();
        for (SemanticSearchResult result : results)
        {
            builder.append(result.getItemName()).append("\n");
        }
        return builder.toString();
    }
}
```

- [ ] **Step 2: Run semantic tests to verify they fail**

Run:

```bash
./gradlew test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: compilation fails because semantic classes do not exist.

- [ ] **Step 3: Implement semantic result DTO**

Create `SemanticSearchResult` with constructor fields: `itemId`, `itemName`, `quantity`, `sourceType`, `sourceName`, `currentlyVisible`, `category`, `reason`, `score`. Add getters for all fields and `isHighlightable()` returning `currentlyVisible && sourceType == StorageSourceType.BANK`.

- [ ] **Step 4: Implement semantic rule DTO**

Create `SemanticRule` with constructor fields: `category`, `reason`, `queryAliases`, `itemNamePatterns`, `foodScores`, `baseScore`. Add `matchesQuery(String normalizedQuery)`, `matchesItem(String normalizedItemName)`, and `scoreFor(String normalizedItemName)`. Matching is case-insensitive because callers pass normalized lowercase text.

- [ ] **Step 5: Implement starter semantic library**

Create `SemanticLibrary.create()` returning rules for:

```text
Prayer restoration: aliases prayer, restore prayer, prayer restoration, ppot; patterns prayer potion, super restore, sanfew serum, blighted super restore.
Warm clothing: aliases warm clothing, wintertodt clothing, cold protection; patterns clue hunter, pyromancer, warm gloves, fire cape, infernal cape, santa hat, bomber jacket.
Web cutters: aliases web, webs, cut webs, things that cut webs, slash; patterns knife, scimitar, sword, slash, dagger, machete, axe.
Crush weapons: aliases crush, crush weapon, crush weapons; patterns mace, warhammer, maul, anchor, hasta, godsword, bludgeon.
Food: aliases food, fastest food, best food, healing, heal; patterns manta ray, shark, karambwan, anglerfish, sea turtle, monkfish, trout, salmon, lobster, swordfish, tuna potato; scores manta ray 22, shark 20, sea turtle 21, anglerfish 22, karambwan 18, monkfish 16, swordfish 14, lobster 12, salmon 9, trout 7.
Poison protection: aliases poison, venom, poison protection, antipoison; patterns antipoison, anti-venom, sanfew serum, antidote.
Barrows teleports: aliases teleport near barrows, barrows teleport, mortton teleport, morytania teleport; patterns barrows teleport, mort'ton teleport, morytania legs, shades of mort'ton, drakan's medallion.
Clue utility: aliases clue stash, clue stashes, stash, items used for clue stashes; patterns saw, hammer, nails, plank, clue hunter, spade, rope, light source, lantern.
Utility tools: aliases tool, utility, light, rope, spade, lockpick; patterns spade, rope, lockpick, lantern, candle, tinderbox, chisel, pestle and mortar.
```

- [ ] **Step 6: Implement semantic search engine**

Create `SemanticSearchEngine` with:

```java
public List<SemanticSearchResult> search(String query, StorageIndex index)
```

Implementation rules:

- Normalize query and item names by lowercasing, removing punctuation except apostrophes inside words, collapsing spaces, and trimming.
- Return an empty list for blank query or null index.
- Find rules whose aliases appear in the normalized query or whose category words appear in the query.
- For each observed item, create results for matching rules.
- If no rule matches the query, use fallback item-name matching where every normalized query token must appear in the normalized item name, allowing prefix matches like `tele` for `teleport`.
- Sort by score descending, currently visible first when scores tie, then item name ascending.
- Avoid duplicate item/source/category results.

- [ ] **Step 7: Run semantic tests to verify they pass**

Run:

```bash
./gradlew test --tests com.semanticbanksearch.SemanticSearchEngineTest
```

Expected: tests pass.

- [ ] **Step 8: Commit semantic core**

Run:

```bash
git add src/main/java/com/semanticbanksearch/SemanticRule.java src/main/java/com/semanticbanksearch/SemanticLibrary.java src/main/java/com/semanticbanksearch/SemanticSearchResult.java src/main/java/com/semanticbanksearch/SemanticSearchEngine.java src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java
git commit -m "Add local semantic search engine"
```

Expected: commit succeeds.

## Task 4: Storage Serialization

**Files:**
- Create: `src/test/java/com/semanticbanksearch/SemanticBankSearchStorageTest.java`
- Create: `src/main/java/com/semanticbanksearch/SemanticBankSearchStorage.java`

- [ ] **Step 1: Write failing serialization tests**

Write tests proving:

```java
StorageIndex loaded = SemanticBankSearchStorage.deserialize(new Gson(), "{bad json");
assertTrue(loaded.items().isEmpty());
```

and:

```java
StorageIndex index = new StorageIndex();
index.record(100, "Prayer potion(4)", 2, StorageSourceType.BANK, "Bank", true, 1_000L);
String json = SemanticBankSearchStorage.serialize(new Gson(), index);
StorageIndex loaded = SemanticBankSearchStorage.deserialize(new Gson(), json);
assertEquals("Prayer potion(4)", loaded.items().get(0).getName());
```

- [ ] **Step 2: Run serialization tests to verify they fail**

Run:

```bash
./gradlew test --tests com.semanticbanksearch.SemanticBankSearchStorageTest
```

Expected: compilation fails because `SemanticBankSearchStorage` does not exist.

- [ ] **Step 3: Implement storage serialization guard**

Create `SemanticBankSearchStorage` with static methods:

```java
static String serialize(Gson gson, StorageIndex index)
static StorageIndex deserialize(Gson gson, String json)
```

Rules:

- Null `gson` returns an empty string or empty index.
- Null `index` serializes an empty `StorageIndex`.
- Null or blank JSON deserializes an empty `StorageIndex`.
- `JsonSyntaxException` and `RuntimeException` during deserialize return an empty `StorageIndex`.

- [ ] **Step 4: Run serialization tests to verify they pass**

Run:

```bash
./gradlew test --tests com.semanticbanksearch.SemanticBankSearchStorageTest
```

Expected: tests pass.

- [ ] **Step 5: Commit serialization**

Run:

```bash
git add src/main/java/com/semanticbanksearch/SemanticBankSearchStorage.java src/test/java/com/semanticbanksearch/SemanticBankSearchStorageTest.java
git commit -m "Add local storage serialization"
```

Expected: commit succeeds.

## Task 5: RuneLite UI and Overlay

**Files:**
- Create: `src/main/java/com/semanticbanksearch/SemanticBankSearchConfig.java`
- Create: `src/main/java/com/semanticbanksearch/SemanticBankSearchOverlay.java`
- Create: `src/main/java/com/semanticbanksearch/SemanticBankSearchPanel.java`

- [ ] **Step 1: Create config**

Write `SemanticBankSearchConfig` with group `semanticbanksearch` and options:

```java
boolean enableHighlights(); // default true
boolean rememberObservedStorage(); // default true
int maximumRememberedEntries(); // default 800, range 100 to 2000
```

- [ ] **Step 2: Create overlay**

Write `SemanticBankSearchOverlay` extending `WidgetItemOverlay`, calling `showOnBank()` in the constructor. Add `setHighlightedItemIds(Collection<Integer> itemIds)`. In `renderItemOverlay`, return unless highlights are enabled and the item ID is highlighted. Draw a translucent blue fill and bright blue border around `widgetItem.getCanvasBounds()` when bounds are not null.

- [ ] **Step 3: Create panel**

Write `SemanticBankSearchPanel` extending `PluginPanel` with:

- `JTextField searchField`
- example buttons for `teleport near barrows`, `crush weapons`, `poison protection`, `prayer restoration`, `warm clothing`, `things that cut webs`, `fastest food I own`
- `updateResults(String query, List<SemanticSearchResult> results, String status)`
- `clearResults()`

The constructor accepts `Consumer<String> searchConsumer` and `Runnable clearConsumer`. Pressing Enter or an example button runs the search callback. The clear button clears the text field, calls `clearConsumer`, and renders starter text.

- [ ] **Step 4: Compile UI classes**

Run:

```bash
./gradlew compileJava
```

Expected: compile succeeds.

- [ ] **Step 5: Commit UI and overlay**

Run:

```bash
git add src/main/java/com/semanticbanksearch/SemanticBankSearchConfig.java src/main/java/com/semanticbanksearch/SemanticBankSearchOverlay.java src/main/java/com/semanticbanksearch/SemanticBankSearchPanel.java
git commit -m "Add search panel and bank overlay"
```

Expected: commit succeeds.

## Task 6: RuneLite Plugin Wiring

**Files:**
- Create: `src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java`
- Modify: `src/dev/java/com/semanticbanksearch/SemanticBankSearchPluginRunner.java`

- [ ] **Step 1: Create plugin skeleton**

Write `SemanticBankSearchPlugin` annotated with:

```java
@PluginDescriptor(
    name = "Semantic Bank Search",
    description = "Searches observed bank and storage items by purpose using local semantic rules.",
    tags = {"bank", "search", "items", "storage", "utility"}
)
```

Inject `Client`, `ClientToolbar`, `ConfigManager`, `OverlayManager`, `ItemManager`, `SemanticBankSearchConfig`, and `Gson`.

- [ ] **Step 2: Wire startup and shutdown**

On startup:

- load `StorageIndex` from config key `index`
- create `SemanticSearchEngine(SemanticLibrary.create())`
- create overlay and panel
- add overlay and navigation button
- observe the bank immediately if open

On shutdown:

- persist index
- remove overlay and navigation button
- null panel, overlay, engine, and navigation button

- [ ] **Step 3: Observe bank items**

On each `GameTick`, check `client.getWidget(InterfaceID.BANK, 1)`. If bank is visible and remembering is enabled:

- get bank item container from `client.getItemContainer(InventoryID.BANK)`
- canonicalize item IDs through `itemManager.canonicalize`
- resolve item names through `itemManager.getItemComposition(canonicalId).getName()`
- record each positive ID and non-empty name in `StorageIndex` with `StorageSourceType.BANK`, source name `Bank`, visible `true`, and current time
- trim to config maximum entries
- persist periodically

When the bank closes, call `index.markSourceNotVisible(StorageSourceType.BANK, "Bank")`, refresh current search results, and persist.

- [ ] **Step 4: Wire searching and highlights**

Add `runSearch(String query)`:

- save current query
- if blank, clear panel and overlay
- otherwise call engine, update panel with results, and set overlay IDs from `result.isHighlightable()`
- status is `Open the bank to see visible item highlights.` when bank is closed and there are results

Add `clearSearch()`:

- clear current query
- send empty ID list to overlay
- render panel starter text

- [ ] **Step 5: Compile plugin wiring**

Run:

```bash
./gradlew compileJava
```

Expected: compile succeeds.

- [ ] **Step 6: Commit plugin wiring**

Run:

```bash
git add src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java src/dev/java/com/semanticbanksearch/SemanticBankSearchPluginRunner.java
git commit -m "Wire Semantic Bank Search plugin"
```

Expected: commit succeeds.

## Task 7: Full Verification and Documentation Polish

**Files:**
- Modify: `README.md`
- Modify only if needed: implementation files from previous tasks

- [ ] **Step 1: Run all tests**

Run:

```bash
./gradlew test
```

Expected: all tests pass.

- [ ] **Step 2: Run compile**

Run:

```bash
./gradlew compileJava
```

Expected: compile succeeds without errors.

- [ ] **Step 3: Build shadow jar**

Run:

```bash
./gradlew shadowJar
```

Expected: jar created under `build/libs/`.

- [ ] **Step 4: Review safety posture**

Search source for forbidden behavior:

```bash
rg "http|https|Socket|Robot|MenuEntry|setMenuEntries|invokeMenuAction|withdraw|deposit|Runtime|getRuntime|ProcessBuilder|reflection|ClassLoader"
```

Expected: no network, automation, menu mutation, subprocess, reflection, or runtime download behavior. Occurrences in README/spec text are acceptable only when describing non-goals.

- [ ] **Step 5: Polish README**

Ensure `README.md` includes:

- what the plugin does
- example searches
- local-only privacy note
- limitation that remembered storage only includes what the client has observed
- no-automation safety note

- [ ] **Step 6: Commit final polish**

Run:

```bash
git add README.md
git commit -m "Document Semantic Bank Search usage"
```

Expected: commit succeeds if README changed. If README is already complete, skip the commit and note that no documentation changes were needed.

- [ ] **Step 7: Final status**

Run:

```bash
git status --short
```

Expected: clean working tree.

## Self-Review

- Spec coverage: scaffold, storage index, semantic engine, serialization, UI panel, overlay, plugin wiring, privacy posture, tests, and verification are all represented by tasks.
- Placeholder scan: no unfinished markers or unresolved file references are intended in this plan.
- Type consistency: package is consistently `com.semanticbanksearch`; plugin class is `SemanticBankSearchPlugin`; runner class is `SemanticBankSearchPluginRunner`; storage key is `index`; config group is `semanticbanksearch`.
