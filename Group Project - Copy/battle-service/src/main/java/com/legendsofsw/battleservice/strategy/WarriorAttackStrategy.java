package com.legendsofsw.battleservice.strategy;

import java.util.List;

import com.legendsofsw.battleservice.model.BattleUnit;

public class WarriorAttackStrategy implements AttackStrategy {

    private static final int WARRIOR_BONUS = 2;

    @Override
    public String execute(BattleUnit attacker, BattleUnit target, List<BattleUnit> allUnits) {
        int damage = Math.max(0, attacker.getAttack() + WARRIOR_BONUS - target.getDefense());
        applyDamage(target, damage);
        return attacker.getName() + " warrior strikes " + target.getName()
                + " for " + damage + " damage (+" + WARRIOR_BONUS + " warrior bonus).";
    }

    @Override
    public String getStrategyName() {
        return "Warrior Attack";
    }
}
