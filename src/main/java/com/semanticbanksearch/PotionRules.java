package com.semanticbanksearch;

import java.util.Arrays;
import java.util.List;

final class PotionRules
{
    private PotionRules()
    {
    }

    static List<SemanticRule> create()
    {
        return Arrays.asList(
            SemanticLibrary.rule(
                "Prayer restoration",
                "Restores prayer points.",
                SemanticLibrary.aliases("prayer", "restore prayer", "prayer restoration", "ppot"),
                SemanticLibrary.patterns("prayer potion", "super restore", "sanfew serum", "blighted super restore"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Run energy restoration",
                "Restores or preserves run energy.",
                SemanticLibrary.aliases("stamina", "run energy", "energy potion", "restore run"),
                SemanticLibrary.patterns("stamina potion", "energy potion", "super energy", "strange fruit", "stamina mix", "ring of endurance"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Poison protection",
                "Protects against poison or venom.",
                SemanticLibrary.aliases("poison", "venom", "poison protection", "antipoison"),
                SemanticLibrary.patterns("antipoison", "anti-venom", "sanfew serum", "antidote"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Antifire protection",
                "Protects against dragonfire.",
                SemanticLibrary.aliases("antifire", "dragonfire", "anti dragon", "dragon protection"),
                SemanticLibrary.patterns("antifire", "super antifire", "extended antifire", "anti-dragon shield", "dragonfire shield"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Combat boosts",
                "Boosts combat stats.",
                SemanticLibrary.aliases("combat boost", "melee boost", "strength boost", "attack boost", "defence boost"),
                SemanticLibrary.patterns("combat potion", "super combat", "attack potion", "strength potion", "defence potion", "divine super combat", "super attack", "super strength", "super defence", "divine super attack", "divine super strength", "divine super defence"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Ranged and magic boosts",
                "Boosts ranged or magic.",
                SemanticLibrary.aliases("ranged boost", "range boost", "magic boost", "mage boost"),
                SemanticLibrary.patterns("ranging potion", "bastion potion", "magic potion", "forgotten brew", "imbued heart", "divine ranging potion", "divine magic potion", "saturated heart"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Skilling boosts",
                "Temporary boosts for skilling levels.",
                SemanticLibrary.aliases("skilling boost", "skill boost", "boost farming", "boost mining", "boost woodcutting", "boost crafting"),
                SemanticLibrary.patterns("botanical pie", "garden pie", "mushroom pie", "admiral pie", "wild pie", "spicy stew", "dwarven stout", "mature dwarven stout", "chef's delight", "wizard's mind bomb"),
                SemanticLibrary.scores(
                    "spicy stew", 30,
                    "botanical pie", 20,
                    "garden pie", 20,
                    "mushroom pie", 20),
                100));
    }
}
