package com.legendsofsw.pveservice.model;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class MobHierarchyTest {

    @Test
    void normalMobHasCorrectStats() {
        NormalMob mob = new NormalMob("Goblin", 3);

        assertEquals("Goblin", mob.getName());
        assertEquals(3, mob.getLevel());
        assertEquals(11, mob.getPower());    // 5 + 3*2
        assertEquals(8, mob.getDefense());   // 5 + 3
        assertEquals(130, mob.getHp());     // 100 + 3*10
        assertEquals(150, mob.getXpReward()); // 50 * 3
        assertEquals(225, mob.getGoldReward()); // 75 * 3
    }

    @Test
    void bossMobHasEnhancedStats() {
        BossMob boss = new BossMob("Dragon", 5);

        assertEquals("Dragon", boss.getName());
        assertEquals(5, boss.getLevel());
        assertEquals(20, boss.getPower());   // 5 + 5*3
        assertEquals(15, boss.getDefense()); // 5 + 5*2
        assertEquals(300, boss.getHp());    // 200 + 5*20
        assertEquals(500, boss.getXpReward()); // 100 * 5 (double)
        assertEquals(750, boss.getGoldReward()); // 150 * 5 (double)
    }

    @Test
    void normalMobDamageMultiplierIs1() {
        NormalMob mob = new NormalMob("Slime", 1);
        assertEquals(1.0, mob.getDamageMultiplier());
    }

    @Test
    void bossMobDamageMultiplierIs1_5() {
        BossMob boss = new BossMob("Boss", 1);
        assertEquals(1.5, boss.getDamageMultiplier());
    }

    @Test
    void toMapContainsAllFields() {
        NormalMob mob = new NormalMob("Wolf", 2);
        Map<String, Object> map = mob.toMap();

        assertEquals("Wolf", map.get("name"));
        assertEquals(2, map.get("level"));
        assertEquals(9, map.get("attack"));       // 5 + 2*2
        assertEquals(7, map.get("defense"));      // 5 + 2
        assertEquals(120, map.get("maxHealth"));  // 100 + 2*10
        assertEquals(120, map.get("currentHealth"));
        assertFalse((boolean) map.get("isPlayerUnit"));
        assertEquals(100, map.get("xpReward"));   // 50*2
        assertEquals(150, map.get("goldReward")); // 75*2
    }

    @Test
    void bossMobToMapHasDoubleRewards() {
        BossMob boss = new BossMob("Ogre", 3);
        Map<String, Object> map = boss.toMap();

        assertEquals(300, map.get("xpReward"));  // 100 * 3
        assertEquals(450, map.get("goldReward")); // 150 * 3
    }

    @Test
    void mobIsAbstract() {
        assertTrue(java.lang.reflect.Modifier.isAbstract(Mob.class.getModifiers()));
    }
}
