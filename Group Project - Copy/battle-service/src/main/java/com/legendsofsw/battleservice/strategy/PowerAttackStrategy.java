package com.legendsofsw.battleservice.strategy;

import java.util.List;

import com.legendsofsw.battleservice.model.BattleUnit;

public class PowerAttackStrategy implements AttackStrategy {

    private static final double POWER_MULTIPLIER = 1.5;

    @Override
    public String execute(BattleUnit attacker, BattleUnit target, List<BattleUnit> allUnits) {
        int damage = (int)(Math.max(0, attacker.getAttack() - target.getDefense()) * POWER_MULTIPLIER);
        applyDamage(target, damage);
        return attacker.getName() + " power attacks " + target.getName()
                + " for " + damage + " damage.";
    }

    @Override
    public String getStrategyName() {
        return "Power Attack";
    }
}
