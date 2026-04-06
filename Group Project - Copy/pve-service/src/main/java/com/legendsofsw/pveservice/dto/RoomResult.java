package com.legendsofsw.pveservice.dto;

import com.legendsofsw.pveservice.model.RoomType;
import java.util.List;
import java.util.Map;

public class RoomResult {

    private int roomNumber;
    private RoomType roomType;
    private Long battleId;
    private List<Map<String, Object>> availableHeroes;
    private List<Map<String, Object>> shopItems;
    private String message;

    public RoomResult() {
    }

    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public Long getBattleId() { return battleId; }
    public void setBattleId(Long battleId) { this.battleId = battleId; }

    public List<Map<String, Object>> getAvailableHeroes() { return availableHeroes; }
    public void setAvailableHeroes(List<Map<String, Object>> availableHeroes) { this.availableHeroes = availableHeroes; }

    public List<Map<String, Object>> getShopItems() { return shopItems; }
    public void setShopItems(List<Map<String, Object>> shopItems) { this.shopItems = shopItems; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
