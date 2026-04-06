package com.legendsofsw.pveservice.controller;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.legendsofsw.pveservice.dto.RoomResult;
import com.legendsofsw.pveservice.dto.StartCampaignRequest;
import com.legendsofsw.pveservice.model.Campaign;
import com.legendsofsw.pveservice.model.RoomType;
import com.legendsofsw.pveservice.service.CampaignService;

class CampaignControllerTest {

    @Test
    void startCampaignReturnsOk() {
        Campaign campaign = new Campaign(1L, 1L);
        CampaignService stub = new CampaignService(null, null) {
            @Override public Campaign startCampaign(Long playerId, Long partyId) { return campaign; }
        };
        CampaignController ctrl = new CampaignController(stub);
        StartCampaignRequest req = new StartCampaignRequest();
        req.setPlayerId(1L);
        req.setPartyId(1L);
        ResponseEntity<Campaign> resp = ctrl.startCampaign(req);
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void getCampaignReturnsOk() {
        Campaign campaign = new Campaign(1L, 1L);
        CampaignService stub = new CampaignService(null, null) {
            @Override public Campaign getCampaign(Long id) { return campaign; }
        };
        CampaignController ctrl = new CampaignController(stub);
        ResponseEntity<?> resp = ctrl.getCampaign(1L);
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void getCampaignNotFoundReturns404() {
        CampaignService stub = new CampaignService(null, null) {
            @Override public Campaign getCampaign(Long id) { throw new RuntimeException("Not found"); }
        };
        CampaignController ctrl = new CampaignController(stub);
        ResponseEntity<?> resp = ctrl.getCampaign(99L);
        assertEquals(404, resp.getStatusCode().value());
    }

    @Test
    void getPlayerCampaignsReturnsOk() {
        CampaignService stub = new CampaignService(null, null) {
            @Override public List<Campaign> getPlayerCampaigns(Long playerId) { return List.of(); }
        };
        CampaignController ctrl = new CampaignController(stub);
        ResponseEntity<List<Campaign>> resp = ctrl.getPlayerCampaigns(1L);
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void nextRoomReturnsOk() {
        RoomResult result = new RoomResult();
        result.setRoomType(RoomType.INN);
        result.setMessage("You found an inn!");
        CampaignService stub = new CampaignService(null, null) {
            @Override public RoomResult advanceToNextRoom(Long id) { return result; }
        };
        CampaignController ctrl = new CampaignController(stub);
        ResponseEntity<?> resp = ctrl.nextRoom(1L);
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void completeBattleReturnsOk() {
        Map<String, Object> battleResult = Map.of("playerWon", true, "message", "Victory!");
        CampaignService stub = new CampaignService(null, null) {
            @Override public Map<String, Object> completeBattle(Long id, boolean won) { return battleResult; }
        };
        CampaignController ctrl = new CampaignController(stub);
        ResponseEntity<?> resp = ctrl.completeBattle(1L, Map.of("playerWon", true));
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void saveCampaignReturnsOk() {
        Campaign campaign = new Campaign(1L, 1L);
        CampaignService stub = new CampaignService(null, null) {
            @Override public Campaign saveCampaign(Long id) { return campaign; }
        };
        CampaignController ctrl = new CampaignController(stub);
        ResponseEntity<?> resp = ctrl.saveCampaign(1L);
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void resumeCampaignReturnsOk() {
        Campaign campaign = new Campaign(1L, 1L);
        CampaignService stub = new CampaignService(null, null) {
            @Override public Campaign resumeCampaign(Long id) { return campaign; }
        };
        CampaignController ctrl = new CampaignController(stub);
        ResponseEntity<?> resp = ctrl.resumeCampaign(1L);
        assertEquals(200, resp.getStatusCode().value());
    }
}
