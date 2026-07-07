package com.semanticbanksearch;

import java.util.Arrays;
import java.util.List;

public final class ReadinessPackLibrary
{
    private ReadinessPackLibrary()
    {
    }

    public static List<ReadinessPack> create()
    {
        return Arrays.asList(
            pack(
                "Barrows trip",
                "Checks the core items for a quick Barrows run.",
                aliases("barrows trip", "barrows run", "barrows prep", "barrows setup", "crypt trip"),
                slots(
                    slot("Spade", ReadinessSlotKind.REQUIRED, "spade", "Needed to dig into the Barrows crypts."),
                    slot("Nearby teleport", ReadinessSlotKind.REQUIRED, "barrows teleport", "Gets you close to the Barrows area."),
                    slot("Prayer restoration", ReadinessSlotKind.REQUIRED, "prayer restoration", "Restores prayer during tunnels and dangerous brother fights."),
                    slot("Food", ReadinessSlotKind.RECOMMENDED, "fastest food", "Sustain for tunnel damage and mistakes."))),
            pack(
                "Vorkath trip",
                "Checks common Vorkath protection, ranged damage, ammunition, and sustain.",
                aliases("vorkath trip", "vorkath run", "vorkath prep", "vorkath setup"),
                slots(
                    slot("Dragonfire protection", ReadinessSlotKind.REQUIRED, "dragonfire protection", "Protects against dragonfire damage."),
                    slot("Ranged weapon", ReadinessSlotKind.RECOMMENDED, "dragon hunter crossbow", "Strong owned ranged option for Vorkath."),
                    slot("Bolts", ReadinessSlotKind.RECOMMENDED, "diamond dragon bolts", "Useful enchanted bolts for Vorkath kills."),
                    slot("Prayer restoration", ReadinessSlotKind.RECOMMENDED, "prayer restoration", "Restores prayer during the trip."),
                    slot("Food", ReadinessSlotKind.RECOMMENDED, "fastest food", "Sustain for mistakes and chip damage."))),
            pack(
                "Zulrah trip",
                "Checks common Zulrah venom protection, weapon switches, travel, and sustain.",
                aliases("zulrah trip", "zulrah run", "zulrah prep", "zulrah setup"),
                slots(
                    slot("Venom protection", ReadinessSlotKind.REQUIRED, "venom", "Protects against venom during the fight."),
                    slot("Magic weapon", ReadinessSlotKind.RECOMMENDED, "trident", "Magic weapon option for Zulrah phases."),
                    slot("Ranged weapon", ReadinessSlotKind.RECOMMENDED, "blowpipe", "Ranged weapon option for Zulrah phases."),
                    slot("Zulrah travel", ReadinessSlotKind.RECOMMENDED, "zul-andra teleport", "Fast travel back to Zul-Andra."),
                    slot("Food", ReadinessSlotKind.RECOMMENDED, "fastest food", "Sustain for mistakes and chip damage."))),
            pack(
                "Fight Caves",
                "Checks common Fight Caves prayer, ranged damage, and long-trip sustain.",
                aliases("fight caves", "jad", "jad supplies", "fire cape", "fight caves prep"),
                slots(
                    slot("Prayer restoration", ReadinessSlotKind.REQUIRED, "prayer restoration", "Keeps protection prayers available across waves."),
                    slot("Ranged weapon", ReadinessSlotKind.RECOMMENDED, "blowpipe", "Strong ranged option for waves and Jad."),
                    slot("Long-trip sustain", ReadinessSlotKind.RECOMMENDED, "saradomin brew", "Long-trip healing for mistakes."),
                    slot("Food", ReadinessSlotKind.OPTIONAL, "fastest food", "Extra sustain if the bank has useful food."))),
            pack(
                "Herb run",
                "Checks common farming run tools, compost, teleports, and movement support.",
                aliases("herb run", "farming run", "herb run prep", "herb run supplies"),
                slots(
                    slot("Seed dibber", ReadinessSlotKind.REQUIRED, "seed dibber", "Needed to plant herb seeds."),
                    slot("Spade", ReadinessSlotKind.REQUIRED, "spade", "Useful for clearing dead patches."),
                    slot("Compost", ReadinessSlotKind.RECOMMENDED, "ultracompost", "Improves crop survival and yield."),
                    slot("Magic secateurs", ReadinessSlotKind.RECOMMENDED, "magic secateurs", "Increases herb yield when equipped."),
                    slot("Run energy", ReadinessSlotKind.OPTIONAL, "run energy", "Keeps longer farm routes moving."))),
            pack(
                "Birdhouse run",
                "Checks common birdhouse run items and Fossil Island travel.",
                aliases("birdhouse run", "bird house run", "birdhouse prep", "birdhouse supplies"),
                slots(
                    slot("Bird houses", ReadinessSlotKind.REQUIRED, "bird house", "Built bird houses to place on Fossil Island."),
                    slot("Clockworks", ReadinessSlotKind.REQUIRED, "clockwork", "Reusable mechanism for bird houses."),
                    slot("Tools", ReadinessSlotKind.RECOMMENDED, "hammer", "Tool support for birdhouse replacement."),
                    slot("Fossil Island travel", ReadinessSlotKind.RECOMMENDED, "fossil island travel", "Gets you back to Fossil Island quickly."))),
            pack(
                "Farm contract",
                "Checks common farming contract tools, compost, and boost items.",
                aliases("farm contract", "farm contracts", "farming contract", "farming contracts"),
                slots(
                    slot("Farming tools", ReadinessSlotKind.REQUIRED, "seed dibber", "Core tool for planting contract crops."),
                    slot("Compost", ReadinessSlotKind.RECOMMENDED, "ultracompost", "Improves survival for contract crops."),
                    slot("Boost", ReadinessSlotKind.OPTIONAL, "garden pie", "Useful farming boost for some planting requirements."),
                    slot("Seeds and saplings", ReadinessSlotKind.OPTIONAL, "farm contract", "Observed seeds and saplings that may satisfy contracts."))),
            pack(
                "Wildy escape",
                "Checks items that help leave dangerous wilderness situations.",
                aliases("wildy escape", "wilderness escape", "escape pkers", "wilderness survival", "wildy survival"),
                slots(
                    slot("One-click escape teleport", ReadinessSlotKind.REQUIRED, "wildy escape", "Fast teleport options for leaving danger when level rules allow it."),
                    slot("Food", ReadinessSlotKind.REQUIRED, "fastest food", "Immediate healing for escapes."),
                    slot("Combo food", ReadinessSlotKind.RECOMMENDED, "karambwan", "Fast combo healing for sudden damage."),
                    slot("Run energy", ReadinessSlotKind.RECOMMENDED, "run energy", "Helps maintain distance while escaping."),
                    slot("Prayer restoration", ReadinessSlotKind.OPTIONAL, "prayer restoration", "Keeps protection prayers available."))),
            pack(
                "Wildy boss",
                "Checks risk-light Wilderness boss supplies and escape support.",
                aliases("wildy boss", "wildy bosses", "wilderness boss", "wilderness bosses", "wildy boss trip"),
                slots(
                    slot("Escape teleport", ReadinessSlotKind.REQUIRED, "wildy escape", "Fast escape option for dangerous Wilderness trips."),
                    slot("Blighted supplies", ReadinessSlotKind.RECOMMENDED, "blighted super restore", "Wilderness-specific restore supplies."),
                    slot("Risk-light gear", ReadinessSlotKind.RECOMMENDED, "black d'hide", "Common low-risk defensive gear."),
                    slot("Freeze support", ReadinessSlotKind.OPTIONAL, "freeze runes", "Runes and staves for freezes or binds."))),
            pack(
                "Dagannoth Kings",
                "Checks common Dagannoth Kings entry tools, poison protection, and sustain.",
                aliases("dagannoth kings", "dks", "dk trip", "rex prime supreme"),
                slots(
                    slot("Door tool", ReadinessSlotKind.REQUIRED, "rune thrownaxe", "Used for Waterbirth Dungeon pathing."),
                    slot("Prayer restoration", ReadinessSlotKind.RECOMMENDED, "prayer restoration", "Restores prayer during DK trips."),
                    slot("Poison protection", ReadinessSlotKind.RECOMMENDED, "poison protection", "Protects against poison on the trip."),
                    slot("Food", ReadinessSlotKind.RECOMMENDED, "fastest food", "Sustain for mistakes and travel damage."))),
            pack(
                "Slayer task",
                "Checks generic Slayer task supplies without assuming a specific monster.",
                aliases("slayer task", "slayer prep", "slayer setup", "generic slayer"),
                slots(
                    slot("Slayer item", ReadinessSlotKind.RECOMMENDED, "slayer helmet", "Common Slayer damage and utility item."),
                    slot("Combat boost", ReadinessSlotKind.RECOMMENDED, "combat boost", "Boosts combat stats for the task."),
                    slot("Prayer restoration", ReadinessSlotKind.OPTIONAL, "prayer restoration", "Restores prayer for protection or offensive prayers."),
                    slot("Food", ReadinessSlotKind.OPTIONAL, "fastest food", "General sustain for the task."))),
            pack(
                "Clue step",
                "Checks common clue tools, travel, and stash-building supplies.",
                aliases("clue step", "clue steps", "clue prep", "clue supplies", "clue stash"),
                slots(
                    slot("Spade", ReadinessSlotKind.REQUIRED, "dig clue", "Needed for dig and coordinate clue steps."),
                    slot("Navigation tools", ReadinessSlotKind.RECOMMENDED, "coordinate clue", "Sextant, watch, and chart support coordinate clues."),
                    slot("Light source", ReadinessSlotKind.RECOMMENDED, "light source", "Useful for clue steps in dark areas."),
                    slot("Stash supplies", ReadinessSlotKind.OPTIONAL, "clue stash", "Useful supplies for building or filling STASH units."),
                    slot("Travel", ReadinessSlotKind.OPTIONAL, "teleport jewellery", "General teleports for moving between clue locations."))),
            pack(
                "Quest tools",
                "Checks common quest and diary utility tools.",
                aliases("quest tools", "quest utility", "diary tools", "achievement diary tools"),
                slots(
                    slot("General tools", ReadinessSlotKind.RECOMMENDED, "quest utility", "Common items used by quests and diaries."),
                    slot("Digging", ReadinessSlotKind.RECOMMENDED, "spade", "Frequent quest and clue utility item."),
                    slot("Light source", ReadinessSlotKind.OPTIONAL, "light source", "Useful for dark quest areas."),
                    slot("Traversal", ReadinessSlotKind.OPTIONAL, "traversal tools", "Tools used for access routes and obstacles."))),
            pack(
                "Wintertodt",
                "Checks common Wintertodt warmth, tools, and food.",
                aliases("wintertodt", "wintertodt prep", "wintertodt supplies", "todt"),
                slots(
                    slot("Warm clothing", ReadinessSlotKind.RECOMMENDED, "warm clothing", "Reduces cold damage."),
                    slot("Tool", ReadinessSlotKind.RECOMMENDED, "knife", "Useful for fletching roots when wanted."),
                    slot("Food", ReadinessSlotKind.RECOMMENDED, "fastest food", "Healing for Wintertodt damage."))));
    }

    private static ReadinessPack pack(String name, String description, List<String> aliases, List<ReadinessSlot> slots)
    {
        return new ReadinessPack(name, description, aliases, slots);
    }

    private static ReadinessSlot slot(String name, ReadinessSlotKind kind, String query, String reason)
    {
        return new ReadinessSlot(name, kind, query, reason);
    }

    private static List<String> aliases(String... values)
    {
        return Arrays.asList(values);
    }

    private static List<ReadinessSlot> slots(ReadinessSlot... values)
    {
        return Arrays.asList(values);
    }
}
