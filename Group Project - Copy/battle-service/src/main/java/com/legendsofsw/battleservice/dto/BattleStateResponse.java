package com.legendsofsw.battleservice.dto;

import com.legendsofsw.battleservice.model.ActionType;
import com.legendsofsw.battleservice.model.BattleUnit;
import java.util.List;

public class BattleStateResponse {

    private Long battleId;
    private String status;
    private int turnNumber;
    private List<BattleUnit> team1;
    private List<BattleUnit> team2;
    private int currentUnitIndex;
    private BattleUnit currentUnit;
    private List<ActionType> availableActions;
    private String lastActionResult;

    public BattleStateResponse() {
    }

    public Long getBattleId() { return battleId; }
    public void setBattleId(Long battleId) { this.battleId = battleId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }

    public List<BattleUnit> getTeam1() { return team1; }
    public void setTeam1(List<BattleUnit> team1) { this.team1 = team1; }

    public List<BattleUnit> getTeam2() { return team2; }
    public void setTeam2(List<BattleUnit> team2) { this.team2 = team2; }

    public int getCurrentUnitIndex() { return currentUnitIndex; }
    public void setCurrentUnitIndex(int currentUnitIndex) { this.currentUnitIndex = currentUnitIndex; }

    public BattleUnit getCurrentUnit() { return currentUnit; }
    public void setCurrentUnit(BattleUnit currentUnit) { this.currentUnit = currentUnit; }

    public List<ActionType> getAvailableActions() { return availableActions; }
    public void setAvailableActions(List<ActionType> availableActions) { this.availableActions = availableActions; }

    public String getLastActionResult() { return lastActionResult; }
    public void setLastActionResult(String lastActionResult) { this.lastActionResult = lastActionResult; }
}
