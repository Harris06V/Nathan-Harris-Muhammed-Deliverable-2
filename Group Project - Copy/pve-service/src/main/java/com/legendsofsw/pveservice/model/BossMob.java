package com.legendsofsw.pveservice.model;

public class BossMob extends Mob {

    private static final double BOSS_DAMAGE_MULTIPLIER = 1.5;

    public BossMob() {
        super();
    }

    public BossMob(String name, int level) {
        super(name, level,
              5 + level * 3,     // power: base 5 + 3 per level (stronger)
              5 + level * 2,     // defense: base 5 + 2 per level (tougher)
              200 + level * 20   // hp: base 200 + 20 per level (double normal)
        );
        setXpReward(100 * level);  // double xp
        setGoldReward(150 * level); // double gold
    }

    @Override
    public double getDamageMultiplier() {
        return BOSS_DAMAGE_MULTIPLIER;
    }
}
