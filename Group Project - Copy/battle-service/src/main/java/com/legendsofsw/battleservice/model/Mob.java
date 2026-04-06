package com.legendsofsw.battleservice.model;

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

    // each mob type attacks differently
    public abstract int attack(BattleUnit target);

    // convert this mob into a BattleUnit for the battle system
    public BattleUnit toBattleUnit(int team) {
        BattleUnit unit = new BattleUnit();
        unit.setName(name);
        unit.setTeam(team);
        unit.setLevel(level);
        unit.setAttack(power);
        unit.setDefense(defense);
        unit.setCurrentHealth(hp);
        unit.setMaxHealth(hp);
        unit.setCurrentMana(0);
        unit.setMaxMana(0);
        unit.setPlayerUnit(false);
        return unit;
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
