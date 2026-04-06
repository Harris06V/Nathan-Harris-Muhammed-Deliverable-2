package com.legendsofsw.partyservice.service;

import com.legendsofsw.partyservice.model.InventoryItem;
import com.legendsofsw.partyservice.model.ItemType;
import com.legendsofsw.partyservice.model.Party;
import com.legendsofsw.partyservice.repository.HeroRepository;
import com.legendsofsw.partyservice.repository.InventoryItemRepository;
import com.legendsofsw.partyservice.repository.PartyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PartyServiceTest {

    @Mock
    private PartyRepository partyRepo;

    @Mock
    private InventoryItemRepository inventoryRepo;

    @Mock
    private HeroRepository heroRepo;

    private HeroService heroService;
    private PartyService partyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        heroService = new HeroService(heroRepo);
        partyService = new PartyService(partyRepo, inventoryRepo, heroService);
    }

    @Test
    void createParty() {
        Party party = new Party(1L);
        party.setId(1L);
        when(partyRepo.save(any(Party.class))).thenReturn(party);

        Party result = partyService.createParty(1L);

        assertEquals(1L, result.getOwnerId());
        assertEquals(0, result.getGold());
    }

    @Test
    void addGold() {
        Party party = new Party(1L);
        party.setId(1L);
        party.setGold(100);
        when(partyRepo.findById(1L)).thenReturn(Optional.of(party));
        when(partyRepo.save(any(Party.class))).thenAnswer(i -> i.getArgument(0));

        Party result = partyService.addGold(1L, 500);

        assertEquals(600, result.getGold());
    }

    @Test
    void removeGoldNotEnoughThrows() {
        Party party = new Party(1L);
        party.setId(1L);
        party.setGold(50);
        when(partyRepo.findById(1L)).thenReturn(Optional.of(party));

        assertThrows(RuntimeException.class,
                () -> partyService.removeGold(1L, 100));
    }

    @Test
    void buyItemSuccess() {
        Party party = new Party(1L);
        party.setId(1L);
        party.setGold(1000);
        when(partyRepo.findById(1L)).thenReturn(Optional.of(party));
        when(partyRepo.save(any(Party.class))).thenAnswer(i -> i.getArgument(0));
        when(inventoryRepo.findByPartyIdAndItemType(1L, ItemType.BREAD)).thenReturn(Optional.empty());
        when(inventoryRepo.save(any(InventoryItem.class))).thenAnswer(i -> i.getArgument(0));

        InventoryItem item = partyService.buyItem(1L, ItemType.BREAD);

        assertEquals(1, item.getQuantity());
        assertEquals(800, party.getGold());
    }

    @Test
    void buyItemNotEnoughGoldThrows() {
        Party party = new Party(1L);
        party.setId(1L);
        party.setGold(100);
        when(partyRepo.findById(1L)).thenReturn(Optional.of(party));

        assertThrows(RuntimeException.class,
                () -> partyService.buyItem(1L, ItemType.STEAK));
    }

    @Test
    void loseGoldPercent() {
        Party party = new Party(1L);
        party.setId(1L);
        party.setGold(1000);
        when(partyRepo.findById(1L)).thenReturn(Optional.of(party));
        when(partyRepo.save(any(Party.class))).thenAnswer(i -> i.getArgument(0));

        Party result = partyService.loseGoldPercent(1L, 0.10);

        assertEquals(900, result.getGold());
    }
}
