package com.legendsofsw.battleservice.model;

public class NormalMob extends Mob {

    public NormalMob() {
        super();
    }

    public NormalMob(String name, int level) {
        super(name, level,
              5 + level * 2,    // power: base 5 + 2 per level
              5 + level,        // defense: base 5 + 1 per level
              100 + level * 10  // hp: base 100 + 10 per level
        );
    }

    @Override
    public int attack(BattleUnit target) {
        int damage = Math.max(0, getPower() - target.getDefense());
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
