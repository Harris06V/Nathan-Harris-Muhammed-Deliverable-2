package com.legendsofsw.pveservice.dto;

public class StartCampaignRequest {

    private Long playerId;
    private Long partyId;

    public StartCampaignRequest() {
    }

    public StartCampaignRequest(Long playerId, Long partyId) {
        this.playerId = playerId;
        this.partyId = partyId;
    }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public Long getPartyId() { return partyId; }
    public void setPartyId(Long partyId) { this.partyId = partyId; }
}
