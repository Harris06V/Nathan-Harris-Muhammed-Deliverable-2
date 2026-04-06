package com.legendsofsw.partyservice.repository;

import com.legendsofsw.partyservice.model.Hero;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HeroRepository extends JpaRepository<Hero, Long> {

    List<Hero> findByPartyId(Long partyId);

    int countByPartyId(Long partyId);
}
