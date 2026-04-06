package com.legendsofsw.partyservice.repository;

import com.legendsofsw.partyservice.model.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PartyRepository extends JpaRepository<Party, Long> {

    List<Party> findByOwnerId(Long ownerId);
}
