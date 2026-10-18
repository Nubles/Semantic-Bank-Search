package com.semanticbanksearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class FinderTest
{
    private final Map<Integer, BankItemMetadata> items = new HashMap<>();
    private Finder finder;
    private StorageIndex index;

    @Before
    public void setUp()
    {
        items.put(385, food("Shark", 20));
        items.put(379, food("Lobster", 12));
        items.put(2434, potion("Prayer potion(4)"));
        items.put(139, potion("Prayer potion(1)"));
        items.put(4151, new BankItemMetadata("Abyssal whip", true, true, false, false, 3, 0, 82, 0, 0, 0, 82, 0, 0, 0, -1));
        items.put(1704, new BankItemMetadata("Amulet of glory(4)", true, false, false, false, 2, 10, 10, 10, 10, 10, 6, 0, 0, 3, -1));

        Vault vault = new Vault();
        Map<Integer, Integer> bank = new LinkedHashMap<>();
        for (int id : items.keySet())
        {
            bank.put(id, 5);
        }
        vault.replace(Vault.BANK, bank, 1_000L);
        vault.replace(Vault.INVENTORY, Collections.singletonMap(385, 10), 2_000L);
        index = vault.toIndex(id -> items.get(id).getName(), Collections.singleton(Vault.BANK));
        finder = new Finder(new SemanticSearchEngine(SemanticLibrary.create()), items::get);
    }

    @Test
    public void purposeSearchSaysWhyEachItemFits()
    {
        Finder.Finding finding = finder.find("prayer restoration", index);
        assertTrue(finding.understood);
        assertEquals(2, finding.hits.size());
        assertEquals("Prayer restoration", finding.hits.get(0).category);
        assertEquals("Restores prayer points.", finding.hits.get(0).reason);
    }

    @Test
    public void panelAnswersMeasuresTheSameWayAsTheBank()
    {
        Finder.Finding finding = finder.find("food over 18 hp", index);
        assertEquals(1, finding.hits.size());
        assertEquals("Shark", finding.hits.get(0).name);
        assertEquals("Heals 20 hitpoints.", finding.hits.get(0).reason);
    }

    @Test
    public void oneItemInSeveralPlacesIsOneHit()
    {
        Finder.Hit shark = finder.find("shark", index).hits.get(0);
        assertEquals(15, shark.quantity);
        assertEquals(2, shark.places.size());
        assertTrue(shark.inOpenBank);
    }

    @Test
    public void comparisonsPickTheBestOwned()
    {
        Finder.Finding finding = finder.find("best food", index);
        assertEquals(1, finding.hits.size());
        assertEquals("Shark", finding.hits.get(0).name);
    }

    @Test
    public void plainNamesAreNotReadAsPurposes()
    {
        Finder.Finding finding = finder.find("lobster", index);
        assertFalse(finding.understood);
        assertEquals(Finder.NAME_MATCH, finding.hits.get(0).category);
    }

    @Test
    public void teleportFindsTeleportJewellery()
    {
        Finder.Finding finding = finder.find("teleport", index);
        assertTrue(finding.understood);
        assertEquals("Amulet of glory(4)", finding.hits.get(0).name);
    }

    @Test
    public void vaultKeepsTheLastPictureOfEachPlace()
    {
        Vault vault = new Vault();
        assertTrue(vault.replace(Vault.BANK, Collections.singletonMap(385, 1), 1L));
        assertFalse(vault.replace(Vault.BANK, Collections.singletonMap(385, 1), 2L));
        assertTrue(vault.replace(Vault.BANK, Collections.emptyMap(), 3L));
        assertTrue(vault.isEmpty());
    }

    private static BankItemMetadata food(String name, int heals)
    {
        return new BankItemMetadata(name, false, false, true, false, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, heals);
    }

    private static BankItemMetadata potion(String name)
    {
        return new BankItemMetadata(name, false, false, false, true, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1);
    }
}
