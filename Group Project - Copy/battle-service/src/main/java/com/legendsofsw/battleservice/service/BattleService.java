package com.legendsofsw.battleservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legendsofsw.battleservice.dto.ActionRequest;
import com.legendsofsw.battleservice.dto.BattleRequest;
import com.legendsofsw.battleservice.dto.BattleStateResponse;
import com.legendsofsw.battleservice.model.ActionType;
import com.legendsofsw.battleservice.model.Battle;
import com.legendsofsw.battleservice.model.BattleStatus;
import com.legendsofsw.battleservice.model.BattleUnit;
import com.legendsofsw.battleservice.repository.BattleRepository;
import com.legendsofsw.battleservice.template.BattleTurnTemplate;
import com.legendsofsw.battleservice.template.PvEBattleTurn;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class BattleService {

    private final BattleRepository battleRepo;
    private final CombatCalculator combatCalc;
    private final ObjectMapper objectMapper;
    private final BattleTurnTemplate turnTemplate;

    public BattleService(BattleRepository battleRepo, CombatCalculator combatCalc) {
        this.battleRepo = battleRepo;
        this.combatCalc = combatCalc;
        this.objectMapper = new ObjectMapper();
        this.turnTemplate = new PvEBattleTurn();
    }

    public BattleStateResponse createBattle(BattleRequest request) {
        // tag units with their team number
        for (BattleUnit u : request.getTeam1()) { u.setTeam(1); }
        for (BattleUnit u : request.getTeam2()) { u.setTeam(2); }

        List<BattleUnit> allUnits = new ArrayList<>();
        allUnits.addAll(request.getTeam1());
        allUnits.addAll(request.getTeam2());

        // sort by level desc, then attack desc for turn order
        allUnits.sort(Comparator.comparingInt(BattleUnit::getLevel).reversed()
                .thenComparingInt(BattleUnit::getAttack).reversed());

        // interleave teams so they alternate turns
        List<BattleUnit> turnOrder = buildTurnOrder(allUnits);

        Battle battle = new Battle();
        battle.setPvp(request.isPvp());
        battle.setStateJson(toJson(turnOrder));
        battle.setCurrentUnitIndex(0);
        battle = battleRepo.save(battle);

        return buildResponse(battle, turnOrder, "Battle started.");
    }

    public BattleStateResponse getBattleState(Long battleId) {
        Battle battle = battleRepo.findById(battleId)
                .orElseThrow(() -> new RuntimeException("Battle not found"));
        List<BattleUnit> units = fromJson(battle.getStateJson());
        return buildResponse(battle, units, battle.getLastActionResult());
    }

    public BattleStateResponse performAction(Long battleId, ActionRequest request) {
        Battle battle = battleRepo.findById(battleId)
                .orElseThrow(() -> new RuntimeException("Battle not found"));

        if (battle.getStatus() != BattleStatus.IN_PROGRESS) {
            throw new RuntimeException("Battle is already over");
        }

        List<BattleUnit> units = fromJson(battle.getStateJson());
        BattleUnit currentUnit = findCurrentUnit(units, battle.getCurrentUnitIndex());

        // handle stunned units
        if (currentUnit.isStunned()) {
            currentUnit.setStunned(false);
            String result = currentUnit.getName() + " is stunned and cannot act.";
            advanceTurn(battle, units);
            battle.setStateJson(toJson(units));
            battleRepo.save(battle);
            return buildResponse(battle, units, result);
        }

        ActionType actionType = ActionType.valueOf(request.getActionType().toUpperCase());

        // WAIT: move unit to end of current round (FIFO postpone)
        if (actionType == ActionType.WAIT) {
            String result = currentUnit.getName() + " waits.";
            int idx = battle.getCurrentUnitIndex();
            units.remove(idx);
            units.add(currentUnit);
            // adjust index so advanceTurn picks the unit now at idx
            battle.setCurrentUnitIndex(idx - 1);
            advanceTurn(battle, units);
            battle.setLastActionResult(result);
            battle.setStateJson(toJson(units));
            battleRepo.save(battle);
            return buildResponse(battle, units, result);
        }

        String result = executeAction(currentUnit, actionType, request.getTargetIndex(), units);

        // check for fire shield reflection
        if (actionType == ActionType.ATTACK && request.getTargetIndex() != null) {
            BattleUnit target = getAliveUnitByIndex(units, request.getTargetIndex());
            if (target != null && isHereticShielded(currentUnit, target, units)) {
                int rawDamage = Math.max(0, currentUnit.getAttack() - target.getDefense());
                int reflected = combatCalc.getFireShieldReflect(target, rawDamage);
                if (reflected > 0) {
                    currentUnit.setCurrentHealth(currentUnit.getCurrentHealth() - reflected);
                    if (currentUnit.getCurrentHealth() <= 0) {
                        currentUnit.setCurrentHealth(0);
                        currentUnit.setAlive(false);
                    }
                    result += " Fire Shield reflects " + reflected + " damage!";
                }
            }
        }

        // check win condition
        if (combatCalc.isTeamDefeated(1, units)) {
            battle.setStatus(BattleStatus.TEAM2_WIN);
        } else if (combatCalc.isTeamDefeated(2, units)) {
            battle.setStatus(BattleStatus.TEAM1_WIN);
        } else {
            advanceTurn(battle, units);
        }

        battle.setLastActionResult(result);
        battle.setStateJson(toJson(units));
        battleRepo.save(battle);

        return buildResponse(battle, units, result);
    }

    private String executeAction(BattleUnit unit, ActionType action, Integer targetIdx, List<BattleUnit> units) {
        BattleUnit target = null;
        if (targetIdx != null) {
            target = getAliveUnitByIndex(units, targetIdx);
        }

        return switch (action) {
            case ATTACK -> {
                if (target == null) throw new RuntimeException("Must specify a target for attack");
                yield combatCalc.processAttack(unit, target, units);
            }
            case DEFEND -> combatCalc.processDefend(unit);
            case WAIT -> unit.getName() + " waits.";
            case CAST_PROTECT -> combatCalc.processProtect(unit, units);
            case CAST_HEAL -> combatCalc.processHeal(unit, units);
            case CAST_FIREBALL -> {
                if (target == null) throw new RuntimeException("Must specify a target for Fireball");
                yield combatCalc.processFireball(unit, target, units);
            }
            case CAST_CHAIN_LIGHTNING -> {
                if (target == null) throw new RuntimeException("Must specify a starting target for Chain Lightning");
                yield combatCalc.processChainLightning(unit, target, units);
            }
            case CAST_BERSERKER_ATTACK -> {
                if (target == null) throw new RuntimeException("Must specify a target for Berserker Attack");
                yield combatCalc.processBerserkerAttack(unit, target, units);
            }
            case CAST_REPLENISH -> combatCalc.processReplenish(unit, units);
        };
    }

    private List<BattleUnit> buildTurnOrder(List<BattleUnit> sorted) {
        List<BattleUnit> team1 = new ArrayList<>();
        List<BattleUnit> team2 = new ArrayList<>();

        for (BattleUnit u : sorted) {
            if (u.getTeam() == 1) team1.add(u);
            else team2.add(u);
        }

        // interleave: highest from each team alternating
        List<BattleUnit> order = new ArrayList<>();
        int i = 0, j = 0;

        // first unit is the one with highest level/attack overall
        boolean team1GoesFirst = true;
        if (!team1.isEmpty() && !team2.isEmpty()) {
            BattleUnit first1 = team1.get(0);
            BattleUnit first2 = team2.get(0);
            if (first2.getLevel() > first1.getLevel() ||
                (first2.getLevel() == first1.getLevel() && first2.getAttack() > first1.getAttack())) {
                team1GoesFirst = false;
            }
        }

        while (i < team1.size() || j < team2.size()) {
            if (team1GoesFirst) {
                if (i < team1.size()) order.add(team1.get(i++));
                if (j < team2.size()) order.add(team2.get(j++));
            } else {
                if (j < team2.size()) order.add(team2.get(j++));
                if (i < team1.size()) order.add(team1.get(i++));
            }
        }

        return order;
    }

    private void advanceTurn(Battle battle, List<BattleUnit> units) {
        int idx = battle.getCurrentUnitIndex();
        int total = units.size();

        // find next alive unit
        for (int attempt = 0; attempt < total; attempt++) {
            idx = (idx + 1) % total;
            if (idx == 0) {
                battle.setTurnNumber(battle.getTurnNumber() + 1);
            }
            if (units.get(idx).isAlive()) {
                battle.setCurrentUnitIndex(idx);
                return;
            }
        }
    }

    private BattleUnit findCurrentUnit(List<BattleUnit> units, int index) {
        if (index >= 0 && index < units.size()) {
            return units.get(index);
        }
        throw new RuntimeException("Invalid unit index");
    }

    private BattleUnit getAliveUnitByIndex(List<BattleUnit> units, int index) {
        if (index >= 0 && index < units.size() && units.get(index).isAlive()) {
            return units.get(index);
        }
        return null;
    }

    private boolean isHereticShielded(BattleUnit attacker, BattleUnit target, List<BattleUnit> units) {
        // check if any ally of the target has HERETIC hybrid and cast fire shield
        for (BattleUnit u : units) {
            if (u.getTeam() == target.getTeam() && "HERETIC".equals(u.getHybridClass())) {
                return target.getShield() > 0;
            }
        }
        return false;
    }

    private BattleStateResponse buildResponse(Battle battle, List<BattleUnit> units, String lastAction) {
        BattleStateResponse response = new BattleStateResponse();
        response.setBattleId(battle.getId());
        response.setStatus(battle.getStatus().name());
        response.setTurnNumber(battle.getTurnNumber());

        List<BattleUnit> t1 = new ArrayList<>();
        List<BattleUnit> t2 = new ArrayList<>();
        for (BattleUnit u : units) {
            if (u.getTeam() == 1) t1.add(u);
            else t2.add(u);
        }
        response.setTeam1(t1);
        response.setTeam2(t2);
        response.setCurrentUnitIndex(battle.getCurrentUnitIndex());

        if (battle.getStatus() == BattleStatus.IN_PROGRESS && battle.getCurrentUnitIndex() < units.size()) {
            BattleUnit current = units.get(battle.getCurrentUnitIndex());
            response.setCurrentUnit(current);
            response.setAvailableActions(current.getAvailableActions());
        }

        response.setLastActionResult(lastAction);
        return response;
    }

    private String toJson(List<BattleUnit> units) {
        try {
            return objectMapper.writeValueAsString(units);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize battle state", e);
        }
    }

    private List<BattleUnit> fromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<BattleUnit>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize battle state", e);
        }
    }
}
