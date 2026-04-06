package com.legendsofsw.battleservice.strategy;

import java.util.List;

import com.legendsofsw.battleservice.model.BattleUnit;

public class OrderAttackStrategy implements AttackStrategy {

    private static final double SHIELD_PERCENT = 0.05;

    @Override
    public String execute(BattleUnit attacker, BattleUnit target, List<BattleUnit> allUnits) {
        int damage = Math.max(0, attacker.getAttack() - target.getDefense());
        applyDamage(target, damage);

        int shieldGain = (int)(attacker.getMaxHealth() * SHIELD_PERCENT);
        attacker.setShield(attacker.getShield() + shieldGain);

        return attacker.getName() + " order strikes " + target.getName()
                + " for " + damage + " damage and gains " + shieldGain + " shield.";
    }

    @Override
    public String getStrategyName() {
        return "Order Attack";
    }
}
