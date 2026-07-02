package com.semanticbanksearch;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.InventoryID;
import net.runelite.api.widgets.ComponentID;

final class ObservedStorageSource
{
    private static final ObservedStorageSource BANK = new ObservedStorageSource(
        InventoryID.BANK,
        ComponentID.BANK_ITEM_CONTAINER,
        StorageSourceType.BANK,
        "Bank");
    private static final ObservedStorageSource SEED_VAULT = new ObservedStorageSource(
        InventoryID.SEED_VAULT,
        ComponentID.SEED_VAULT_ITEM_CONTAINER,
        StorageSourceType.OTHER_STORAGE,
        "Seed Vault");
    private static final ObservedStorageSource GROUP_STORAGE = new ObservedStorageSource(
        InventoryID.GROUP_STORAGE,
        ComponentID.GROUP_STORAGE_ITEM_CONTAINER,
        StorageSourceType.OTHER_STORAGE,
        "Group Storage");

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
        this.sourceType = sourceType;
        this.sourceName = sourceName;
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
        return Collections.unmodifiableList(Arrays.asList(BANK, SEED_VAULT, GROUP_STORAGE));
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
