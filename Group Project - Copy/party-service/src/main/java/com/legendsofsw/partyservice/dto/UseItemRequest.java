package com.legendsofsw.partyservice.dto;

public class UseItemRequest {

    private String itemType;
    private Long heroId;

    public UseItemRequest() {
    }

    public UseItemRequest(String itemType, Long heroId) {
        this.itemType = itemType;
        this.heroId = heroId;
    }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public Long getHeroId() { return heroId; }
    public void setHeroId(Long heroId) { this.heroId = heroId; }
}
