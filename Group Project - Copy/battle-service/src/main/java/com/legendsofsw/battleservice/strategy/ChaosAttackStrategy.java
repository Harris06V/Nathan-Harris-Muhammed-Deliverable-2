package com.legendsofsw.battleservice.strategy;

import java.util.List;

import com.legendsofsw.battleservice.model.BattleUnit;

public class ChaosAttackStrategy implements AttackStrategy {

    private static final int DAMAGE_MULTIPLIER = 3;
    private static final double SELF_DAMAGE_RATIO = 0.5;

    @Override
    public String execute(BattleUnit attacker, BattleUnit target, List<BattleUnit> allUnits) {
        int rawDamage = Math.max(0, attacker.getAttack() - target.getDefense());
        int damage = rawDamage * DAMAGE_MULTIPLIER;
        applyDamage(target, damage);

        int selfDamage = (int)(rawDamage * SELF_DAMAGE_RATIO);
        attacker.setCurrentHealth(attacker.getCurrentHealth() - selfDamage);
        if (attacker.getCurrentHealth() <= 0) {
            attacker.setCurrentHealth(0);
            attacker.setAlive(false);
        }

        return attacker.getName() + " chaos strikes " + target.getName()
                + " for " + damage + " damage! Takes " + selfDamage + " self-damage.";
    }

    @Override
    public String getStrategyName() {
        return "Chaos Attack";
    }
}
