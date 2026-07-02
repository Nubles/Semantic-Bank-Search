package com.semanticbanksearch;

import java.util.Arrays;
import java.util.List;

final class CombatRules
{
    private CombatRules()
    {
    }

    static List<SemanticRule> create()
    {
        return Arrays.asList(
            SemanticLibrary.rule(
                "Crush weapons",
                "Can attack with crush style.",
                SemanticLibrary.aliases("crush", "crush weapon", "crush weapons"),
                SemanticLibrary.patterns("mace", "warhammer", "maul", "anchor", "hasta", "godsword", "bludgeon"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Slash weapons",
                "Can attack with slash style or cut webs.",
                SemanticLibrary.aliases("slash", "slash weapon", "cut webs"),
                SemanticLibrary.patterns("scimitar", "sword", "dagger", "machete", "axe", "spear"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Stab weapons",
                "Can attack with stab style.",
                SemanticLibrary.aliases("stab", "stab weapon"),
                SemanticLibrary.patterns("dagger", "spear", "hasta", "rapier", "shortsword"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Ranged weapons",
                "Can attack with ranged combat.",
                SemanticLibrary.aliases("ranged weapon", "range weapon", "bow", "crossbow"),
                SemanticLibrary.patterns("bow", "crossbow", "blowpipe", "ballista", "chinchompa"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Ranged ammunition",
                "Ammunition for ranged weapons.",
                SemanticLibrary.aliases("ranged ammo", "range ammo", "arrows", "bolts"),
                SemanticLibrary.patterns("arrow", "arrows", "bolt", "bolts", "dart", "darts", "javelin", "knife"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Magic runes",
                "Runes used to cast spells.",
                SemanticLibrary.aliases("magic runes", "spell runes"),
                SemanticLibrary.patterns(
                    "air rune",
                    "water rune",
                    "earth rune",
                    "fire rune",
                    "mind rune",
                    "chaos rune",
                    "death rune",
                    "blood rune",
                    "soul rune",
                    "law rune",
                    "nature rune",
                    "cosmic rune",
                    "astral rune"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Magic weapons",
                "Weapons used for magic combat.",
                SemanticLibrary.aliases("magic weapon", "mage weapon", "staff", "wand"),
                SemanticLibrary.patterns("staff", "wand", "trident", "sceptre", "tome"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Alchemy runes",
                "Runes and fire sources used for alchemy spells.",
                SemanticLibrary.aliases("alchemy", "alchemy runes", "high alch", "low alch", "alching"),
                SemanticLibrary.patterns("nature rune", "fire rune", "staff of fire", "fire staff", "tome of fire", "smoke battlestaff"),
                SemanticLibrary.scores(
                    "nature rune", 30,
                    "staff of fire", 20,
                    "fire rune", 15),
                105),
            SemanticLibrary.rule(
                "Binding and freezing runes",
                "Runes and staves used for binds, freezes, and Ancient Magicks.",
                SemanticLibrary.aliases("freeze runes", "binding runes", "ice barrage", "ice burst", "entangle"),
                SemanticLibrary.patterns("water rune", "earth rune", "death rune", "blood rune", "chaos rune", "nature rune", "ancient staff", "kodai", "water staff"),
                SemanticLibrary.scores(
                    "death rune", 20,
                    "blood rune", 20,
                    "ancient staff", 20),
                105));
    }
}
