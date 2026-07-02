package com.semanticbanksearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

final class MinigameRules
{
    private MinigameRules()
    {
    }

    static List<SemanticRule> create()
    {
        return Arrays.asList(
            excludingRule(
                "Wintertodt supplies",
                "Warm clothing, tools, and food used for Wintertodt prep.",
                SemanticLibrary.aliases("wintertodt", "wintertodt supplies", "wintertodt prep", "todt supplies"),
                SemanticLibrary.patterns("clue hunter", "pyromancer", "warm gloves", "bomber jacket", "axe", "knife", "tinderbox", "hammer", "rejuvenation potion", "cake", "jug of wine", "karambwan", "shark"),
                SemanticLibrary.scores(
                    "clue hunter", 30,
                    "pyromancer", 30,
                    "tinderbox", 25,
                    "axe", 20,
                    "hammer", 15),
                115,
                SemanticLibrary.patterns("pickaxe", "battleaxe")),
            excludingRule(
                "Tempoross supplies",
                "Fishing tools and utility items used for Tempoross prep.",
                SemanticLibrary.aliases("tempoross", "tempoross supplies", "tempoross prep", "tempoross fishing"),
                SemanticLibrary.patterns("harpoon", "dragon harpoon", "crystal harpoon", "infernal harpoon", "rope", "hammer", "bucket", "angler hat", "angler top", "angler waders", "angler boots", "small fishing net"),
                SemanticLibrary.scores(
                    "dragon harpoon", 35,
                    "crystal harpoon", 35,
                    "infernal harpoon", 35,
                    "harpoon", 25,
                    "bucket", 15),
                115,
                combatHammerPatterns()),
            SemanticLibrary.rule(
                "Guardians of the Rift supplies",
                "Runecrafting pouches and supplies used for Guardians of the Rift.",
                SemanticLibrary.aliases("gotr", "guardians of the rift", "guardians of the rift supplies", "gotr pouch", "gotr supplies"),
                SemanticLibrary.patterns("small pouch", "medium pouch", "large pouch", "giant pouch", "colossal pouch", "chisel", "binding necklace", "pure essence", "rune essence", "graceful", "stamina potion", "energy potion"),
                SemanticLibrary.scores(
                    "colossal pouch", 40,
                    "giant pouch", 35,
                    "large pouch", 30,
                    "medium pouch", 25,
                    "small pouch", 20,
                    "chisel", 20,
                    "binding necklace", 20),
                115),
            excludingRule(
                "Giants' Foundry supplies",
                "Smithing bars and utility items used for Giants' Foundry.",
                SemanticLibrary.aliases("giants foundry", "giants' foundry", "foundry supplies", "giants foundry supplies"),
                SemanticLibrary.patterns("bronze bar", "iron bar", "steel bar", "mithril bar", "adamantite bar", "runite bar", "hammer", "ice gloves", "smiths gloves", "stamina potion", "dwarven stout"),
                SemanticLibrary.scores(
                    "runite bar", 30,
                    "adamantite bar", 25,
                    "mithril bar", 20,
                    "steel bar", 15,
                    "ice gloves", 20),
                110,
                combatHammerPatterns()),
            excludingRule(
                "Mahogany Homes supplies",
                "Construction tools, materials, and teleports used for Mahogany Homes.",
                SemanticLibrary.aliases("mahogany homes", "mahogany homes supplies", "mahogany homes prep", "construction contracts"),
                SemanticLibrary.patterns("saw", "hammer", "plank", "oak plank", "teak plank", "mahogany plank", "steel bar", "house teleport", "teleport to house", "varrock teleport", "falador teleport", "ardougne teleport", "ardy teleport", "stamina potion", "graceful"),
                SemanticLibrary.scores(
                    "saw", 30,
                    "hammer", 30,
                    "mahogany plank", 25,
                    "teak plank", 20,
                    "oak plank", 15,
                    "steel bar", 15),
                115,
                combatHammerPatterns()),
            SemanticLibrary.rule(
                "Forestry supplies",
                "Woodcutting tools and event supplies used for Forestry.",
                SemanticLibrary.aliases("forestry", "forestry supplies", "forestry kit", "forestry prep"),
                SemanticLibrary.patterns("forestry kit", "felling axe", "rune axe", "dragon axe", "crystal axe", "log", "logs", "secateurs", "stamina potion", "graceful", "forester's ration", "anima bark"),
                SemanticLibrary.scores(
                    "forestry kit", 35,
                    "crystal axe", 30,
                    "dragon axe", 25,
                    "rune axe", 20,
                    "forester's ration", 20),
                105),
            SemanticLibrary.rule(
                "Tithe Farm supplies",
                "Farming tools and run energy supplies used for Tithe Farm.",
                SemanticLibrary.aliases("tithe farm", "tithe farm supplies", "tithe supplies", "tithe prep"),
                SemanticLibrary.patterns("watering can", "seed dibber", "spade", "graceful", "stamina potion", "botanical pie", "garden pie", "farmer"),
                SemanticLibrary.scores(
                    "watering can", 35,
                    "seed dibber", 25,
                    "spade", 20,
                    "graceful", 15),
                105),
            SemanticLibrary.rule(
                "Pyramid Plunder supplies",
                "Travel, thieving, and sustain supplies used for Pyramid Plunder.",
                SemanticLibrary.aliases("pyramid plunder", "pyramid plunder supplies", "plunder supplies", "sceptre run"),
                SemanticLibrary.patterns("pharaoh's sceptre", "camulet", "desert amulet", "antipoison", "anti-venom", "lockpick", "stamina potion", "energy potion", "shark", "karambwan"),
                SemanticLibrary.scores(
                    "pharaoh's sceptre", 35,
                    "lockpick", 25,
                    "antipoison", 20,
                    "stamina potion", 20),
                105),
            excludingRule(
                "Blast Furnace supplies",
                "Smithing utility items and ores used for Blast Furnace.",
                SemanticLibrary.aliases("blast furnace", "blast furnace supplies", "blast furnace prep", "bf supplies"),
                SemanticLibrary.patterns("coal bag", "ice gloves", "goldsmith gauntlets", "smiths gloves", "stamina potion", "energy potion", "coal", "iron ore", "gold ore", "mithril ore", "adamantite ore", "runite ore"),
                SemanticLibrary.scores(
                    "coal bag", 35,
                    "ice gloves", 30,
                    "goldsmith gauntlets", 25,
                    "stamina potion", 15),
                115,
                SemanticLibrary.patterns("charcoal")));
    }

    private static SemanticRule excludingRule(
        String category,
        String reason,
        List<String> queryAliases,
        List<String> itemNamePatterns,
        Map<String, Integer> itemScores,
        int baseScore,
        List<String> excludedItemNamePatterns)
    {
        return new ExcludingSemanticRule(category, reason, queryAliases, itemNamePatterns, itemScores, baseScore, excludedItemNamePatterns);
    }

    private static List<String> combatHammerPatterns()
    {
        return SemanticLibrary.patterns("granite hammer", "warhammer", "torag's hammers");
    }

    private static final class ExcludingSemanticRule extends SemanticRule
    {
        private final List<String> excludedItemNamePatterns;

        private ExcludingSemanticRule(
            String category,
            String reason,
            List<String> queryAliases,
            List<String> itemNamePatterns,
            Map<String, Integer> itemScores,
            int baseScore,
            List<String> excludedItemNamePatterns)
        {
            super(category, reason, queryAliases, itemNamePatterns, itemScores, baseScore);
            this.excludedItemNamePatterns = normalizeExcludedPatterns(excludedItemNamePatterns);
        }

        @Override
        public boolean matchesItem(String normalizedItemName)
        {
            if (!super.matchesItem(normalizedItemName))
            {
                return false;
            }

            for (String excludedPattern : excludedItemNamePatterns)
            {
                if (normalizedItemName.contains(excludedPattern))
                {
                    return false;
                }
            }
            return true;
        }

        private static List<String> normalizeExcludedPatterns(List<String> values)
        {
            if (values == null || values.isEmpty())
            {
                return Collections.emptyList();
            }

            List<String> normalized = new ArrayList<>();
            for (String value : values)
            {
                String normalizedValue = SemanticSearchEngine.normalize(value);
                if (!normalizedValue.isEmpty())
                {
                    normalized.add(normalizedValue);
                }
            }
            return Collections.unmodifiableList(normalized);
        }
    }
}
