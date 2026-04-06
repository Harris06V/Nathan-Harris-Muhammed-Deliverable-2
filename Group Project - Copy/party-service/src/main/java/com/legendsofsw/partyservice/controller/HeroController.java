package com.legendsofsw.partyservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.legendsofsw.partyservice.dto.CreateHeroRequest;
import com.legendsofsw.partyservice.dto.LevelUpRequest;
import com.legendsofsw.partyservice.model.Hero;
import com.legendsofsw.partyservice.service.HeroService;

@RestController
@RequestMapping("/api/heroes")
public class HeroController {

    private final HeroService heroService;

    public HeroController(HeroService heroService) {
        this.heroService = heroService;
    }

    @PostMapping("/party/{partyId}")
    public ResponseEntity<?> createHero(@PathVariable Long partyId, @RequestBody CreateHeroRequest request) {
        try {
            int count = heroService.countHeroesInParty(partyId);
            if (count >= 5) {
                return ResponseEntity.badRequest().body(Map.of("error", "Party already has 5 heroes"));
            }
            Hero hero = heroService.createHero(partyId, request);
            return ResponseEntity.ok(hero);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{heroId}")
    public ResponseEntity<?> getHero(@PathVariable Long heroId) {
        try {
            return ResponseEntity.ok(heroService.getHero(heroId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/party/{partyId}")
    public ResponseEntity<List<Hero>> getHeroesByParty(@PathVariable Long partyId) {
        return ResponseEntity.ok(heroService.getHeroesByParty(partyId));
    }

    @PostMapping("/{heroId}/level-up")
    public ResponseEntity<?> levelUp(@PathVariable Long heroId, @RequestBody LevelUpRequest request) {
        try {
            Hero hero = heroService.levelUp(heroId, request);
            return ResponseEntity.ok(hero);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{heroId}/add-experience")
    public ResponseEntity<Hero> addExperience(@PathVariable Long heroId, @RequestBody Map<String, Integer> body) {
        Hero hero = heroService.addExperience(heroId, body.get("amount"));
        return ResponseEntity.ok(hero);
    }

    @PostMapping("/{heroId}/lose-experience")
    public ResponseEntity<Hero> loseExperience(@PathVariable Long heroId) {
        Hero hero = heroService.loseExperience(heroId, 0);
        return ResponseEntity.ok(hero);
    }

    @PostMapping("/{heroId}/revive")
    public ResponseEntity<Hero> reviveHero(@PathVariable Long heroId) {
        return ResponseEntity.ok(heroService.reviveHero(heroId));
    }

    @DeleteMapping("/{heroId}")
    public ResponseEntity<?> deleteHero(@PathVariable Long heroId) {
        heroService.deleteHero(heroId);
        return ResponseEntity.ok(Map.of("message", "Hero deleted"));
    }
}
