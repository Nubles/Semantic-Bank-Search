package com.semanticbanksearch;

import java.util.Set;
import net.runelite.api.EquipmentInventorySlot;

final class EquipmentSemanticClassifier
{
    private enum QueryType
    {
        NONE,
        WEAPON,
        ARMOUR,
        EQUIPMENT,
        MELEE_GEAR,
        RANGED_GEAR,
        MAGIC_GEAR,
        PRAYER_GEAR
    }

    private static final Set<String> WEAPON_QUERIES = Set.of(
        "weapon", "weapons", "weaponry", "wep", "weps", "wepon", "wepons", "weapn", "weapns", "all weapons");
    private static final Set<String> ARMOUR_QUERIES = Set.of(
        "armour", "armor", "armours", "armors", "armour gear", "armor gear", "all armour", "all armor");
    private static final Set<String> EQUIPMENT_QUERIES = Set.of(
        "gear", "equipment", "equipable", "equippable", "all gear", "all equipment");
    private static final Set<String> MELEE_QUERIES = Set.of(
        "melee gear", "melee equipment", "melee weapon", "melee weapons", "melee wep", "melee weps");
    private static final Set<String> RANGED_QUERIES = Set.of(
        "range gear", "ranged gear", "range equipment", "ranged equipment");
    private static final Set<String> MAGIC_QUERIES = Set.of(
        "mage gear", "magic gear", "mage equipment", "magic equipment");
    private static final Set<String> PRAYER_QUERIES = Set.of(
        "prayer gear", "prayer equipment", "prayer bonus", "prayer items");

    boolean recognizes(String query)
    {
        return queryType(query) != QueryType.NONE;
    }

    boolean matches(String query, BankItemMetadata item)
    {
        if (item == null)
        {
            return false;
        }

        switch (queryType(query))
        {
            case WEAPON:
                return isWeapon(item);
            case ARMOUR:
                return isArmour(item);
            case EQUIPMENT:
                return item.isEquipable();
            case MELEE_GEAR:
                return item.isEquipable() && item.meleeScore() > Math.max(item.rangedScore(), item.magicScore());
            case RANGED_GEAR:
                return item.isEquipable() && item.rangedScore() > Math.max(item.meleeScore(), item.magicScore());
            case MAGIC_GEAR:
                return item.isEquipable() && item.magicScore() > Math.max(item.meleeScore(), item.rangedScore());
            case PRAYER_GEAR:
                return item.isEquipable() && item.getPrayer() > 0;
            default:
                return false;
        }
    }

    private static boolean isWeapon(BankItemMetadata item)
    {
        return item.getEquipmentSlot() == EquipmentInventorySlot.WEAPON.getSlotIdx() || item.isWieldable();
    }

    private static boolean isArmour(BankItemMetadata item)
    {
        if (!item.isEquipable())
        {
            return false;
        }

        int slot = item.getEquipmentSlot();
        return slot != EquipmentInventorySlot.WEAPON.getSlotIdx()
            && slot != EquipmentInventorySlot.AMMO.getSlotIdx()
            && slot != EquipmentInventorySlot.RING.getSlotIdx()
            && slot != EquipmentInventorySlot.AMULET.getSlotIdx();
    }

    private static QueryType queryType(String query)
    {
        String normalized = SemanticSearchEngine.normalize(query);
        if (WEAPON_QUERIES.contains(normalized))
        {
            return QueryType.WEAPON;
        }
        if (ARMOUR_QUERIES.contains(normalized))
        {
            return QueryType.ARMOUR;
        }
        if (EQUIPMENT_QUERIES.contains(normalized))
        {
            return QueryType.EQUIPMENT;
        }
        if (MELEE_QUERIES.contains(normalized))
        {
            return QueryType.MELEE_GEAR;
        }
        if (RANGED_QUERIES.contains(normalized))
        {
            return QueryType.RANGED_GEAR;
        }
        if (MAGIC_QUERIES.contains(normalized))
        {
            return QueryType.MAGIC_GEAR;
        }
        if (PRAYER_QUERIES.contains(normalized))
        {
            return QueryType.PRAYER_GEAR;
        }
        return QueryType.NONE;
    }
}
