package com.semanticbanksearch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import net.runelite.api.EquipmentInventorySlot;
import org.junit.Test;

public class SemanticBankCompositeFilterTest
{
    @Test
    public void fourDosePrayerPotsRequireBothConstraints()
    {
        Map<Integer, BankItemMetadata> items = new HashMap<>();
        items.put(1, potion("Prayer potion(4)"));
        items.put(2, potion("Prayer potion(1)"));
        items.put(3, potion("Super combat potion(4)"));
        SemanticBankFilter filter = filter(items);

        assertTrue(filter.decision("4 dose prayer pots", 1));
        assertFalse(filter.decision("4 dose prayer pots", 2));
        assertFalse(filter.decision("4 dose prayer pots", 3));
    }

    @Test
    public void meleeGearWithPrayerRequiresStyleAndPrayerBonus()
    {
        Map<Integer, BankItemMetadata> items = new HashMap<>();
        items.put(1, equipment("Blessed sword", 50, 0, 0, 4));
        items.put(2, equipment("Abyssal whip", 82, 0, 0, 0));
        items.put(3, equipment("Blessed bow", 0, 50, 0, 4));
        SemanticBankFilter filter = filter(items);

        assertTrue(filter.decision("melee gear with prayer", 1));
        assertFalse(filter.decision("melee gear with prayer", 2));
        assertFalse(filter.decision("melee gear with prayer", 3));
    }

    @Test
    public void rangedWeaponsForDragonsRequireStyleAndTargetPurpose()
    {
        Map<Integer, BankItemMetadata> items = new HashMap<>();
        items.put(1, equipment("Dragon hunter crossbow", 0, 95, 0, 0));
        items.put(2, equipment("Magic shortbow", 0, 69, 0, 0));
        items.put(3, equipment("Dragon hunter lance", 85, 0, 0, 0));
        SemanticBankFilter filter = filter(items);

        assertTrue(filter.decision("ranged weapons for dragons", 1));
        assertFalse(filter.decision("ranged weapons for dragons", 2));
        assertFalse(filter.decision("ranged weapons for dragons", 3));
    }

    private static SemanticBankFilter filter(Map<Integer, BankItemMetadata> items)
    {
        return new SemanticBankFilter(new SemanticSearchEngine(SemanticLibrary.create()), items::get);
    }

    private static BankItemMetadata potion(String name)
    {
        return new BankItemMetadata(
            name, false, false, false, true, -1,
            0, 0, 0, 0, 0, 0, 0, 0f, 0);
    }

    private static BankItemMetadata equipment(String name, int melee, int ranged, int magic, int prayer)
    {
        return new BankItemMetadata(
            name,
            true,
            true,
            false,
            false,
            EquipmentInventorySlot.WEAPON.getSlotIdx(),
            melee,
            melee,
            melee,
            magic,
            ranged,
            melee,
            ranged,
            0f,
            prayer);
    }
}
