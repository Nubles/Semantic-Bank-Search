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
                "Clue step",
                "Checks common clue tools, travel, and stash-building supplies.",
                aliases("clue step", "clue steps", "clue prep", "clue supplies", "clue stash"),
                slots(
                    slot("Spade", ReadinessSlotKind.REQUIRED, "dig clue", "Needed for dig and coordinate clue steps."),
                    slot("Navigation tools", ReadinessSlotKind.RECOMMENDED, "coordinate clue", "Sextant, watch, and chart support coordinate clues."),
                    slot("Light source", ReadinessSlotKind.RECOMMENDED, "light source", "Useful for clue steps in dark areas."),
                    slot("Stash supplies", ReadinessSlotKind.OPTIONAL, "clue stash", "Useful supplies for building or filling STASH units."),
                    slot("Travel", ReadinessSlotKind.OPTIONAL, "teleport jewellery", "General teleports for moving between clue locations."))));
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

