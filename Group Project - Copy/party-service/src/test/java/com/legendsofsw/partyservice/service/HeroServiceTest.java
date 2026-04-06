package com.legendsofsw.partyservice.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.legendsofsw.partyservice.dto.CreateHeroRequest;
import com.legendsofsw.partyservice.dto.LevelUpRequest;
import com.legendsofsw.partyservice.model.Hero;
import com.legendsofsw.partyservice.model.HeroClass;
import com.legendsofsw.partyservice.model.HybridClass;
import com.legendsofsw.partyservice.repository.HeroRepository;

class HeroServiceTest {

    @Mock
    private HeroRepository heroRepo;

    @InjectMocks
    private HeroService heroService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createHeroWithWarriorClass() {
        when(heroRepo.save(any(Hero.class))).thenAnswer(i -> {
            Hero h = i.getArgument(0);
            h.setId(1L);
            return h;
        });

        Hero hero = heroService.createHero(1L, new CreateHeroRequest("Knight Bob", "WARRIOR"));

        assertEquals("Knight Bob", hero.getName());
        assertEquals(1, hero.getLevel());
        assertEquals(1, hero.getWarriorLevel());
        // base 5 + 1 (base per level) + 2 (warrior bonus)
        assertEquals(8, hero.getAttack());
        // base 5 + 1 + 3
        assertEquals(9, hero.getDefense());
    }

    @Test
    void createHeroWithOrderClass() {
        when(heroRepo.save(any(Hero.class))).thenAnswer(i -> {
            Hero h = i.getArgument(0);
            h.setId(1L);
            return h;
        });

        Hero hero = heroService.createHero(1L, new CreateHeroRequest("Healer Amy", "ORDER"));

        assertEquals(1, hero.getOrderLevel());
        // base 5 + 1 (level) + 0 (order attack bonus)
        assertEquals(6, hero.getAttack());
        // base 5 + 1 + 2 (order defense bonus)
        assertEquals(8, hero.getDefense());
        // base 50 + 2 + 5 (order mana bonus)
        assertEquals(57, hero.getMaxMana());
    }

    @Test
    void heroStartsWith100HP() {
        when(heroRepo.save(any(Hero.class))).thenAnswer(i -> {
            Hero h = i.getArgument(0);
            h.setId(1L);
            return h;
        });

        Hero hero = heroService.createHero(1L, new CreateHeroRequest("Test", "MAGE"));

        // base 100 + 5 (level HP) + 0 
        assertEquals(105, hero.getMaxHealth());
    }

    @Test
    void levelUpIncreasesStats() {
        Hero hero = Hero.createNew(1L, "Fighter", HeroClass.WARRIOR);
        hero.setId(1L);
        hero.setExperience(99999);
        when(heroRepo.findById(1L)).thenReturn(Optional.of(hero));
        when(heroRepo.save(any(Hero.class))).thenAnswer(i -> i.getArgument(0));

        int attackBefore = hero.getAttack();
        heroService.levelUp(1L, new LevelUpRequest("WARRIOR"));

        assertEquals(2, hero.getLevel());
        assertTrue(hero.getAttack() > attackBefore);
    }

    @Test
    void levelUpMaxLevelThrows() {
        Hero hero = Hero.createNew(1L, "Max", HeroClass.CHAOS);
        hero.setId(1L);
        hero.setLevel(20);
        when(heroRepo.findById(1L)).thenReturn(Optional.of(hero));

        assertThrows(RuntimeException.class,
                () -> heroService.levelUp(1L, new LevelUpRequest("CHAOS")));
    }

    @Test
    void levelUpNotEnoughExpThrows() {
        Hero hero = Hero.createNew(1L, "Low", HeroClass.CHAOS);
        hero.setId(1L);
        hero.setExperience(0);
        when(heroRepo.findById(1L)).thenReturn(Optional.of(hero));

        assertThrows(RuntimeException.class,
                () -> heroService.levelUp(1L, new LevelUpRequest("CHAOS")));
    }

    @Test
    void specializationAtLevel5() {
        Hero hero = Hero.createNew(1L, "Specialist", HeroClass.WARRIOR);

        // level warrior class to 5 
        for (int i = 0; i < 4; i++) {
            hero.setLevel(hero.getLevel() + 1);
            hero.addClassLevel(HeroClass.WARRIOR);
        }

        assertEquals(5, hero.getWarriorLevel());
        assertEquals(HeroClass.WARRIOR, hero.getSpecialization());
        assertEquals(HybridClass.NONE, hero.getHybridClass());
    }

    @Test
    void hybridClassAtTwoLevel5s() {
        Hero hero = Hero.createNew(1L, "Hybrid", HeroClass.WARRIOR);

        // get warrior to level 5
        for (int i = 0; i < 4; i++) {
            hero.setLevel(hero.getLevel() + 1);
            hero.addClassLevel(HeroClass.WARRIOR);
        }
        assertEquals(HeroClass.WARRIOR, hero.getSpecialization());

        // get mage to level 5
        for (int i = 0; i < 5; i++) {
            hero.setLevel(hero.getLevel() + 1);
            hero.addClassLevel(HeroClass.MAGE);
        }

        assertEquals(HybridClass.WARLOCK, hero.getHybridClass());
        assertNull(hero.getSpecialization());
    }

    @Test
    void experienceCalculation() {
        // Exp(L) = sum of (500 + 75*l + 20*l^2) for l=2..L
        int expToLevel2 = Hero.calcExpToLevel(2);
        // 500 + 75*2 + 20*4 = 500 + 150 + 80 = 730
        assertEquals(730, expToLevel2);

        int expToLevel3 = Hero.calcExpToLevel(3);
        // 730 + (500 + 75*3 + 20*9) = 730 + 500 + 225 + 180 = 1635
        assertEquals(1635, expToLevel3);
    }

    @Test
    void reviveHero() {
        Hero hero = Hero.createNew(1L, "Dead", HeroClass.CHAOS);
        hero.setId(1L);
        hero.setAlive(false);
        hero.setCurrentHealth(0);
        when(heroRepo.findById(1L)).thenReturn(Optional.of(hero));
        when(heroRepo.save(any(Hero.class))).thenAnswer(i -> i.getArgument(0));

        Hero result = heroService.reviveHero(1L);

        assertTrue(result.isAlive());
        assertEquals(result.getMaxHealth(), result.getCurrentHealth());
    }
}
