package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class SemanticBankQueryParser
{
    List<String> parse(String query)
    {
        String normalized = SemanticSearchEngine.normalize(query);
        if (normalized.isEmpty())
        {
            return Collections.emptyList();
        }

        List<String> clauses = new ArrayList<>();
        addPotionDoseClause(normalized, clauses);
        addCombatStyleClause(normalized, clauses);
        addPrayerGearClause(normalized, clauses);
        addTargetClause(normalized, clauses);
        addPotionPurposeClause(normalized, clauses);

        return clauses.size() < 2 ? Collections.emptyList() : Collections.unmodifiableList(clauses);
    }

    private static void addPotionDoseClause(String query, List<String> clauses)
    {
        if (!hasPotionWord(query))
        {
            return;
        }
        if (query.contains("4 dose") || query.contains("four dose") || query.contains("full pot"))
        {
            addUnique(clauses, "full pots");
        }
        else if (query.contains("1 dose") || query.contains("one dose")
            || query.contains("2 dose") || query.contains("two dose") || query.contains("low dose"))
        {
            addUnique(clauses, "low dose pots");
        }
    }

    private static void addCombatStyleClause(String query, List<String> clauses)
    {
        boolean weaponQuery = containsAny(query, "weapon", "weapons", "wep", "weps");
        boolean gearQuery = weaponQuery || containsAny(query, "gear", "equipment");
        if (!gearQuery)
        {
            return;
        }

        if (containsAny(query, "range", "ranged"))
        {
            addUnique(clauses, weaponQuery ? "ranged weapon" : "range gear");
        }
        else if (containsAny(query, "mage", "magic"))
        {
            addUnique(clauses, weaponQuery ? "magic weapon" : "mage gear");
        }
        else if (query.contains("melee"))
        {
            addUnique(clauses, "melee gear");
        }
    }

    private static void addPrayerGearClause(String query, List<String> clauses)
    {
        if (query.contains("prayer") && containsAny(query, "gear", "equipment", "weapon", "weapons", "wep", "weps"))
        {
            addUnique(clauses, "prayer gear");
        }
    }

    private static void addTargetClause(String query, List<String> clauses)
    {
        if (containsAny(query, "dragon", "dragons", "dragon task", "dragon slayer"))
        {
            addUnique(clauses, "dragon slayer task");
        }
        else if (containsAny(query, "demon", "demons", "demon task", "demon slayer"))
        {
            addUnique(clauses, "demon slayer task");
        }
        else if (containsAny(query, "undead", "zombie", "skeleton", "ankou"))
        {
            addUnique(clauses, "undead slayer");
        }
    }

    private static void addPotionPurposeClause(String query, List<String> clauses)
    {
        if (!hasPotionWord(query))
        {
            return;
        }

        if (containsAny(query, "prayer", "ppot", "pray"))
        {
            addUnique(clauses, "prayer restoration");
        }
        else if (containsAny(query, "stamina", "energy", "run energy"))
        {
            addUnique(clauses, "run energy");
        }
        else if (containsAny(query, "poison", "venom", "antipoison"))
        {
            addUnique(clauses, "poison protection");
        }
        else if (containsAny(query, "combat", "melee", "strength", "attack", "defence"))
        {
            addUnique(clauses, "combat boost");
        }
        else if (containsAny(query, "range", "ranged", "mage", "magic"))
        {
            addUnique(clauses, query.contains("range") ? "ranged boost" : "magic boost");
        }
    }

    private static boolean hasPotionWord(String query)
    {
        return containsAny(query, "pot", "pots", "potion", "potions", "ppot");
    }

    private static boolean containsAny(String query, String... phrases)
    {
        for (String phrase : phrases)
        {
            if (containsPhrase(query, phrase))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean containsPhrase(String query, String phrase)
    {
        return (" " + query + " ").contains(" " + phrase + " ");
    }

    private static void addUnique(List<String> clauses, String clause)
    {
        if (!clauses.contains(clause))
        {
            clauses.add(clause);
        }
    }
}
