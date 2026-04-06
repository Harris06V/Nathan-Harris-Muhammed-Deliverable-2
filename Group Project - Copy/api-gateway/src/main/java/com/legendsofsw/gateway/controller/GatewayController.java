package com.legendsofsw.gateway.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;

// routes all incoming requests to the correct backend service based on URL prefix
@RestController
@RequestMapping("/api")
public class GatewayController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gateway.player-service.url}")
    private String playerServiceUrl;

    @Value("${gateway.party-service.url}")
    private String partyServiceUrl;

    @Value("${gateway.battle-service.url}")
    private String battleServiceUrl;

    @Value("${gateway.pve-service.url}")
    private String pveServiceUrl;

    @Value("${gateway.pvp-service.url}")
    private String pvpServiceUrl;

    // player service routes
    @RequestMapping(value = "/players/**", method = {RequestMethod.GET, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyPlayerService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forward(playerServiceUrl, request, body);
    }

    // party service routes
    @RequestMapping(value = "/parties/**", method = {RequestMethod.GET, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyPartyService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forward(partyServiceUrl, request, body);
    }

    // hero routes go to party service
    @RequestMapping(value = "/heroes/**", method = {RequestMethod.GET, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyHeroService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forward(partyServiceUrl, request, body);
    }

    // battle service routes
    @RequestMapping(value = "/battles/**", method = {RequestMethod.GET, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyBattleService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forward(battleServiceUrl, request, body);
    }

    // pve service routes
    @RequestMapping(value = "/campaigns/**", method = {RequestMethod.GET, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyPveService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forward(pveServiceUrl, request, body);
    }

    // pvp service routes
    @RequestMapping(value = "/pvp/**", method = {RequestMethod.GET, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> proxyPvpService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forward(pvpServiceUrl, request, body);
    }

    // health check
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "api-gateway"));
    }

    private ResponseEntity<?> forward(String serviceUrl, HttpServletRequest request, String body) {
        String path = request.getRequestURI();
        String query = request.getQueryString();
        String url = serviceUrl + path + (query != null ? "?" + query : "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
            return ResponseEntity.status(response.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Service unavailable: " + e.getMessage()));
        }
    }
}
