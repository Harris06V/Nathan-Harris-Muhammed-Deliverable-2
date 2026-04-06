package com.legendsofsw.battleservice.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.legendsofsw.battleservice.model.BattleUnit;
import com.legendsofsw.battleservice.strategy.AttackStrategy;
import com.legendsofsw.battleservice.strategy.BasicAttackStrategy;
import com.legendsofsw.battleservice.strategy.ChaosAttackStrategy;
import com.legendsofsw.battleservice.strategy.MageAttackStrategy;
import com.legendsofsw.battleservice.strategy.OrderAttackStrategy;
import com.legendsofsw.battleservice.strategy.WarriorAttackStrategy;

@Component
public class CombatCalculator {

    private final Random random = new Random();

    // strategy pattern: resolve which attack strategy to use based on unit class
    public AttackStrategy resolveStrategy(BattleUnit unit) {
        if (!unit.isPlayerUnit()) {
            return new BasicAttackStrategy();
        }
        if (unit.getWarriorLevel() > 0 && unit.getWarriorLevel() >= unit.getMageLevel()
                && unit.getWarriorLevel() >= unit.getOrderLevel()
                && unit.getWarriorLevel() >= unit.getChaosLevel()) {
            return new WarriorAttackStrategy();
        }
        if (unit.getMageLevel() > 0 && unit.getMageLevel() >= unit.getWarriorLevel()
                && unit.getMageLevel() >= unit.getOrderLevel()
                && unit.getMageLevel() >= unit.getChaosLevel()) {
            return new MageAttackStrategy();
        }
        if (unit.getOrderLevel() > 0 && unit.getOrderLevel() >= unit.getWarriorLevel()
                && unit.getOrderLevel() >= unit.getMageLevel()
                && unit.getOrderLevel() >= unit.getChaosLevel()) {
            return new OrderAttackStrategy();
        }
        if (unit.getChaosLevel() > 0) {
            return new ChaosAttackStrategy();
        }
        return new BasicAttackStrategy();
    }

    // basic attack: damage = attacker attack - defender defense (spec: UA - UD)
    // now enhanced with strategy pattern for base damage + hybrid class bonuses
    // minimum damage floor so battles don't stall when stats are close
    private static final int MINIMUM_DAMAGE = 15;

    public String processAttack(BattleUnit attacker, BattleUnit target, List<BattleUnit> allUnits) {
        int rawDamage = Math.max(MINIMUM_DAMAGE, attacker.getAttack() - target.getDefense());

        // warlock mana burn: 10% of target total mana
        boolean isManaburn = "WARLOCK".equals(attacker.getHybridClass());

        // rogue: 50% chance for extra attack at 50% damage
        boolean isRogue = "ROGUE".equals(attacker.getHybridClass());

        applyDamageToUnit(target, rawDamage);
        StringBuilder result = new StringBuilder();
        result.append(attacker.getName()).append(" attacks ").append(target.getName())
              .append(" for ").append(rawDamage).append(" damage.");

        if (isManaburn && rawDamage > 0) {
            int manaBurned = (int) (target.getMaxMana() * 0.10);
            target.setCurrentMana(Math.max(0, target.getCurrentMana() - manaBurned));
            result.append(" Burns ").append(manaBurned).append(" mana.");
        }

        if (isRogue && random.nextDouble() < 0.50) {
            List<BattleUnit> enemies = getEnemies(attacker, allUnits);
            if (!enemies.isEmpty()) {
                BattleUnit rogueTarget = enemies.get(random.nextInt(enemies.size()));
                int bonusDamage = (int) (rawDamage * 0.50);
                applyDamageToUnit(rogueTarget, bonusDamage);
                result.append(" Sneak attack on ").append(rogueTarget.getName())
                      .append(" for ").append(bonusDamage).append(".");
            }
        }

        return result.toString();
    }

    public String processDefend(BattleUnit unit) {
        unit.setCurrentHealth(Math.min(unit.getCurrentHealth() + 10, unit.getMaxHealth()));
        unit.setCurrentMana(Math.min(unit.getCurrentMana() + 5, unit.getMaxMana()));
        return unit.getName() + " defends. +10 HP, +5 mana.";
    }

    // protect: shield all allies for 10% of each hero's max health
    public String processProtect(BattleUnit caster, List<BattleUnit> allUnits) {
        boolean isHeretic = "HERETIC".equals(caster.getHybridClass());
        boolean isProphet = "PROPHET".equals(caster.getHybridClass());
        int cost = 25;
        caster.setCurrentMana(caster.getCurrentMana() - cost);

        List<BattleUnit> allies = getAllies(caster, allUnits);
        double shieldPercent = isProphet ? 0.20 : 0.10;

        for (BattleUnit ally : allies) {
            int shieldAmount = (int) (ally.getMaxHealth() * shieldPercent);
            ally.setShield(ally.getShield() + shieldAmount);
        }

        String shieldType = isHeretic ? "Fire Shield" : "Protect";
        return caster.getName() + " casts " + shieldType + " on all allies.";
    }

    // heal: heal hero with lowest HP for 25% of their max health
    public String processHeal(BattleUnit caster, List<BattleUnit> allUnits) {
        boolean isPriest = "PRIEST".equals(caster.getHybridClass());
        boolean isProphet = "PROPHET".equals(caster.getHybridClass());
        int cost = 35;
        caster.setCurrentMana(caster.getCurrentMana() - cost);

        double healPercent = isProphet ? 0.50 : 0.25;

        if (isPriest) {
            // heal all allies
            List<BattleUnit> allies = getAllies(caster, allUnits);
            for (BattleUnit ally : allies) {
                int healAmount = (int) (ally.getMaxHealth() * healPercent);
                ally.setCurrentHealth(Math.min(ally.getCurrentHealth() + healAmount, ally.getMaxHealth()));
            }
            return caster.getName() + " casts Heal on all allies.";
        } else {
            // heal the ally with the lowest current HP
            BattleUnit lowestHp = findLowestHpAlly(caster, allUnits);
            if (lowestHp != null) {
                int healAmount = (int) (lowestHp.getMaxHealth() * healPercent);
                lowestHp.setCurrentHealth(Math.min(lowestHp.getCurrentHealth() + healAmount, lowestHp.getMaxHealth()));
                return caster.getName() + " heals " + lowestHp.getName() + " for " + healAmount + " HP.";
            }
            return caster.getName() + " casts Heal but no valid target.";
        }
    }

    // fireball: attack up to 3 enemies
    public String processFireball(BattleUnit caster, BattleUnit primaryTarget, List<BattleUnit> allUnits) {
        boolean isSorcerer = "SORCERER".equals(caster.getHybridClass());
        int cost = 30;
        caster.setCurrentMana(caster.getCurrentMana() - cost);

        List<BattleUnit> enemies = getEnemies(caster, allUnits);
        int maxTargets = Math.min(3, enemies.size());
        int damageMultiplier = isSorcerer ? 2 : 1;

        StringBuilder result = new StringBuilder();
        result.append(caster.getName()).append(" casts Fireball!");

        List<BattleUnit> targets = new ArrayList<>();
        targets.add(primaryTarget);

        for (BattleUnit enemy : enemies) {
            if (enemy != primaryTarget && targets.size() < maxTargets) {
                targets.add(enemy);
            }
        }

        for (BattleUnit target : targets) {
            int damage = Math.max(0, caster.getAttack() - target.getDefense()) * damageMultiplier;
            applyDamageToUnit(target, damage);
            result.append(" ").append(target.getName()).append(" takes ").append(damage).append(" damage.");
        }

        return result.toString();
    }

    // chain lightning: hits all enemies, 100% to first, 25% (or 50% for invoker) to each subsequent
    public String processChainLightning(BattleUnit caster, BattleUnit primaryTarget, List<BattleUnit> allUnits) {
        boolean isInvoker = "INVOKER".equals(caster.getHybridClass());
        int cost = 40;
        caster.setCurrentMana(caster.getCurrentMana() - cost);

        double falloffRate = isInvoker ? 0.50 : 0.25;

        List<BattleUnit> enemies = getEnemies(caster, allUnits);
        enemies.remove(primaryTarget);
        Collections.shuffle(enemies, random);
        enemies.add(0, primaryTarget);

        int baseDamage = Math.max(0, caster.getAttack() - primaryTarget.getDefense());
        double currentDamage = baseDamage;

        StringBuilder result = new StringBuilder();
        result.append(caster.getName()).append(" casts Chain Lightning!");

        for (int i = 0; i < enemies.size(); i++) {
            BattleUnit target = enemies.get(i);
            int dmg = (int) currentDamage;
            applyDamageToUnit(target, dmg);
            result.append(" ").append(target.getName()).append(": ").append(dmg).append(" dmg.");
            currentDamage = currentDamage * falloffRate;
        }

        return result.toString();
    }

    // berserker attack: attack main target + 2 more for 25% damage
    public String processBerserkerAttack(BattleUnit caster, BattleUnit primaryTarget, List<BattleUnit> allUnits) {
        boolean isKnight = "KNIGHT".equals(caster.getHybridClass());
        boolean isPaladin = "PALADIN".equals(caster.getHybridClass());
        int cost = 60;
        caster.setCurrentMana(caster.getCurrentMana() - cost);

        StringBuilder result = new StringBuilder();

        // paladin heals self before attacking
        if (isPaladin) {
            int healAmount = (int) (caster.getMaxHealth() * 0.10);
            caster.setCurrentHealth(Math.min(caster.getCurrentHealth() + healAmount, caster.getMaxHealth()));
            result.append(caster.getName()).append(" heals self for ").append(healAmount).append(" HP. ");
        }

        int mainDamage = Math.max(0, caster.getAttack() - primaryTarget.getDefense());
        applyDamageToUnit(primaryTarget, mainDamage);
        result.append(caster.getName()).append(" berserker attacks ").append(primaryTarget.getName())
              .append(" for ").append(mainDamage).append(".");

        if (isKnight && random.nextDouble() < 0.50) {
            primaryTarget.setStunned(true);
            result.append(" ").append(primaryTarget.getName()).append(" is stunned!");
        }

        // hit up to 2 more enemies for 25% damage
        List<BattleUnit> enemies = getEnemies(caster, allUnits);
        int bonusCount = 0;
        for (BattleUnit enemy : enemies) {
            if (enemy != primaryTarget && enemy.isAlive() && bonusCount < 2) {
                int bonusDmg = (int) (mainDamage * 0.25);
                applyDamageToUnit(enemy, bonusDmg);
                result.append(" ").append(enemy.getName()).append(": ").append(bonusDmg).append(" splash.");

                if (isKnight && random.nextDouble() < 0.50) {
                    enemy.setStunned(true);
                    result.append(" Stunned!");
                }
                bonusCount++;
            }
        }

        return result.toString();
    }

    // replenish: +30 mana to allies, +60 to self
    public String processReplenish(BattleUnit caster, List<BattleUnit> allUnits) {
        boolean isProphet = "PROPHET".equals(caster.getHybridClass());
        boolean isWizard = "WIZARD".equals(caster.getHybridClass());
        int cost = isWizard ? 40 : 80;
        caster.setCurrentMana(caster.getCurrentMana() - cost);

        int allyMana = isProphet ? 60 : 30;
        int selfMana = isProphet ? 120 : 60;

        List<BattleUnit> allies = getAllies(caster, allUnits);
        for (BattleUnit ally : allies) {
            if (ally == caster) {
                ally.setCurrentMana(Math.min(ally.getCurrentMana() + selfMana, ally.getMaxMana()));
            } else {
                ally.setCurrentMana(Math.min(ally.getCurrentMana() + allyMana, ally.getMaxMana()));
            }
        }

        return caster.getName() + " casts Replenish. Allies gain " + allyMana + " mana, self gains " + selfMana + ".";
    }

    // apply damage considering shields
    private void applyDamageToUnit(BattleUnit target, int damage) {
        if (damage <= 0) return;

        int remainingDamage = damage;

        // absorb with shield first
        if (target.getShield() > 0) {
            if (target.getShield() >= remainingDamage) {
                target.setShield(target.getShield() - remainingDamage);
                remainingDamage = 0;
            } else {
                remainingDamage -= target.getShield();
                target.setShield(0);
            }
        }

        target.setCurrentHealth(target.getCurrentHealth() - remainingDamage);
        if (target.getCurrentHealth() <= 0) {
            target.setCurrentHealth(0);
            target.setAlive(false);
        }
    }

    // fire shield reflects 10% damage back
    public int getFireShieldReflect(BattleUnit target, int damage) {
        if (target.getShield() > 0) {
            return (int) (damage * 0.10);
        }
        return 0;
    }

    public List<BattleUnit> getAllies(BattleUnit unit, List<BattleUnit> allUnits) {
        List<BattleUnit> allies = new ArrayList<>();
        for (BattleUnit u : allUnits) {
            if (u.getTeam() == unit.getTeam() && u.isAlive()) {
                allies.add(u);
            }
        }
        return allies;
    }

    public List<BattleUnit> getEnemies(BattleUnit unit, List<BattleUnit> allUnits) {
        List<BattleUnit> enemies = new ArrayList<>();
        for (BattleUnit u : allUnits) {
            if (u.getTeam() != unit.getTeam() && u.isAlive()) {
                enemies.add(u);
            }
        }
        return enemies;
    }

    private BattleUnit findLowestHpAlly(BattleUnit caster, List<BattleUnit> allUnits) {
        BattleUnit lowest = null;
        for (BattleUnit u : allUnits) {
            if (u.getTeam() == caster.getTeam() && u.isAlive()) {
                if (lowest == null || u.getCurrentHealth() < lowest.getCurrentHealth()) {
                    lowest = u;
                }
            }
        }
        return lowest;
    }

    // check if one team has been wiped out
    public boolean isTeamDefeated(int team, List<BattleUnit> allUnits) {
        for (BattleUnit u : allUnits) {
            if (u.getTeam() == team && u.isAlive()) {
                return false;
            }
        }
        return true;
    }
}
