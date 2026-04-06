package com.legendsofsw.battleservice.template;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.legendsofsw.battleservice.model.BattleUnit;
import com.legendsofsw.battleservice.strategy.AttackStrategy;
import com.legendsofsw.battleservice.strategy.BasicAttackStrategy;

public class PvEBattleTurn extends BattleTurnTemplate {

    private final AttackStrategy attackStrategy;

    public PvEBattleTurn() {
        this.attackStrategy = new BasicAttackStrategy();
    }

    public PvEBattleTurn(AttackStrategy attackStrategy) {
        this.attackStrategy = attackStrategy;
    }

    @Override
    protected List<BattleUnit> determineTurnOrder(List<BattleUnit> team1, List<BattleUnit> team2) {
        List<BattleUnit> all = new ArrayList<>();
        all.addAll(team1);
        all.addAll(team2);

        // sort by level descending, then attack descending
        all.sort(Comparator.comparingInt(BattleUnit::getLevel).reversed()
                .thenComparingInt(BattleUnit::getAttack).reversed());

        // interleave teams so they alternate turns
        List<BattleUnit> t1 = new ArrayList<>();
        List<BattleUnit> t2 = new ArrayList<>();
        for (BattleUnit u : all) {
            if (u.getTeam() == 1) t1.add(u);
            else t2.add(u);
        }

        List<BattleUnit> order = new ArrayList<>();
        boolean team1First = true;

        if (!t1.isEmpty() && !t2.isEmpty()) {
            BattleUnit first1 = t1.get(0);
            BattleUnit first2 = t2.get(0);
            if (first2.getLevel() > first1.getLevel() ||
                    (first2.getLevel() == first1.getLevel() && first2.getAttack() > first1.getAttack())) {
                team1First = false;
            }
        }

        int i = 0, j = 0;
        while (i < t1.size() || j < t2.size()) {
            if (team1First) {
                if (i < t1.size()) order.add(t1.get(i++));
                if (j < t2.size()) order.add(t2.get(j++));
            } else {
                if (j < t2.size()) order.add(t2.get(j++));
                if (i < t1.size()) order.add(t1.get(i++));
            }
        }

        return order;
    }

    @Override
    protected String executeCurrentAction(BattleUnit currentUnit, List<BattleUnit> allUnits) {
        if (!currentUnit.isAlive()) {
            return currentUnit.getName() + " is dead and cannot act.";
        }

        if (currentUnit.isStunned()) {
            currentUnit.setStunned(false);
            return currentUnit.getName() + " is stunned and cannot act.";
        }

        // enemy AI: pick a random alive opponent and attack
        if (!currentUnit.isPlayerUnit()) {
            List<BattleUnit> targets = new ArrayList<>();
            for (BattleUnit u : allUnits) {
                if (u.getTeam() != currentUnit.getTeam() && u.isAlive()) {
                    targets.add(u);
                }
            }
            if (!targets.isEmpty()) {
                BattleUnit target = targets.get((int)(Math.random() * targets.size()));
                return attackStrategy.execute(currentUnit, target, allUnits);
            }
            return currentUnit.getName() + " has no targets.";
        }

        return currentUnit.getName() + " awaits player input.";
    }

    @Override
    protected void applyEndOfTurnEffects(List<BattleUnit> team1, List<BattleUnit> team2) {
        // mana regeneration: +2 mana for all alive units
        List<BattleUnit> all = new ArrayList<>();
        all.addAll(team1);
        all.addAll(team2);

        for (BattleUnit unit : all) {
            if (unit.isAlive()) {
                int newMana = Math.min(unit.getCurrentMana() + 2, unit.getMaxMana());
                unit.setCurrentMana(newMana);
            }
        }
    }

    @Override
    protected String checkBattleResult(List<BattleUnit> team1, List<BattleUnit> team2) {
        if (isTeamWiped(team1)) {
            return "Team 2 wins!";
        }
        if (isTeamWiped(team2)) {
            return "Team 1 wins!";
        }
        return "";
    }
}
