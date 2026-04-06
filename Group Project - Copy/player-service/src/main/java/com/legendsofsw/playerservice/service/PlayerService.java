package com.legendsofsw.playerservice.service;

import com.legendsofsw.playerservice.dto.LoginRequest;
import com.legendsofsw.playerservice.dto.LoginResponse;
import com.legendsofsw.playerservice.dto.RegisterRequest;
import com.legendsofsw.playerservice.model.Player;
import com.legendsofsw.playerservice.model.PvpRecord;
import com.legendsofsw.playerservice.model.SavedParty;
import com.legendsofsw.playerservice.model.ScoreEntry;
import com.legendsofsw.playerservice.repository.PlayerRepository;
import com.legendsofsw.playerservice.repository.PvpRecordRepository;
import com.legendsofsw.playerservice.repository.SavedPartyRepository;
import com.legendsofsw.playerservice.repository.ScoreEntryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerService {

    private final PlayerRepository playerRepo;
    private final SavedPartyRepository savedPartyRepo;
    private final ScoreEntryRepository scoreRepo;
    private final PvpRecordRepository pvpRecordRepo;

    public PlayerService(PlayerRepository playerRepo, SavedPartyRepository savedPartyRepo,
                         ScoreEntryRepository scoreRepo, PvpRecordRepository pvpRecordRepo) {
        this.playerRepo = playerRepo;
        this.savedPartyRepo = savedPartyRepo;
        this.scoreRepo = scoreRepo;
        this.pvpRecordRepo = pvpRecordRepo;
    }

    public Player register(RegisterRequest request) {
        if (playerRepo.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        Player player = new Player(request.getUsername(), request.getPassword());
        player = playerRepo.save(player);

        pvpRecordRepo.save(new PvpRecord(player.getId()));
        return player;
    }

    public LoginResponse login(LoginRequest request) {
        Player player = playerRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!player.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        return new LoginResponse(player.getId(), player.getUsername(), "Login successful");
    }

    public Player getPlayer(Long id) {
        return playerRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found"));
    }

    public Player getPlayerByUsername(String username) {
        return playerRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Player not found"));
    }

    // saved party methods

    public List<SavedParty> getSavedParties(Long playerId) {
        return savedPartyRepo.findByPlayerId(playerId);
    }

    public SavedParty saveParty(Long playerId, String name, String partyDataJson) {
        int count = savedPartyRepo.countByPlayerId(playerId);
        if (count >= 5) {
            throw new RuntimeException("Max 5 saved parties. Delete one first.");
        }
        SavedParty party = new SavedParty(playerId, name, partyDataJson);
        return savedPartyRepo.save(party);
    }

    public void deleteSavedParty(Long playerId, Long partyId) {
        SavedParty party = savedPartyRepo.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Saved party not found"));
        if (!party.getPlayerId().equals(playerId)) {
            throw new RuntimeException("Party does not belong to this player");
        }
        savedPartyRepo.delete(party);
    }

    public SavedParty replaceSavedParty(Long playerId, Long oldPartyId, String name, String partyDataJson) {
        deleteSavedParty(playerId, oldPartyId);
        SavedParty newParty = new SavedParty(playerId, name, partyDataJson);
        return savedPartyRepo.save(newParty);
    }

    // score and leaderboard methods

    public ScoreEntry addScore(Long playerId, int score) {
        Player player = getPlayer(playerId);
        ScoreEntry entry = new ScoreEntry(playerId, player.getUsername(), score);
        return scoreRepo.save(entry);
    }

    public List<ScoreEntry> getLeaderboard() {
        return scoreRepo.findTop10ByOrderByScoreDesc();
    }

    public List<ScoreEntry> getPlayerScores(Long playerId) {
        return scoreRepo.findByPlayerIdOrderByScoreDesc(playerId);
    }

    // pvp record methods

    public PvpRecord getPvpRecord(Long playerId) {
        return pvpRecordRepo.findByPlayerId(playerId)
                .orElseGet(() -> {
                    PvpRecord record = new PvpRecord(playerId);
                    return pvpRecordRepo.save(record);
                });
    }

    public PvpRecord recordWin(Long playerId) {
        PvpRecord record = getPvpRecord(playerId);
        record.setWins(record.getWins() + 1);
        return pvpRecordRepo.save(record);
    }

    public PvpRecord recordLoss(Long playerId) {
        PvpRecord record = getPvpRecord(playerId);
        record.setLosses(record.getLosses() + 1);
        return pvpRecordRepo.save(record);
    }

    public List<PvpRecord> getLeagueStandings() {
        return pvpRecordRepo.findAllByOrderByWinsDesc();
    }
}
