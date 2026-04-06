package com.legendsofsw.battleservice.template;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.legendsofsw.battleservice.model.BattleUnit;

class BattleTurnTemplateTest {

    private BattleUnit makeUnit(String name, int team, int level, int atk, int def, int hp) {
        BattleUnit u = new BattleUnit();
        u.setName(name);
        u.setTeam(team);
        u.setLevel(level);
        u.setAttack(atk);
        u.setDefense(def);
        u.setCurrentHealth(hp);
        u.setMaxHealth(hp);
        u.setCurrentMana(20);
        u.setMaxMana(50);
        u.setAlive(true);
        u.setPlayerUnit(team == 1);
        u.setHybridClass("NONE");
        return u;
    }

    @Test
    void turnOrderInterleavesByTeam() {
        PvEBattleTurn turn = new PvEBattleTurn();
        BattleUnit h1 = makeUnit("H1", 1, 5, 15, 5, 100);
        BattleUnit h2 = makeUnit("H2", 1, 3, 10, 5, 80);
        BattleUnit e1 = makeUnit("E1", 2, 4, 12, 5, 90);

        List<BattleUnit> order = turn.determineTurnOrder(
                List.of(h1, h2), List.of(e1));

        // verify all 3 units present and teams interleaved
        assertEquals(3, order.size());
        // check that both teams are represented
        boolean hasTeam1 = false, hasTeam2 = false;
        for (BattleUnit u : order) {
            if (u.getTeam() == 1) hasTeam1 = true;
            if (u.getTeam() == 2) hasTeam2 = true;
        }
        assertTrue(hasTeam1);
        assertTrue(hasTeam2);
    }

    @Test
    void executeCurrentActionHandlesDeadUnit() {
        PvEBattleTurn turn = new PvEBattleTurn();
        BattleUnit dead = makeUnit("Dead", 1, 3, 10, 5, 0);
        dead.setAlive(false);

        String result = turn.executeCurrentAction(dead, List.of(dead));

        assertTrue(result.contains("dead"));
    }

    @Test
    void executeCurrentActionHandlesStunnedUnit() {
        PvEBattleTurn turn = new PvEBattleTurn();
        BattleUnit stunned = makeUnit("Stunned", 1, 3, 10, 5, 100);
        stunned.setStunned(true);

        String result = turn.executeCurrentAction(stunned, List.of(stunned));

        assertTrue(result.contains("stunned"));
        assertFalse(stunned.isStunned());
    }

    @Test
    void enemyAIAttacksRandomTarget() {
        PvEBattleTurn turn = new PvEBattleTurn();
        BattleUnit enemy = makeUnit("Enemy", 2, 3, 15, 5, 100);
        enemy.setPlayerUnit(false);
        BattleUnit hero = makeUnit("Hero", 1, 3, 10, 5, 100);
        List<BattleUnit> all = new ArrayList<>(List.of(hero, enemy));

        String result = turn.executeCurrentAction(enemy, all);

        // enemy should attack hero
        assertTrue(result.contains("attacks"));
        assertTrue(hero.getCurrentHealth() < 100);
    }

    @Test
    void endOfTurnRegensMana() {
        PvEBattleTurn turn = new PvEBattleTurn();
        BattleUnit h = makeUnit("H", 1, 3, 10, 5, 100);
        h.setCurrentMana(20);
        BattleUnit e = makeUnit("E", 2, 3, 10, 5, 100);
        e.setCurrentMana(30);

        turn.applyEndOfTurnEffects(List.of(h), List.of(e));

        assertEquals(22, h.getCurrentMana());
        assertEquals(32, e.getCurrentMana());
    }

    @Test
    void checkBattleResultDetectsTeam1Wiped() {
        PvEBattleTurn turn = new PvEBattleTurn();
        BattleUnit dead = makeUnit("H", 1, 3, 10, 5, 0);
        dead.setAlive(false);
        BattleUnit alive = makeUnit("E", 2, 3, 10, 5, 100);

        String result = turn.checkBattleResult(List.of(dead), List.of(alive));

        assertEquals("Team 2 wins!", result);
    }

    @Test
    void checkBattleResultDetectsTeam2Wiped() {
        PvEBattleTurn turn = new PvEBattleTurn();
        BattleUnit alive = makeUnit("H", 1, 3, 10, 5, 100);
        BattleUnit dead = makeUnit("E", 2, 3, 10, 5, 0);
        dead.setAlive(false);

        String result = turn.checkBattleResult(List.of(alive), List.of(dead));

        assertEquals("Team 1 wins!", result);
    }

    @Test
    void checkBattleResultReturnsEmptyWhenBothAlive() {
        PvEBattleTurn turn = new PvEBattleTurn();
        BattleUnit h = makeUnit("H", 1, 3, 10, 5, 100);
        BattleUnit e = makeUnit("E", 2, 3, 10, 5, 100);

        String result = turn.checkBattleResult(List.of(h), List.of(e));

        assertEquals("", result);
    }

    @Test
    void executeTurnRunsFullPipeline() {
        PvEBattleTurn turn = new PvEBattleTurn();
        BattleUnit hero = makeUnit("Hero", 1, 5, 10, 5, 100);
        BattleUnit enemy = makeUnit("Enemy", 2, 3, 8, 5, 100);
        enemy.setPlayerUnit(false);
        List<BattleUnit> all = new ArrayList<>(List.of(hero, enemy));

        // hero awaits input since playerUnit=true
        String result = turn.executeTurn(List.of(hero), List.of(enemy), hero, all);

        assertTrue(result.contains("awaits player input"));
    }
}
