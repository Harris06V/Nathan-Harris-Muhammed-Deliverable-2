package com.legendsofsw.partyservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "heroes")
public class Hero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long partyId;

    private String name;

    private int level;
    private int experience;
    private int experienceToNextLevel;

    private int attack;
    private int defense;
    private int currentHealth;
    private int maxHealth;
    private int currentMana;
    private int maxMana;

    // tracks how many levels put into each class
    private int orderLevel;
    private int chaosLevel;
    private int warriorLevel;
    private int mageLevel;

    @Enumerated(EnumType.STRING)
    private HeroClass specialization;

    @Enumerated(EnumType.STRING)
    private HybridClass hybridClass;

    private boolean alive;

    public Hero() {
    }

    // creates a new level 1 hero with a starting class
    public static Hero createNew(Long partyId, String name, HeroClass startingClass) {
        Hero hero = new Hero();
        hero.partyId = partyId;
        hero.name = name;
        hero.level = 1;
        hero.experience = 0;
        hero.attack = 5;
        hero.defense = 5;
        hero.maxHealth = 100;
        hero.currentHealth = 100;
        hero.maxMana = 50;
        hero.currentMana = 50;
        hero.orderLevel = 0;
        hero.chaosLevel = 0;
        hero.warriorLevel = 0;
        hero.mageLevel = 0;
        hero.specialization = null;
        hero.hybridClass = HybridClass.NONE;
        hero.alive = true;

        // put the first level into the chosen class
        hero.addClassLevel(startingClass);
        hero.experienceToNextLevel = calcExpToLevel(2);
        return hero;
    }

    // exp needed to reach a given level
    public static int calcExpToLevel(int targetLevel) {
        if (targetLevel <= 1) return 0;
        int total = 0;
        for (int l = 2; l <= targetLevel; l++) {
            total += 500 + 75 * l + 20 * l * l;
        }
        return total;
    }

    public void addClassLevel(HeroClass chosenClass) {
        // base stats per level
        attack += 1;
        defense += 1;
        maxHealth += 5;
        maxMana += 2;

        int[] classBonus = getClassBonus(chosenClass);

        // if we have a specialization and it matches the chosen class 
        boolean doubled = (specialization != null && hybridClass == HybridClass.NONE
                && getSpecializationBaseClass().equals(chosenClass));

        int multiplier = doubled ? 2 : 1;
        attack += classBonus[0] * multiplier;
        defense += classBonus[1] * multiplier;
        maxHealth += classBonus[2] * multiplier;
        maxMana += classBonus[3] * multiplier;

        // increment the  level
        switch (chosenClass) {
            case ORDER -> orderLevel++;
            case CHAOS -> chaosLevel++;
            case WARRIOR -> warriorLevel++;
            case MAGE -> mageLevel++;
        }

        // check for specialization or hybrid
        checkSpecializationAndHybrid();

        // heal to new max on level up
        currentHealth = maxHealth;
        currentMana = maxMana;
    }

    private int[] getClassBonus(HeroClass heroClass) {
        // returns attack, defense, health, mana
        return switch (heroClass) {
            case ORDER -> new int[]{0, 2, 0, 5};
            case CHAOS -> new int[]{3, 0, 5, 0};
            case WARRIOR -> new int[]{2, 3, 0, 0};
            case MAGE -> new int[]{1, 0, 0, 5};
        };
    }

    private void checkSpecializationAndHybrid() {
        int classesAtFive = 0;
        HeroClass first = null;
        HeroClass second = null;

        if (orderLevel >= 5) {
            classesAtFive++;
            if (first == null) first = HeroClass.ORDER;
            else second = HeroClass.ORDER;
        }
        if (chaosLevel >= 5) {
            classesAtFive++;
            if (first == null) first = HeroClass.CHAOS;
            else second = HeroClass.CHAOS;
        }
        if (warriorLevel >= 5) {
            classesAtFive++;
            if (first == null) first = HeroClass.WARRIOR;
            else second = HeroClass.WARRIOR;
        }
        if (mageLevel >= 5) {
            classesAtFive++;
            if (first == null) first = HeroClass.MAGE;
            else second = HeroClass.MAGE;
        }

        if (classesAtFive >= 2 && hybridClass == HybridClass.NONE) {
            hybridClass = determineHybrid(first, second);
            specialization = null;
        } else if (classesAtFive == 1 && specialization == null) {
            specialization = first;
        }
    }

    private HybridClass determineHybrid(HeroClass first, HeroClass second) {
        // same class = specialization hybrid
        if (first == second) {
            return switch (first) {
                case ORDER -> HybridClass.PRIEST;
                case CHAOS -> HybridClass.INVOKER;
                case WARRIOR -> HybridClass.KNIGHT;
                case MAGE -> HybridClass.WIZARD;
            };
        }

        // normalize the pair so order doesn't matter
        if (first.ordinal() > second.ordinal()) {
            HeroClass temp = first;
            first = second;
            second = temp;
        }

        if (first == HeroClass.ORDER && second == HeroClass.CHAOS) return HybridClass.HERETIC;
        if (first == HeroClass.ORDER && second == HeroClass.WARRIOR) return HybridClass.PALADIN;
        if (first == HeroClass.ORDER && second == HeroClass.MAGE) return HybridClass.PROPHET;
        if (first == HeroClass.CHAOS && second == HeroClass.WARRIOR) return HybridClass.ROGUE;
        if (first == HeroClass.CHAOS && second == HeroClass.MAGE) return HybridClass.SORCERER;
        if (first == HeroClass.WARRIOR && second == HeroClass.MAGE) return HybridClass.WARLOCK;

        return HybridClass.NONE;
    }

    private HeroClass getSpecializationBaseClass() {
        if (specialization == null) return null;
        return specialization;
    }

    public int getClassLevel(HeroClass heroClass) {
        return switch (heroClass) {
            case ORDER -> orderLevel;
            case CHAOS -> chaosLevel;
            case WARRIOR -> warriorLevel;
            case MAGE -> mageLevel;
        };
    }

    // apply item effects
    public void useItem(ItemType item) {
        if (item.isReviveAndFull()) {
            alive = true;
            currentHealth = maxHealth;
            currentMana = maxMana;
        } else {
            if (!alive) return;
            currentHealth = Math.min(currentHealth + item.getHpRestore(), maxHealth);
            currentMana = Math.min(currentMana + item.getManaRestore(), maxMana);
        }
    }

    public void reviveAtInn() {
        alive = true;
        currentHealth = maxHealth;
        currentMana = maxMana;
    }

    public void takeDamage(int damage) {
        currentHealth -= damage;
        if (currentHealth <= 0) {
            currentHealth = 0;
            alive = false;
        }
    }



    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPartyId() { return partyId; }
    public void setPartyId(Long partyId) { this.partyId = partyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public int getExperienceToNextLevel() { return experienceToNextLevel; }
    public void setExperienceToNextLevel(int experienceToNextLevel) { this.experienceToNextLevel = experienceToNextLevel; }

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

    public int getOrderLevel() { return orderLevel; }
    public void setOrderLevel(int orderLevel) { this.orderLevel = orderLevel; }

    public int getChaosLevel() { return chaosLevel; }
    public void setChaosLevel(int chaosLevel) { this.chaosLevel = chaosLevel; }

    public int getWarriorLevel() { return warriorLevel; }
    public void setWarriorLevel(int warriorLevel) { this.warriorLevel = warriorLevel; }

    public int getMageLevel() { return mageLevel; }
    public void setMageLevel(int mageLevel) { this.mageLevel = mageLevel; }

    public HeroClass getSpecialization() { return specialization; }
    public void setSpecialization(HeroClass specialization) { this.specialization = specialization; }

    public HybridClass getHybridClass() { return hybridClass; }
    public void setHybridClass(HybridClass hybridClass) { this.hybridClass = hybridClass; }

    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
}
