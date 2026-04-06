package com.legendsofsw.battleservice.strategy;

import java.util.List;

import com.legendsofsw.battleservice.model.BattleUnit;

public class MageAttackStrategy implements AttackStrategy {

    private static final int MANA_COST = 10;
    private static final double MAGIC_MULTIPLIER = 1.5;

    @Override
    public String execute(BattleUnit attacker, BattleUnit target, List<BattleUnit> allUnits) {
        if (attacker.getCurrentMana() < MANA_COST) {
            int damage = Math.max(0, attacker.getAttack() - target.getDefense());
            applyDamage(target, damage);
            return attacker.getName() + " attacks " + target.getName()
                    + " for " + damage + " damage (low mana).";
        }

        attacker.setCurrentMana(attacker.getCurrentMana() - MANA_COST);
        int damage = Math.max(0, (int)(attacker.getAttack() * MAGIC_MULTIPLIER) - target.getDefense());
        applyDamage(target, damage);
        return attacker.getName() + " casts arcane bolt at " + target.getName()
                + " for " + damage + " magic damage.";
    }

    @Override
    public String getStrategyName() {
        return "Mage Attack";
    }
}
