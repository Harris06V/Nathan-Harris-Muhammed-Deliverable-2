package com.legendsofsw.battleservice.service;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.legendsofsw.battleservice.model.BattleUnit;

class CombatCalculatorTest {

    private CombatCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new CombatCalculator();
    }

    private BattleUnit createUnit(String name, int team, int attack, int defense, int hp, int mana) {
        BattleUnit unit = new BattleUnit();
        unit.setName(name);
        unit.setTeam(team);
        unit.setAttack(attack);
        unit.setDefense(defense);
        unit.setCurrentHealth(hp);
        unit.setMaxHealth(hp);
        unit.setCurrentMana(mana);
        unit.setMaxMana(mana);
        unit.setAlive(true);
        unit.setPlayerUnit(true);
        unit.setLevel(1);
        unit.setHybridClass("NONE");
        return unit;
    }

    @Test
    void basicAttackDamage() {
        BattleUnit attacker = createUnit("Attacker", 1, 15, 5, 100, 50);
        BattleUnit defender = createUnit("Defender", 2, 5, 8, 100, 50);
        List<BattleUnit> all = List.of(attacker, defender);

        calculator.processAttack(attacker, defender, all);

        // damage = 15 - 8 = 7
        assertEquals(93, defender.getCurrentHealth());
    }

    @Test
    void attackCantDealNegativeDamage() {
        BattleUnit attacker = createUnit("Weak", 1, 3, 5, 100, 50);
        BattleUnit defender = createUnit("Tank", 2, 5, 20, 100, 50);
        List<BattleUnit> all = List.of(attacker, defender);

        calculator.processAttack(attacker, defender, all);

        assertEquals(100, defender.getCurrentHealth());
    }

    @Test
    void defendRestoresHPAndMana() {
        BattleUnit unit = createUnit("Defender", 1, 5, 5, 80, 30);
        unit.setMaxHealth(100);
        unit.setMaxMana(50);

        calculator.processDefend(unit);

        assertEquals(90, unit.getCurrentHealth());
        assertEquals(35, unit.getCurrentMana());
    }

    @Test
    void defendDoesNotExceedMax() {
        BattleUnit unit = createUnit("Full", 1, 5, 5, 100, 50);

        calculator.processDefend(unit);

        assertEquals(100, unit.getCurrentHealth());
        assertEquals(50, unit.getCurrentMana());
    }

    @Test
    void protectAddsShield() {
        BattleUnit caster = createUnit("Caster", 1, 5, 5, 100, 50);
        caster.setOrderLevel(3);
        BattleUnit ally = createUnit("Ally", 1, 5, 5, 200, 50);
        List<BattleUnit> all = List.of(caster, ally);

        calculator.processProtect(caster, all);

        // shield = 10% of each unit's max HP
        assertEquals(10, caster.getShield()); // 10% of 100
        assertEquals(20, ally.getShield()); // 10% of 200
        assertEquals(25, caster.getCurrentMana()); // 50 - 25 cost
    }

    @Test
    void healTargetsLowestHP() {
        BattleUnit caster = createUnit("Healer", 1, 5, 5, 100, 50);
        caster.setOrderLevel(3);
        BattleUnit low = createUnit("Low", 1, 5, 5, 30, 50);
        low.setMaxHealth(100);
        BattleUnit high = createUnit("High", 1, 5, 5, 90, 50);
        high.setMaxHealth(100);
        List<BattleUnit> all = List.of(caster, low, high);

        calculator.processHeal(caster, all);

        // heals lowest by 25% of their max (100 * 0.25 = 25)
        assertEquals(55, low.getCurrentHealth());
        assertEquals(15, caster.getCurrentMana()); // 50 - 35 cost
    }

    @Test
    void fireballHitsUpToThree() {
        BattleUnit caster = createUnit("Mage", 1, 20, 5, 100, 50);
        caster.setChaosLevel(3);
        BattleUnit e1 = createUnit("E1", 2, 5, 5, 100, 0);
        BattleUnit e2 = createUnit("E2", 2, 5, 5, 100, 0);
        BattleUnit e3 = createUnit("E3", 2, 5, 5, 100, 0);
        BattleUnit e4 = createUnit("E4", 2, 5, 5, 100, 0);
        List<BattleUnit> all = new ArrayList<>(List.of(caster, e1, e2, e3, e4));

        calculator.processFireball(caster, e1, all);

        // e1 takes 20-5=15 damage, e2 and e3 also take 15 each, e4 unaffected
        assertEquals(85, e1.getCurrentHealth());
        int hitCount = 0;
        if (e2.getCurrentHealth() < 100) hitCount++;
        if (e3.getCurrentHealth() < 100) hitCount++;
        if (e4.getCurrentHealth() < 100) hitCount++;
        assertEquals(2, hitCount); // only 2 more targets beyond primary
    }

    @Test
    void shieldAbsorbsDamage() {
        BattleUnit attacker = createUnit("Attacker", 1, 15, 5, 100, 50);
        BattleUnit shielded = createUnit("Shielded", 2, 5, 5, 100, 50);
        shielded.setShield(20);
        List<BattleUnit> all = List.of(attacker, shielded);

        calculator.processAttack(attacker, shielded, all);

        // damage = 15 - 5 = 10 absorbed by shield
        assertEquals(100, shielded.getCurrentHealth());
        assertEquals(10, shielded.getShield());
    }

    @Test
    void unitDiesAtZeroHP() {
        BattleUnit attacker = createUnit("Killer", 1, 100, 5, 100, 50);
        BattleUnit weak = createUnit("Weak", 2, 5, 5, 50, 50);
        List<BattleUnit> all = List.of(attacker, weak);

        calculator.processAttack(attacker, weak, all);

        assertEquals(0, weak.getCurrentHealth());
        assertFalse(weak.isAlive());
    }

    @Test
    void teamDefeatedCheck() {
        BattleUnit alive = createUnit("Alive", 1, 5, 5, 100, 50);
        BattleUnit dead = createUnit("Dead", 2, 5, 5, 0, 0);
        dead.setAlive(false);
        List<BattleUnit> all = List.of(alive, dead);

        assertFalse(calculator.isTeamDefeated(1, all));
        assertTrue(calculator.isTeamDefeated(2, all));
    }

    @Test
    void chainLightningDamageDecays() {
        BattleUnit caster = createUnit("Mage", 1, 20, 5, 100, 50);
        caster.setChaosLevel(3);
        BattleUnit e1 = createUnit("E1", 2, 5, 5, 100, 0);
        BattleUnit e2 = createUnit("E2", 2, 5, 5, 100, 0);
        List<BattleUnit> all = new ArrayList<>(List.of(caster, e1, e2));

        calculator.processChainLightning(caster, e1, all);

        // primary target: 20-5=15 full damage
        assertEquals(85, e1.getCurrentHealth());
        // secondary gets reduced damage
        assertTrue(e2.getCurrentHealth() < 100);
        // mana cost is 40
        assertEquals(10, caster.getCurrentMana());
    }

    @Test
    void berserkerAttackHitsMultipleTargets() {
        BattleUnit caster = createUnit("Warrior", 1, 20, 5, 100, 100);
        caster.setWarriorLevel(3);
        BattleUnit e1 = createUnit("E1", 2, 5, 5, 100, 0);
        BattleUnit e2 = createUnit("E2", 2, 5, 5, 100, 0);
        List<BattleUnit> all = new ArrayList<>(List.of(caster, e1, e2));

        calculator.processBerserkerAttack(caster, e1, all);

        // main target: 20-5=15
        assertEquals(85, e1.getCurrentHealth());
        // splash: 15*0.25=3
        assertEquals(97, e2.getCurrentHealth());
        // mana cost is 60
        assertEquals(40, caster.getCurrentMana());
    }

    @Test
    void replenishRestoresMana() {
        BattleUnit caster = createUnit("Mage", 1, 5, 5, 100, 100);
        caster.setMageLevel(3);
        BattleUnit ally = createUnit("Ally", 1, 5, 5, 100, 20);
        ally.setMaxMana(50);
        List<BattleUnit> all = List.of(caster, ally);

        calculator.processReplenish(caster, all);

        // caster: 100 - 80  + 60  = 80
        assertEquals(80, caster.getCurrentMana());
        // ally: 20 + 30 = 50 
        assertEquals(50, ally.getCurrentMana());
    }
}
