package com.legendsofsw.partyservice.controller;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.legendsofsw.partyservice.dto.CreateHeroRequest;
import com.legendsofsw.partyservice.model.Hero;
import com.legendsofsw.partyservice.service.HeroService;

class HeroControllerTest {

    private Hero makeHero() {
        Hero hero = new Hero();
        hero.setId(1L);
        hero.setName("TestHero");
        hero.setLevel(1);
        return hero;
    }

    @Test
    void createHeroReturnsOk() {
        Hero hero = makeHero();
        HeroService stub = new HeroService(null) {
            @Override public int countHeroesInParty(Long partyId) { return 2; }
            @Override public Hero createHero(Long partyId, CreateHeroRequest r) { return hero; }
        };
        HeroController ctrl = new HeroController(stub);
        ResponseEntity<?> resp = ctrl.createHero(1L, new CreateHeroRequest());
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void getHeroReturnsOk() {
        Hero hero = makeHero();
        HeroService stub = new HeroService(null) {
            @Override public Hero getHero(Long id) { return hero; }
        };
        HeroController ctrl = new HeroController(stub);
        ResponseEntity<?> resp = ctrl.getHero(1L);
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void getHeroNotFoundReturns404() {
        HeroService stub = new HeroService(null) {
            @Override public Hero getHero(Long id) { throw new RuntimeException("Hero not found"); }
        };
        HeroController ctrl = new HeroController(stub);
        ResponseEntity<?> resp = ctrl.getHero(99L);
        assertEquals(404, resp.getStatusCode().value());
    }

    @Test
    void getHeroesByPartyReturnsOk() {
        HeroService stub = new HeroService(null) {
            @Override public List<Hero> getHeroesByParty(Long partyId) { return List.of(makeHero()); }
        };
        HeroController ctrl = new HeroController(stub);
        ResponseEntity<List<Hero>> resp = ctrl.getHeroesByParty(1L);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(1, resp.getBody().size());
    }

    @Test
    void addExperienceReturnsOk() {
        Hero hero = makeHero();
        HeroService stub = new HeroService(null) {
            @Override public Hero addExperience(Long heroId, int amount) { return hero; }
        };
        HeroController ctrl = new HeroController(stub);
        ResponseEntity<Hero> resp = ctrl.addExperience(1L, java.util.Map.of("amount", 100));
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void loseExperienceReturnsOk() {
        Hero hero = makeHero();
        HeroService stub = new HeroService(null) {
            @Override public Hero loseExperience(Long heroId, int amount) { return hero; }
        };
        HeroController ctrl = new HeroController(stub);
        ResponseEntity<Hero> resp = ctrl.loseExperience(1L);
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void reviveHeroReturnsOk() {
        Hero hero = makeHero();
        HeroService stub = new HeroService(null) {
            @Override public Hero reviveHero(Long heroId) { return hero; }
        };
        HeroController ctrl = new HeroController(stub);
        ResponseEntity<Hero> resp = ctrl.reviveHero(1L);
        assertEquals(200, resp.getStatusCode().value());
    }
}
