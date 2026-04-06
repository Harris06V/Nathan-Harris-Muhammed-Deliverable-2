# Legends of Sword and Wand

Made by Nathan, Harris, and Muhammad 

## Project Structure
- **Legends of Sword and Wand**
  - **player-service**: User authentication, profiles, saved parties, leaderboard
  - **party-service**: Hero management, classes, leveling, items
  - **battle-service**: Core combat system, turn-based battles
  - **pve-service**: PvE campaign, room encounters, inns
  - **pvp-service**: PvP invitations, matches, league
  - **api-gateway**: Central routing gateway for all backend services
  - **game-client**: Java Swing desktop GUI application
  - **docs**: API documentation
  - **.github/workflows**: CI/CD pipeline
  - **docker-compose.yml**: Multi-container deployment config
  - **README.md**
  - **VisualDiagram.png**

## Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.2.5
- **Build Tool:** Maven
- **Database:** H2 
- **Containerization:** Docker 
- **CI/CD:** GitHub Actions
- **Testing:** JUnit 5, Mockito

## Prerequisites


JDK  17.0.x (Temurin recommended)  https://adoptium.net/temurin/releases/ 
Maven  3.9.x  https://maven.apache.org/download.cgi 
Docker  24.0+  https://docs.docker.com/get-docker/ 
Docker Compose  2.20+ (included with Docker Desktop) 
Git  2.40+  https://git-scm.com/downloads 

Verify installations:

```bash
java -version       # should show 17.x
mvn -version        # should show 3.9.x
docker --version    # should show 24.x+
docker compose version  # should show 2.20+
```

**Note:** Docker Desktop on Windows/Mac includes Docker Compose. On Linux install the `docker-compose-plugin` package separately.

## How to Build and Run

### Running all services with Docker Compose

The easiest way to run everything

```bash
docker-compose up -d --build
```

This starts all 6 services (player, party, battle, pve, pvp, api-gateway) with correct interservice URLs. The gateway is available at `http://localhost:8080`.

### Running the Game Client

```bash
cd game-client
mvn clean package
java -jar target/game-client-1.0.0.jar http://localhost:8080
```

The client connects to the API gateway. Pass a different URL as the first argument if the gateway is elsewhere.

### Running a single service locally

```bash
cd player-service
mvn clean package
java -jar target/player-service-1.0.0.jar
```

Repeat for each service

### Running all service tests

```bash
mvn test -f player-service/pom.xml; mvn test -f party-service/pom.xml; mvn test -f battle-service/pom.xml; mvn test -f pve-service/pom.xml; mvn test -f pvp-service/pom.xml; mvn test -f api-gateway/pom.xml
```

### cleaning all service test
```bash
mvn clean -f player-service/pom.xml; mvn clean -f party-service/pom.xml; mvn clean -f battle-service/pom.xml; mvn clean -f pve-service/pom.xml; mvn clean -f pvp-service/pom.xml; mvn clean -f api-gateway/pom.xml
```

### Building Docker images

Each service has its own Dockerfile. To build:

```bash
docker build -t legendsofsw/player-service:latest ./player-service
docker build -t legendsofsw/party-service:latest ./party-service
docker build -t legendsofsw/battle-service:latest ./battle-service
docker build -t legendsofsw/pve-service:latest ./pve-service
docker build -t legendsofsw/pvp-service:latest ./pvp-service
docker build -t legendsofsw/api-gateway:latest ./api-gateway
```

### Running with Docker

```bash
docker run -d -p 8081:8081 legendsofsw/player-service:latest
docker run -d -p 8082:8082 legendsofsw/party-service:latest
docker run -d -p 8083:8083 legendsofsw/battle-service:latest
docker run -d -p 8084:8084 legendsofsw/pve-service:latest
docker run -d -p 8085:8085 legendsofsw/pvp-service:latest
docker run -d -p 8080:8080 legendsofsw/api-gateway:latest
```

Or just use `docker-compose up -d --build` which handles networking and env vars automatically.

## Service Ports

| Service | Port |
|---------|------|
| API Gateway | 8080 |
| Player Service | 8081 |
| Party Service | 8082 |
| Battle Service | 8083 |
| PvE Service | 8084 |
| PvP Service | 8085 |

## Game Overview

Players create accounts build parties of heroes up to 5 battle through a 30 room PvE campaign or challenge other players in PvP Heroes can be one of four classes Order, Chaos, Warrior, Mage and can specialize or hybridize for unique abilities.

### Hero Classes and Hybrid System

Each hero starts at level 1 with a chosen class. Each level up you choose which class to invest in Reaching level 5 in a class grants specialization Reaching level 5 in two classes creates a hybrid with combined abilities

- **Priest** (Order+Order) - Heal affects all allies
- **Heretic** (Order+Chaos) - Fire Shield reflects damage
- **Paladin** (Order+Warrior) - Berserker heals self first
- **Prophet** (Order+Mage) - Friendly spells doubled
- **Invoker** (Chaos+Chaos) - Chain Lightning 50% falloff
- **Rogue** (Chaos+Warrior) - 50% bonus sneak attack
- **Sorcerer** (Chaos+Mage) - Fireball double damage
- **Knight** (Warrior+Warrior) - Berserker can stun
- **Warlock** (Warrior+Mage) - Attacks burn mana
- **Wizard** (Mage+Mage) - Replenish costs 40 mana

## API Documentation

See [docs/api-documentation.md](docs/api-documentation.md) for full API reference.

## CI/CD

The GitHub Actions pipeline runs on push to `main` or `develop`:
1. Runs unit tests for each service (player, party, battle, pve, pvp, api-gateway)
2. Builds the game client JAR
3. On `main` branch it builds and pushes Docker images to GitHub Container Registry
4. Runs integration tests using docker-compose (gateway health check, register/login flow)

## System Architecture

```
Game Client (Swing) --> API Gateway (8080) --> player-service (8081)
                                           --> party-service  (8082)
                                           --> battle-service (8083)
                                           --> pve-service    (8084)
                                           --> pvp-service    (8085)

pve-service --> player-service, party-service, battle-service
pvp-service --> player-service, party-service, battle-service
```

All client requests go through the API gateway. The gateway forwards requests to the correct backend based on URL prefix. PvE and PvP services also call other services directly for game logic (starting battles, reading party data, recording scores).

## Configuration

Service URLs are set in each service `application.properties` and can be overridden when deploying to containers.

### Environment Variables Reference

| Variable | Used By | Default | Description |
|----------|---------|---------|-------------|
| `GATEWAY_PLAYER-SERVICE_URL` | api-gateway | `http://localhost:8081` | Player service URL |
| `GATEWAY_PARTY-SERVICE_URL` | api-gateway | `http://localhost:8082` | Party service URL |
| `GATEWAY_BATTLE-SERVICE_URL` | api-gateway | `http://localhost:8083` | Battle service URL |
| `GATEWAY_PVE-SERVICE_URL` | api-gateway | `http://localhost:8084` | PvE service URL |
| `GATEWAY_PVP-SERVICE_URL` | api-gateway | `http://localhost:8085` | PvP service URL |
| `PVE_BATTLE-SERVICE_URL` | pve-service | `http://localhost:8083` | Battle service URL |
| `PVE_PARTY-SERVICE_URL` | pve-service | `http://localhost:8082` | Party service URL |
| `PVE_PLAYER-SERVICE_URL` | pve-service | `http://localhost:8081` | Player service URL |
| `PVP_PLAYER-SERVICE_URL` | pvp-service | `http://localhost:8081` | Player service URL |
| `PVP_BATTLE-SERVICE_URL` | pvp-service | `http://localhost:8083` | Battle service URL |
| `PVP_PARTY-SERVICE_URL` | pvp-service | `http://localhost:8082` | Party service URL |

When running with Docker Compose these are automatically set to container hostnames (e.g. `http://player-service:8081`). When running locally the defaults point to `localhost`.

### Database Configuration

Each service stores data in an H2 file database under its own `./data/` folder:

| Service | Database File | Console URL |
|---------|--------------|-------------|
| player-service | `./data/playerdb` | `http://localhost:8081/h2-console` |
| party-service | `./data/partydb` | `http://localhost:8082/h2-console` |
| battle-service | `./data/battledb` | `http://localhost:8083/h2-console` |
| pve-service | `./data/pvedb` | `http://localhost:8084/h2-console` |
| pvp-service | `./data/pvpdb` | `http://localhost:8085/h2-console` |

JDBC URL format: `jdbc:h2:file:./data/<dbname>`, username `sa`, no password.

## Troubleshooting

### Port already in use

If a port is taken, stop the conflicting process or change the port:

```bash
# Find what is using a port (Windows)
netstat -ano | findstr :8080

# Find what is using a port (Linux/Mac)
lsof -i :8080
```

To change a service port, edit its `application.properties` file and update the corresponding environment variables in `docker-compose.yml`.

### Docker out of memory

Running all 6 services in Docker needs at least 4 GB of RAM allocated to Docker. On Docker Desktop go to Settings → Resources → Memory and set it to 4 GB or higher.

### H2 database locked

If a service fails to start with a "Database may be already in use" error it means another process has a lock on the `.mv.db` file. Stop any other instances of that service or delete the `data/*.mv.db` file to start fresh.

### Services cannot reach each other locally

When running services individually (not via Docker Compose) make sure all services are running before starting pve-service or pvp-service since they depend on player-service, party-service, and battle-service. Start services in this order:

1. player-service (8081)
2. party-service (8082)
3. battle-service (8083)
4. pve-service (8084)
5. pvp-service (8085)
6. api-gateway (8080)
7. game-client

### Windows vs Linux path differences

On Windows use backslashes or quote paths with spaces. The Maven and Docker commands shown above work the same on both OS. The H2 database files use relative paths (`./data/`) which work cross-platform.

# Contribution Attestation

Nathan   - player-service, pve-service
Harris   - party-service, battle-service  
Muhammad - pvp-service

## Visual Aids

`Visual Diagram.png`

## Major Design Decisions 

We split the game into five separate services so each part can be built and run on its own. Each service has its own database and port. PvE and PvP call the other services over HTTP when they need player data or to start a battle. We use H2 so we dont need to install a separate database and we use file storage when running and in memory when testing. The battle service stores the whole battle state as JSON so we can save and load turns and unit stats without lots of tables. Each service has its own Dockerfile so the container split matches the service split. Combat logic lives in a stateless calculator so we can test it without touching the database or HTTP.

### Why H2 instead of a shared PostgreSQL

H2 is embedded in each service JAR so there is no external database server to install or configure. Each service owns its own data files which keeps the services fully independent. If one database goes down it does not take the others with it. For a game of this scale H2 handles the load fine and makes local development and testing simpler since you just run the JAR and the database is there. The tradeoff is that H2 is not ideal for heavy concurrent writes or production-scale workloads but for this project it keeps things lightweight and portable.

### Why REST over message queues

REST is synchronous and straightforward. When the PvE service needs to start a battle it sends a POST and gets the battle ID back immediately. With a message queue we would need to handle async responses, retries, and message ordering which adds complexity without a clear benefit here. Every inter-service call in our game is request-response (create a battle, get a party, record a score) so REST fits naturally. The tradeoff is tighter coupling between services since the caller blocks until the response comes back but for our use case the simplicity outweighs that.

### Why Java Swing instead of a web client

The whole backend is Java and Spring Boot so keeping the client in Java means one language across the entire project. Swing runs as a desktop application which avoids needing a web server, bundler, or browser framework. It connects to the API gateway over HTTP the same way a web frontend would. The tradeoff is that players need Java installed to run it and the UI is less polished than a modern web app but it meets the requirement of a functional GUI and keeps the tech stack consistent.

### Architecture tradeoffs

- **Stateless services with persistent databases** means any service instance can be replaced without losing data but inter-service HTTP calls add latency compared to a monolith.
- **Per-service databases** give full independence but mean we cannot do cross-service joins. We handle this by having services call each other's APIs.
- **JSON-serialized battle state** makes it easy to persist and restore an entire battle in one field but makes partial queries on battle data harder.
- **API gateway as a single entry point** simplifies the client since it only needs one URL but the gateway becomes a single point of failure.
- **Docker Compose for orchestration** is simple and works well for development and demos but for real production we would need Kubernetes for scaling and self-healing.








