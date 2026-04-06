package com.legendsofsw.pveservice.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.legendsofsw.pveservice.dto.RoomResult;
import com.legendsofsw.pveservice.model.Campaign;
import com.legendsofsw.pveservice.model.CampaignStatus;
import com.legendsofsw.pveservice.model.RoomType;
import com.legendsofsw.pveservice.repository.CampaignRepository;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepo;
    private final RoomGenerator roomGenerator;
    private final RestTemplate restTemplate;

    @Value("${pve.battle-service.url:http://localhost:8083}")
    private String battleServiceUrl;

    @Value("${pve.party-service.url:http://localhost:8082}")
    private String partyServiceUrl;

    @Value("${pve.player-service.url:http://localhost:8081}")
    private String playerServiceUrl;

    public CampaignService(CampaignRepository campaignRepo, RoomGenerator roomGenerator) {
        this.campaignRepo = campaignRepo;
        this.roomGenerator = roomGenerator;
        this.restTemplate = new RestTemplate();
    }

    public Campaign startCampaign(Long playerId, Long partyId) {
        Campaign campaign = new Campaign(playerId, partyId);
        return campaignRepo.save(campaign);
    }

    public Campaign getCampaign(Long campaignId) {
        return campaignRepo.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));
    }

    public List<Campaign> getPlayerCampaigns(Long playerId) {
        return campaignRepo.findByPlayerId(playerId);
    }

    public RoomResult advanceToNextRoom(Long campaignId) {
        Campaign campaign = getCampaign(campaignId);

        if (campaign.getStatus() == CampaignStatus.COMPLETED) {
            throw new RuntimeException("Campaign is already complete");
        }
        if (campaign.getStatus() == CampaignStatus.IN_BATTLE) {
            throw new RuntimeException("Finish the current battle first");
        }

        campaign.setCurrentRoom(campaign.getCurrentRoom() + 1);

        if (campaign.getCurrentRoom() > campaign.getTotalRooms()) {
            campaign.setStatus(CampaignStatus.COMPLETED);
            campaignRepo.save(campaign);
            throw new RuntimeException("Campaign has ended after 30 rooms");
        }

        // get level from party service
        int cumulativeLevel = getCumulativeLevel(campaign.getPartyId());

        // room type determined by probability: 60% battle (+3% per 10 cumulative levels)
        RoomType roomType = roomGenerator.generateRoomType(cumulativeLevel);
        campaign.setLastRoomType(roomType);

        RoomResult result = new RoomResult();
        result.setRoomNumber(campaign.getCurrentRoom());
        result.setRoomType(roomType);

        if (roomType == RoomType.BATTLE) {
            campaign.setStatus(CampaignStatus.IN_BATTLE);

            List<Map<String, Object>> enemies = roomGenerator.generateEnemyParty(cumulativeLevel);
            campaign.setPendingExp(roomGenerator.calcExperienceGain(enemies));
            campaign.setPendingGold(roomGenerator.calcGoldGain(enemies));
            result.setMessage("You encountered enemies! Prepare for battle.");

            try {
                Long battleId = startBattleViaService(campaign.getPartyId(), enemies);
                campaign.setCurrentBattleId(battleId);
                result.setBattleId(battleId);
            } catch (Exception e) {
                result.setMessage("Battle setup pending. Enemies generated with cumulative level ~" + cumulativeLevel);
            }

        } else {
            campaign.setStatus(CampaignStatus.AT_INN);
            result.setMessage("You found an inn! Your heroes are revived and healed.");
            result.setShopItems(roomGenerator.getShopItems());
            int partySize = getPartySize(campaign.getPartyId());
            result.setAvailableHeroes(roomGenerator.generateRecruitableHeroes(campaign.getCurrentRoom(), partySize));

            // revive all heroes via party service
            try {
                revivePartyHeroes(campaign.getPartyId());
            } catch (Exception e) {
                // service might not be running yet
            }
        }

        campaignRepo.save(campaign);
        return result;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> completeBattle(Long campaignId, boolean playerWon) {
        Campaign campaign = getCampaign(campaignId);

        if (campaign.getStatus() != CampaignStatus.IN_BATTLE) {
            throw new RuntimeException("Not currently in a battle");
        }

        Map<String, Object> battleResult = new HashMap<>();
        battleResult.put("playerWon", playerWon);

        if (playerWon) {
            int expGained = campaign.getPendingExp();
            int goldGained = campaign.getPendingGold();

            battleResult.put("experienceGained", expGained);
            battleResult.put("goldGained", goldGained);
            battleResult.put("message", "Victory! Gained " + expGained + " exp and " + goldGained + " gold.");

            // add gold to party
            try {
                addGoldToParty(campaign.getPartyId(), goldGained);
            } catch (Exception e) {
                // standalone mode
            }

            // distribute exp to each surviving hero
            try {
                distributeExpToHeroes(campaign.getPartyId(), expGained);
            } catch (Exception e) {
                // standalone mode
            }
        } else {
            battleResult.put("message", "Defeat. Lost 10% gold and 30% current level exp.");

            // lose 10% gold and 30% current level exp
            try {
                loseGoldPercent(campaign.getPartyId(), 0.10);
            } catch (Exception e) {
                // standalone mode
            }

            // apply 30% current-level exp loss to each hero
            try {
                applyExpLossToHeroes(campaign.getPartyId());
            } catch (Exception e) {
                // standalone mode
            }
        }

        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setCurrentBattleId(null);

        // check if campaign is complete
        if (campaign.getCurrentRoom() >= campaign.getTotalRooms()) {
            campaign.setStatus(CampaignStatus.COMPLETED);
            int cumulativeLevel = getCumulativeLevelSafe(campaign.getPartyId());
            int gold = getPartyGoldSafe(campaign.getPartyId());
            int score = roomGenerator.calcFinalScore(cumulativeLevel, gold, campaign.getTotalItemSpending());
            battleResult.put("finalScore", score);
            battleResult.put("message", battleResult.get("message") + " Campaign complete! Final score: " + score);

            // save score
            try {
                saveScoreToPlayerService(campaign.getPlayerId(), score);
            } catch (Exception e) {
                // standalone mode
            }
        }

        campaignRepo.save(campaign);
        return battleResult;
    }

    public Campaign saveCampaign(Long campaignId) {
        Campaign campaign = getCampaign(campaignId);
        if (campaign.getStatus() == CampaignStatus.IN_BATTLE) {
            throw new RuntimeException("Cannot save during a battle");
        }
        campaign.setStatus(CampaignStatus.PAUSED);
        return campaignRepo.save(campaign);
    }

    public Campaign resumeCampaign(Long campaignId) {
        Campaign campaign = getCampaign(campaignId);
        if (campaign.getStatus() != CampaignStatus.PAUSED) {
            throw new RuntimeException("Campaign is not paused");
        }
        campaign.setStatus(CampaignStatus.ACTIVE);
        return campaignRepo.save(campaign);
    }

    public Campaign recordItemPurchase(Long campaignId, int cost) {
        Campaign campaign = getCampaign(campaignId);
        campaign.setTotalItemSpending(campaign.getTotalItemSpending() + cost);
        return campaignRepo.save(campaign);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> completeCampaign(Long campaignId) {
        Campaign campaign = getCampaign(campaignId);
        campaign.setStatus(CampaignStatus.COMPLETED);

        int cumulativeLevel = getCumulativeLevelSafe(campaign.getPartyId());
        int gold = getPartyGoldSafe(campaign.getPartyId());
        int score = roomGenerator.calcFinalScore(cumulativeLevel, gold, campaign.getTotalItemSpending());

        try {
            saveScoreToPlayerService(campaign.getPlayerId(), score);
        } catch (Exception e) {
            // standalone mode
        }

        campaignRepo.save(campaign);

        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        result.put("cumulativeLevel", cumulativeLevel);
        result.put("gold", gold);
        result.put("message", "Campaign complete. Final score: " + score);
        return result;
    }


    private int getCumulativeLevel(Long partyId) {
        try {
            Map result = restTemplate.getForObject(
                    partyServiceUrl + "/api/parties/" + partyId + "/cumulative-level", Map.class);
            if (result != null && result.containsKey("cumulativeLevel")) {
                return (int) result.get("cumulativeLevel");
            }
        } catch (Exception e) {
            // party service not available
        }
        return 5;
    }

    private int getCumulativeLevelSafe(Long partyId) {
        return getCumulativeLevel(partyId);
    }

    @SuppressWarnings("unchecked")
    private int getPartySize(Long partyId) {
        try {
            List heroes = restTemplate.getForObject(
                    partyServiceUrl + "/api/heroes/party/" + partyId, List.class);
            return heroes != null ? heroes.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private int getPartyGoldSafe(Long partyId) {
        try {
            Map result = restTemplate.getForObject(
                    partyServiceUrl + "/api/parties/" + partyId, Map.class);
            if (result != null && result.containsKey("party")) {
                Map party = (Map) result.get("party");
                return (int) party.get("gold");
            }
        } catch (Exception e) {
            // party service not available
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    private Long startBattleViaService(Long partyId, List<Map<String, Object>> enemies) {
        Map<String, Object> battleRequest = new HashMap<>();
        battleRequest.put("team2", enemies);
        battleRequest.put("pvp", false);

        // get heroes from party service and set as team1
        try {
            List heroes = restTemplate.getForObject(
                    partyServiceUrl + "/api/heroes/party/" + partyId, List.class);
            battleRequest.put("team1", heroes);
        } catch (Exception e) {
            battleRequest.put("team1", List.of());
        }

        Map result = restTemplate.postForObject(
                battleServiceUrl + "/api/battles", battleRequest, Map.class);
        if (result != null && result.containsKey("battleId")) {
            return Long.valueOf(result.get("battleId").toString());
        }
        return null;
    }

    private void revivePartyHeroes(Long partyId) {
        try {
            List heroes = restTemplate.getForObject(
                    partyServiceUrl + "/api/heroes/party/" + partyId, List.class);
            if (heroes != null) {
                for (Object hero : heroes) {
                    Map heroMap = (Map) hero;
                    Long heroId = Long.valueOf(heroMap.get("id").toString());
                    restTemplate.postForObject(
                            partyServiceUrl + "/api/heroes/" + heroId + "/revive", null, Object.class);
                }
            }
        } catch (Exception e) {
            // party service not available
        }
    }

    private void addGoldToParty(Long partyId, int amount) {
        Map<String, Integer> body = new HashMap<>();
        body.put("amount", amount);
        restTemplate.postForObject(
                partyServiceUrl + "/api/parties/" + partyId + "/gold", body, Object.class);
    }

    private void loseGoldPercent(Long partyId, double percent) {
        // handled by party service
        try {
            int gold = getPartyGoldSafe(partyId);
            int loss = (int) (gold * percent);
            Map<String, Integer> body = new HashMap<>();
            body.put("amount", -loss);
            restTemplate.postForObject(
                    partyServiceUrl + "/api/parties/" + partyId + "/gold", body, Object.class);
        } catch (Exception e) {
            // standalone mode
        }
    }

    private void saveScoreToPlayerService(Long playerId, int score) {
        Map<String, Integer> body = new HashMap<>();
        body.put("score", score);
        restTemplate.postForObject(
                playerServiceUrl + "/api/players/" + playerId + "/scores", body, Object.class);
    }

    @SuppressWarnings("unchecked")
    private void distributeExpToHeroes(Long partyId, int totalExp) {
        List heroes = restTemplate.getForObject(
                partyServiceUrl + "/api/heroes/party/" + partyId, List.class);
        if (heroes != null && !heroes.isEmpty()) {
            int perHero = totalExp / heroes.size();
            for (Object hero : heroes) {
                Map heroMap = (Map) hero;
                Long heroId = Long.valueOf(heroMap.get("id").toString());
                Map<String, Integer> body = new HashMap<>();
                body.put("amount", perHero);
                restTemplate.postForObject(
                        partyServiceUrl + "/api/heroes/" + heroId + "/add-experience", body, Object.class);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void applyExpLossToHeroes(Long partyId) {
        List heroes = restTemplate.getForObject(
                partyServiceUrl + "/api/heroes/party/" + partyId, List.class);
        if (heroes != null) {
            for (Object hero : heroes) {
                Map heroMap = (Map) hero;
                Long heroId = Long.valueOf(heroMap.get("id").toString());
                restTemplate.postForObject(
                        partyServiceUrl + "/api/heroes/" + heroId + "/lose-experience", null, Object.class);
            }
        }
    }
}
