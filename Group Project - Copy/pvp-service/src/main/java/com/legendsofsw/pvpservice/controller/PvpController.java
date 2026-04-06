package com.legendsofsw.pvpservice.controller;

import com.legendsofsw.pvpservice.dto.InviteRequest;
import com.legendsofsw.pvpservice.model.PvpInvitation;
import com.legendsofsw.pvpservice.model.PvpMatch;
import com.legendsofsw.pvpservice.service.PvpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pvp")
public class PvpController {

    private final PvpService pvpService;

    public PvpController(PvpService pvpService) {
        this.pvpService = pvpService;
    }

    @PostMapping("/invite")
    public ResponseEntity<?> sendInvitation(@RequestBody InviteRequest request) {
        try {
            PvpInvitation invitation = pvpService.sendInvitation(request);
            return ResponseEntity.ok(invitation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/invite/{invitationId}/accept")
    public ResponseEntity<?> acceptInvitation(@PathVariable Long invitationId) {
        try {
            PvpInvitation invitation = pvpService.acceptInvitation(invitationId);
            return ResponseEntity.ok(invitation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/invite/{invitationId}/decline")
    public ResponseEntity<?> declineInvitation(@PathVariable Long invitationId) {
        try {
            PvpInvitation invitation = pvpService.declineInvitation(invitationId);
            return ResponseEntity.ok(invitation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/invites/received/{playerId}")
    public ResponseEntity<List<PvpInvitation>> getPendingInvitations(@PathVariable Long playerId) {
        return ResponseEntity.ok(pvpService.getPendingInvitations(playerId));
    }

    @GetMapping("/invites/sent/{playerId}")
    public ResponseEntity<List<PvpInvitation>> getSentInvitations(@PathVariable Long playerId) {
        return ResponseEntity.ok(pvpService.getSentInvitations(playerId));
    }

    @GetMapping("/matches/{playerId}")
    public ResponseEntity<List<PvpMatch>> getPlayerMatches(@PathVariable Long playerId) {
        return ResponseEntity.ok(pvpService.getPlayerMatches(playerId));
    }

    @GetMapping("/match/{matchId}")
    public ResponseEntity<?> getMatch(@PathVariable Long matchId) {
        try {
            return ResponseEntity.ok(pvpService.getMatch(matchId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/match/{matchId}/complete")
    public ResponseEntity<?> completeMatch(@PathVariable Long matchId, @RequestBody Map<String, Long> body) {
        try {
            PvpMatch match = pvpService.completeMatch(matchId, body.get("winnerId"));
            return ResponseEntity.ok(match);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
