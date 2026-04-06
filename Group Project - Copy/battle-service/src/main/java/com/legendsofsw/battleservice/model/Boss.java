package com.legendsofsw.battleservice.model;

public class Boss extends Mob {

    private static final double BOSS_DAMAGE_MULTIPLIER = 1.5;

    public Boss() {
        super();
    }

    public Boss(String name, int level) {
        super(name, level,
              5 + level * 3,     // power: base 5 + 3 per level (stronger than normal)
              5 + level * 2,     // defense: base 5 + 2 per level (tougher than normal)
              200 + level * 20   // hp: base 200 + 20 per level (double normal)
        );
        setXpReward(100 * level);  // double xp
        setGoldReward(150 * level); // double gold
    }

    @Override
    public int attack(BattleUnit target) {
        int rawDamage = Math.max(0, getPower() - target.getDefense());
        int damage = (int)(rawDamage * BOSS_DAMAGE_MULTIPLIER);
        int remaining = damage;

        if (target.getShield() > 0) {
            if (target.getShield() >= remaining) {
                target.setShield(target.getShield() - remaining);
                remaining = 0;
            } else {
                remaining -= target.getShield();
                target.setShield(0);
            }
        }

        target.setCurrentHealth(target.getCurrentHealth() - remaining);
        if (target.getCurrentHealth() <= 0) {
            target.setCurrentHealth(0);
            target.setAlive(false);
        }

        return damage;
    }
}
