package com.legendsofsw.pveservice.repository;

import com.legendsofsw.pveservice.model.Campaign;
import com.legendsofsw.pveservice.model.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByPlayerId(Long playerId);

    Optional<Campaign> findByPlayerIdAndStatus(Long playerId, CampaignStatus status);
}
