package com.legendsofsw.partyservice.repository;

import com.legendsofsw.partyservice.model.InventoryItem;
import com.legendsofsw.partyservice.model.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findByPartyId(Long partyId);

    Optional<InventoryItem> findByPartyIdAndItemType(Long partyId, ItemType itemType);
}
