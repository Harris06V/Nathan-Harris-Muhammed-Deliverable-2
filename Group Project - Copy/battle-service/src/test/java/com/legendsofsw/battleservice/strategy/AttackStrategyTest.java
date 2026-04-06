package com.legendsofsw.battleservice.strategy;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.legendsofsw.battleservice.model.BattleUnit;

class AttackStrategyTest {

    private BattleUnit makeUnit(String name, int team, int atk, int def, int hp, int mana) {
        BattleUnit u = new BattleUnit();
        u.setName(name);
        u.setTeam(team);
        u.setAttack(atk);
        u.setDefense(def);
        u.setCurrentHealth(hp);
        u.setMaxHealth(hp);
        u.setCurrentMana(mana);
        u.setMaxMana(mana);
        u.setAlive(true);
        u.setPlayerUnit(true);
        u.setLevel(3);
        u.setHybridClass("NONE");
        return u;
    }

    @Test
    void basicAttackDealsDamage() {
        AttackStrategy strategy = new BasicAttackStrategy();
        BattleUnit attacker = makeUnit("Hero", 1, 15, 5, 100, 50);
        BattleUnit target = makeUnit("Enemy", 2, 5, 5, 100, 0);

        String result = strategy.execute(attacker, target, List.of(attacker, target));

        assertEquals(90, target.getCurrentHealth());
        assertTrue(result.contains("attacks"));
    }

    @Test
    void warriorAttackAddsBonus() {
        AttackStrategy strategy = new WarriorAttackStrategy();
        BattleUnit attacker = makeUnit("Warrior", 1, 15, 5, 100, 50);
        BattleUnit target = makeUnit("Enemy", 2, 5, 5, 100, 0);

        strategy.execute(attacker, target, List.of(attacker, target));

        // 15 + 2 bonus - 5 def = 12 damage
        assertEquals(88, target.getCurrentHealth());
    }

    @Test
    void mageAttackUsesMultiplierWithMana() {
        AttackStrategy strategy = new MageAttackStrategy();
        BattleUnit attacker = makeUnit("Mage", 1, 20, 5, 100, 50);
        BattleUnit target = makeUnit("Enemy", 2, 5, 5, 100, 0);

        strategy.execute(attacker, target, List.of(attacker, target));

        // (20 * 1.5) - 5 = 25 damage, mana cost 10
        assertEquals(75, target.getCurrentHealth());
        assertEquals(40, attacker.getCurrentMana());
    }

    @Test
    void mageAttackFallsBackWhenLowMana() {
        AttackStrategy strategy = new MageAttackStrategy();
        BattleUnit attacker = makeUnit("Mage", 1, 20, 5, 100, 5);
        BattleUnit target = makeUnit("Enemy", 2, 5, 5, 100, 0);

        String result = strategy.execute(attacker, target, List.of(attacker, target));

        // 20 - 5 = 15 normal damage, no mana used
        assertEquals(85, target.getCurrentHealth());
        assertTrue(result.contains("low mana"));
    }

    @Test
    void powerAttackDeals150PercentDamage() {
        AttackStrategy strategy = new PowerAttackStrategy();
        BattleUnit attacker = makeUnit("Power", 1, 15, 5, 100, 50);
        BattleUnit target = makeUnit("Enemy", 2, 5, 5, 100, 0);

        strategy.execute(attacker, target, List.of(attacker, target));

        // (15-5) * 1.5 = 15 damage
        assertEquals(85, target.getCurrentHealth());
    }

    @Test
    void orderAttackGrantsShield() {
        AttackStrategy strategy = new OrderAttackStrategy();
        BattleUnit attacker = makeUnit("Paladin", 1, 15, 5, 100, 50);
        BattleUnit target = makeUnit("Enemy", 2, 5, 5, 100, 0);

        strategy.execute(attacker, target, List.of(attacker, target));

        // 15 - 5 = 10 damage, shield = 5% of 100 maxHP = 5
        assertEquals(90, target.getCurrentHealth());
        assertEquals(5, attacker.getShield());
    }

    @Test
    void chaosAttackDealsTripleDamageWithSelfDamage() {
        AttackStrategy strategy = new ChaosAttackStrategy();
        BattleUnit attacker = makeUnit("Chaos", 1, 15, 5, 100, 50);
        BattleUnit target = makeUnit("Enemy", 2, 5, 5, 100, 0);

        strategy.execute(attacker, target, List.of(attacker, target));

        // raw = 15-5=10, target takes 10*3=30, self = 10*0.5=5
        assertEquals(70, target.getCurrentHealth());
        assertEquals(95, attacker.getCurrentHealth());
    }

    @Test
    void bossAttackDeals150PercentDamage() {
        AttackStrategy strategy = new BossAttackStrategy();
        BattleUnit attacker = makeUnit("Boss", 2, 20, 5, 200, 0);
        BattleUnit target = makeUnit("Hero", 1, 10, 5, 100, 50);

        strategy.execute(attacker, target, List.of(attacker, target));

        // (20-5) * 1.5 = 22 damage
        assertEquals(78, target.getCurrentHealth());
    }

    @Test
    void applyDamageAbsorbsShield() {
        AttackStrategy strategy = new BasicAttackStrategy();
        BattleUnit attacker = makeUnit("Hero", 1, 20, 5, 100, 50);
        BattleUnit target = makeUnit("Shielded", 2, 5, 5, 100, 0);
        target.setShield(10);

        strategy.execute(attacker, target, List.of(attacker, target));

        // 20-5=15, shield absorbs 10, 5 to hp
        assertEquals(95, target.getCurrentHealth());
        assertEquals(0, target.getShield());
    }

    @Test
    void eachStrategyHasName() {
        assertEquals("Basic Attack", new BasicAttackStrategy().getStrategyName());
        assertEquals("Warrior Attack", new WarriorAttackStrategy().getStrategyName());
        assertEquals("Mage Attack", new MageAttackStrategy().getStrategyName());
        assertEquals("Power Attack", new PowerAttackStrategy().getStrategyName());
        assertEquals("Order Attack", new OrderAttackStrategy().getStrategyName());
        assertEquals("Chaos Attack", new ChaosAttackStrategy().getStrategyName());
        assertEquals("Boss Attack", new BossAttackStrategy().getStrategyName());
    }
}
