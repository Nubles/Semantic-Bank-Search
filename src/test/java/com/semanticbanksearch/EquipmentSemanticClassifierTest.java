package com.semanticbanksearch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.runelite.api.EquipmentInventorySlot;
import org.junit.Test;

public class EquipmentSemanticClassifierTest
{
    private final EquipmentSemanticClassifier classifier = new EquipmentSemanticClassifier();

    @Test
    public void genericWeaponAliasesUseWeaponSlotMetadata()
    {
        BankItemMetadata scimitar = item("Dragon scimitar", EquipmentInventorySlot.WEAPON, 45, 0, 0, 0);

        assertTrue(classifier.matches("weapon", scimitar));
        assertTrue(classifier.matches("weps", scimitar));
        assertTrue(classifier.matches("wepons", scimitar));
    }

    @Test
    public void wieldActionProvidesFallbackWhenStatsAreUnavailable()
    {
        BankItemMetadata unknownWieldable = new BankItemMetadata(
            "New weapon", false, true, false, false, -1, 0, 0, 0, 0, 0, 0, 0, 0f, 0);

        assertTrue(classifier.matches("weapons", unknownWieldable));
    }

    @Test
    public void armourIncludesProtectiveSlotsButExcludesJewelleryAndAmmo()
    {
        assertTrue(classifier.matches("armour", item("Rune platebody", EquipmentInventorySlot.BODY, 0, 0, 0, 0)));
        assertFalse(classifier.matches("armor", item("Berserker ring", EquipmentInventorySlot.RING, 0, 0, 0, 0)));
        assertFalse(classifier.matches("armor", item("Amulet of fury", EquipmentInventorySlot.AMULET, 0, 0, 0, 0)));
        assertFalse(classifier.matches("armor", item("Dragon arrow", EquipmentInventorySlot.AMMO, 0, 0, 0, 0)));
    }

    @Test
    public void broadEquipmentSearchIncludesAllEquipableSlots()
    {
        assertTrue(classifier.matches("gear", item("Berserker ring", EquipmentInventorySlot.RING, 0, 0, 0, 0)));
        assertTrue(classifier.matches("equipment", item("Dragon scimitar", EquipmentInventorySlot.WEAPON, 45, 0, 0, 0)));
    }

    @Test
    public void combatStyleGearUsesDominantOffensiveStats()
    {
        BankItemMetadata melee = item("Abyssal whip", EquipmentInventorySlot.WEAPON, 82, 0, 0, 0);
        BankItemMetadata ranged = item("Magic shortbow", EquipmentInventorySlot.WEAPON, 0, 69, 0, 0);
        BankItemMetadata magic = item("Trident of the seas", EquipmentInventorySlot.WEAPON, 0, 0, 25, 0);

        assertTrue(classifier.matches("melee gear", melee));
        assertTrue(classifier.matches("range gear", ranged));
        assertTrue(classifier.matches("mage gear", magic));
        assertFalse(classifier.matches("mage gear", melee));
    }

    @Test
    public void prayerGearRequiresPositivePrayerBonus()
    {
        BankItemMetadata proselyte = item("Proselyte hauberk", EquipmentInventorySlot.BODY, 0, 0, 0, 8);
        BankItemMetadata rune = item("Rune platebody", EquipmentInventorySlot.BODY, 0, 0, 0, 0);

        assertTrue(classifier.matches("prayer gear", proselyte));
        assertFalse(classifier.matches("prayer gear", rune));
    }

    private static BankItemMetadata item(
        String name,
        EquipmentInventorySlot slot,
        int melee,
        int ranged,
        int magic,
        int prayer)
    {
        return new BankItemMetadata(
            name,
            true,
            slot == EquipmentInventorySlot.WEAPON,
            false,
            false,
            slot.getSlotIdx(),
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
