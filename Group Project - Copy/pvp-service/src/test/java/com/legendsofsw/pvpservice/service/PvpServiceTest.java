package com.legendsofsw.pvpservice.service;

import com.legendsofsw.pvpservice.dto.InviteRequest;
import com.legendsofsw.pvpservice.model.InvitationStatus;
import com.legendsofsw.pvpservice.model.PvpInvitation;
import com.legendsofsw.pvpservice.model.PvpMatch;
import com.legendsofsw.pvpservice.repository.PvpInvitationRepository;
import com.legendsofsw.pvpservice.repository.PvpMatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PvpServiceTest {

    @Mock
    private PvpInvitationRepository invitationRepo;

    @Mock
    private PvpMatchRepository matchRepo;

    private PvpService pvpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pvpService = new PvpService(invitationRepo, matchRepo);
    }

    @Test
    void sendInvitationSelfInviteThrows() {
        InviteRequest request = new InviteRequest(1L, "alice", "alice");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pvpService.sendInvitation(request));
        assertTrue(ex.getMessage().contains("Cannot invite yourself"));
    }

    @Test
    void sendInvitationPlayerNotFoundThrows() {
        InviteRequest request = new InviteRequest(1L, "alice", "bob");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pvpService.sendInvitation(request));
        assertTrue(ex.getMessage().contains("Player not found"));
    }

    @Test
    void acceptInvitation() {
        PvpInvitation invitation = new PvpInvitation(1L, "sender", 2L, "receiver");
        invitation.setId(1L);
        when(invitationRepo.findById(1L)).thenReturn(Optional.of(invitation));
        when(invitationRepo.save(any(PvpInvitation.class))).thenAnswer(i -> i.getArgument(0));
        when(matchRepo.save(any(PvpMatch.class))).thenAnswer(i -> {
            PvpMatch m = i.getArgument(0);
            m.setId(1L);
            return m;
        });

        PvpInvitation result = pvpService.acceptInvitation(1L);

        assertEquals(InvitationStatus.ACCEPTED, result.getStatus());
        verify(matchRepo).save(any(PvpMatch.class));
    }

    @Test
    void acceptAlreadyAcceptedThrows() {
        PvpInvitation invitation = new PvpInvitation(1L, "sender", 2L, "receiver");
        invitation.setId(1L);
        invitation.setStatus(InvitationStatus.ACCEPTED);
        when(invitationRepo.findById(1L)).thenReturn(Optional.of(invitation));

        assertThrows(RuntimeException.class, () -> pvpService.acceptInvitation(1L));
    }

    @Test
    void declineInvitation() {
        PvpInvitation invitation = new PvpInvitation(1L, "sender", 2L, "receiver");
        invitation.setId(1L);
        when(invitationRepo.findById(1L)).thenReturn(Optional.of(invitation));
        when(invitationRepo.save(any(PvpInvitation.class))).thenAnswer(i -> i.getArgument(0));

        PvpInvitation result = pvpService.declineInvitation(1L);

        assertEquals(InvitationStatus.DECLINED, result.getStatus());
    }

    @Test
    void getPendingInvitations() {
        List<PvpInvitation> invites = List.of(
                new PvpInvitation(1L, "a", 2L, "b"),
                new PvpInvitation(3L, "c", 2L, "b")
        );
        when(invitationRepo.findByReceiverIdAndStatus(2L, InvitationStatus.PENDING)).thenReturn(invites);

        List<PvpInvitation> result = pvpService.getPendingInvitations(2L);

        assertEquals(2, result.size());
    }

    @Test
    void completeMatch() {
        PvpMatch match = new PvpMatch(1L, 2L);
        match.setId(1L);
        when(matchRepo.findById(1L)).thenReturn(Optional.of(match));
        when(matchRepo.save(any(PvpMatch.class))).thenAnswer(i -> i.getArgument(0));

        PvpMatch result = pvpService.completeMatch(1L, 1L);

        assertTrue(result.isCompleted());
        assertEquals(1L, result.getWinnerId());
    }

    @Test
    void getPlayerMatches() {
        PvpMatch m1 = new PvpMatch(1L, 2L);
        PvpMatch m2 = new PvpMatch(3L, 1L);
        when(matchRepo.findByPlayer1IdOrPlayer2Id(1L, 1L)).thenReturn(List.of(m1, m2));

        List<PvpMatch> result = pvpService.getPlayerMatches(1L);

        assertEquals(2, result.size());
    }
}
