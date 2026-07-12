package com.semanticbanksearch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.EquipmentInventorySlot;
import org.junit.Test;

public class RelativeRankingClassifierTest
{
    private final RelativeRankingClassifier classifier = new RelativeRankingClassifier();

    @Test
    public void bestFoodUsesHighestOwnedHealingAndAllowsTies()
    {
        BankItemMetadata shark = food("Shark", 20);
        BankItemMetadata manta = food("Manta ray", 22);
        BankItemMetadata darkCrab = food("Dark crab", 22);
        List<BankItemMetadata> owned = Arrays.asList(shark, manta, darkCrab);

        assertFalse(classifier.matches("best food", shark, owned));
        assertTrue(classifier.matches("best food", manta, owned));
        assertTrue(classifier.matches("best food", darkCrab, owned));
    }

    @Test
    public void goodFoodMeansAtLeastEightyPercentOfBestOwned()
    {
        BankItemMetadata lobster = food("Lobster", 12);
        BankItemMetadata shark = food("Shark", 20);
        BankItemMetadata manta = food("Manta ray", 22);
        List<BankItemMetadata> owned = Arrays.asList(lobster, shark, manta);

        assertFalse(classifier.matches("good food", lobster, owned));
        assertTrue(classifier.matches("good food", shark, owned));
        assertTrue(classifier.matches("good food", manta, owned));
    }

    @Test
    public void bestPrayerGearIsComparedPerEquipmentSlot()
    {
        BankItemMetadata bodyFour = gear("Prayer body", EquipmentInventorySlot.BODY, 4);
        BankItemMetadata bodyEight = gear("Better prayer body", EquipmentInventorySlot.BODY, 8);
        BankItemMetadata legsSix = gear("Prayer legs", EquipmentInventorySlot.LEGS, 6);
        List<BankItemMetadata> owned = Arrays.asList(bodyFour, bodyEight, legsSix);

        assertFalse(classifier.matches("best prayer gear", bodyFour, owned));
        assertTrue(classifier.matches("best prayer gear", bodyEight, owned));
        assertTrue(classifier.matches("best prayer gear", legsSix, owned));
    }

    @Test
    public void strongestRangedWeaponUsesOwnedStyleScore()
    {
        BankItemMetadata shortbow = weapon("Magic shortbow", 0, 69, 0);
        BankItemMetadata crossbow = weapon("Dragon crossbow", 0, 94, 0);
        BankItemMetadata whip = weapon("Abyssal whip", 82, 0, 0);
        List<BankItemMetadata> owned = Arrays.asList(shortbow, crossbow, whip);

        assertFalse(classifier.matches("strongest ranged weapon", shortbow, owned));
        assertTrue(classifier.matches("strongest ranged weapon", crossbow, owned));
        assertFalse(classifier.matches("strongest ranged weapon", whip, owned));
    }

    @Test
    public void relativeRankingFiltersTheLiveOwnedBank()
    {
        Map<Integer, BankItemMetadata> items = new HashMap<>();
        items.put(1, food("Shark", 20));
        items.put(2, food("Manta ray", 22));
        SemanticBankFilter filter = new SemanticBankFilter(
            new SemanticSearchEngine(SemanticLibrary.create()),
            items::get,
            () -> Arrays.asList(1, 2));

        assertFalse(filter.decision("best food", 1));
        assertTrue(filter.decision("best food", 2));
    }

    @Test
    public void explanationStatesTheComparedValue()
    {
        assertTrue(classifier.explanation("best food", food("Manta ray", 22)).contains("22 HP"));
        assertTrue(classifier.explanation(
            "best prayer gear",
            gear("Prayer body", EquipmentInventorySlot.BODY, 8)).contains("+8 prayer"));
    }

    private static BankItemMetadata food(String name, int healing)
    {
        return new BankItemMetadata(
            name, false, false, true, false, -1,
            0, 0, 0, 0, 0, 0, 0, 0f, 0, healing);
    }

    private static BankItemMetadata gear(String name, EquipmentInventorySlot slot, int prayer)
    {
        return new BankItemMetadata(
            name, true, false, false, false, slot.getSlotIdx(),
            0, 0, 0, 0, 0, 0, 0, 0f, prayer, -1);
    }

    private static BankItemMetadata weapon(String name, int melee, int ranged, int magic)
    {
        return new BankItemMetadata(
            name, true, true, false, false, EquipmentInventorySlot.WEAPON.getSlotIdx(),
            melee, melee, melee, magic, ranged, melee, ranged, 0f, 0, -1);
    }
}
