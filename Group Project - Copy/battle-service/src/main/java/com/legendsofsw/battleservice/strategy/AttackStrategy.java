package com.legendsofsw.battleservice.strategy;

import java.util.List;

import com.legendsofsw.battleservice.model.BattleUnit;

public interface AttackStrategy {

    String execute(BattleUnit attacker, BattleUnit target, List<BattleUnit> allUnits);

    String getStrategyName();

    default void applyDamage(BattleUnit target, int damage) {
        if (damage <= 0) return;
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
    }
}
