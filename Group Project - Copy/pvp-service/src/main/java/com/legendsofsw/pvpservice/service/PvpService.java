package com.legendsofsw.pvpservice.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.legendsofsw.pvpservice.dto.InviteRequest;
import com.legendsofsw.pvpservice.model.InvitationStatus;
import com.legendsofsw.pvpservice.model.PvpInvitation;
import com.legendsofsw.pvpservice.model.PvpMatch;
import com.legendsofsw.pvpservice.repository.PvpInvitationRepository;
import com.legendsofsw.pvpservice.repository.PvpMatchRepository;

@Service
public class PvpService {

    private final PvpInvitationRepository invitationRepo;
    private final PvpMatchRepository matchRepo;
    private final RestTemplate restTemplate;

    @Value("${pvp.player-service.url:http://localhost:8081}")
    private String playerServiceUrl;

    @Value("${pvp.battle-service.url:http://localhost:8083}")
    private String battleServiceUrl;

    @Value("${pvp.party-service.url:http://localhost:8082}")
    private String partyServiceUrl;

    @Autowired
    public PvpService(PvpInvitationRepository invitationRepo, PvpMatchRepository matchRepo) {
        this.invitationRepo = invitationRepo;
        this.matchRepo = matchRepo;
        this.restTemplate = new RestTemplate();
    }

    PvpService(PvpInvitationRepository invitationRepo, PvpMatchRepository matchRepo, RestTemplate restTemplate) {
        this.invitationRepo = invitationRepo;
        this.matchRepo = matchRepo;
        this.restTemplate = restTemplate;
    }

    public PvpInvitation sendInvitation(InviteRequest request) {
        if (request.getSenderUsername().equalsIgnoreCase(request.getReceiverUsername())) {
            throw new RuntimeException("Cannot invite yourself to PvP");
        }

        Long receiverId = lookupPlayerId(request.getReceiverUsername());
        if (receiverId == null) {
            throw new RuntimeException("Player not found: " + request.getReceiverUsername());
        }

        boolean senderHasParties = playerHasSavedParties(request.getSenderId());
        boolean receiverHasParties = playerHasSavedParties(receiverId);

        if (!senderHasParties) {
            throw new RuntimeException("You need at least one saved party for PvP");
        }
        if (!receiverHasParties) {
            throw new RuntimeException("The invited player has no saved parties");
        }

        PvpInvitation invitation = new PvpInvitation(
                request.getSenderId(),
                request.getSenderUsername(),
                receiverId,
                request.getReceiverUsername()
        );
        return invitationRepo.save(invitation);
    }

    public PvpInvitation acceptInvitation(Long invitationId) {
        PvpInvitation invitation = invitationRepo.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("Invitation is no longer pending");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepo.save(invitation);

        // create match
        PvpMatch match = new PvpMatch(invitation.getSenderId(), invitation.getReceiverId());
        matchRepo.save(match);

        return invitation;
    }

    public PvpInvitation declineInvitation(Long invitationId) {
        PvpInvitation invitation = invitationRepo.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        invitation.setStatus(InvitationStatus.DECLINED);
        return invitationRepo.save(invitation);
    }

    public List<PvpInvitation> getPendingInvitations(Long playerId) {
        return invitationRepo.findByReceiverIdAndStatus(playerId, InvitationStatus.PENDING);
    }

    public List<PvpInvitation> getSentInvitations(Long playerId) {
        return invitationRepo.findBySenderIdAndStatus(playerId, InvitationStatus.PENDING);
    }

    public PvpMatch completeMatch(Long matchId, Long winnerId) {
        PvpMatch match = matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        match.setWinnerId(winnerId);
        match.setCompleted(true);

        Long loserId = match.getPlayer1Id().equals(winnerId) ? match.getPlayer2Id() : match.getPlayer1Id();

        // update player records
        try {
            restTemplate.postForObject(
                    playerServiceUrl + "/api/players/" + winnerId + "/pvp-record/win", null, Object.class);
            restTemplate.postForObject(
                    playerServiceUrl + "/api/players/" + loserId + "/pvp-record/loss", null, Object.class);
        } catch (Exception e) {
            // standalone mode
        }

        return matchRepo.save(match);
    }

    public List<PvpMatch> getPlayerMatches(Long playerId) {
        return matchRepo.findByPlayer1IdOrPlayer2Id(playerId, playerId);
    }

    public PvpMatch getMatch(Long matchId) {
        return matchRepo.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));
    }

    @SuppressWarnings("unchecked")
    private Long lookupPlayerId(String username) {
        try {
            Map result = restTemplate.getForObject(
                    playerServiceUrl + "/api/players/by-username/" + username, Map.class);
            if (result != null && result.containsKey("id")) {
                return Long.valueOf(result.get("id").toString());
            }
        } catch (Exception e) {
            // player service not available allow creation anyway for testing
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private boolean playerHasSavedParties(Long playerId) {
        try {
            List result = restTemplate.getForObject(
                    playerServiceUrl + "/api/players/" + playerId + "/parties", List.class);
            return result != null && !result.isEmpty();
        } catch (Exception e) {
            return true;
        }
    }
}
