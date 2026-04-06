package com.legendsofsw.battleservice.controller;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.legendsofsw.battleservice.dto.ActionRequest;
import com.legendsofsw.battleservice.dto.BattleRequest;
import com.legendsofsw.battleservice.dto.BattleStateResponse;
import com.legendsofsw.battleservice.service.BattleService;

class BattleControllerTest {

    private BattleStateResponse makeBattleResponse() {
        BattleStateResponse resp = new BattleStateResponse();
        resp.setBattleId(1L);
        resp.setStatus("IN_PROGRESS");
        resp.setTurnNumber(1);
        resp.setTeam1(List.of());
        resp.setTeam2(List.of());
        resp.setCurrentUnitIndex(0);
        return resp;
    }

    private BattleService stubService(BattleStateResponse response,
                                      boolean throwOnGet, boolean throwOnAction) {
        return new BattleService(null, null) {
            @Override public BattleStateResponse createBattle(BattleRequest r) { return response; }
            @Override public BattleStateResponse getBattleState(Long id) {
                if (throwOnGet) throw new RuntimeException("Not found");
                return response;
            }
            @Override public BattleStateResponse performAction(Long id, ActionRequest r) {
                if (throwOnAction) throw new RuntimeException("Battle is already over");
                return response;
            }
        };
    }

    @Test
    void createBattleReturnsOk() {
        BattleController ctrl = new BattleController(stubService(makeBattleResponse(), false, false));
        ResponseEntity<BattleStateResponse> resp = ctrl.createBattle(new BattleRequest());
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(1L, resp.getBody().getBattleId());
        assertEquals("IN_PROGRESS", resp.getBody().getStatus());
    }

    @Test
    void getBattleStateReturnsOk() {
        BattleController ctrl = new BattleController(stubService(makeBattleResponse(), false, false));
        ResponseEntity<?> resp = ctrl.getBattleState(1L);
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void getBattleNotFoundReturns404() {
        BattleController ctrl = new BattleController(stubService(null, true, false));
        ResponseEntity<?> resp = ctrl.getBattleState(99L);
        assertEquals(404, resp.getStatusCode().value());
    }

    @Test
    void performActionReturnsOk() {
        BattleStateResponse response = makeBattleResponse();
        response.setLastActionResult("Hero attacks Enemy for 10 damage.");
        BattleController ctrl = new BattleController(stubService(response, false, false));
        ResponseEntity<?> resp = ctrl.performAction(1L, new ActionRequest());
        assertEquals(200, resp.getStatusCode().value());
    }

    @Test
    void performActionOnFinishedBattleReturnsBadRequest() {
        BattleController ctrl = new BattleController(stubService(null, false, true));
        ResponseEntity<?> resp = ctrl.performAction(1L, new ActionRequest());
        assertEquals(400, resp.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) resp.getBody();
        assertEquals("Battle is already over", body.get("error"));
    }

    @Test
    void performActionWaitReturnsOk() {
        BattleStateResponse response = makeBattleResponse();
        response.setLastActionResult("Hero waits.");
        BattleController ctrl = new BattleController(stubService(response, false, false));
        ResponseEntity<?> resp = ctrl.performAction(1L, new ActionRequest());
        assertEquals(200, resp.getStatusCode().value());
    }
}
