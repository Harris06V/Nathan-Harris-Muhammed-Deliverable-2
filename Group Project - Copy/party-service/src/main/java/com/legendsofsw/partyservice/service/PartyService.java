package com.legendsofsw.partyservice.service;

import com.legendsofsw.partyservice.dto.UseItemRequest;
import com.legendsofsw.partyservice.model.Hero;
import com.legendsofsw.partyservice.model.InventoryItem;
import com.legendsofsw.partyservice.model.ItemType;
import com.legendsofsw.partyservice.model.Party;
import com.legendsofsw.partyservice.repository.InventoryItemRepository;
import com.legendsofsw.partyservice.repository.PartyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartyService {

    private final PartyRepository partyRepo;
    private final InventoryItemRepository inventoryRepo;
    private final HeroService heroService;

    public PartyService(PartyRepository partyRepo, InventoryItemRepository inventoryRepo,
                        HeroService heroService) {
        this.partyRepo = partyRepo;
        this.inventoryRepo = inventoryRepo;
        this.heroService = heroService;
    }

    public Party createParty(Long ownerId) {
        Party party = new Party(ownerId);
        return partyRepo.save(party);
    }

    public Party getParty(Long partyId) {
        return partyRepo.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found"));
    }

    public List<Party> getPartiesByOwner(Long ownerId) {
        return partyRepo.findByOwnerId(ownerId);
    }

    public Party addGold(Long partyId, int amount) {
        Party party = getParty(partyId);
        party.setGold(party.getGold() + amount);
        return partyRepo.save(party);
    }

    public Party removeGold(Long partyId, int amount) {
        Party party = getParty(partyId);
        if (party.getGold() < amount) {
            throw new RuntimeException("Not enough gold");
        }
        party.setGold(party.getGold() - amount);
        return partyRepo.save(party);
    }

    public Party loseGoldPercent(Long partyId, double percent) {
        Party party = getParty(partyId);
        int loss = (int) (party.getGold() * percent);
        party.setGold(party.getGold() - loss);
        return partyRepo.save(party);
    }

    // buying items from inn
    public InventoryItem buyItem(Long partyId, ItemType itemType) {
        Party party = getParty(partyId);
        int cost = itemType.getCost();

        if (party.getGold() < cost) {
            throw new RuntimeException("Not enough gold to buy " + itemType.name());
        }

        party.setGold(party.getGold() - cost);
        partyRepo.save(party);

        // add to inventory or increment quantity
        InventoryItem item = inventoryRepo.findByPartyIdAndItemType(partyId, itemType)
                .orElse(new InventoryItem(partyId, itemType, 0));
        item.setQuantity(item.getQuantity() + 1);
        return inventoryRepo.save(item);
    }

    // use an item on a hero
    public Hero useItem(Long partyId, UseItemRequest request) {
        ItemType itemType = ItemType.valueOf(request.getItemType().toUpperCase());

        InventoryItem item = inventoryRepo.findByPartyIdAndItemType(partyId, itemType)
                .orElseThrow(() -> new RuntimeException("Item not in inventory"));

        if (item.getQuantity() <= 0) {
            throw new RuntimeException("No " + itemType.name() + " left");
        }

        Hero hero = heroService.getHero(request.getHeroId());
        hero.useItem(itemType);
        heroService.saveHero(hero);

        item.setQuantity(item.getQuantity() - 1);
        if (item.getQuantity() == 0) {
            inventoryRepo.delete(item);
        } else {
            inventoryRepo.save(item);
        }

        return hero;
    }

    public List<InventoryItem> getInventory(Long partyId) {
        return inventoryRepo.findByPartyId(partyId);
    }

    public void deleteParty(Long partyId) {
        List<Hero> heroes = heroService.getHeroesByParty(partyId);
        for (Hero h : heroes) {
            heroService.deleteHero(h.getId());
        }
        List<InventoryItem> items = inventoryRepo.findByPartyId(partyId);
        inventoryRepo.deleteAll(items);
        partyRepo.deleteById(partyId);
    }

    // get cumulative level of all heroes in party
    public int getCumulativeLevel(Long partyId) {
        List<Hero> heroes = heroService.getHeroesByParty(partyId);
        int total = 0;
        for (Hero h : heroes) {
            total += h.getLevel();
        }
        return total;
    }

    public Party saveParty(Party party) {
        return partyRepo.save(party);
    }
}
