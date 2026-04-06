package com.legendsofsw.partyservice.controller;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.legendsofsw.partyservice.model.Hero;
import com.legendsofsw.partyservice.model.InventoryItem;
import com.legendsofsw.partyservice.model.Party;
import com.legendsofsw.partyservice.service.HeroService;
import com.legendsofsw.partyservice.service.PartyService;

class PartyControllerTest {

    @Test
    void createPartyReturnsOk() {
        Party party = new Party();
        party.setId(1L);
        PartyService stubParty = new PartyService(null, null, null) {
            @Override public Party createParty(Long ownerId) { return party; }
        };
        PartyController ctrl = new PartyController(stubParty, new HeroService(null));
        ResponseEntity<Party> resp = ctrl.createParty(Map.of("ownerId", 10L));
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(1L, resp.getBody().getId());
    }

    @Test
    void getPartyReturnsOk() {
        Party party = new Party();
        party.setId(1L);
        PartyService stubParty = new PartyService(null, null, null) {
            @Override public Party getParty(Long id) { return party; }
            @Override public List<InventoryItem> getInventory(Long partyId) { return List.of(); }
        };
        HeroService stubHero = new HeroService(null) {
            @Override public List<Hero> getHeroesByParty(Long partyId) { return List.of(); }
        };
        PartyController ctrl = new PartyController(stubParty, stubHero);
        ResponseEntity<?> resp = ctrl.getParty(1L);
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void getPartyNotFoundReturns404() {
        PartyService stubParty = new PartyService(null, null, null) {
            @Override public Party getParty(Long id) { throw new RuntimeException("Not found"); }
        };
        PartyController ctrl = new PartyController(stubParty, new HeroService(null));
        ResponseEntity<?> resp = ctrl.getParty(99L);
        assertEquals(404, resp.getStatusCode().value());
    }

    @Test
    void addGoldReturnsOk() {
        Party party = new Party();
        party.setId(1L);
        PartyService stubParty = new PartyService(null, null, null) {
            @Override public Party addGold(Long partyId, int amount) { return party; }
        };
        PartyController ctrl = new PartyController(stubParty, new HeroService(null));
        ResponseEntity<?> resp = ctrl.addGold(1L, Map.of("amount", 500));
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void getCumulativeLevelReturnsOk() {
        PartyService stubParty = new PartyService(null, null, null) {
            @Override public int getCumulativeLevel(Long partyId) { return 15; }
        };
        PartyController ctrl = new PartyController(stubParty, new HeroService(null));
        ResponseEntity<Map<String, Integer>> resp = ctrl.getCumulativeLevel(1L);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(15, resp.getBody().get("cumulativeLevel"));
    }

    @Test
    void getInventoryReturnsOk() {
        PartyService stubParty = new PartyService(null, null, null) {
            @Override public List<InventoryItem> getInventory(Long partyId) { return List.of(); }
        };
        PartyController ctrl = new PartyController(stubParty, new HeroService(null));
        ResponseEntity<List<InventoryItem>> resp = ctrl.getInventory(1L);
        assertEquals(200, resp.getStatusCode().value());
    }
}
