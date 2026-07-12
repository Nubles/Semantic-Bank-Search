package com.semanticbanksearch;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.runelite.api.EquipmentInventorySlot;

final class RelativeRankingClassifier
{
    private enum Ranking
    {
        NONE,
        BEST_FOOD,
        GOOD_FOOD,
        BEST_PRAYER,
        BEST_MELEE,
        BEST_RANGED,
        BEST_MAGIC
    }

    private static final Set<String> BEST_FOOD_QUERIES = Set.of(
        "best food", "best food i own", "highest healing food", "most healing food", "strongest food");
    private static final Set<String> GOOD_FOOD_QUERIES = Set.of(
        "good food", "good healing food", "high healing food");
    private static final Set<String> BEST_PRAYER_QUERIES = Set.of(
        "best prayer gear", "highest prayer gear", "highest prayer bonus", "best prayer equipment");
    private static final Set<String> BEST_MELEE_QUERIES = Set.of(
        "best melee weapon", "best melee wep", "strongest melee weapon", "strongest melee wep");
    private static final Set<String> BEST_RANGED_QUERIES = Set.of(
        "best ranged weapon", "best range weapon", "best ranged wep", "strongest ranged weapon", "strongest range weapon");
    private static final Set<String> BEST_MAGIC_QUERIES = Set.of(
        "best magic weapon", "best mage weapon", "best magic wep", "strongest magic weapon", "strongest mage weapon");

    boolean recognizes(String query)
    {
        return ranking(query) != Ranking.NONE;
    }

    boolean matches(String query, BankItemMetadata candidate, List<BankItemMetadata> ownedItems)
    {
        if (candidate == null)
        {
            return false;
        }

        List<BankItemMetadata> safeItems = ownedItems == null ? Collections.emptyList() : ownedItems;
        switch (ranking(query))
        {
            case BEST_FOOD:
                return candidate.isEdible() && candidate.getHealing() >= 0
                    && candidate.getHealing() == maximum(safeItems, Ranking.BEST_FOOD, candidate);
            case GOOD_FOOD:
                int bestHealing = maximum(safeItems, Ranking.BEST_FOOD, candidate);
                return candidate.isEdible() && candidate.getHealing() >= 0 && bestHealing >= 0
                    && candidate.getHealing() * 100 >= bestHealing * 80;
            case BEST_PRAYER:
                return candidate.isEquipable() && candidate.getPrayer() > 0
                    && candidate.getPrayer() == maximum(safeItems, Ranking.BEST_PRAYER, candidate);
            case BEST_MELEE:
                return isWeapon(candidate) && candidate.meleeScore() > 0
                    && candidate.meleeScore() == maximum(safeItems, Ranking.BEST_MELEE, candidate);
            case BEST_RANGED:
                return isWeapon(candidate) && candidate.rangedScore() > 0
                    && candidate.rangedScore() == maximum(safeItems, Ranking.BEST_RANGED, candidate);
            case BEST_MAGIC:
                return isWeapon(candidate) && candidate.magicScore() > 0
                    && Math.round(candidate.magicScore()) == maximum(safeItems, Ranking.BEST_MAGIC, candidate);
            default:
                return false;
        }
    }

    String explanation(String query, BankItemMetadata item)
    {
        if (item == null)
        {
            return "";
        }
        switch (ranking(query))
        {
            case BEST_FOOD:
            case GOOD_FOOD:
                return "Heals " + item.getHealing() + " HP";
            case BEST_PRAYER:
                return "+" + item.getPrayer() + " prayer in this equipment slot";
            case BEST_MELEE:
                return "Melee score " + item.meleeScore();
            case BEST_RANGED:
                return "Ranged score " + item.rangedScore();
            case BEST_MAGIC:
                return "Magic score " + formatScore(item.magicScore());
            default:
                return "";
        }
    }

    int score(String query, BankItemMetadata item)
    {
        if (item == null)
        {
            return 0;
        }
        switch (ranking(query))
        {
            case BEST_FOOD:
            case GOOD_FOOD:
                return item.getHealing();
            case BEST_PRAYER:
                return item.getPrayer();
            case BEST_MELEE:
                return item.meleeScore();
            case BEST_RANGED:
                return item.rangedScore();
            case BEST_MAGIC:
                return Math.round(item.magicScore() * 10f);
            default:
                return 0;
        }
    }

    private static int maximum(List<BankItemMetadata> items, Ranking ranking, BankItemMetadata candidate)
    {
        int maximum = -1;
        for (BankItemMetadata item : items)
        {
            if (item == null)
            {
                continue;
            }
            if (ranking == Ranking.BEST_FOOD && item.isEdible())
            {
                maximum = Math.max(maximum, item.getHealing());
            }
            else if (ranking == Ranking.BEST_PRAYER && item.isEquipable()
                && item.getEquipmentSlot() == candidate.getEquipmentSlot())
            {
                maximum = Math.max(maximum, item.getPrayer());
            }
            else if (ranking == Ranking.BEST_MELEE && isWeapon(item))
            {
                maximum = Math.max(maximum, item.meleeScore());
            }
            else if (ranking == Ranking.BEST_RANGED && isWeapon(item))
            {
                maximum = Math.max(maximum, item.rangedScore());
            }
            else if (ranking == Ranking.BEST_MAGIC && isWeapon(item))
            {
                maximum = Math.max(maximum, Math.round(item.magicScore()));
            }
        }
        return maximum;
    }

    private static boolean isWeapon(BankItemMetadata item)
    {
        return item.getEquipmentSlot() == EquipmentInventorySlot.WEAPON.getSlotIdx() || item.isWieldable();
    }

    private static String formatScore(float score)
    {
        return score == Math.round(score) ? Integer.toString(Math.round(score)) : Float.toString(score);
    }

    private static Ranking ranking(String query)
    {
        String normalized = SemanticSearchEngine.normalize(query);
        if (BEST_FOOD_QUERIES.contains(normalized))
        {
            return Ranking.BEST_FOOD;
        }
        if (GOOD_FOOD_QUERIES.contains(normalized))
        {
            return Ranking.GOOD_FOOD;
        }
        if (BEST_PRAYER_QUERIES.contains(normalized))
        {
            return Ranking.BEST_PRAYER;
        }
        if (BEST_MELEE_QUERIES.contains(normalized))
        {
            return Ranking.BEST_MELEE;
        }
        if (BEST_RANGED_QUERIES.contains(normalized))
        {
            return Ranking.BEST_RANGED;
        }
        if (BEST_MAGIC_QUERIES.contains(normalized))
        {
            return Ranking.BEST_MAGIC;
        }
        return Ranking.NONE;
    }
}
