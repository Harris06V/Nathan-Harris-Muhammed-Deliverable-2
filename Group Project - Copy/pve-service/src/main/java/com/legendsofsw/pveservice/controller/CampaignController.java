package com.legendsofsw.pveservice.controller;

import com.legendsofsw.pveservice.dto.RoomResult;
import com.legendsofsw.pveservice.dto.StartCampaignRequest;
import com.legendsofsw.pveservice.model.Campaign;
import com.legendsofsw.pveservice.service.CampaignService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping
    public ResponseEntity<Campaign> startCampaign(@RequestBody StartCampaignRequest request) {
        Campaign campaign = campaignService.startCampaign(request.getPlayerId(), request.getPartyId());
        return ResponseEntity.ok(campaign);
    }

    @GetMapping("/{campaignId}")
    public ResponseEntity<?> getCampaign(@PathVariable Long campaignId) {
        try {
            return ResponseEntity.ok(campaignService.getCampaign(campaignId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<Campaign>> getPlayerCampaigns(@PathVariable Long playerId) {
        return ResponseEntity.ok(campaignService.getPlayerCampaigns(playerId));
    }

    @PostMapping("/{campaignId}/next-room")
    public ResponseEntity<?> nextRoom(@PathVariable Long campaignId) {
        try {
            RoomResult result = campaignService.advanceToNextRoom(campaignId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{campaignId}/complete-battle")
    public ResponseEntity<?> completeBattle(@PathVariable Long campaignId,
                                             @RequestBody Map<String, Boolean> body) {
        try {
            boolean playerWon = body.getOrDefault("playerWon", false);
            Map<String, Object> result = campaignService.completeBattle(campaignId, playerWon);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{campaignId}/save")
    public ResponseEntity<?> saveCampaign(@PathVariable Long campaignId) {
        try {
            Campaign campaign = campaignService.saveCampaign(campaignId);
            return ResponseEntity.ok(campaign);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{campaignId}/resume")
    public ResponseEntity<?> resumeCampaign(@PathVariable Long campaignId) {
        try {
            Campaign campaign = campaignService.resumeCampaign(campaignId);
            return ResponseEntity.ok(campaign);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{campaignId}/item-purchase")
    public ResponseEntity<?> recordItemPurchase(@PathVariable Long campaignId,
                                                 @RequestBody Map<String, Integer> body) {
        try {
            int cost = body.getOrDefault("cost", 0);
            Campaign campaign = campaignService.recordItemPurchase(campaignId, cost);
            return ResponseEntity.ok(campaign);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{campaignId}/complete")
    public ResponseEntity<?> completeCampaign(@PathVariable Long campaignId) {
        try {
            Map<String, Object> result = campaignService.completeCampaign(campaignId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
