package com.legendsofsw.playerservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.legendsofsw.playerservice.dto.LoginRequest;
import com.legendsofsw.playerservice.dto.LoginResponse;
import com.legendsofsw.playerservice.dto.RegisterRequest;
import com.legendsofsw.playerservice.model.Player;
import com.legendsofsw.playerservice.model.PvpRecord;
import com.legendsofsw.playerservice.model.SavedParty;
import com.legendsofsw.playerservice.model.ScoreEntry;
import com.legendsofsw.playerservice.service.PlayerService;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            Player player = playerService.register(request);
            return ResponseEntity.ok(Map.of(
                    "id", player.getId(),
                    "username", player.getUsername(),
                    "message", "Registration successful"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = playerService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPlayer(@PathVariable Long id) {
        try {
            Player player = playerService.getPlayer(id);
            return ResponseEntity.ok(Map.of(
                    "id", player.getId(),
                    "username", player.getUsername()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<?> getPlayerByUsername(@PathVariable String username) {
        try {
            Player player = playerService.getPlayerByUsername(username);
            return ResponseEntity.ok(Map.of(
                    "id", player.getId(),
                    "username", player.getUsername()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // saved party 

    @GetMapping("/{playerId}/parties")
    public ResponseEntity<List<SavedParty>> getSavedParties(@PathVariable Long playerId) {
        return ResponseEntity.ok(playerService.getSavedParties(playerId));
    }

    @PostMapping("/{playerId}/parties")
    public ResponseEntity<?> saveParty(@PathVariable Long playerId, @RequestBody SavedParty party) {
        try {
            SavedParty saved = playerService.saveParty(playerId, party.getName(), party.getPartyDataJson());
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{playerId}/parties/{partyId}")
    public ResponseEntity<?> deleteSavedParty(@PathVariable Long playerId, @PathVariable Long partyId) {
        try {
            playerService.deleteSavedParty(playerId, partyId);
            return ResponseEntity.ok(Map.of("message", "Party deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{playerId}/parties/{oldPartyId}/replace")
    public ResponseEntity<?> replaceSavedParty(@PathVariable Long playerId,
                                                @PathVariable Long oldPartyId,
                                                @RequestBody SavedParty party) {
        try {
            SavedParty saved = playerService.replaceSavedParty(playerId, oldPartyId,
                    party.getName(), party.getPartyDataJson());
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // score and leaderboard 

    @PostMapping("/{playerId}/scores")
    public ResponseEntity<ScoreEntry> addScore(@PathVariable Long playerId, @RequestBody Map<String, Integer> body) {
        ScoreEntry entry = playerService.addScore(playerId, body.get("score"));
        return ResponseEntity.ok(entry);
    }

    @GetMapping("/{playerId}/scores")
    public ResponseEntity<List<ScoreEntry>> getPlayerScores(@PathVariable Long playerId) {
        return ResponseEntity.ok(playerService.getPlayerScores(playerId));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<ScoreEntry>> getLeaderboard() {
        return ResponseEntity.ok(playerService.getLeaderboard());
    }

    // pvp record 

    @GetMapping("/{playerId}/pvp-record")
    public ResponseEntity<PvpRecord> getPvpRecord(@PathVariable Long playerId) {
        return ResponseEntity.ok(playerService.getPvpRecord(playerId));
    }

    @PostMapping("/{playerId}/pvp-record/win")
    public ResponseEntity<PvpRecord> recordWin(@PathVariable Long playerId) {
        return ResponseEntity.ok(playerService.recordWin(playerId));
    }

    @PostMapping("/{playerId}/pvp-record/loss")
    public ResponseEntity<PvpRecord> recordLoss(@PathVariable Long playerId) {
        return ResponseEntity.ok(playerService.recordLoss(playerId));
    }

    @GetMapping("/league")
    public ResponseEntity<List<PvpRecord>> getLeagueStandings() {
        return ResponseEntity.ok(playerService.getLeagueStandings());
    }
}
