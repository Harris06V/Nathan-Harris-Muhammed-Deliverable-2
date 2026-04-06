# Legends of Sword and Wand - API Documentation

## Architecture Overview

The game is split into 5 backend microservices plus an API gateway and a desktop client.

| Service | Port | Purpose |
|---------|------|---------|
| API Gateway | 8080 | Central routing for all client requests |
| Player Service | 8081 | User accounts, auth, saved parties, leaderboard, PvP records |
| Party Service | 8082 | Hero management, classes, leveling, items, inventory |
| Battle Service | 8083 | Core combat mechanics, turn-based battle resolution |
| PvE Service | 8084 | PvE campaign, room encounters, inns, scoring |
| PvP Service | 8085 | PvP invitations, matches, league standings |

All services use REST APIs with JSON request/response bodies. Each service has its own H2 database. Services that need data from other services make HTTP calls using RestTemplate.

## API Gateway (port 8080)

The gateway is the single entry point for the game client. It forwards requests to the correct backend based on URL path prefix:

- `/api/players/**` → Player Service (8081)
- `/api/parties/**` → Party Service (8082)
- `/api/heroes/**` → Party Service (8082)
- `/api/battles/**` → Battle Service (8083)
- `/api/campaigns/**` → PvE Service (8084)
- `/api/pvp/**` → PvP Service (8085)

### GET /api/health
Gateway health check.

**Response (200):**
```json
{ "status": "UP", "service": "api-gateway" }
```

Service URLs are configured in `application.properties` and overridden via environment variables in docker-compose.



## Player Service (port 8081)

### POST /api/players/register
Create a new player account.

**Request body:**
```json
{ "username": "string", "password": "string" }
```

**Response (200):**
```json
{ "id": 1, "username": "string", "message": "Registration successful" }
```

### POST /api/players/login
Authenticate an existing player

**Request body:**
```json
{ "username": "string", "password": "string" }
```

**Response (200):**
```json
{ "playerId": 1, "username": "string", "message": "Login successful" }
```

### GET /api/players/{id}
Get player profile by ID.

**Response (200):**
```json
{ "id": 1, "username": "string" }
```

### GET /api/players/by-username/{username}
Look up player by username.

### GET /api/players/{playerId}/parties
Get all saved parties for a player (max 5)

### POST /api/players/{playerId}/parties
Save a party to the player's profile.

**Request body:**
```json
{ "name": "My Team", "partyDataJson": "{...}" }
```

### DELETE /api/players/{playerId}/parties/{partyId}
Delete a saved party

### PUT /api/players/{playerId}/parties/{oldPartyId}/replace
Replace an existing saved party with a new one

### POST /api/players/{playerId}/scores
Add a campaign score

**Request body:**
```json
{ "score": 15000 }
```

### GET /api/players/{playerId}/scores
Get all scores for a player.

### GET /api/players/leaderboard
Get top 10 scores (hall of fame)

### GET /api/players/{playerId}/pvp-record
Get PvP win/loss record.

### POST /api/players/{playerId}/pvp-record/win
Record a PvP win.

### POST /api/players/{playerId}/pvp-record/loss
Record a PvP loss.

### GET /api/players/league
Get league standings sorted by wins.

---

## Party Service (port 8082)

### POST /api/parties
Create a new party.

**Request body:**
```json
{ "ownerId": 1 }
```

### GET /api/parties/{partyId}
Get party details including heroes and inventory.

**Response (200):**
```json
{
  "party": { "id": 1, "ownerId": 1, "gold": 500 },
  "heroes": [...],
  "inventory": [...]
}
```

### GET /api/parties/owner/{ownerId}
Get all parties owned by a player.

### POST /api/parties/{partyId}/gold
Add gold to party.

**Request body:**
```json
{ "amount": 500 }
```

### POST /api/parties/{partyId}/buy
Buy an item from the shop.

**Request body:**
```json
{ "itemType": "BREAD" }
```
Valid item types: BREAD (200g), CHEESE (500g), STEAK (1000g), WATER (150g), JUICE (400g), WINE (750g), ELIXIR (2000g)

### POST /api/parties/{partyId}/use-item
Use an item on a hero.

**Request body:**
```json
{ "itemType": "BREAD", "heroId": 1 }
```

### GET /api/parties/{partyId}/inventory
Get all items in the party's inventory.

### GET /api/parties/{partyId}/cumulative-level
Get the total level of all heroes in the party.

### DELETE /api/parties/{partyId}
Delete a party and all its heroes.

### POST /api/heroes/party/{partyId}
Create a new hero and add to party 

**Request body:**
```json
{ "name": "Knight Bob", "heroClass": "WARRIOR" }
```
Valid classes: ORDER, CHAOS, WARRIOR, MAGE

**Response - Hero object with all stats.**

### GET /api/heroes/{heroId}
Get hero details.

### GET /api/heroes/party/{partyId}
Get all heroes in a party.

### POST /api/heroes/{heroId}/level-up
Level up a hero in a chosen class.

**Request body:**
```json
{ "chosenClass": "WARRIOR" }
```

### POST /api/heroes/{heroId}/add-experience
Add experience to a hero.

**Request body:**
```json
{ "amount": 500 }
```

### POST /api/heroes/{heroId}/revive
Revive a dead hero to full HP and mana.

### POST /api/heroes/{heroId}/lose-experience
Deduct 30% of the hero's current-level experience. Used after a battle loss. Heroes cannot lose levels.

### DELETE /api/heroes/{heroId}
Remove a hero.

---

## Battle Service (port 8083)

### POST /api/battles
Start a new battle.

**Request body:**
```json
{
  "team1": [
    {
      "name": "Hero1", "level": 5, "attack": 15, "defense": 10,
      "currentHealth": 150, "maxHealth": 150, "currentMana": 80, "maxMana": 80,
      "orderLevel": 0, "chaosLevel": 0, "warriorLevel": 5, "mageLevel": 0,
      "specialization": "WARRIOR", "hybridClass": "NONE", "isPlayerUnit": true
    }
  ],
  "team2": [
    {
      "name": "Enemy1", "level": 3, "attack": 10, "defense": 5,
      "currentHealth": 100, "maxHealth": 100, "currentMana": 0, "maxMana": 0,
      "isPlayerUnit": false
    }
  ],
  "pvp": false
}
```

**Response - BattleStateResponse:**
```json
{
  "battleId": 1,
  "status": "IN_PROGRESS",
  "turnNumber": 1,
  "team1": [...],
  "team2": [...],
  "currentUnitIndex": 0,
  "currentUnit": {...},
  "availableActions": ["ATTACK", "DEFEND", "WAIT", "CAST_BERSERKER_ATTACK"],
  "lastActionResult": "Battle started."
}
```

### GET /api/battles/{battleId}
Get current battle state.

### POST /api/battles/{battleId}/action
Perform an action for the current unit.

**Request body:**
```json
{ "actionType": "ATTACK", "targetIndex": 1 }
```

Valid action types:
- **ATTACK** - basic attack requires targetIndex
- **DEFEND** - skip turn, regain +10 HP and +5 mana
- **WAIT** - postpone action to end of turn
- **CAST_PROTECT** - shield all allies (Order, 25 mana)
- **CAST_HEAL** - heal lowest HP ally (Order, 35 mana)
- **CAST_FIREBALL** - hit up to 3 enemies (Chaos, 30 mana, requires targetIndex)
- **CAST_CHAIN_LIGHTNING** - chain damage to all enemies (Chaos, 40 mana, requires targetIndex)
- **CAST_BERSERKER_ATTACK** - cleave attack (Warrior, 60 mana, requires targetIndex)
- **CAST_REPLENISH** - restore mana to allies (Mage, 80 mana / 40 for Wizard)

Battle ends when all units of one team reach 0 HP Status changes to TEAM1_WIN or TEAM2_WIN.

---

## PvE Service (port 8084)

### POST /api/campaigns
Start a new PvE campaign (30 rooms).

**Request body:**
```json
{ "playerId": 1, "partyId": 1 }
```

### GET /api/campaigns/{campaignId}
Get campaign state.

### GET /api/campaigns/player/{playerId}
Get all campaigns for a player.

### POST /api/campaigns/{campaignId}/next-room
Advance to the next room. Returns either a battle encounter or an inn.

**Response for battle:**
```json
{
  "roomNumber": 5,
  "roomType": "BATTLE",
  "battleId": 3,
  "message": "You encountered enemies! Prepare for battle."
}
```

**Response for inn:**
```json
{
  "roomNumber": 6,
  "roomType": "INN",
  "shopItems": [...],
  "availableHeroes": [...],
  "message": "You found an inn! Your heroes are revived and healed."
}
```

### POST /api/campaigns/{campaignId}/complete-battle
Report battle result to the campaign.

**Request body:**
```json
{ "playerWon": true }
```

### POST /api/campaigns/{campaignId}/save
Save and pause campaign progress cannot save during battle.

### POST /api/campaigns/{campaignId}/resume
Resume a paused campaign.

### POST /api/campaigns/{campaignId}/complete
Force-complete the campaign and calculate final score.

### POST /api/campaigns/{campaignId}/item-purchase
Record an item purchase cost for final score calculation.

**Request body:**
```json
{ "cost": 500 }
```

---

## PvP Service (port 8085)

### POST /api/pvp/invite
Send a PvP battle invitation.

**Request body:**
```json
{ "senderId": 1, "senderUsername": "player1", "receiverUsername": "player2" }
```

### POST /api/pvp/invite/{invitationId}/accept
Accept a pending invitation. Creates a PvP match.

### POST /api/pvp/invite/{invitationId}/decline
Decline a pending invitation.

### GET /api/pvp/invites/received/{playerId}
Get all pending invitations received by a player.

### GET /api/pvp/invites/sent/{playerId}
Get all pending invitations sent by a player.

### GET /api/pvp/matches/{playerId}
Get all PvP matches for a player.

### GET /api/pvp/match/{matchId}
Get match details.

### POST /api/pvp/match/{matchId}/complete
Report match result.

**Request body:**
```json
{ "winnerId": 1 }
```

---

## Data Flow Between Services

 PvE campaign flow PvE Service calls Party Service to get hero data and cumulative levels calls Battle Service to run battles and calls Player Service to save scores.

 PvP flow PvP Service calls Player Service to validate usernames and check saved parties, calls Battle Service for the actual combat, and updates win/loss records on Player Service.

Battle flow Battle Service is stateless in terms of persistent hero data. It receives unit data in the battle request, runs the combat, and returns results. The calling service PvE or PvP handles updating persistent state.

## Inter service Communication

All inter service calls use HTTP REST via Spring's RestTemplate. Service URLs are configured via application.properties and can be overridden with environment variables for container deployment.

Default service URLs:
- Player Service: http://localhost:8081
- Party Service: http://localhost:8082
- Battle Service: http://localhost:8083
- PvE Service: http://localhost:8084
- PvP Service: http://localhost:8085
- API Gateway: http://localhost:8080

When running with docker-compose, services reference each other by container name (e.g. `http://player-service:8081`).

## Game Client

The game client is a Java Swing desktop application that connects to the API gateway. It provides a GUI for all game features:

- **Login / Register** - Account creation and authentication
- **New Campaign** - Create a hero, start a 30-room PvE campaign
- **Continue Campaign** - Resume a paused campaign
- **Battle View** - Turn-based combat with action buttons and target selection
- **Inn** - Buy items, recruit heroes, use inventory during campaign
- **Party View** - View detailed hero stats
- **Saved Parties** - Load and delete saved party configurations (max 5)
- **PvP** - Send/accept/decline invitations, view matches and records
- **Leaderboard** - Hall of Fame, personal scores, league standings

### Running the Client

```bash
cd game-client
mvn clean package
java -jar target/game-client-1.0.0.jar http://localhost:8080
```

The URL argument defaults to `http://localhost:8080` if not provided.

## Docker Compose Deployment

`docker-compose.yml` in the project root starts all 6 services with correct networking:

```bash
docker-compose up -d --build
```

Each service gets a named volume for its H2 database so data persists across restarts. Inter-service URLs are set via environment variables that override application.properties defaults.

