package com.legendsofsw.pvpservice.controller;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.legendsofsw.pvpservice.dto.InviteRequest;
import com.legendsofsw.pvpservice.model.PvpInvitation;
import com.legendsofsw.pvpservice.model.PvpMatch;
import com.legendsofsw.pvpservice.service.PvpService;

class PvpControllerTest {

    @Test
    void sendInvitationReturnsOk() {
        PvpInvitation invite = new PvpInvitation();
        PvpService stub = new PvpService(null, null) {
            @Override public PvpInvitation sendInvitation(InviteRequest r) { return invite; }
        };
        PvpController ctrl = new PvpController(stub);
        ResponseEntity<?> resp = ctrl.sendInvitation(new InviteRequest());
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void sendInvitationToSelfReturnsBadRequest() {
        PvpService stub = new PvpService(null, null) {
            @Override public PvpInvitation sendInvitation(InviteRequest r) {
                throw new RuntimeException("Cannot invite yourself");
            }
        };
        PvpController ctrl = new PvpController(stub);
        ResponseEntity<?> resp = ctrl.sendInvitation(new InviteRequest());
        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    void acceptInvitationReturnsOk() {
        PvpInvitation invite = new PvpInvitation();
        PvpService stub = new PvpService(null, null) {
            @Override public PvpInvitation acceptInvitation(Long id) { return invite; }
        };
        PvpController ctrl = new PvpController(stub);
        ResponseEntity<?> resp = ctrl.acceptInvitation(1L);
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void declineInvitationReturnsOk() {
        PvpInvitation invite = new PvpInvitation();
        PvpService stub = new PvpService(null, null) {
            @Override public PvpInvitation declineInvitation(Long id) { return invite; }
        };
        PvpController ctrl = new PvpController(stub);
        ResponseEntity<?> resp = ctrl.declineInvitation(1L);
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void getPendingInvitationsReturnsOk() {
        PvpService stub = new PvpService(null, null) {
            @Override public List<PvpInvitation> getPendingInvitations(Long playerId) { return List.of(); }
        };
        PvpController ctrl = new PvpController(stub);
        ResponseEntity<List<PvpInvitation>> resp = ctrl.getPendingInvitations(1L);
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void getPlayerMatchesReturnsOk() {
        PvpService stub = new PvpService(null, null) {
            @Override public List<PvpMatch> getPlayerMatches(Long playerId) { return List.of(); }
        };
        PvpController ctrl = new PvpController(stub);
        ResponseEntity<List<PvpMatch>> resp = ctrl.getPlayerMatches(1L);
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void getMatchNotFoundReturns404() {
        PvpService stub = new PvpService(null, null) {
            @Override public PvpMatch getMatch(Long id) { throw new RuntimeException("Not found"); }
        };
        PvpController ctrl = new PvpController(stub);
        ResponseEntity<?> resp = ctrl.getMatch(99L);
        assertEquals(404, resp.getStatusCode().value());
    }

    @Test
    void completeMatchReturnsOk() {
        PvpMatch match = new PvpMatch();
        PvpService stub = new PvpService(null, null) {
            @Override public PvpMatch completeMatch(Long matchId, Long winnerId) { return match; }
        };
        PvpController ctrl = new PvpController(stub);
        ResponseEntity<?> resp = ctrl.completeMatch(1L, Map.of("winnerId", 2L));
        assertEquals(200, resp.getStatusCode().value());
    }
}
