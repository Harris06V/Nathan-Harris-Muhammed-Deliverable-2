package com.legendsofsw.battleservice.repository;

import com.legendsofsw.battleservice.model.Battle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BattleRepository extends JpaRepository<Battle, Long> {
}
