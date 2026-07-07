package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.List;

public final class ItemAwarenessClassifier
{
    private ItemAwarenessClassifier()
    {
    }

    public static List<String> mechanicalTags(ObservedItem item)
    {
        List<String> tags = new ArrayList<>();
        if (item == null)
        {
            return tags;
        }

        String name = SemanticSearchEngine.normalize(item.getName());
        if (name.isEmpty() || name.equals("null"))
        {
            return tags;
        }

        addIf(tags, isFood(name), "Food");
        addIf(tags, name.contains("rune"), "Rune");
        addIf(tags, isPotion(name), "Potion");
        addIf(tags, isTeleport(name), "Teleport");
        addIf(tags, isSeed(name), "Seed");
        addIf(tags, isLog(name), "Log");
        addIf(tags, isOre(name), "Ore");
        addIf(tags, isBar(name), "Bar");
        addIf(tags, isHerb(name), "Herb");
        addIf(tags, isGem(name), "Gem");
        addIf(tags, isTool(name), "Tool");
        addIf(tags, isEquipment(name), "Equipment");
        return tags;
    }

    public static boolean isKnownObservedItem(ObservedItem item)
    {
        if (item == null)
        {
            return false;
        }
        String name = SemanticSearchEngine.normalize(item.getName());
        return !name.isEmpty() && !name.equals("null");
    }

    private static boolean isFood(String name)
    {
        return containsAny(name,
            "shark", "karambwan", "manta ray", "anglerfish", "sea turtle", "monkfish", "lobster",
            "swordfish", "salmon", "trout", "tuna potato", "dark crab", "saradomin brew", "pie");
    }

    private static boolean isPotion(String name)
    {
        return name.contains("potion") || name.contains("antipoison") || name.contains("anti venom") || name.contains("antifire");
    }

    private static boolean isTeleport(String name)
    {
        return name.contains("teleport") || name.contains("tablet") || name.contains("teletab");
    }

    private static boolean isSeed(String name)
    {
        return name.endsWith(" seed") || name.contains(" seed ");
    }

    private static boolean isLog(String name)
    {
        return name.equals("logs") || name.endsWith(" logs") || name.endsWith(" log");
    }

    private static boolean isOre(String name)
    {
        return name.endsWith(" ore");
    }

    private static boolean isBar(String name)
    {
        return name.endsWith(" bar");
    }

    private static boolean isHerb(String name)
    {
        return name.endsWith(" herb") || containsAny(name, "guam", "marrentill", "tarromin", "harralander", "ranarr", "snapdragon", "torstol");
    }

    private static boolean isGem(String name)
    {
        return containsAny(name, "sapphire", "emerald", "ruby", "diamond", "opal", "jade", "topaz", "dragonstone", "onyx", "zenyte");
    }

    private static boolean isTool(String name)
    {
        return containsAny(name, "spade", "rope", "hammer", "chisel", "knife", "saw", "tinderbox", "pickaxe", "axe", "rake", "seed dibber", "secateurs");
    }

    private static boolean isEquipment(String name)
    {
        return containsAny(name, "helm", "helmet", "body", "legs", "shield", "sword", "scimitar", "crossbow", "bow", "staff", "boots", "gloves", "cape", "amulet", "ring");
    }

    private static boolean containsAny(String name, String... patterns)
    {
        for (String pattern : patterns)
        {
            if (name.contains(pattern))
            {
                return true;
            }
        }
        return false;
    }

    private static void addIf(List<String> tags, boolean condition, String tag)
    {
        if (condition && !tags.contains(tag))
        {
            tags.add(tag);
        }
    }
}
