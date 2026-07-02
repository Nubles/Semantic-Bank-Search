# Safe Observed Storage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add safe, read-only observation for clearly identified non-bank storage containers so Semantic Bank Search can search remembered Seed Vault and Group Storage items alongside bank items.

**Architecture:** Add a scanner layer that reads only allow-listed `InventoryID` containers when their matching storage widget component is visible. Keep `SemanticBankSearchPlugin` responsible for lifecycle, persistence, panel refresh, and bank-only highlighting. POH storage remains represented in the model but is not activated until a reliable widget-item extraction path is proven.

**Tech Stack:** Java 11, RuneLite client APIs, Gradle, JUnit 4.

---

## Scope Decisions

- Implement direct safe container observation for `InventoryID.BANK`, `InventoryID.SEED_VAULT`, and `InventoryID.GROUP_STORAGE`.
- Gate each direct container by a visible packed `ComponentID` so cached item containers are not treated as open storage.
- Keep highlighting bank-only. Non-bank storage results remain searchable and listed, but not highlightable.
- Do not index trade, shops, reward chests, loot chests, inventory, equipment, deposit box inventory, or temporary minigame containers.
- Do not implement POH widget scraping in this plan. The API exposes `ComponentID.POH_TREASURE_CHEST_INV_CONTAINER`, but POH has no direct `InventoryID` in the local RuneLite API, so it needs a separate widget-item extraction design.

## File Structure

- Create `src/main/java/com/semanticbanksearch/ObservedStorageSource.java`: allow-listed source descriptor and source key helpers.
- Create `src/main/java/com/semanticbanksearch/ObservedStorageSnapshot.java`: one visible source plus the items observed in it.
- Create `src/main/java/com/semanticbanksearch/ObservedStorageScanner.java`: visibility-gated direct `InventoryID` item-container scanner.
- Modify `src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java`: use scanner for bank, Seed Vault, and Group Storage; mark sources not visible when widgets disappear.
- Modify `README.md`: document active safe storage sources and POH follow-up.
- Create `src/test/java/com/semanticbanksearch/ObservedStorageSourceTest.java`.
- Create `src/test/java/com/semanticbanksearch/ObservedStorageScannerTest.java`.
- Modify `src/test/java/com/semanticbanksearch/StorageIndexTest.java`.
- Modify `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`.
- Modify `src/test/java/com/semanticbanksearch/SemanticBankSearchStorageTest.java`.

## Task 1: Source Descriptors And Scanner

**Files:**
- Create: `src/main/java/com/semanticbanksearch/ObservedStorageSource.java`
- Create: `src/main/java/com/semanticbanksearch/ObservedStorageSnapshot.java`
- Create: `src/main/java/com/semanticbanksearch/ObservedStorageScanner.java`
- Create: `src/test/java/com/semanticbanksearch/ObservedStorageSourceTest.java`
- Create: `src/test/java/com/semanticbanksearch/ObservedStorageScannerTest.java`

- [ ] **Step 1: Write failing source allow-list tests**

Create `src/test/java/com/semanticbanksearch/ObservedStorageSourceTest.java`:

```java
package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import net.runelite.api.InventoryID;
import net.runelite.api.widgets.ComponentID;
import org.junit.Test;

public class ObservedStorageSourceTest
{
    @Test
    public void safeDirectSourcesIncludeOnlyStableStorageContainers()
    {
        List<ObservedStorageSource> sources = ObservedStorageSource.safeDirectInventorySources();

        assertEquals(3, sources.size());
        assertTrue(hasSource(sources, InventoryID.BANK, ComponentID.BANK_ITEM_CONTAINER, StorageSourceType.BANK, "Bank"));
        assertTrue(hasSource(sources, InventoryID.SEED_VAULT, ComponentID.SEED_VAULT_ITEM_CONTAINER, StorageSourceType.OTHER_STORAGE, "Seed Vault"));
        assertTrue(hasSource(sources, InventoryID.GROUP_STORAGE, ComponentID.GROUP_STORAGE_ITEM_CONTAINER, StorageSourceType.OTHER_STORAGE, "Group Storage"));
    }

    @Test
    public void unsafeContainersAreNotAllowListed()
    {
        List<ObservedStorageSource> sources = ObservedStorageSource.safeDirectInventorySources();

        assertFalse(hasInventory(sources, InventoryID.TRADE));
        assertFalse(hasInventory(sources, InventoryID.TRADEOTHER));
        assertFalse(hasInventory(sources, InventoryID.INVENTORY));
        assertFalse(hasInventory(sources, InventoryID.EQUIPMENT));
        assertFalse(hasInventory(sources, InventoryID.BARROWS_REWARD));
        assertFalse(hasInventory(sources, InventoryID.TOA_REWARD_CHEST));
        assertFalse(hasInventory(sources, InventoryID.WILDERNESS_LOOT_CHEST));
    }

    private static boolean hasSource(
        List<ObservedStorageSource> sources,
        InventoryID inventoryId,
        int visibleComponentId,
        StorageSourceType sourceType,
        String sourceName)
    {
        for (ObservedStorageSource source : sources)
        {
            if (source.getInventoryId() == inventoryId
                && source.getVisibleComponentId() == visibleComponentId
                && source.getSourceType() == sourceType
                && source.getSourceName().equals(sourceName))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean hasInventory(List<ObservedStorageSource> sources, InventoryID inventoryId)
    {
        for (ObservedStorageSource source : sources)
        {
            if (source.getInventoryId() == inventoryId)
            {
                return true;
            }
        }
        return false;
    }
}
```

- [ ] **Step 2: Write failing scanner tests**

Create `src/test/java/com/semanticbanksearch/ObservedStorageScannerTest.java`:

```java
package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Node;
import net.runelite.api.widgets.ComponentID;
import org.junit.Test;

public class ObservedStorageScannerTest
{
    @Test
    public void scansVisibleSafeDirectContainers()
    {
        Map<InventoryID, ItemContainer> containers = new HashMap<>();
        containers.put(InventoryID.SEED_VAULT, new FakeItemContainer(
            new Item(100, 7),
            new Item(101, 0),
            new Item(-1, 1),
            null));

        ObservedStorageScanner scanner = new ObservedStorageScanner(
            containers::get,
            componentId -> componentId == ComponentID.SEED_VAULT_ITEM_CONTAINER,
            itemId -> itemId + 10_000,
            itemId -> itemId == 10_100 ? "Ranarr seed" : "");

        List<ObservedStorageSnapshot> snapshots = scanner.scan(
            List.of(ObservedStorageSource.seedVault()),
            1_000L);

        assertEquals(1, snapshots.size());
        assertEquals(StorageSourceType.OTHER_STORAGE, snapshots.get(0).getSource().getSourceType());
        assertEquals("Seed Vault", snapshots.get(0).getSource().getSourceName());
        assertEquals(1, snapshots.get(0).getItems().size());
        assertEquals(10_100, snapshots.get(0).getItems().get(0).getItemId());
        assertEquals("Ranarr seed", snapshots.get(0).getItems().get(0).getName());
        assertEquals(7, snapshots.get(0).getItems().get(0).getQuantity());
        assertTrue(snapshots.get(0).getItems().get(0).isCurrentlyVisible());
    }

    @Test
    public void hiddenContainersDoNotProduceSnapshotsEvenWhenContainerIsCached()
    {
        Map<InventoryID, ItemContainer> containers = new HashMap<>();
        containers.put(InventoryID.GROUP_STORAGE, new FakeItemContainer(new Item(200, 1)));

        ObservedStorageScanner scanner = new ObservedStorageScanner(
            containers::get,
            componentId -> false,
            itemId -> itemId,
            itemId -> "Dragon axe");

        List<ObservedStorageSnapshot> snapshots = scanner.scan(
            List.of(ObservedStorageSource.groupStorage()),
            1_000L);

        assertTrue(snapshots.isEmpty());
    }

    @Test
    public void absentContainersDoNotProduceSnapshots()
    {
        ObservedStorageScanner scanner = new ObservedStorageScanner(
            inventoryId -> null,
            componentId -> true,
            itemId -> itemId,
            itemId -> "Item " + itemId);

        List<ObservedStorageSnapshot> snapshots = scanner.scan(
            List.of(ObservedStorageSource.groupStorage()),
            1_000L);

        assertTrue(snapshots.isEmpty());
    }

    @Test
    public void returnedSnapshotsCannotMutateScannerState()
    {
        Map<InventoryID, ItemContainer> containers = new HashMap<>();
        containers.put(InventoryID.GROUP_STORAGE, new FakeItemContainer(new Item(200, 1)));

        ObservedStorageScanner scanner = new ObservedStorageScanner(
            containers::get,
            componentId -> componentId == ComponentID.GROUP_STORAGE_ITEM_CONTAINER,
            itemId -> itemId,
            itemId -> "Dragon axe");

        ObservedStorageSnapshot snapshot = scanner.scan(
            List.of(ObservedStorageSource.groupStorage()),
            1_000L).get(0);

        snapshot.getItems().clear();

        List<ObservedStorageSnapshot> snapshots = scanner.scan(
            List.of(ObservedStorageSource.groupStorage()),
            1_000L);
        assertEquals(1, snapshots.get(0).getItems().size());
        assertFalse(snapshots.get(0).getItems().isEmpty());
    }

    private static final class FakeItemContainer implements ItemContainer
    {
        private final Item[] items;

        private FakeItemContainer(Item... items)
        {
            this.items = items;
        }

        @Override
        public int getId()
        {
            return 0;
        }

        @Override
        public Item[] getItems()
        {
            return items;
        }

        @Override
        public Item getItem(int index)
        {
            return items[index];
        }

        @Override
        public boolean contains(int itemId)
        {
            return find(itemId) >= 0;
        }

        @Override
        public int count(int itemId)
        {
            int count = 0;
            for (Item item : items)
            {
                if (item != null && item.getId() == itemId)
                {
                    count += item.getQuantity();
                }
            }
            return count;
        }

        @Override
        public int size()
        {
            return items.length;
        }

        @Override
        public int count()
        {
            return items.length;
        }

        @Override
        public int find(int itemId)
        {
            for (int i = 0; i < items.length; i++)
            {
                if (items[i] != null && items[i].getId() == itemId)
                {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public Node getNext()
        {
            return null;
        }

        @Override
        public Node getPrevious()
        {
            return null;
        }

        @Override
        public long getHash()
        {
            return 0L;
        }
    }
}
```

- [ ] **Step 3: Run scanner tests to verify red state**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.ObservedStorageSourceTest --tests com.semanticbanksearch.ObservedStorageScannerTest
```

Expected: compilation fails because the new source, snapshot, and scanner classes do not exist.

- [ ] **Step 4: Create `ObservedStorageSource`**

Create `src/main/java/com/semanticbanksearch/ObservedStorageSource.java`:

```java
package com.semanticbanksearch;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.InventoryID;
import net.runelite.api.widgets.ComponentID;

final class ObservedStorageSource
{
    private static final ObservedStorageSource BANK = directInventory(
        InventoryID.BANK,
        ComponentID.BANK_ITEM_CONTAINER,
        StorageSourceType.BANK,
        "Bank");
    private static final ObservedStorageSource SEED_VAULT = directInventory(
        InventoryID.SEED_VAULT,
        ComponentID.SEED_VAULT_ITEM_CONTAINER,
        StorageSourceType.OTHER_STORAGE,
        "Seed Vault");
    private static final ObservedStorageSource GROUP_STORAGE = directInventory(
        InventoryID.GROUP_STORAGE,
        ComponentID.GROUP_STORAGE_ITEM_CONTAINER,
        StorageSourceType.OTHER_STORAGE,
        "Group Storage");
    private static final List<ObservedStorageSource> SAFE_DIRECT_INVENTORY_SOURCES =
        Collections.unmodifiableList(Arrays.asList(BANK, SEED_VAULT, GROUP_STORAGE));

    private final InventoryID inventoryId;
    private final int visibleComponentId;
    private final StorageSourceType sourceType;
    private final String sourceName;

    private ObservedStorageSource(
        InventoryID inventoryId,
        int visibleComponentId,
        StorageSourceType sourceType,
        String sourceName)
    {
        this.inventoryId = inventoryId;
        this.visibleComponentId = visibleComponentId;
        this.sourceType = ObservedItem.normalizeSourceType(sourceType);
        this.sourceName = sourceName == null ? "" : sourceName.trim();
    }

    static ObservedStorageSource directInventory(
        InventoryID inventoryId,
        int visibleComponentId,
        StorageSourceType sourceType,
        String sourceName)
    {
        return new ObservedStorageSource(inventoryId, visibleComponentId, sourceType, sourceName);
    }

    static ObservedStorageSource bank()
    {
        return BANK;
    }

    static ObservedStorageSource seedVault()
    {
        return SEED_VAULT;
    }

    static ObservedStorageSource groupStorage()
    {
        return GROUP_STORAGE;
    }

    static List<ObservedStorageSource> safeDirectInventorySources()
    {
        return SAFE_DIRECT_INVENTORY_SOURCES;
    }

    InventoryID getInventoryId()
    {
        return inventoryId;
    }

    int getVisibleComponentId()
    {
        return visibleComponentId;
    }

    StorageSourceType getSourceType()
    {
        return sourceType;
    }

    String getSourceName()
    {
        return sourceName;
    }

    String key()
    {
        return sourceType + "|" + sourceName;
    }
}
```

- [ ] **Step 5: Create `ObservedStorageSnapshot`**

Create `src/main/java/com/semanticbanksearch/ObservedStorageSnapshot.java`:

```java
package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.List;

final class ObservedStorageSnapshot
{
    private final ObservedStorageSource source;
    private final List<ObservedItem> items;

    ObservedStorageSnapshot(ObservedStorageSource source, List<ObservedItem> items)
    {
        this.source = source;
        this.items = copyItems(items);
    }

    ObservedStorageSource getSource()
    {
        return source;
    }

    List<ObservedItem> getItems()
    {
        return copyItems(items);
    }

    private static List<ObservedItem> copyItems(List<ObservedItem> sourceItems)
    {
        List<ObservedItem> copies = new ArrayList<>();
        if (sourceItems == null)
        {
            return copies;
        }

        for (ObservedItem item : sourceItems)
        {
            if (item != null)
            {
                copies.add(new ObservedItem(item));
            }
        }
        return copies;
    }
}
```

- [ ] **Step 6: Create `ObservedStorageScanner`**

Create `src/main/java/com/semanticbanksearch/ObservedStorageScanner.java`:

```java
package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

final class ObservedStorageScanner
{
    interface ContainerProvider
    {
        ItemContainer getItemContainer(InventoryID inventoryId);
    }

    interface WidgetVisibilityProvider
    {
        boolean isVisible(int packedComponentId);
    }

    interface ItemCanonicalizer
    {
        int canonicalize(int itemId);
    }

    interface ItemNameResolver
    {
        String itemName(int canonicalItemId);
    }

    private final ContainerProvider containerProvider;
    private final WidgetVisibilityProvider widgetVisibilityProvider;
    private final ItemCanonicalizer itemCanonicalizer;
    private final ItemNameResolver itemNameResolver;

    ObservedStorageScanner(
        ContainerProvider containerProvider,
        WidgetVisibilityProvider widgetVisibilityProvider,
        ItemCanonicalizer itemCanonicalizer,
        ItemNameResolver itemNameResolver)
    {
        this.containerProvider = containerProvider;
        this.widgetVisibilityProvider = widgetVisibilityProvider;
        this.itemCanonicalizer = itemCanonicalizer;
        this.itemNameResolver = itemNameResolver;
    }

    List<ObservedStorageSnapshot> scan(List<ObservedStorageSource> sources, long now)
    {
        if (sources == null || sources.isEmpty()
            || containerProvider == null
            || widgetVisibilityProvider == null
            || itemCanonicalizer == null
            || itemNameResolver == null)
        {
            return Collections.emptyList();
        }

        List<ObservedStorageSnapshot> snapshots = new ArrayList<>();
        for (ObservedStorageSource source : sources)
        {
            if (source == null
                || source.getInventoryId() == null
                || !widgetVisibilityProvider.isVisible(source.getVisibleComponentId()))
            {
                continue;
            }

            ItemContainer container = containerProvider.getItemContainer(source.getInventoryId());
            if (container == null)
            {
                continue;
            }

            snapshots.add(new ObservedStorageSnapshot(source, scanItems(source, container, now)));
        }
        return snapshots;
    }

    private List<ObservedItem> scanItems(ObservedStorageSource source, ItemContainer container, long now)
    {
        List<ObservedItem> observedItems = new ArrayList<>();
        Item[] items = container.getItems();
        if (items == null)
        {
            return observedItems;
        }

        for (Item item : items)
        {
            if (item == null || item.getId() <= 0 || item.getQuantity() <= 0)
            {
                continue;
            }

            int canonicalId = itemCanonicalizer.canonicalize(item.getId());
            String name = clean(itemNameResolver.itemName(canonicalId));
            if (canonicalId <= 0 || name.isEmpty() || name.equalsIgnoreCase("null"))
            {
                continue;
            }

            observedItems.add(new ObservedItem(
                canonicalId,
                name,
                item.getQuantity(),
                source.getSourceType(),
                source.getSourceName(),
                true,
                now));
        }
        return observedItems;
    }

    private static String clean(String text)
    {
        return text == null ? "" : text.trim();
    }
}
```

- [ ] **Step 7: Run scanner tests**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.ObservedStorageSourceTest --tests com.semanticbanksearch.ObservedStorageScannerTest
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 8: Commit Task 1**

Run:

```powershell
git add src/main/java/com/semanticbanksearch/ObservedStorageSource.java src/main/java/com/semanticbanksearch/ObservedStorageSnapshot.java src/main/java/com/semanticbanksearch/ObservedStorageScanner.java src/test/java/com/semanticbanksearch/ObservedStorageSourceTest.java src/test/java/com/semanticbanksearch/ObservedStorageScannerTest.java
git commit -m "Add safe observed storage scanner"
```

## Task 2: Plugin Integration For Safe Direct Storage

**Files:**
- Modify: `src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java`
- Modify: `src/test/java/com/semanticbanksearch/StorageIndexTest.java`

- [ ] **Step 1: Add index visibility regression for non-bank sources**

Add this test to `StorageIndexTest` before `itemById(...)`:

```java
@Test
public void nonBankSourceItemsBecomeRememberedWhenSourceCloses()
{
    StorageIndex index = new StorageIndex();
    index.record(100, "Ranarr seed", 5, StorageSourceType.OTHER_STORAGE, "Seed Vault", true, 1_000L);

    index.markSourceNotVisible(StorageSourceType.OTHER_STORAGE, "Seed Vault");

    List<ObservedItem> items = index.items();
    assertEquals(1, items.size());
    assertEquals("Ranarr seed", items.get(0).getName());
    assertEquals(StorageSourceType.OTHER_STORAGE, items.get(0).getSourceType());
    assertEquals("Seed Vault", items.get(0).getSourceName());
    assertFalse(items.get(0).isCurrentlyVisible());
}
```

- [ ] **Step 2: Run index test**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.StorageIndexTest
```

Expected: `BUILD SUCCESSFUL`. This documents existing behavior before plugin integration.

- [ ] **Step 3: Refactor plugin fields and imports**

In `SemanticBankSearchPlugin`, add imports:

```java
import java.util.HashSet;
import java.util.Set;
import net.runelite.api.widgets.ComponentID;
```

Add fields near `engine`:

```java
private ObservedStorageScanner storageScanner;
private final Set<String> visibleStorageSourceKeys = new HashSet<>();
```

- [ ] **Step 4: Initialize and clear scanner lifecycle**

In `startUp()`, after creating `engine`, initialize the scanner:

```java
storageScanner = new ObservedStorageScanner(
    client::getItemContainer,
    this::isWidgetVisible,
    itemManager::canonicalize,
    this::resolveItemName);
```

In `shutDown()`, replace the single bank visibility mark with:

```java
markAllStorageSourcesNotVisible();
```

Then set these fields during shutdown cleanup:

```java
storageScanner = null;
visibleStorageSourceKeys.clear();
```

- [ ] **Step 5: Replace startup observation behavior**

In `startUp()`, replace:

```java
bankOpen = isBankOpen();
if (bankOpen && config.rememberObservedStorage())
{
    observeBank(System.currentTimeMillis());
}
else
{
    index.markSourceNotVisible(StorageSourceType.BANK, BANK_SOURCE_NAME);
}
```

with:

```java
bankOpen = isBankOpen();
if (config.rememberObservedStorage())
{
    observeSafeStorage(System.currentTimeMillis());
}
else
{
    markAllStorageSourcesNotVisible();
}
```

- [ ] **Step 6: Replace bank-only game tick observation**

In `onGameTick`, replace the current bank-only observation block with:

```java
boolean currentlyBankOpen = isBankOpen();
bankOpen = currentlyBankOpen;
if (config.rememberObservedStorage())
{
    boolean changed = observeSafeStorage(now);
    if (lastPersistMillis == 0L || now - lastPersistMillis >= SAVE_INTERVAL_MILLIS)
    {
        persist();
    }
    if (changed)
    {
        refreshActivePanelMode();
    }
}
else if (!visibleStorageSourceKeys.isEmpty())
{
    markAllStorageSourcesNotVisible();
    refreshActivePanelMode();
    persist();
}
```

This keeps search refresh tied to storage visibility or content changes rather than every tick.

- [ ] **Step 7: Replace `observeBank` with `observeSafeStorage` helpers**

Remove the old `observeBank(long now)` method and add:

```java
private boolean observeSafeStorage(long now)
{
    if (index == null || storageScanner == null || !config.rememberObservedStorage())
    {
        return false;
    }

    List<ObservedStorageSnapshot> snapshots = storageScanner.scan(
        ObservedStorageSource.safeDirectInventorySources(),
        now);
    Set<String> currentlyVisibleSourceKeys = new HashSet<>();
    boolean changed = false;
    for (ObservedStorageSnapshot snapshot : snapshots)
    {
        ObservedStorageSource source = snapshot.getSource();
        currentlyVisibleSourceKeys.add(source.key());
        index.replaceVisibleSourceItems(
            source.getSourceType(),
            source.getSourceName(),
            snapshot.getItems());
        changed = true;
    }

    for (String previousKey : new HashSet<>(visibleStorageSourceKeys))
    {
        if (!currentlyVisibleSourceKeys.contains(previousKey))
        {
            markSourceKeyNotVisible(previousKey);
            changed = true;
        }
    }
    if (!currentlyVisibleSourceKeys.equals(visibleStorageSourceKeys))
    {
        changed = true;
    }
    visibleStorageSourceKeys.clear();
    visibleStorageSourceKeys.addAll(currentlyVisibleSourceKeys);
    index.trimToMaximumEntries(config.maximumRememberedEntries());
    return changed;
}

private void markAllStorageSourcesNotVisible()
{
    if (index == null)
    {
        return;
    }

    for (ObservedStorageSource source : ObservedStorageSource.safeDirectInventorySources())
    {
        index.markSourceNotVisible(source.getSourceType(), source.getSourceName());
    }
    visibleStorageSourceKeys.clear();
}

private void markSourceKeyNotVisible(String sourceKey)
{
    for (ObservedStorageSource source : ObservedStorageSource.safeDirectInventorySources())
    {
        if (source.key().equals(sourceKey))
        {
            index.markSourceNotVisible(source.getSourceType(), source.getSourceName());
            return;
        }
    }
}
```

- [ ] **Step 8: Update widget visibility helper**

Replace the old `isBankOpen()` method with both helpers:

```java
private boolean isBankOpen()
{
    return isWidgetVisible(ComponentID.BANK_ITEM_CONTAINER);
}

private boolean isWidgetVisible(int packedComponentId)
{
    Widget widget = client.getWidget(packedComponentId);
    return widget != null && !widget.isHidden();
}
```

Remove the now-unused `InterfaceID` import.

- [ ] **Step 9: Run focused compile and index tests**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.StorageIndexTest
.\gradlew.bat compileJava
```

Expected: both commands complete with `BUILD SUCCESSFUL`.

- [ ] **Step 10: Commit Task 2**

Run:

```powershell
git add src/main/java/com/semanticbanksearch/SemanticBankSearchPlugin.java src/test/java/com/semanticbanksearch/StorageIndexTest.java
git commit -m "Observe safe storage containers"
```

## Task 3: Search, Persistence, And Documentation Guards

**Files:**
- Modify: `src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java`
- Modify: `src/test/java/com/semanticbanksearch/SemanticBankSearchStorageTest.java`
- Modify: `README.md`

- [ ] **Step 1: Add search behavior test for non-bank storage**

Add this test to `SemanticSearchEngineTest` above `names(...)`:

```java
@Test
public void nonBankObservedStorageItemsAreSearchableButNotHighlightable()
{
    StorageIndex index = new StorageIndex();
    index.record(900, "Ranarr seed", 10, StorageSourceType.OTHER_STORAGE, "Seed Vault", true, 1_000L);
    index.record(901, "Seed dibber", 1, StorageSourceType.BANK, "Bank", true, 1_000L);

    List<SemanticSearchResult> results = new SemanticSearchEngine(SemanticLibrary.create()).search("herb run", index);

    assertTrue(names(results).contains("Ranarr seed"));
    assertTrue(names(results).contains("Seed dibber"));
    for (SemanticSearchResult result : results)
    {
        if (result.getItemName().equals("Ranarr seed"))
        {
            assertEquals("Seed Vault", result.getSourceName());
            assertFalse(result.isHighlightable());
        }
    }
}
```

- [ ] **Step 2: Add storage round-trip test for non-bank sources**

Add this test to `SemanticBankSearchStorageTest`:

```java
@Test
public void roundTripPreservesNonBankStorageSource()
{
    StorageIndex index = new StorageIndex();
    index.record(100, "Ranarr seed", 10, StorageSourceType.OTHER_STORAGE, "Seed Vault", true, 1_000L);

    String json = SemanticBankSearchStorage.serialize(new Gson(), index);
    StorageIndex loaded = SemanticBankSearchStorage.deserialize(new Gson(), json);

    assertEquals("Ranarr seed", loaded.items().get(0).getName());
    assertEquals(10, loaded.items().get(0).getQuantity());
    assertEquals(StorageSourceType.OTHER_STORAGE, loaded.items().get(0).getSourceType());
    assertEquals("Seed Vault", loaded.items().get(0).getSourceName());
    assertTrue(loaded.items().get(0).isCurrentlyVisible());
}
```

- [ ] **Step 3: Run search and storage tests**

Run:

```powershell
.\gradlew.bat test --tests com.semanticbanksearch.SemanticSearchEngineTest --tests com.semanticbanksearch.SemanticBankSearchStorageTest
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Update README capability wording**

In `README.md`, replace:

```markdown
The plugin is local-only. It reads visible bank items, remembers observed storage locally, and highlights matching visible bank items. Remembered storage only includes items the client has observed; it cannot infer items from tabs, accounts, or storage containers that have not been opened in RuneLite.
```

with:

```markdown
The plugin is local-only. It reads visible bank items plus safe observed storage containers such as Seed Vault and Group Storage, remembers those observations locally, and highlights matching visible bank items. Remembered storage only includes items the client has observed; it cannot infer items from tabs, accounts, or storage containers that have not been opened in RuneLite. POH storage remains limited to sources that RuneLite exposes clearly and safely.
```

- [ ] **Step 5: Run full verification**

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

- [ ] **Step 6: Commit Task 3**

Run:

```powershell
git add README.md src/test/java/com/semanticbanksearch/SemanticSearchEngineTest.java src/test/java/com/semanticbanksearch/SemanticBankSearchStorageTest.java
git commit -m "Document safe observed storage support"
```

## Self-Review

- Spec coverage: The plan adds safe direct storage observation, non-bank searchable remembered items, bank-only highlighting, allow-list safety, docs, tests, and final verification. POH is explicitly deferred because the approved design allowed deferral when widget extraction is uncertain.
- Placeholder scan: The plan contains no unfinished-marker text.
- Type consistency: `ObservedStorageSource`, `ObservedStorageSnapshot`, and `ObservedStorageScanner` signatures match across implementation and tests.
- Scope control: The plan avoids trade/shop/reward/temporary containers, avoids widget scraping, avoids non-bank overlays, and does not add network, AI, or game actions.

