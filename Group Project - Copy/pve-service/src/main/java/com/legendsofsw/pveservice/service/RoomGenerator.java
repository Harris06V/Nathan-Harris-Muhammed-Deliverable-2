package com.legendsofsw.pveservice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.legendsofsw.pveservice.model.BossMob;
import com.legendsofsw.pveservice.model.Mob;
import com.legendsofsw.pveservice.model.NormalMob;
import com.legendsofsw.pveservice.model.RoomType;

@Component
public class RoomGenerator {

    private final Random random = new Random();

    // battle probability starts at 60% increases by 3% per 10 cumulative hero levels
    public RoomType generateRoomType(int cumulativeLevel) {
        int bracket = cumulativeLevel / 10;
        double battleChance = Math.min(0.60 + (bracket * 0.03), 0.90);

        if (random.nextDouble() < battleChance) {
            return RoomType.BATTLE;
        }
        return RoomType.INN;
    }

    // generate enemy party scaled to player cumulative level using Mob hierarchy
    public List<Map<String, Object>> generateEnemyParty(int playerCumulativeLevel) {
        int numEnemies = 1 + random.nextInt(5);
        int maxCumulativeLevel = Math.max(playerCumulativeLevel, numEnemies);

        List<Map<String, Object>> enemies = new ArrayList<>();
        int remainingLevels = maxCumulativeLevel;

        for (int i = 0; i < numEnemies; i++) {
            int maxForThis = Math.min(10, remainingLevels - (numEnemies - i - 1));
            if (maxForThis < 1) maxForThis = 1;
            int enemyLevel = 1 + random.nextInt(maxForThis);
            remainingLevels -= enemyLevel;

            // use Mob hierarchy: 20% chance for a boss on the last enemy
            Mob mob;
            if (i == numEnemies - 1 && random.nextDouble() < 0.20) {
                mob = new BossMob("Boss " + (i + 1), enemyLevel);
            } else {
                mob = new NormalMob("Enemy " + (i + 1), enemyLevel);
            }

            enemies.add(mob.toMap());
        }

        return enemies;
    }

    // generate recruitable heroes for the inn (max party size is 5)
    public List<Map<String, Object>> generateRecruitableHeroes(int currentRoom, int currentPartySize) {
        if (currentRoom > 10 || currentPartySize >= 5) {
            return new ArrayList<>();
        }

        String[] classes = {"ORDER", "CHAOS", "WARRIOR", "MAGE"};
        int count = 1 + random.nextInt(2);
        List<Map<String, Object>> heroes = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String heroClass = classes[random.nextInt(classes.length)];
            int level = 1 + random.nextInt(4);
            int cost = level == 1 ? 0 : 200 * level;

            Map<String, Object> hero = new HashMap<>();
            hero.put("name", heroClass.charAt(0) + heroClass.substring(1).toLowerCase() + " Recruit");
            hero.put("heroClass", heroClass);
            hero.put("level", level);
            hero.put("cost", cost);
            heroes.add(hero);
        }

        return heroes;
    }

    // build shop item list with costs
    public List<Map<String, Object>> getShopItems() {
        List<Map<String, Object>> items = new ArrayList<>();
        String[][] shopData = {
            {"BREAD", "200", "+20 HP"},
            {"CHEESE", "500", "+50 HP"},
            {"STEAK", "1000", "+200 HP"},
            {"WATER", "150", "+10 mana"},
            {"JUICE", "400", "+30 mana"},
            {"WINE", "750", "+100 mana"},
            {"ELIXIR", "2000", "Revive + Full HP + Full mana"}
        };
        for (String[] entry : shopData) {
            Map<String, Object> item = new HashMap<>();
            item.put("itemType", entry[0]);
            item.put("cost", Integer.parseInt(entry[1]));
            item.put("effect", entry[2]);
            items.add(item);
        }
        return items;
    }

    // experience per enemy: uses mob's actual xpReward (bosses give double)
    public int calcExperienceGain(List<Map<String, Object>> enemies) {
        int total = 0;
        for (Map<String, Object> enemy : enemies) {
            if (enemy.containsKey("xpReward")) {
                total += (int) enemy.get("xpReward");
            } else {
                int level = (int) enemy.get("level");
                total += 50 * level;
            }
        }
        return total;
    }

    // gold per enemy: uses mob's actual goldReward (bosses give double)
    public int calcGoldGain(List<Map<String, Object>> enemies) {
        int total = 0;
        for (Map<String, Object> enemy : enemies) {
            if (enemy.containsKey("goldReward")) {
                total += (int) enemy.get("goldReward");
            } else {
                int level = (int) enemy.get("level");
                total += 75 * level;
            }
        }
        return total;
    }

    // final score: 100 per hero level + 10 per gold + half item price * 10
    public int calcFinalScore(int totalHeroLevels, int gold, int itemSpending) {
        return (totalHeroLevels * 100) + (gold * 10) + (itemSpending * 5);
    }
}
