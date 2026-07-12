package com.semanticbanksearch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class NumericSemanticClassifierTest
{
    private final NumericSemanticClassifier classifier = new NumericSemanticClassifier();

    @Test
    public void healingComparisonsRespectStrictAndInclusiveLanguage()
    {
        BankItemMetadata shark = food("Shark", 20);

        assertTrue(classifier.matches("food over 18 hp", shark));
        assertFalse(classifier.matches("food over 20 hp", shark));
        assertTrue(classifier.matches("food at least 20 hp", shark));
    }

    @Test
    public void missingHealingDataFailsClosed()
    {
        assertFalse(classifier.matches("food over 1 hp", food("Unknown food", -1)));
    }

    @Test
    public void prayerBonusComparisonsSupportPlusNotation()
    {
        BankItemMetadata item = gear("Proselyte hauberk", 8);

        assertTrue(classifier.matches("prayer gear over +5", item));
        assertFalse(classifier.matches("prayer gear over +8", item));
        assertTrue(classifier.matches("prayer gear at least +8", item));
    }

    @Test
    public void numericalQueriesReachLiveBankFilter()
    {
        Map<Integer, BankItemMetadata> items = new HashMap<>();
        items.put(1, food("Manta ray", 22));
        items.put(2, food("Lobster", 12));
        SemanticBankFilter filter = new SemanticBankFilter(
            new SemanticSearchEngine(SemanticLibrary.create()),
            items::get);

        assertTrue(filter.decision("food over 18 hp", 1));
        assertFalse(filter.decision("food over 18 hp", 2));
    }

    private static BankItemMetadata food(String name, int healing)
    {
        return new BankItemMetadata(
            name, false, false, true, false, -1,
            0, 0, 0, 0, 0, 0, 0, 0f, 0, healing);
    }

    private static BankItemMetadata gear(String name, int prayer)
    {
        return new BankItemMetadata(
            name, true, false, false, false, 4,
            0, 0, 0, 0, 0, 0, 0, 0f, prayer, -1);
    }
}
