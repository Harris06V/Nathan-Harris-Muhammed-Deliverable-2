package com.legendsofsw.playerservice.repository;

import com.legendsofsw.playerservice.model.PvpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface PvpRecordRepository extends JpaRepository<PvpRecord, Long> {

    Optional<PvpRecord> findByPlayerId(Long playerId);

    List<PvpRecord> findAllByOrderByWinsDesc();
}
