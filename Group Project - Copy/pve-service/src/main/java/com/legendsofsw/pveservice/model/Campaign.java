package com.legendsofsw.pveservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long playerId;

    @Column(nullable = false)
    private Long partyId;

    private int currentRoom;

    private int totalRooms;

    @Enumerated(EnumType.STRING)
    private CampaignStatus status;

    @Enumerated(EnumType.STRING)
    private RoomType lastRoomType;

    private Long currentBattleId;

    private int pendingExp;
    private int pendingGold;
    private int totalItemSpending;

    public Campaign() {
        this.totalRooms = 30;
        this.currentRoom = 0;
        this.status = CampaignStatus.ACTIVE;
        this.pendingExp = 0;
        this.pendingGold = 0;
        this.totalItemSpending = 0;
    }

    public Campaign(Long playerId, Long partyId) {
        this();
        this.playerId = playerId;
        this.partyId = partyId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public Long getPartyId() { return partyId; }
    public void setPartyId(Long partyId) { this.partyId = partyId; }

    public int getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(int currentRoom) { this.currentRoom = currentRoom; }

    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }

    public CampaignStatus getStatus() { return status; }
    public void setStatus(CampaignStatus status) { this.status = status; }

    public RoomType getLastRoomType() { return lastRoomType; }
    public void setLastRoomType(RoomType lastRoomType) { this.lastRoomType = lastRoomType; }

    public Long getCurrentBattleId() { return currentBattleId; }
    public void setCurrentBattleId(Long currentBattleId) { this.currentBattleId = currentBattleId; }

    public int getPendingExp() { return pendingExp; }
    public void setPendingExp(int pendingExp) { this.pendingExp = pendingExp; }

    public int getPendingGold() { return pendingGold; }
    public void setPendingGold(int pendingGold) { this.pendingGold = pendingGold; }

    public int getTotalItemSpending() { return totalItemSpending; }
    public void setTotalItemSpending(int totalItemSpending) { this.totalItemSpending = totalItemSpending; }
}
