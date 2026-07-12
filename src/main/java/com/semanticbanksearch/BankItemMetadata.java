package com.semanticbanksearch;

final class BankItemMetadata
{
    private final String name;
    private final boolean equipable;
    private final boolean wieldable;
    private final boolean edible;
    private final boolean drinkable;
    private final int equipmentSlot;
    private final int attackStab;
    private final int attackSlash;
    private final int attackCrush;
    private final int attackMagic;
    private final int attackRange;
    private final int strength;
    private final int rangedStrength;
    private final float magicDamage;
    private final int prayer;
    private int healing = -1;

    BankItemMetadata(
        String name,
        boolean equipable,
        boolean wieldable,
        boolean edible,
        boolean drinkable,
        int equipmentSlot,
        int attackStab,
        int attackSlash,
        int attackCrush,
        int attackMagic,
        int attackRange,
        int strength,
        int rangedStrength,
        float magicDamage,
        int prayer)
    {
        this.name = name == null ? "" : name.trim();
        this.equipable = equipable;
        this.wieldable = wieldable;
        this.edible = edible;
        this.drinkable = drinkable;
        this.equipmentSlot = equipmentSlot;
        this.attackStab = attackStab;
        this.attackSlash = attackSlash;
        this.attackCrush = attackCrush;
        this.attackMagic = attackMagic;
        this.attackRange = attackRange;
        this.strength = strength;
        this.rangedStrength = rangedStrength;
        this.magicDamage = magicDamage;
        this.prayer = prayer;
    }

    BankItemMetadata(
        String name,
        boolean equipable,
        boolean wieldable,
        boolean edible,
        boolean drinkable,
        int equipmentSlot,
        int attackStab,
        int attackSlash,
        int attackCrush,
        int attackMagic,
        int attackRange,
        int strength,
        int rangedStrength,
        float magicDamage,
        int prayer,
        int healing)
    {
        this(
            name,
            equipable,
            wieldable,
            edible,
            drinkable,
            equipmentSlot,
            attackStab,
            attackSlash,
            attackCrush,
            attackMagic,
            attackRange,
            strength,
            rangedStrength,
            magicDamage,
            prayer);
        this.healing = Math.max(-1, healing);
    }

    String getName()
    {
        return name;
    }

    boolean isEquipable()
    {
        return equipable;
    }

    boolean isWieldable()
    {
        return wieldable;
    }

    boolean isEdible()
    {
        return edible;
    }

    boolean isDrinkable()
    {
        return drinkable;
    }

    int getEquipmentSlot()
    {
        return equipmentSlot;
    }

    int meleeScore()
    {
        return Math.max(Math.max(attackStab, attackSlash), attackCrush) + Math.max(0, strength);
    }

    int rangedScore()
    {
        return attackRange + Math.max(0, rangedStrength);
    }

    float magicScore()
    {
        return attackMagic + Math.max(0f, magicDamage);
    }

    int getPrayer()
    {
        return prayer;
    }

    int getHealing()
    {
        return healing;
    }
}
