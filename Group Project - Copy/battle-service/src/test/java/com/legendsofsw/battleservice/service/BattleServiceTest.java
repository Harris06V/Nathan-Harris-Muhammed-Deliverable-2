package com.legendsofsw.battleservice.service;

import com.legendsofsw.battleservice.dto.ActionRequest;
import com.legendsofsw.battleservice.dto.BattleRequest;
import com.legendsofsw.battleservice.dto.BattleStateResponse;
import com.legendsofsw.battleservice.model.Battle;
import com.legendsofsw.battleservice.model.BattleUnit;
import com.legendsofsw.battleservice.repository.BattleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BattleServiceTest {

    @Mock
    private BattleRepository battleRepo;

    private BattleService battleService;

    private final Battle[] lastSaved = {null};

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        CombatCalculator calculator = new CombatCalculator();
        battleService = new BattleService(battleRepo, calculator);

        when(battleRepo.save(any(Battle.class))).thenAnswer(i -> {
            Battle b = i.getArgument(0);
            if (b.getId() == null) b.setId(1L);
            lastSaved[0] = b;
            return b;
        });
        when(battleRepo.findById(1L)).thenAnswer(i -> Optional.ofNullable(lastSaved[0]));
    }

    private BattleUnit makeUnit(String name, int team, int level, int attack, int defense, int hp) {
        BattleUnit u = new BattleUnit();
        u.setName(name);
        u.setTeam(team);
        u.setLevel(level);
        u.setAttack(attack);
        u.setDefense(defense);
        u.setCurrentHealth(hp);
        u.setMaxHealth(hp);
        u.setCurrentMana(50);
        u.setMaxMana(50);
        u.setAlive(true);
        u.setPlayerUnit(team == 1);
        u.setHybridClass("NONE");
        return u;
    }

    private BattleStateResponse createSimpleBattle(BattleUnit hero, BattleUnit enemy) {
        BattleRequest req = new BattleRequest();
        req.setTeam1(new ArrayList<>(List.of(hero)));
        req.setTeam2(new ArrayList<>(List.of(enemy)));
        req.setPvp(false);
        return battleService.createBattle(req);
    }

    @Test
    void createBattleSetsUpCorrectly() {
        BattleStateResponse resp = createSimpleBattle(
                makeUnit("Hero", 1, 5, 15, 10, 100),
                makeUnit("Enemy", 2, 3, 10, 5, 80));

        assertEquals(1L, resp.getBattleId());
        assertEquals("IN_PROGRESS", resp.getStatus());
        assertEquals(1, resp.getTurnNumber());
        assertNotNull(resp.getCurrentUnit());
    }

    @Test
    void createBattleHigherLevelGoesFirst() {
        BattleStateResponse resp = createSimpleBattle(
                makeUnit("Low", 1, 2, 10, 5, 80),
                makeUnit("High", 2, 8, 15, 10, 100));

        assertEquals("High", resp.getCurrentUnit().getName());
    }

    @Test
    void createBattleHasBothTeams() {
        BattleRequest req = new BattleRequest();
        req.setTeam1(new ArrayList<>(List.of(
                makeUnit("H1", 1, 3, 10, 5, 100),
                makeUnit("H2", 1, 2, 8, 4, 80))));
        req.setTeam2(new ArrayList<>(List.of(
                makeUnit("E1", 2, 4, 12, 6, 120))));
        req.setPvp(false);

        BattleStateResponse resp = battleService.createBattle(req);

        assertEquals(2, resp.getTeam1().size());
        assertEquals(1, resp.getTeam2().size());
    }

    @Test
    void performActionAttack() {
        createSimpleBattle(
                makeUnit("Hero", 1, 5, 15, 10, 100),
                makeUnit("Enemy", 2, 3, 10, 5, 80));

        BattleStateResponse resp = battleService.performAction(1L,
                new ActionRequest("ATTACK", 1));

        assertEquals(70, resp.getTeam2().get(0).getCurrentHealth());
        assertTrue(resp.getLastActionResult().contains("attacks"));
    }

    @Test
    void performActionWait() {
        createSimpleBattle(
                makeUnit("Hero", 1, 5, 15, 10, 100),
                makeUnit("Enemy", 2, 3, 10, 5, 80));

        BattleStateResponse resp = battleService.performAction(1L,
                new ActionRequest("WAIT", null));

        assertTrue(resp.getLastActionResult().contains("waits"));
        assertEquals("IN_PROGRESS", resp.getStatus());
    }

    @Test
    void performActionDefend() {
        BattleUnit hero = makeUnit("Hero", 1, 5, 15, 10, 80);
        hero.setMaxHealth(100);
        hero.setCurrentMana(40);
        hero.setMaxMana(50);
        createSimpleBattle(hero, makeUnit("Enemy", 2, 3, 10, 5, 80));

        BattleStateResponse resp = battleService.performAction(1L,
                new ActionRequest("DEFEND", null));

        BattleUnit heroAfter = resp.getTeam1().get(0);
        assertEquals(90, heroAfter.getCurrentHealth());
        assertEquals(45, heroAfter.getCurrentMana());
    }

    @Test
    void battleEndsWhenTeamWiped() {
        createSimpleBattle(
                makeUnit("Hero", 1, 5, 100, 10, 200),
                makeUnit("Enemy", 2, 3, 10, 5, 50));

        BattleStateResponse resp = battleService.performAction(1L,
                new ActionRequest("ATTACK", 1));

        assertEquals("TEAM1_WIN", resp.getStatus());
        assertFalse(resp.getTeam2().get(0).isAlive());
    }

    @Test
    void performActionOnFinishedBattleThrows() {
        createSimpleBattle(
                makeUnit("Hero", 1, 5, 100, 10, 200),
                makeUnit("Enemy", 2, 3, 10, 5, 50));

        battleService.performAction(1L, new ActionRequest("ATTACK", 1));

        assertThrows(RuntimeException.class, () ->
                battleService.performAction(1L, new ActionRequest("WAIT", null)));
    }
}
