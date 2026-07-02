package com.semanticbanksearch;

import java.util.Arrays;
import java.util.List;

final class SlayerRules
{
    private SlayerRules()
    {
    }

    static List<SemanticRule> create()
    {
        return Arrays.asList(
            SemanticLibrary.rule(
                "Dragon slayer task",
                "Useful for dragon Slayer tasks: antifire protection, dragonfire shields, and dragon-hunter weapons.",
                SemanticLibrary.aliases("dragon task", "dragon slayer task", "slayer dragons", "metal dragons", "blue dragons", "black dragons"),
                SemanticLibrary.patterns(
                    "antifire",
                    "super antifire",
                    "extended antifire",
                    "anti-dragon shield",
                    "dragonfire shield",
                    "dragon hunter lance",
                    "dragon hunter crossbow",
                    "stab weapon",
                    "hasta"),
                SemanticLibrary.scores(
                    "dragon hunter lance", 35,
                    "dragon hunter crossbow", 35,
                    "extended antifire", 25,
                    "anti-dragon shield", 20),
                120),
            SemanticLibrary.rule(
                "Demon slayer task",
                "Useful for demon Slayer tasks: demonbane weapons, prayer restoration, and Slayer gear.",
                SemanticLibrary.aliases("demon task", "demon slayer task", "black demons", "greater demons", "abyssal demons"),
                SemanticLibrary.patterns("arclight", "darklight", "silverlight", "emberlight", "prayer potion", "super restore", "slayer helmet"),
                SemanticLibrary.scores(
                    "arclight", 35,
                    "emberlight", 35,
                    "darklight", 25),
                115),
            SemanticLibrary.rule(
                "Undead slayer task",
                "Useful for undead Slayer tasks: salve amulets, undead spells, prayer restoration, and Slayer gear.",
                SemanticLibrary.aliases("undead task", "undead slayer", "ankou task", "zombie task", "skeleton task"),
                SemanticLibrary.patterns("salve amulet", "crumble undead", "slayer helmet", "prayer potion", "super restore"),
                SemanticLibrary.scores("salve amulet", 35),
                110),
            SemanticLibrary.rule(
                "Kalphite slayer task",
                "Useful for Kalphite Slayer tasks: desert access, Keris, melee weapons, and Slayer gear.",
                SemanticLibrary.aliases("kalphite task", "kalphite slayer", "kalphites"),
                SemanticLibrary.patterns("rope", "desert amulet", "shantay pass", "waterskin", "keris", "hasta", "mace", "slayer helmet"),
                SemanticLibrary.scores(
                    "keris", 30,
                    "rope", 20),
                105),
            SemanticLibrary.rule(
                "Lizardman slayer task",
                "Useful for Lizardman Slayer tasks: Shayzien protection, ranged gear, antidotes, and prayer restoration.",
                SemanticLibrary.aliases("lizardman task", "lizardmen", "shamans", "lizardman shaman"),
                SemanticLibrary.patterns("shayzien", "slayer helmet", "ranging potion", "crossbow", "blowpipe", "antidote", "prayer potion"),
                SemanticLibrary.scores("shayzien", 35),
                110),
            SemanticLibrary.rule(
                "Wyvern slayer task",
                "Useful for Wyvern Slayer tasks: wyvern shields, ranged gear, and prayer restoration.",
                SemanticLibrary.aliases("wyvern task", "skeletal wyverns", "fossil island wyverns", "wyvern slayer"),
                SemanticLibrary.patterns(
                    "elemental shield",
                    "mind shield",
                    "dragonfire shield",
                    "ancient wyvern shield",
                    "wyvern shield",
                    "ranging potion",
                    "crossbow",
                    "prayer potion"),
                SemanticLibrary.scores(
                    "ancient wyvern shield", 35,
                    "elemental shield", 25,
                    "mind shield", 25),
                115),
            SemanticLibrary.rule(
                "Dust devil slayer task",
                "Useful for Dust devil Slayer tasks: face protection, Slayer gear, burst runes, and prayer restoration.",
                SemanticLibrary.aliases("dust devil", "dust devils", "dust devil task"),
                SemanticLibrary.patterns("facemask", "slayer helmet", "death rune", "blood rune", "chaos rune", "ice burst", "ice barrage", "prayer potion"),
                SemanticLibrary.scores(
                    "facemask", 30,
                    "slayer helmet", 30,
                    "death rune", 15),
                115),
            SemanticLibrary.rule(
                "Smoke devil slayer task",
                "Useful for Smoke devil Slayer tasks: face protection, Slayer gear, magic gear, runes, and prayer restoration.",
                SemanticLibrary.aliases("smoke devil", "smoke devils", "smoke devil task"),
                SemanticLibrary.patterns("facemask", "slayer helmet", "death rune", "blood rune", "ice barrage", "occult necklace", "prayer potion"),
                SemanticLibrary.scores(
                    "slayer helmet", 30,
                    "facemask", 25),
                115),
            SemanticLibrary.rule(
                "Gargoyle slayer task",
                "Useful for Gargoyle Slayer tasks: finishing hammers, Slayer gear, sustain, and prayer restoration.",
                SemanticLibrary.aliases("gargoyle", "gargoyles", "gargoyle task"),
                SemanticLibrary.patterns("rock hammer", "granite hammer", "slayer helmet", "guthan", "prayer potion"),
                SemanticLibrary.scores(
                    "rock hammer", 35,
                    "granite hammer", 30),
                115),
            SemanticLibrary.rule(
                "Basilisk slayer task",
                "Useful for Basilisk Slayer tasks: mirror shields, V's shield, Slayer gear, and prayer restoration.",
                SemanticLibrary.aliases("basilisk", "basilisks", "basilisk task", "basilisk knight"),
                SemanticLibrary.patterns("mirror shield", "v's shield", "slayer helmet", "prayer potion"),
                SemanticLibrary.scores(
                    "mirror shield", 35,
                    "v's shield", 35),
                115));
    }
}