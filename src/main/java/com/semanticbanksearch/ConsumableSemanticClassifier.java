package com.semanticbanksearch;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ConsumableSemanticClassifier
{
    private enum QueryType
    {
        NONE,
        FOOD,
        POTION,
        COMBO_FOOD,
        FULL_POTION,
        LOW_DOSE_POTION
    }

    private static final Set<String> FOOD_QUERIES = Set.of(
        "food", "foods", "eats", "edible", "edibles", "healing food", "food i own", "all food");
    private static final Set<String> POTION_QUERIES = Set.of(
        "potion", "potions", "pot", "pots", "all potions", "all pots");
    private static final Set<String> COMBO_FOOD_QUERIES = Set.of(
        "combo food", "combo foods", "combo eats", "fast combo food");
    private static final Set<String> FULL_POTION_QUERIES = Set.of(
        "full potions", "full pots", "4 dose potion", "4 dose potions", "4 dose pot", "4 dose pots");
    private static final Set<String> LOW_DOSE_POTION_QUERIES = Set.of(
        "low dose potions", "low dose pots", "1 dose potions", "1 dose pots", "2 dose potions", "2 dose pots");
    private static final Set<String> COMBO_FOOD_NAMES = Set.of(
        "cooked karambwan", "karambwan");
    private static final Pattern DOSE_SUFFIX = Pattern.compile("\\(([1-4])\\)$");

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
            case FOOD:
                return item.isEdible();
            case POTION:
                return isPotion(item);
            case COMBO_FOOD:
                return item.isEdible() && COMBO_FOOD_NAMES.contains(normalizedBaseName(item.getName()));
            case FULL_POTION:
                return isPotion(item) && dose(item.getName()) == 4;
            case LOW_DOSE_POTION:
                int dose = dose(item.getName());
                return isPotion(item) && dose > 0 && dose <= 2;
            default:
                return false;
        }
    }

    private static boolean isPotion(BankItemMetadata item)
    {
        if (!item.isDrinkable())
        {
            return false;
        }

        String name = SemanticSearchEngine.normalize(item.getName());
        return name.contains("potion")
            || name.contains("brew")
            || name.contains("restore")
            || name.contains("serum")
            || name.contains("antipoison")
            || name.contains("anti venom")
            || name.contains("antidote")
            || name.contains("mix");
    }

    private static int dose(String itemName)
    {
        Matcher matcher = DOSE_SUFFIX.matcher(itemName == null ? "" : itemName.trim());
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private static String normalizedBaseName(String itemName)
    {
        return SemanticSearchEngine.normalize(
            itemName == null ? "" : itemName.replaceFirst("\\([1-4]\\)$", ""));
    }

    private static QueryType queryType(String query)
    {
        String normalized = SemanticSearchEngine.normalize(query);
        if (FOOD_QUERIES.contains(normalized))
        {
            return QueryType.FOOD;
        }
        if (POTION_QUERIES.contains(normalized))
        {
            return QueryType.POTION;
        }
        if (COMBO_FOOD_QUERIES.contains(normalized))
        {
            return QueryType.COMBO_FOOD;
        }
        if (FULL_POTION_QUERIES.contains(normalized))
        {
            return QueryType.FULL_POTION;
        }
        if (LOW_DOSE_POTION_QUERIES.contains(normalized))
        {
            return QueryType.LOW_DOSE_POTION;
        }
        return QueryType.NONE;
    }
}
