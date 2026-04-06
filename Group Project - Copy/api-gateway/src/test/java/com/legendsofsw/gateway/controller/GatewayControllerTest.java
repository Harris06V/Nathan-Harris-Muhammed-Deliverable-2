package com.legendsofsw.gateway.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GatewayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("api-gateway"));
    }

    @Test
    void playerRouteReturnsServiceUnavailableWhenBackendDown() throws Exception {
        // backend services are not running during unit tests
        // so gateway should return 503 Service Unavailable
        mockMvc.perform(get("/api/players/1"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void partyRouteReturnsServiceUnavailableWhenBackendDown() throws Exception {
        mockMvc.perform(get("/api/parties/1"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void heroRouteReturnsServiceUnavailableWhenBackendDown() throws Exception {
        mockMvc.perform(get("/api/heroes/1"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void battleRouteReturnsServiceUnavailableWhenBackendDown() throws Exception {
        mockMvc.perform(get("/api/battles/1"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void campaignRouteReturnsServiceUnavailableWhenBackendDown() throws Exception {
        mockMvc.perform(get("/api/campaigns/1"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void pvpRouteReturnsServiceUnavailableWhenBackendDown() throws Exception {
        mockMvc.perform(get("/api/pvp/matches/1"))
                .andExpect(status().isServiceUnavailable());
    }
}
