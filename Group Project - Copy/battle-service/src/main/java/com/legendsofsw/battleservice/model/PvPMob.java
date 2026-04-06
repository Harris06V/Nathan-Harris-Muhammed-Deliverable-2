package com.legendsofsw.battleservice.model;

public class PvPMob extends Mob {

    private BattleUnit wrappedUnit;

    public PvPMob() {
        super();
    }

    public PvPMob(BattleUnit heroUnit) {
        super(heroUnit.getName(), heroUnit.getLevel(),
              heroUnit.getAttack(), heroUnit.getDefense(),
              heroUnit.getMaxHealth());
        this.wrappedUnit = heroUnit;
    }

    @Override
    public int attack(BattleUnit target) {
        // PvP mob uses the wrapped hero's stats directly
        int damage = Math.max(0, getPower() - target.getDefense());
        int remaining = damage;

        if (target.getShield() > 0) {
            if (target.getShield() >= remaining) {
                target.setShield(target.getShield() - remaining);
                remaining = 0;
            } else {
                remaining -= target.getShield();
                target.setShield(0);
            }
        }

        target.setCurrentHealth(target.getCurrentHealth() - remaining);
        if (target.getCurrentHealth() <= 0) {
            target.setCurrentHealth(0);
            target.setAlive(false);
        }

        return damage;
    }

    @Override
    public BattleUnit toBattleUnit(int team) {
        if (wrappedUnit != null) {
            // preserve all class info from the original hero
            BattleUnit unit = new BattleUnit();
            unit.setName(wrappedUnit.getName());
            unit.setTeam(team);
            unit.setLevel(wrappedUnit.getLevel());
            unit.setAttack(wrappedUnit.getAttack());
            unit.setDefense(wrappedUnit.getDefense());
            unit.setCurrentHealth(wrappedUnit.getMaxHealth());
            unit.setMaxHealth(wrappedUnit.getMaxHealth());
            unit.setCurrentMana(wrappedUnit.getMaxMana());
            unit.setMaxMana(wrappedUnit.getMaxMana());
            unit.setOrderLevel(wrappedUnit.getOrderLevel());
            unit.setChaosLevel(wrappedUnit.getChaosLevel());
            unit.setWarriorLevel(wrappedUnit.getWarriorLevel());
            unit.setMageLevel(wrappedUnit.getMageLevel());
            unit.setSpecialization(wrappedUnit.getSpecialization());
            unit.setHybridClass(wrappedUnit.getHybridClass());
            unit.setPlayerUnit(true);
            return unit;
        }
        return super.toBattleUnit(team);
    }

    public BattleUnit getWrappedUnit() { return wrappedUnit; }
    public void setWrappedUnit(BattleUnit wrappedUnit) { this.wrappedUnit = wrappedUnit; }
}
