package com.legendsofsw.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

// handles all HTTP calls to the API gateway
public class ApiClient {

    private final String baseUrl;
    private final Gson gson = new Gson();

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    // --- player service ---

    public Map<String, Object> register(String username, String password) {
        Map<String, String> body = Map.of("username", username, "password", password);
        return post("/api/players/register", body);
    }

    public Map<String, Object> login(String username, String password) {
        Map<String, String> body = Map.of("username", username, "password", password);
        return post("/api/players/login", body);
    }

    public Map<String, Object> getPlayer(long playerId) {
        return get("/api/players/" + playerId);
    }

    public List<Map<String, Object>> getSavedParties(long playerId) {
        return getList("/api/players/" + playerId + "/parties");
    }

    public Map<String, Object> saveParty(long playerId, String name, String partyDataJson) {
        Map<String, String> body = Map.of("name", name, "partyDataJson", partyDataJson);
        return post("/api/players/" + playerId + "/parties", body);
    }

    public Map<String, Object> deleteParty(long playerId, long partyId) {
        return delete("/api/players/" + playerId + "/parties/" + partyId);
    }

    public Map<String, Object> replaceParty(long playerId, long oldPartyId, String name, String partyDataJson) {
        Map<String, String> body = Map.of("name", name, "partyDataJson", partyDataJson);
        return put("/api/players/" + playerId + "/parties/" + oldPartyId + "/replace", body);
    }

    public List<Map<String, Object>> getLeaderboard() {
        return getList("/api/players/leaderboard");
    }

    public List<Map<String, Object>> getPlayerScores(long playerId) {
        return getList("/api/players/" + playerId + "/scores");
    }

    public Map<String, Object> getPvpRecord(long playerId) {
        return get("/api/players/" + playerId + "/pvp-record");
    }

    public List<Map<String, Object>> getLeagueStandings() {
        return getList("/api/players/league");
    }

    // --- party service ---

    public Map<String, Object> createParty(long ownerId) {
        return post("/api/parties", Map.of("ownerId", ownerId));
    }

    public Map<String, Object> getParty(long partyId) {
        return get("/api/parties/" + partyId);
    }

    public List<Map<String, Object>> getPartiesByOwner(long ownerId) {
        return getList("/api/parties/owner/" + ownerId);
    }

    public Map<String, Object> addGold(long partyId, int amount) {
        return post("/api/parties/" + partyId + "/gold", Map.of("amount", amount));
    }

    public Map<String, Object> buyItem(long partyId, String itemType) {
        return post("/api/parties/" + partyId + "/buy", Map.of("itemType", itemType));
    }

    public Map<String, Object> useItem(long partyId, String itemType, long heroId) {
        return post("/api/parties/" + partyId + "/use-item",
                Map.of("itemType", itemType, "heroId", heroId));
    }

    public List<Map<String, Object>> getInventory(long partyId) {
        return getList("/api/parties/" + partyId + "/inventory");
    }

    public Map<String, Object> getCumulativeLevel(long partyId) {
        return get("/api/parties/" + partyId + "/cumulative-level");
    }

    // --- hero service ---

    public Map<String, Object> createHero(long partyId, String name, String heroClass) {
        return post("/api/heroes/party/" + partyId, Map.of("name", name, "heroClass", heroClass));
    }

    public Map<String, Object> getHero(long heroId) {
        return get("/api/heroes/" + heroId);
    }

    public List<Map<String, Object>> getHeroesByParty(long partyId) {
        return getList("/api/heroes/party/" + partyId);
    }

    public Map<String, Object> levelUpHero(long heroId, String chosenClass) {
        return post("/api/heroes/" + heroId + "/level-up", Map.of("chosenClass", chosenClass));
    }

    public Map<String, Object> addExperience(long heroId, int amount) {
        return post("/api/heroes/" + heroId + "/add-experience", Map.of("amount", amount));
    }

    public Map<String, Object> reviveHero(long heroId) {
        return post("/api/heroes/" + heroId + "/revive", Map.of());
    }

    // --- battle service ---

    public Map<String, Object> createPvpBattle(Object team1, Object team2) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("team1", team1);
        body.put("team2", team2);
        body.put("pvp", true);
        return post("/api/battles", body);
    }

    public Map<String, Object> getBattleState(long battleId) {
        return get("/api/battles/" + battleId);
    }

    public Map<String, Object> performAction(long battleId, String actionType, Integer targetIndex) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("actionType", actionType);
        if (targetIndex != null) {
            body.put("targetIndex", targetIndex);
        }
        return post("/api/battles/" + battleId + "/action", body);
    }

    // --- pve service ---

    public Map<String, Object> startCampaign(long playerId, long partyId) {
        return post("/api/campaigns", Map.of("playerId", playerId, "partyId", partyId));
    }

    public Map<String, Object> getCampaign(long campaignId) {
        return get("/api/campaigns/" + campaignId);
    }

    public List<Map<String, Object>> getPlayerCampaigns(long playerId) {
        return getList("/api/campaigns/player/" + playerId);
    }

    public Map<String, Object> nextRoom(long campaignId) {
        return post("/api/campaigns/" + campaignId + "/next-room", Map.of());
    }

    public Map<String, Object> completeBattle(long campaignId, boolean playerWon) {
        return post("/api/campaigns/" + campaignId + "/complete-battle", Map.of("playerWon", playerWon));
    }

    public Map<String, Object> saveCampaign(long campaignId) {
        return post("/api/campaigns/" + campaignId + "/save", Map.of());
    }

    public Map<String, Object> resumeCampaign(long campaignId) {
        return post("/api/campaigns/" + campaignId + "/resume", Map.of());
    }

    public Map<String, Object> completeCampaign(long campaignId) {
        return post("/api/campaigns/" + campaignId + "/complete", Map.of());
    }

    public Map<String, Object> recordItemPurchase(long campaignId, int cost) {
        return post("/api/campaigns/" + campaignId + "/item-purchase", Map.of("cost", cost));
    }

    // --- pvp service ---

    public Map<String, Object> sendPvpInvite(long senderId, String senderUsername, String receiverUsername) {
        return post("/api/pvp/invite", Map.of(
                "senderId", senderId,
                "senderUsername", senderUsername,
                "receiverUsername", receiverUsername
        ));
    }

    public Map<String, Object> acceptInvite(long invitationId) {
        return post("/api/pvp/invite/" + invitationId + "/accept", Map.of());
    }

    public Map<String, Object> declineInvite(long invitationId) {
        return post("/api/pvp/invite/" + invitationId + "/decline", Map.of());
    }

    public List<Map<String, Object>> getReceivedInvites(long playerId) {
        return getList("/api/pvp/invites/received/" + playerId);
    }

    public List<Map<String, Object>> getSentInvites(long playerId) {
        return getList("/api/pvp/invites/sent/" + playerId);
    }

    public List<Map<String, Object>> getPvpMatches(long playerId) {
        return getList("/api/pvp/matches/" + playerId);
    }

    public Map<String, Object> getPvpMatch(long matchId) {
        return get("/api/pvp/match/" + matchId);
    }

    public Map<String, Object> completePvpMatch(long matchId, long winnerId) {
        return post("/api/pvp/match/" + matchId + "/complete", Map.of("winnerId", winnerId));
    }

    // --- HTTP helpers ---

    private Map<String, Object> get(String path) {
        try {
            URL url = URI.create(baseUrl + path).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            return readResponse(conn);
        } catch (Exception e) {
            return Map.of("error", "Connection failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getList(String path) {
        try {
            URL url = URI.create(baseUrl + path).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            String body = readBody(conn);
            return gson.fromJson(body, new TypeToken<List<Map<String, Object>>>() {}.getType());
        } catch (Exception e) {
            return List.of();
        }
    }

    private Map<String, Object> post(String path, Object body) {
        return sendWithBody("POST", path, body);
    }

    private Map<String, Object> put(String path, Object body) {
        return sendWithBody("PUT", path, body);
    }

    private Map<String, Object> delete(String path) {
        try {
            URL url = URI.create(baseUrl + path).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept", "application/json");
            return readResponse(conn);
        } catch (Exception e) {
            return Map.of("error", "Connection failed: " + e.getMessage());
        }
    }

    private Map<String, Object> sendWithBody(String method, String path, Object body) {
        try {
            URL url = URI.create(baseUrl + path).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            String json = gson.toJson(body);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            return readResponse(conn);
        } catch (Exception e) {
            return Map.of("error", "Connection failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readResponse(HttpURLConnection conn) throws IOException {
        String body = readBody(conn);
        if (body == null || body.isEmpty()) {
            return Map.of("error", "Empty response");
        }
        try {
            return gson.fromJson(body, new TypeToken<Map<String, Object>>() {}.getType());
        } catch (Exception e) {
            return Map.of("raw", body);
        }
    }

    private String readBody(HttpURLConnection conn) throws IOException {
        InputStream is;
        try {
            is = conn.getInputStream();
        } catch (IOException e) {
            is = conn.getErrorStream();
        }
        if (is == null) return "";

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
