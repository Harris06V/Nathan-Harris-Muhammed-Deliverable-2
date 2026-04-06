package com.legendsofsw.pveservice.service;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.legendsofsw.pveservice.model.RoomType;

class RoomGeneratorTest {

    private RoomGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new RoomGenerator();
    }

    @Test
    void roomTypeIsNeverNull() {
        for (int i = 0; i < 100; i++) {
            RoomType type = generator.generateRoomType(5);
            assertNotNull(type);
        }
    }

    @Test
    void enemyPartyHasCorrectSize() {
        for (int i = 0; i < 20; i++) {
            List<Map<String, Object>> enemies = generator.generateEnemyParty(10);
            assertTrue(enemies.size() >= 1 && enemies.size() <= 5);
        }
    }

    @Test
    void enemyHasRequiredFields() {
        List<Map<String, Object>> enemies = generator.generateEnemyParty(5);

        for (Map<String, Object> enemy : enemies) {
            assertTrue(enemy.containsKey("name"));
            assertTrue(enemy.containsKey("level"));
            assertTrue(enemy.containsKey("attack"));
            assertTrue(enemy.containsKey("defense"));
            assertTrue(enemy.containsKey("maxHealth"));
            assertTrue(enemy.containsKey("currentHealth"));
        }
    }

    @Test
    void enemyLevelIsWithinRange() {
        List<Map<String, Object>> enemies = generator.generateEnemyParty(20);

        for (Map<String, Object> enemy : enemies) {
            int level = (int) enemy.get("level");
            assertTrue(level >= 1 && level <= 10);
        }
    }

    @Test
    void recruitableHeroesOnlyInFirst10Rooms() {
        List<Map<String, Object>> heroes5 = generator.generateRecruitableHeroes(5, 3);
        assertFalse(heroes5.isEmpty());

        List<Map<String, Object>> heroes15 = generator.generateRecruitableHeroes(15, 3);
        assertTrue(heroes15.isEmpty());
    }

    @Test
    void recruitableHeroHasClassAndCost() {
        List<Map<String, Object>> heroes = generator.generateRecruitableHeroes(3, 2);

        for (Map<String, Object> hero : heroes) {
            assertTrue(hero.containsKey("heroClass"));
            assertTrue(hero.containsKey("level"));
            assertTrue(hero.containsKey("cost"));
            int level = (int) hero.get("level");
            int cost = (int) hero.get("cost");
            if (level == 1) assertEquals(0, cost);
            else assertEquals(200 * level, cost);
        }
    }

    @Test
    void experienceCalculation() {
        List<Map<String, Object>> enemies = List.of(
                Map.of("level", 3),
                Map.of("level", 5)
        );
        int exp = generator.calcExperienceGain(enemies);
        // 50*3 + 50*5 = 150 + 250 = 400
        assertEquals(400, exp);
    }

    @Test
    void goldCalculation() {
        List<Map<String, Object>> enemies = List.of(
                Map.of("level", 2),
                Map.of("level", 4)
        );
        int gold = generator.calcGoldGain(enemies);
        // 75*2 + 75*4 = 150 + 300 = 450
        assertEquals(450, gold);
    }

    @Test
    void scoreCalculation() {
        int score = generator.calcFinalScore(30, 5000, 2000);
        // 30*100 + 5000*10 + 2000*5 = 3000 + 50000 + 10000 = 63000
        assertEquals(63000, score);
    }

    @Test
    void shopItemsListComplete() {
        List<Map<String, Object>> items = generator.getShopItems();
        assertEquals(7, items.size());
    }
}
