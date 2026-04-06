package com.legendsofsw.battleservice.controller;

import com.legendsofsw.battleservice.dto.ActionRequest;
import com.legendsofsw.battleservice.dto.BattleRequest;
import com.legendsofsw.battleservice.dto.BattleStateResponse;
import com.legendsofsw.battleservice.service.BattleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/battles")
public class BattleController {

    private final BattleService battleService;

    public BattleController(BattleService battleService) {
        this.battleService = battleService;
    }

    @PostMapping
    public ResponseEntity<BattleStateResponse> createBattle(@RequestBody BattleRequest request) {
        BattleStateResponse response = battleService.createBattle(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{battleId}")
    public ResponseEntity<?> getBattleState(@PathVariable Long battleId) {
        try {
            BattleStateResponse response = battleService.getBattleState(battleId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{battleId}/action")
    public ResponseEntity<?> performAction(@PathVariable Long battleId, @RequestBody ActionRequest request) {
        try {
            BattleStateResponse response = battleService.performAction(battleId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
