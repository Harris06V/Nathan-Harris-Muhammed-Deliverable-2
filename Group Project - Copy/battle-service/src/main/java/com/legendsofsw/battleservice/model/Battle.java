package com.legendsofsw.battleservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "battles")
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String stateJson;

    @Enumerated(EnumType.STRING)
    private BattleStatus status;

    private int currentUnitIndex;
    private int turnNumber;
    private boolean isPvp;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String lastActionResult;

    public Battle() {
        this.status = BattleStatus.IN_PROGRESS;
        this.currentUnitIndex = 0;
        this.turnNumber = 1;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStateJson() { return stateJson; }
    public void setStateJson(String stateJson) { this.stateJson = stateJson; }

    public BattleStatus getStatus() { return status; }
    public void setStatus(BattleStatus status) { this.status = status; }

    public int getCurrentUnitIndex() { return currentUnitIndex; }
    public void setCurrentUnitIndex(int currentUnitIndex) { this.currentUnitIndex = currentUnitIndex; }

    public int getTurnNumber() { return turnNumber; }
    public void setTurnNumber(int turnNumber) { this.turnNumber = turnNumber; }

    public boolean isPvp() { return isPvp; }
    public void setPvp(boolean pvp) { isPvp = pvp; }

    public String getLastActionResult() { return lastActionResult; }
    public void setLastActionResult(String lastActionResult) { this.lastActionResult = lastActionResult; }
}
