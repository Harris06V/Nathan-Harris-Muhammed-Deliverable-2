package com.legendsofsw.partyservice.controller;

import com.legendsofsw.partyservice.dto.UseItemRequest;
import com.legendsofsw.partyservice.model.Hero;
import com.legendsofsw.partyservice.model.InventoryItem;
import com.legendsofsw.partyservice.model.ItemType;
import com.legendsofsw.partyservice.model.Party;
import com.legendsofsw.partyservice.service.HeroService;
import com.legendsofsw.partyservice.service.PartyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parties")
public class PartyController {

    private final PartyService partyService;
    private final HeroService heroService;

    public PartyController(PartyService partyService, HeroService heroService) {
        this.partyService = partyService;
        this.heroService = heroService;
    }

    @PostMapping
    public ResponseEntity<Party> createParty(@RequestBody Map<String, Long> body) {
        Party party = partyService.createParty(body.get("ownerId"));
        return ResponseEntity.ok(party);
    }

    @GetMapping("/{partyId}")
    public ResponseEntity<?> getParty(@PathVariable Long partyId) {
        try {
            Party party = partyService.getParty(partyId);
            List<Hero> heroes = heroService.getHeroesByParty(partyId);
            List<InventoryItem> inventory = partyService.getInventory(partyId);
            return ResponseEntity.ok(Map.of(
                    "party", party,
                    "heroes", heroes,
                    "inventory", inventory
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Party>> getPartiesByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(partyService.getPartiesByOwner(ownerId));
    }

    @PostMapping("/{partyId}/gold")
    public ResponseEntity<?> addGold(@PathVariable Long partyId, @RequestBody Map<String, Integer> body) {
        try {
            Party party = partyService.addGold(partyId, body.get("amount"));
            return ResponseEntity.ok(party);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{partyId}/buy")
    public ResponseEntity<?> buyItem(@PathVariable Long partyId, @RequestBody Map<String, String> body) {
        try {
            ItemType itemType = ItemType.valueOf(body.get("itemType").toUpperCase());
            InventoryItem item = partyService.buyItem(partyId, itemType);
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{partyId}/use-item")
    public ResponseEntity<?> useItem(@PathVariable Long partyId, @RequestBody UseItemRequest request) {
        try {
            Hero hero = partyService.useItem(partyId, request);
            return ResponseEntity.ok(hero);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{partyId}/inventory")
    public ResponseEntity<List<InventoryItem>> getInventory(@PathVariable Long partyId) {
        return ResponseEntity.ok(partyService.getInventory(partyId));
    }

    @GetMapping("/{partyId}/cumulative-level")
    public ResponseEntity<Map<String, Integer>> getCumulativeLevel(@PathVariable Long partyId) {
        return ResponseEntity.ok(Map.of("cumulativeLevel", partyService.getCumulativeLevel(partyId)));
    }

    @DeleteMapping("/{partyId}")
    public ResponseEntity<?> deleteParty(@PathVariable Long partyId) {
        try {
            partyService.deleteParty(partyId);
            return ResponseEntity.ok(Map.of("message", "Party deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
