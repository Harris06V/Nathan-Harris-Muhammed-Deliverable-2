package com.legendsofsw.pveservice.service;

import com.legendsofsw.pveservice.model.Campaign;
import com.legendsofsw.pveservice.model.CampaignStatus;
import com.legendsofsw.pveservice.repository.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepo;

    private CampaignService campaignService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RoomGenerator roomGenerator = new RoomGenerator();
        campaignService = new CampaignService(campaignRepo, roomGenerator);
    }

    @Test
    void startCampaign() {
        when(campaignRepo.save(any(Campaign.class))).thenAnswer(i -> {
            Campaign c = i.getArgument(0);
            c.setId(1L);
            return c;
        });

        Campaign campaign = campaignService.startCampaign(1L, 1L);

        assertEquals(1L, campaign.getPlayerId());
        assertEquals(1L, campaign.getPartyId());
        assertEquals(0, campaign.getCurrentRoom());
        assertEquals(CampaignStatus.ACTIVE, campaign.getStatus());
    }

    @Test
    void saveCampaignDuringBattleThrows() {
        Campaign campaign = new Campaign(1L, 1L);
        campaign.setId(1L);
        campaign.setStatus(CampaignStatus.IN_BATTLE);
        when(campaignRepo.findById(1L)).thenReturn(Optional.of(campaign));

        assertThrows(RuntimeException.class, () -> campaignService.saveCampaign(1L));
    }

    @Test
    void saveCampaignSuccess() {
        Campaign campaign = new Campaign(1L, 1L);
        campaign.setId(1L);
        campaign.setStatus(CampaignStatus.ACTIVE);
        when(campaignRepo.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignRepo.save(any(Campaign.class))).thenAnswer(i -> i.getArgument(0));

        Campaign result = campaignService.saveCampaign(1L);

        assertEquals(CampaignStatus.PAUSED, result.getStatus());
    }

    @Test
    void resumeCampaign() {
        Campaign campaign = new Campaign(1L, 1L);
        campaign.setId(1L);
        campaign.setStatus(CampaignStatus.PAUSED);
        when(campaignRepo.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignRepo.save(any(Campaign.class))).thenAnswer(i -> i.getArgument(0));

        Campaign result = campaignService.resumeCampaign(1L);

        assertEquals(CampaignStatus.ACTIVE, result.getStatus());
    }

    @Test
    void resumeNonPausedThrows() {
        Campaign campaign = new Campaign(1L, 1L);
        campaign.setId(1L);
        campaign.setStatus(CampaignStatus.ACTIVE);
        when(campaignRepo.findById(1L)).thenReturn(Optional.of(campaign));

        assertThrows(RuntimeException.class, () -> campaignService.resumeCampaign(1L));
    }

    @Test
    void completeBattleVictory() {
        Campaign campaign = new Campaign(1L, 1L);
        campaign.setId(1L);
        campaign.setCurrentRoom(5);
        campaign.setStatus(CampaignStatus.IN_BATTLE);
        campaign.setPendingExp(250);
        campaign.setPendingGold(375);
        when(campaignRepo.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignRepo.save(any(Campaign.class))).thenAnswer(i -> i.getArgument(0));

        var result = campaignService.completeBattle(1L, true);

        assertTrue((boolean) result.get("playerWon"));
        assertEquals(250, result.get("experienceGained"));
        assertEquals(375, result.get("goldGained"));
    }

    @Test
    void completeBattleDefeat() {
        Campaign campaign = new Campaign(1L, 1L);
        campaign.setId(1L);
        campaign.setCurrentRoom(5);
        campaign.setStatus(CampaignStatus.IN_BATTLE);
        when(campaignRepo.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignRepo.save(any(Campaign.class))).thenAnswer(i -> i.getArgument(0));

        var result = campaignService.completeBattle(1L, false);

        assertFalse((boolean) result.get("playerWon"));
    }

    @Test
    void recordItemPurchase() {
        Campaign campaign = new Campaign(1L, 1L);
        campaign.setId(1L);
        campaign.setStatus(CampaignStatus.AT_INN);
        campaign.setTotalItemSpending(0);
        when(campaignRepo.findById(1L)).thenReturn(Optional.of(campaign));
        when(campaignRepo.save(any(Campaign.class))).thenAnswer(i -> i.getArgument(0));

        Campaign result = campaignService.recordItemPurchase(1L, 500);
        assertEquals(500, result.getTotalItemSpending());

        Campaign result2 = campaignService.recordItemPurchase(1L, 200);
        assertEquals(700, result2.getTotalItemSpending());
    }

    @Test
    void completeBattleNotInBattleThrows() {
        Campaign campaign = new Campaign(1L, 1L);
        campaign.setId(1L);
        campaign.setStatus(CampaignStatus.ACTIVE);
        when(campaignRepo.findById(1L)).thenReturn(Optional.of(campaign));

        assertThrows(RuntimeException.class,
                () -> campaignService.completeBattle(1L, true));
    }
}
