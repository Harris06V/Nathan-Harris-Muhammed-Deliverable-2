package com.legendsofsw.pvpservice.repository;

import com.legendsofsw.pvpservice.model.PvpMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PvpMatchRepository extends JpaRepository<PvpMatch, Long> {

    List<PvpMatch> findByPlayer1IdOrPlayer2Id(Long player1Id, Long player2Id);
}
