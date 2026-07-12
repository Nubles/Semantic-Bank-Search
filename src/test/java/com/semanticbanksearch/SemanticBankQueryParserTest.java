package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

public class SemanticBankQueryParserTest
{
    private final SemanticBankQueryParser parser = new SemanticBankQueryParser();

    @Test
    public void parsesDoseAndPotionPurpose()
    {
        assertEquals(
            Arrays.asList("full pots", "prayer restoration"),
            parser.parse("4 dose prayer pots"));
    }

    @Test
    public void parsesCombatStyleAndPrayerConstraint()
    {
        assertEquals(
            Arrays.asList("melee gear", "prayer gear"),
            parser.parse("melee gear with prayer"));
    }

    @Test
    public void parsesWeaponStyleAndTarget()
    {
        assertEquals(
            Arrays.asList("ranged weapon", "dragon slayer task"),
            parser.parse("ranged weapons for dragons"));
    }

    @Test
    public void leavesSingleIntentQueriesToExistingClassifiers()
    {
        assertTrue(parser.parse("weps").isEmpty());
        assertTrue(parser.parse("prayer restoration").isEmpty());
    }
}
