package com.legendsofsw.battleservice.template;

import java.util.List;

import com.legendsofsw.battleservice.model.BattleUnit;

public abstract class BattleTurnTemplate {

    // template method - defines the skeleton of a battle turn
    public final String executeTurn(List<BattleUnit> team1, List<BattleUnit> team2,
                                     BattleUnit currentUnit, List<BattleUnit> allUnits) {
        StringBuilder log = new StringBuilder();

        List<BattleUnit> turnOrder = determineTurnOrder(team1, team2);
        log.append(executeCurrentAction(currentUnit, allUnits));
        applyEndOfTurnEffects(team1, team2);

        String result = checkBattleResult(team1, team2);
        if (!result.isEmpty()) {
            log.append(" ").append(result);
        }

        return log.toString();
    }

    // step 1: determine the order units act in
    protected abstract List<BattleUnit> determineTurnOrder(List<BattleUnit> team1, List<BattleUnit> team2);

    // step 2: execute the current unit's action
    protected abstract String executeCurrentAction(BattleUnit currentUnit, List<BattleUnit> allUnits);

    // step 3: apply end of turn effects (regen, poison, etc)
    protected abstract void applyEndOfTurnEffects(List<BattleUnit> team1, List<BattleUnit> team2);

    // step 4: check if battle is over
    protected abstract String checkBattleResult(List<BattleUnit> team1, List<BattleUnit> team2);

    // helper: check if all units on a team are dead
    protected boolean isTeamWiped(List<BattleUnit> team) {
        for (BattleUnit unit : team) {
            if (unit.isAlive()) return false;
        }
        return true;
    }
}
