package com.legendsofsw.battleservice.model;

import java.util.ArrayList;
import java.util.List;

public class BattleUnit {

    private Long heroId;
    private String name;
    private int team;
    private int level;
    private int attack;
    private int defense;
    private int currentHealth;
    private int maxHealth;
    private int currentMana;
    private int maxMana;
    private int shield;
    private boolean stunned;
    private boolean alive;
    private boolean isPlayerUnit;

    // class info for ability resolution
    private int orderLevel;
    private int chaosLevel;
    private int warriorLevel;
    private int mageLevel;
    private String specialization;
    private String hybridClass;

    public BattleUnit() {
        this.shield = 0;
        this.stunned = false;
        this.alive = true;
    }

    public List<ActionType> getAvailableActions() {
        List<ActionType> actions = new ArrayList<>();
        actions.add(ActionType.ATTACK);
        actions.add(ActionType.DEFEND);
        actions.add(ActionType.WAIT);

        if (!isPlayerUnit) {
            return actions;
        }

        // abilities based on class levels
        if (orderLevel > 0) {
            if (currentMana >= 25) actions.add(ActionType.CAST_PROTECT);
            if (currentMana >= 35) actions.add(ActionType.CAST_HEAL);
        }
        if (chaosLevel > 0) {
            if (currentMana >= 30) actions.add(ActionType.CAST_FIREBALL);
            if (currentMana >= 40) actions.add(ActionType.CAST_CHAIN_LIGHTNING);
        }
        if (warriorLevel > 0) {
            if (currentMana >= 60) actions.add(ActionType.CAST_BERSERKER_ATTACK);
        }
        if (mageLevel > 0) {
            int replenishCost = "WIZARD".equals(hybridClass) ? 40 : 80;
            if (currentMana >= replenishCost) actions.add(ActionType.CAST_REPLENISH);
        }

        return actions;
    }

    public Long getHeroId() { return heroId; }
    public void setHeroId(Long heroId) { this.heroId = heroId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTeam() { return team; }
    public void setTeam(int team) { this.team = team; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }

    public int getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(int currentHealth) { this.currentHealth = currentHealth; }

    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }

    public int getCurrentMana() { return currentMana; }
    public void setCurrentMana(int currentMana) { this.currentMana = currentMana; }

    public int getMaxMana() { return maxMana; }
    public void setMaxMana(int maxMana) { this.maxMana = maxMana; }

    public int getShield() { return shield; }
    public void setShield(int shield) { this.shield = shield; }

    public boolean isStunned() { return stunned; }
    public void setStunned(boolean stunned) { this.stunned = stunned; }

    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }

    public boolean isPlayerUnit() { return isPlayerUnit; }
    public void setPlayerUnit(boolean playerUnit) { isPlayerUnit = playerUnit; }

    public int getOrderLevel() { return orderLevel; }
    public void setOrderLevel(int orderLevel) { this.orderLevel = orderLevel; }

    public int getChaosLevel() { return chaosLevel; }
    public void setChaosLevel(int chaosLevel) { this.chaosLevel = chaosLevel; }

    public int getWarriorLevel() { return warriorLevel; }
    public void setWarriorLevel(int warriorLevel) { this.warriorLevel = warriorLevel; }

    public int getMageLevel() { return mageLevel; }
    public void setMageLevel(int mageLevel) { this.mageLevel = mageLevel; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getHybridClass() { return hybridClass; }
    public void setHybridClass(String hybridClass) { this.hybridClass = hybridClass; }
}
