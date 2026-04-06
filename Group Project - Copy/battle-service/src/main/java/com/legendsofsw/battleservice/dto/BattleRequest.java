package com.legendsofsw.battleservice.dto;

import com.legendsofsw.battleservice.model.BattleUnit;
import java.util.List;

public class BattleRequest {

    private List<BattleUnit> team1;
    private List<BattleUnit> team2;
    private boolean pvp;

    public BattleRequest() {
    }

    public List<BattleUnit> getTeam1() { return team1; }
    public void setTeam1(List<BattleUnit> team1) { this.team1 = team1; }

    public List<BattleUnit> getTeam2() { return team2; }
    public void setTeam2(List<BattleUnit> team2) { this.team2 = team2; }

    public boolean isPvp() { return pvp; }
    public void setPvp(boolean pvp) { this.pvp = pvp; }
}
