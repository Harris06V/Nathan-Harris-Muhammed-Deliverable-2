package com.legendsofsw.playerservice.repository;

import com.legendsofsw.playerservice.model.SavedParty;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavedPartyRepository extends JpaRepository<SavedParty, Long> {

    List<SavedParty> findByPlayerId(Long playerId);

    int countByPlayerId(Long playerId);
}
