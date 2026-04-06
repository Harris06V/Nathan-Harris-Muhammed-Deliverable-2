package com.legendsofsw.playerservice.controller;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.legendsofsw.playerservice.dto.LoginRequest;
import com.legendsofsw.playerservice.dto.LoginResponse;
import com.legendsofsw.playerservice.dto.RegisterRequest;
import com.legendsofsw.playerservice.model.Player;
import com.legendsofsw.playerservice.model.PvpRecord;
import com.legendsofsw.playerservice.model.ScoreEntry;
import com.legendsofsw.playerservice.service.PlayerService;

class PlayerControllerTest {

    @Test
    void registerReturnsOk() {
        Player player = new Player();
        player.setId(1L);
        player.setUsername("testUser");
        PlayerService stub = new PlayerService(null, null, null, null) {
            @Override public Player register(RegisterRequest r) { return player; }
        };
        PlayerController ctrl = new PlayerController(stub);
        ResponseEntity<?> resp = ctrl.register(new RegisterRequest());
        assertEquals(200, resp.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) resp.getBody();
        assertEquals("testUser", body.get("username"));
        assertEquals("Registration successful", body.get("message"));
    }

    @Test
    void registerDuplicateReturnsBadRequest() {
        PlayerService stub = new PlayerService(null, null, null, null) {
            @Override public Player register(RegisterRequest r) { throw new RuntimeException("Username taken"); }
        };
        PlayerController ctrl = new PlayerController(stub);
        ResponseEntity<?> resp = ctrl.register(new RegisterRequest());
        assertEquals(400, resp.getStatusCode().value());
    }

    @Test
    void loginReturnsOk() {
        LoginResponse loginResp = new LoginResponse(1L, "testUser", "Login successful");
        PlayerService stub = new PlayerService(null, null, null, null) {
            @Override public LoginResponse login(LoginRequest r) { return loginResp; }
        };
        PlayerController ctrl = new PlayerController(stub);
        ResponseEntity<?> resp = ctrl.login(new LoginRequest());
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void getPlayerReturnsOk() {
        Player player = new Player();
        player.setId(1L);
        player.setUsername("hero");
        PlayerService stub = new PlayerService(null, null, null, null) {
            @Override public Player getPlayer(Long id) { return player; }
        };
        PlayerController ctrl = new PlayerController(stub);
        ResponseEntity<?> resp = ctrl.getPlayer(1L);
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void getPlayerNotFoundReturns404() {
        PlayerService stub = new PlayerService(null, null, null, null) {
            @Override public Player getPlayer(Long id) { throw new RuntimeException("Not found"); }
        };
        PlayerController ctrl = new PlayerController(stub);
        ResponseEntity<?> resp = ctrl.getPlayer(99L);
        assertEquals(404, resp.getStatusCode().value());
    }

    @Test
    void getLeaderboardReturnsOk() {
        PlayerService stub = new PlayerService(null, null, null, null) {
            @Override public List<ScoreEntry> getLeaderboard() { return List.of(); }
        };
        PlayerController ctrl = new PlayerController(stub);
        ResponseEntity<List<ScoreEntry>> resp = ctrl.getLeaderboard();
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void getPvpRecordReturnsOk() {
        PvpRecord record = new PvpRecord();
        PlayerService stub = new PlayerService(null, null, null, null) {
            @Override public PvpRecord getPvpRecord(Long playerId) { return record; }
        };
        PlayerController ctrl = new PlayerController(stub);
        ResponseEntity<PvpRecord> resp = ctrl.getPvpRecord(1L);
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void addScoreReturnsOk() {
        ScoreEntry entry = new ScoreEntry();
        PlayerService stub = new PlayerService(null, null, null, null) {
            @Override public ScoreEntry addScore(Long playerId, int score) { return entry; }
        };
        PlayerController ctrl = new PlayerController(stub);
        ResponseEntity<ScoreEntry> resp = ctrl.addScore(1L, Map.of("score", 500));
        assertEquals(200, resp.getStatusCode().value());
    }
}
