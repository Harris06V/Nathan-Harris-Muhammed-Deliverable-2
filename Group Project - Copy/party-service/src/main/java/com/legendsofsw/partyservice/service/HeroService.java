package com.legendsofsw.partyservice.service;

import com.legendsofsw.partyservice.dto.CreateHeroRequest;
import com.legendsofsw.partyservice.dto.LevelUpRequest;
import com.legendsofsw.partyservice.model.Hero;
import com.legendsofsw.partyservice.model.HeroClass;
import com.legendsofsw.partyservice.observer.GameObserver;
import com.legendsofsw.partyservice.observer.Subject;
import com.legendsofsw.partyservice.repository.HeroRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HeroService implements Subject {

    private final HeroRepository heroRepo;
    private final List<GameObserver> observers = new ArrayList<>();

    public HeroService(HeroRepository heroRepo) {
        this.heroRepo = heroRepo;
    }

    @Override
    public void attach(GameObserver observer) {
        observers.add(observer);
    }

    @Override
    public void detach(GameObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        // called after specific events with context
    }

    private void notifyLevelUp(String heroName, int newLevel, String className) {
        for (GameObserver observer : observers) {
            observer.onHeroLevelUp(heroName, newLevel, className);
        }
    }

    private void notifyHeroDefeated(String heroName) {
        for (GameObserver observer : observers) {
            observer.onHeroDefeated(heroName);
        }
    }

    public Hero createHero(Long partyId, CreateHeroRequest request) {
        HeroClass heroClass = HeroClass.valueOf(request.getHeroClass().toUpperCase());
        Hero hero = Hero.createNew(partyId, request.getName(), heroClass);
        return heroRepo.save(hero);
    }

    public Hero getHero(Long heroId) {
        return heroRepo.findById(heroId)
                .orElseThrow(() -> new RuntimeException("Hero not found"));
    }

    public List<Hero> getHeroesByParty(Long partyId) {
        return heroRepo.findByPartyId(partyId);
    }

    public Hero levelUp(Long heroId, LevelUpRequest request) {
        Hero hero = getHero(heroId);

        if (hero.getLevel() >= 20) {
            throw new RuntimeException("Hero is already at max level");
        }

        if (hero.getExperience() < hero.getExperienceToNextLevel()) {
            throw new RuntimeException("Not enough experience to level up");
        }

        HeroClass chosenClass = HeroClass.valueOf(request.getChosenClass().toUpperCase());

        hero.setLevel(hero.getLevel() + 1);
        hero.addClassLevel(chosenClass);
        hero.setExperienceToNextLevel(Hero.calcExpToLevel(hero.getLevel() + 1));

        Hero saved = heroRepo.save(hero);
        notifyLevelUp(hero.getName(), hero.getLevel(), chosenClass.name());
        return saved;
    }

    public Hero addExperience(Long heroId, int exp) {
        Hero hero = getHero(heroId);
        hero.setExperience(hero.getExperience() + exp);
        return heroRepo.save(hero);
    }

    public Hero loseExperience(Long heroId, int exp) {
        Hero hero = getHero(heroId);
        int currentLevelExp = hero.getExperience() - Hero.calcExpToLevel(hero.getLevel());
        int loss = (int) (currentLevelExp * 0.30);
        hero.setExperience(Math.max(Hero.calcExpToLevel(hero.getLevel()), hero.getExperience() - loss));
        return heroRepo.save(hero);
    }

    public Hero healHero(Long heroId, int amount) {
        Hero hero = getHero(heroId);
        hero.setCurrentHealth(Math.min(hero.getCurrentHealth() + amount, hero.getMaxHealth()));
        return heroRepo.save(hero);
    }

    public Hero reviveHero(Long heroId) {
        Hero hero = getHero(heroId);
        hero.reviveAtInn();
        return heroRepo.save(hero);
    }

    public void deleteHero(Long heroId) {
        heroRepo.deleteById(heroId);
    }

    public Hero saveHero(Hero hero) {
        return heroRepo.save(hero);
    }

    public int countHeroesInParty(Long partyId) {
        return heroRepo.countByPartyId(partyId);
    }
}
