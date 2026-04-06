package com.legendsofsw.battleservice.strategy;

import java.util.List;

import com.legendsofsw.battleservice.model.BattleUnit;

public class BossAttackStrategy implements AttackStrategy {

    private static final double BOSS_MULTIPLIER = 1.5;

    @Override
    public String execute(BattleUnit attacker, BattleUnit target, List<BattleUnit> allUnits) {
        int damage = (int)(Math.max(0, attacker.getAttack() - target.getDefense()) * BOSS_MULTIPLIER);
        applyDamage(target, damage);
        return attacker.getName() + " unleashes a powerful boss attack on " + target.getName()
                + " for " + damage + " damage!";
    }

    @Override
    public String getStrategyName() {
        return "Boss Attack";
    }
}
