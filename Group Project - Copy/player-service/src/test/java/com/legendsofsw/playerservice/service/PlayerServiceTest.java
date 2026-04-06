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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepo;

    @Mock
    private SavedPartyRepository savedPartyRepo;

    @Mock
    private ScoreEntryRepository scoreRepo;

    @Mock
    private PvpRecordRepository pvpRecordRepo;

    @InjectMocks
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerNewPlayer() {
        RegisterRequest request = new RegisterRequest("testUser", "pass123");
        Player savedPlayer = new Player("testUser", "pass123");
        savedPlayer.setId(1L);

        when(playerRepo.existsByUsername("testUser")).thenReturn(false);
        when(playerRepo.save(any(Player.class))).thenReturn(savedPlayer);
        when(pvpRecordRepo.save(any(PvpRecord.class))).thenReturn(new PvpRecord(1L));

        Player result = playerService.register(request);

        assertEquals("testUser", result.getUsername());
        verify(playerRepo).save(any(Player.class));
        verify(pvpRecordRepo).save(any(PvpRecord.class));
    }

    @Test
    void registerDuplicateUsernameThrows() {
        RegisterRequest request = new RegisterRequest("taken", "pass");
        when(playerRepo.existsByUsername("taken")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> playerService.register(request));
    }

    @Test
    void loginSuccess() {
        Player player = new Player("user1", "password");
        player.setId(1L);
        when(playerRepo.findByUsername("user1")).thenReturn(Optional.of(player));

        LoginResponse response = playerService.login(new LoginRequest("user1", "password"));

        assertEquals(1L, response.getPlayerId());
        assertEquals("user1", response.getUsername());
    }

    @Test
    void loginWrongPasswordThrows() {
        Player player = new Player("user1", "password");
        player.setId(1L);
        when(playerRepo.findByUsername("user1")).thenReturn(Optional.of(player));

        assertThrows(RuntimeException.class,
                () -> playerService.login(new LoginRequest("user1", "wrong")));
    }

    @Test
    void loginNonExistentUserThrows() {
        when(playerRepo.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> playerService.login(new LoginRequest("ghost", "pass")));
    }

    @Test
    void savePartySuccess() {
        when(savedPartyRepo.countByPlayerId(1L)).thenReturn(3);
        SavedParty party = new SavedParty(1L, "My Team", "{}");
        party.setId(1L);
        when(savedPartyRepo.save(any(SavedParty.class))).thenReturn(party);

        SavedParty result = playerService.saveParty(1L, "My Team", "{}");

        assertEquals("My Team", result.getName());
    }

    @Test
    void savePartyMaxLimitThrows() {
        when(savedPartyRepo.countByPlayerId(1L)).thenReturn(5);

        assertThrows(RuntimeException.class,
                () -> playerService.saveParty(1L, "Extra", "{}"));
    }

    @Test
    void deleteSavedPartyWrongOwnerThrows() {
        SavedParty party = new SavedParty(2L, "Other", "{}");
        party.setId(1L);
        when(savedPartyRepo.findById(1L)).thenReturn(Optional.of(party));

        assertThrows(RuntimeException.class,
                () -> playerService.deleteSavedParty(1L, 1L));
    }

    @Test
    void getLeaderboard() {
        List<ScoreEntry> entries = List.of(
                new ScoreEntry(1L, "player1", 5000),
                new ScoreEntry(2L, "player2", 3000)
        );
        when(scoreRepo.findTop10ByOrderByScoreDesc()).thenReturn(entries);

        List<ScoreEntry> result = playerService.getLeaderboard();

        assertEquals(2, result.size());
        assertEquals(5000, result.get(0).getScore());
    }

    @Test
    void recordWinAndLoss() {
        PvpRecord record = new PvpRecord(1L);
        record.setId(1L);
        when(pvpRecordRepo.findByPlayerId(1L)).thenReturn(Optional.of(record));
        when(pvpRecordRepo.save(any(PvpRecord.class))).thenAnswer(i -> i.getArgument(0));

        PvpRecord winResult = playerService.recordWin(1L);
        assertEquals(1, winResult.getWins());

        PvpRecord lossResult = playerService.recordLoss(1L);
        assertEquals(1, lossResult.getLosses());
    }
}
