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
                SemanticLibrary.patterns("stamina potion", "energy potion", "super energy", "strange fruit"),
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
                SemanticLibrary.patterns("combat potion", "super combat", "attack potion", "strength potion", "defence potion", "divine super combat"),
                SemanticLibrary.scores(),
                100),
            SemanticLibrary.rule(
                "Ranged and magic boosts",
                "Boosts ranged or magic.",
                SemanticLibrary.aliases("ranged boost", "range boost", "magic boost", "mage boost"),
                SemanticLibrary.patterns("ranging potion", "bastion potion", "magic potion", "forgotten brew", "imbued heart"),
                SemanticLibrary.scores(),
                100));
    }
}
