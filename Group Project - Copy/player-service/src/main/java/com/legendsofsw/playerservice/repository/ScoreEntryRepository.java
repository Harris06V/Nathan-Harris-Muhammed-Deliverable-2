package com.legendsofsw.playerservice.repository;

import com.legendsofsw.playerservice.model.ScoreEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScoreEntryRepository extends JpaRepository<ScoreEntry, Long> {

    List<ScoreEntry> findTop10ByOrderByScoreDesc();

    List<ScoreEntry> findByPlayerIdOrderByScoreDesc(Long playerId);
}
