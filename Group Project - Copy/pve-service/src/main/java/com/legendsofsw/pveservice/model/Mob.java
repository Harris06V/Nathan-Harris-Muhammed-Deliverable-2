package com.legendsofsw.pveservice.model;

import java.util.HashMap;
import java.util.Map;

public abstract class Mob {

    private String name;
    private int hp;
    private int power;
    private int defense;
    private int level;
    private int xpReward;
    private int goldReward;

    public Mob() {}

    public Mob(String name, int level, int power, int defense, int hp) {
        this.name = name;
        this.level = level;
        this.power = power;
        this.defense = defense;
        this.hp = hp;
        this.xpReward = 50 * level;
        this.goldReward = 75 * level;
    }

    // each mob type has different stats or behavior
    public abstract double getDamageMultiplier();

    // convert to a map for the battle service API
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("level", level);
        map.put("attack", power);
        map.put("defense", defense);
        map.put("maxHealth", hp);
        map.put("currentHealth", hp);
        map.put("maxMana", 0);
        map.put("currentMana", 0);
        map.put("isPlayerUnit", false);
        map.put("orderLevel", 0);
        map.put("chaosLevel", 0);
        map.put("warriorLevel", 0);
        map.put("mageLevel", 0);
        map.put("xpReward", xpReward);
        map.put("goldReward", goldReward);
        return map;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getPower() { return power; }
    public void setPower(int power) { this.power = power; }

    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }

    public int getGoldReward() { return goldReward; }
    public void setGoldReward(int goldReward) { this.goldReward = goldReward; }
}
