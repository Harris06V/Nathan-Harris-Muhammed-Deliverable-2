package com.legendsofsw.battleservice.strategy;

import java.util.List;

import com.legendsofsw.battleservice.model.BattleUnit;

public class BasicAttackStrategy implements AttackStrategy {

    @Override
    public String execute(BattleUnit attacker, BattleUnit target, List<BattleUnit> allUnits) {
        int damage = Math.max(0, attacker.getAttack() - target.getDefense());
        applyDamage(target, damage);
        return attacker.getName() + " attacks " + target.getName() + " for " + damage + " damage.";
    }

    @Override
    public String getStrategyName() {
        return "Basic Attack";
    }
}
