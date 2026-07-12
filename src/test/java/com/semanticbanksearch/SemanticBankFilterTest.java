package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class SemanticBankFilterTest
{
    private SemanticBankFilter filter;

    @Before
    public void setUp()
    {
        Map<Integer, BankItemMetadata> items = new HashMap<>();
        items.put(100, item("Prayer potion(4)", false, false, -1));
        items.put(101, item("Shark", false, false, -1));
        items.put(102, item("Barrows teleport", false, false, -1));
        items.put(103, item("Dragon scimitar", true, true, 3));
        filter = new SemanticBankFilter(
            new SemanticSearchEngine(SemanticLibrary.create()),
            items::get);
    }

    @Test
    public void naturalLanguagePurposeFiltersLiveBankItems()
    {
        assertTrue(filter.decision("prayer restoration", 100));
        assertFalse(filter.decision("prayer restoration", 101));
    }

    @Test
    public void forcePrefixDoesNotRequireColon()
    {
        assertEquals("prayer restoration", filter.semanticQuery("sem prayer restoration"));
        assertTrue(filter.decision("sem prayer restoration", 100));
        assertFalse(filter.decision("sem prayer restoration", 102));
    }

    @Test
    public void weaponAndPlayerShorthandFilterWieldableItems()
    {
        assertTrue(filter.decision("weapon", 103));
        assertTrue(filter.decision("weps", 103));
        assertFalse(filter.decision("weps", 101));
    }

    @Test
    public void ordinaryItemNameSearchIsLeftToRuneLite()
    {
        assertNull(filter.decision("shark", 101));
    }

    @Test
    public void bankTagsSyntaxIsLeftUntouched()
    {
        assertNull(filter.decision("tag:slayer", 100));
    }

    @Test
    public void fuzzyPurposeQueryWorksInBank()
    {
        assertTrue(filter.decision("prayr restoraton", 100));
        assertFalse(filter.decision("prayr restoraton", 101));
    }

    @Test
    public void bankLayoutPlaceholderIsLeftUntouched()
    {
        assertNull(filter.decision("prayer restoration", -1));
    }

    private static BankItemMetadata item(String name, boolean equipable, boolean wieldable, int slot)
    {
        return new BankItemMetadata(name, equipable, wieldable, false, false, slot, 0, 0, 0, 0, 0, 0, 0, 0f, 0);
    }
}
