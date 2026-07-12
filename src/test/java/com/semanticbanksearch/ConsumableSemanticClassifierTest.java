package com.semanticbanksearch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConsumableSemanticClassifierTest
{
    private final ConsumableSemanticClassifier classifier = new ConsumableSemanticClassifier();

    @Test
    public void foodUsesLiveEatAction()
    {
        assertTrue(classifier.matches("food", consumable("New cooked fish", true, false)));
        assertFalse(classifier.matches("food", consumable("Raw shark", false, false)));
    }

    @Test
    public void potionAliasesUseDrinkActionAndPotionIdentity()
    {
        assertTrue(classifier.matches("potions", consumable("Prayer potion(4)", false, true)));
        assertTrue(classifier.matches("pots", consumable("Saradomin brew(4)", false, true)));
        assertFalse(classifier.matches("pots", consumable("Beer", false, true)));
    }

    @Test
    public void comboFoodIsConservative()
    {
        assertTrue(classifier.matches("combo food", consumable("Cooked karambwan", true, false)));
        assertFalse(classifier.matches("combo food", consumable("Shark", true, false)));
    }

    @Test
    public void doseQueriesSeparateFullAndCleanupPotions()
    {
        BankItemMetadata full = consumable("Super restore(4)", false, true);
        BankItemMetadata oneDose = consumable("Super restore(1)", false, true);
        BankItemMetadata twoDose = consumable("Super restore(2)", false, true);

        assertTrue(classifier.matches("full pots", full));
        assertFalse(classifier.matches("full pots", oneDose));
        assertTrue(classifier.matches("low dose pots", oneDose));
        assertTrue(classifier.matches("low dose pots", twoDose));
        assertFalse(classifier.matches("low dose pots", full));
    }

    private static BankItemMetadata consumable(String name, boolean edible, boolean drinkable)
    {
        return new BankItemMetadata(
            name,
            false,
            false,
            edible,
            drinkable,
            -1,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0f,
            0);
    }
}
