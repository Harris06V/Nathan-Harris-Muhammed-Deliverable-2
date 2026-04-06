# Legends of Sword and Wand

Made by Nathan, Harris, and Muhammad 

## Project Structure
 **Legends of Sword and Wand**
   **playerservice**: User authentication, profiles, saved parties, leaderboard
   **partyservice**: Hero management, classes, leveling, items
   **battleservice**: Core combat system, turnbased battles
   **pveservice**: PvE campaign, room encounters, inns
   **pvpservice**: PvP invitations, matches, league
   **apigateway**: Central routing gateway for all backend services
   **gameclient**: Java Swing desktop GUI application
   **docs**: API documentation
   **.github/workflows**: CI/CD pipeline
   **dockercompose.yml**: Multicontainer deployment config
   **README.md**
   **VisualDiagram.png**

## Tech Stack

 **Language:** Java 17
 **Framework:** Spring Boot 3.2.5
 **Build Tool:** Maven
 **Database:** H2 
 **Containerization:** Docker 
 **CI/CD:** GitHub Actions
 **Testing:** JUnit 5, Mockito

## Prerequisites


JDK  17.0.x (Temurin recommended)  https://adoptium.net/temurin/releases/ 
Maven  3.9.x  https://maven.apache.org/download.cgi 
Docker  24.0+  https://docs.docker.com/getdocker/ 
Docker Compose  2.20+ (included with Docker Desktop) 
Git  2.40+  https://gitscm.com/downloads 

Verify installations:

```bash
java version       # should show 17.x
mvn version        # should show 3.9.x
docker version    # should show 24.x+
docker compose version  # should show 2.20+
```

**Note:** Docker Desktop on Windows/Mac includes Docker Compose. On Linux install the `dockercomposeplugin` package separately.

## How to Build and Run

### Running all services with Docker Compose

The easiest way to run everything

```bash
dockercompose up d build
```

This starts all 6 services (player, party, battle, pve, pvp, apigateway) with correct interservice URLs. The gateway is available at `http://localhost:8080`.

### Running the Game Client

```bash
cd gameclient
mvn clean package
java jar target/gameclient1.0.0.jar http://localhost:8080
```

The client connects to the API gateway. Pass a different URL as the first argument if the gateway is elsewhere.

### Running a single service locally

```bash
cd playerservice
mvn clean package
java jar target/playerservice1.0.0.jar
```

Repeat for each service

### Running all service tests

```bash
mvn test f playerservice/pom.xml; mvn test f partyservice/pom.xml; mvn test f battleservice/pom.xml; mvn test f pveservice/pom.xml; mvn test f pvpservice/pom.xml; mvn test f apigateway/pom.xml
```

### cleaning all service test
```bash
mvn clean f playerservice/pom.xml; mvn clean f partyservice/pom.xml; mvn clean f battleservice/pom.xml; mvn clean f pveservice/pom.xml; mvn clean f pvpservice/pom.xml; mvn clean f apigateway/pom.xml
```

### Building Docker images

Each service has its own Dockerfile. To build:

```bash
docker build t legendsofsw/playerservice:latest ./playerservice
docker build t legendsofsw/partyservice:latest ./partyservice
docker build t legendsofsw/battleservice:latest ./battleservice
docker build t legendsofsw/pveservice:latest ./pveservice
docker build t legendsofsw/pvpservice:latest ./pvpservice
docker build t legendsofsw/apigateway:latest ./apigateway
```

### Running with Docker

```bash
docker run d p 8081:8081 legendsofsw/playerservice:latest
docker run d p 8082:8082 legendsofsw/partyservice:latest
docker run d p 8083:8083 legendsofsw/battleservice:latest
docker run d p 8084:8084 legendsofsw/pveservice:latest
docker run d p 8085:8085 legendsofsw/pvpservice:latest
docker run d p 8080:8080 legendsofsw/apigateway:latest
```

Or just use `dockercompose up d build` which handles networking and env vars automatically.

## Service Ports

| Service | Port |
|||
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

 **Priest** (Order+Order)  Heal affects all allies
 **Heretic** (Order+Chaos)  Fire Shield reflects damage
 **Paladin** (Order+Warrior)  Berserker heals self first
 **Prophet** (Order+Mage)  Friendly spells doubled
 **Invoker** (Chaos+Chaos)  Chain Lightning 50% falloff
 **Rogue** (Chaos+Warrior)  50% bonus sneak attack
 **Sorcerer** (Chaos+Mage)  Fireball double damage
 **Knight** (Warrior+Warrior)  Berserker can stun
 **Warlock** (Warrior+Mage)  Attacks burn mana
 **Wizard** (Mage+Mage)  Replenish costs 40 mana

## API Documentation

See [docs/apidocumentation.md](docs/apidocumentation.md) for full API reference.

## CI/CD

The GitHub Actions pipeline runs on push to `main` or `develop`:
1. Runs unit tests for each service (player, party, battle, pve, pvp, apigateway)
2. Builds the game client JAR
3. On `main` branch it builds and pushes Docker images to GitHub Container Registry
4. Runs integration tests using dockercompose (gateway health check, register/login flow)

## System Architecture

```
Game Client (Swing) > API Gateway (8080) > playerservice (8081)
                                           > partyservice  (8082)
                                           > battleservice (8083)
                                           > pveservice    (8084)
                                           > pvpservice    (8085)

pveservice > playerservice, partyservice, battleservice
pvpservice > playerservice, partyservice, battleservice
```

All client requests go through the API gateway. The gateway forwards requests to the correct backend based on URL prefix. PvE and PvP services also call other services directly for game logic (starting battles, reading party data, recording scores).

## Configuration

Service URLs are set in each service `application.properties` and can be overridden when deploying to containers.

### Environment Variables Reference

| Variable | Used By | Default | Description |
|||||
| `GATEWAY_PLAYERSERVICE_URL` | apigateway | `http://localhost:8081` | Player service URL |
| `GATEWAY_PARTYSERVICE_URL` | apigateway | `http://localhost:8082` | Party service URL |
| `GATEWAY_BATTLESERVICE_URL` | apigateway | `http://localhost:8083` | Battle service URL |
| `GATEWAY_PVESERVICE_URL` | apigateway | `http://localhost:8084` | PvE service URL |
| `GATEWAY_PVPSERVICE_URL` | apigateway | `http://localhost:8085` | PvP service URL |
| `PVE_BATTLESERVICE_URL` | pveservice | `http://localhost:8083` | Battle service URL |
| `PVE_PARTYSERVICE_URL` | pveservice | `http://localhost:8082` | Party service URL |
| `PVE_PLAYERSERVICE_URL` | pveservice | `http://localhost:8081` | Player service URL |
| `PVP_PLAYERSERVICE_URL` | pvpservice | `http://localhost:8081` | Player service URL |
| `PVP_BATTLESERVICE_URL` | pvpservice | `http://localhost:8083` | Battle service URL |
| `PVP_PARTYSERVICE_URL` | pvpservice | `http://localhost:8082` | Party service URL |

When running with Docker Compose these are automatically set to container hostnames (e.g. `http://playerservice:8081`). When running locally the defaults point to `localhost`.

### Database Configuration

Each service stores data in an H2 file database under its own `./data/` folder:

| Service | Database File | Console URL |
||||
| playerservice | `./data/playerdb` | `http://localhost:8081/h2console` |
| partyservice | `./data/partydb` | `http://localhost:8082/h2console` |
| battleservice | `./data/battledb` | `http://localhost:8083/h2console` |
| pveservice | `./data/pvedb` | `http://localhost:8084/h2console` |
| pvpservice | `./data/pvpdb` | `http://localhost:8085/h2console` |

JDBC URL format: `jdbc:h2:file:./data/<dbname>`, username `sa`, no password.

## Troubleshooting

### Port already in use

If a port is taken, stop the conflicting process or change the port:

```bash
# Find what is using a port (Windows)
netstat ano | findstr :8080

# Find what is using a port (Linux/Mac)
lsof i :8080
```

To change a service port, edit its `application.properties` file and update the corresponding environment variables in `dockercompose.yml`.

### Docker out of memory

Running all 6 services in Docker needs at least 4 GB of RAM allocated to Docker. On Docker Desktop go to Settings → Resources → Memory and set it to 4 GB or higher.

### H2 database locked

If a service fails to start with a "Database may be already in use" error it means another process has a lock on the `.mv.db` file. Stop any other instances of that service or delete the `data/*.mv.db` file to start fresh.

### Services cannot reach each other locally

When running services individually (not via Docker Compose) make sure all services are running before starting pveservice or pvpservice since they depend on playerservice, partyservice, and battleservice. Start services in this order:

1. playerservice (8081)
2. partyservice (8082)
3. battleservice (8083)
4. pveservice (8084)
5. pvpservice (8085)
6. apigateway (8080)
7. gameclient

### Windows vs Linux path differences

On Windows use backslashes or quote paths with spaces. The Maven and Docker commands shown above work the same on both OS. The H2 database files use relative paths (`./data/`) which work crossplatform.

# Contribution Attestation

Nathan    playerservice, pveservice
Harris    partyservice, battleservice  
Muhammad  pvpservice

## Diagrams 

Component Diagram (UML 1)
All client requests go through the API Gateway at port 8080, which routes them to the correct service by URL prefix. PvE and PvP services also call Battle, Party, and Player services directly over REST for game logic. Each service has its own isolated database.
PvE Campaign Workflow (UML 2)
The PvE service drives the campaign loop it generates the room type, calls Battle Service to run fights, then calls Party Service to distribute exp/gold. At room 30 it calculates the final score and posts it to Player Service.
Battle System Workflow (UML 3)
Battle Service receives both teams, builds a turn order, then processes each unit's action (attack, defend, wait, or special) in a loop. It tracks HP, mana, shields, and stuns until one team is fully dead, then returns the result.
PvP Sequence Diagram (UML 4)
Player 1 sends an invite via the Gateway to PvP Service, which validates the username against Player Service. When Player 2 accepts, PvP Service creates a match and calls Battle Service to run it. On completion, PvP Service records the win/loss to Player Service and updates league standings.

HTTP Interfaces (Visual Diagram)
Every service exposes REST endpoints over HTTP with JSON bodies. For example, to start a battle you POST to /api/battles with a JSON payload containing two unit arrays, and you get back a battleId. To take a turn you POST to /api/battles/{id}/action with the action type and target, and receive the updated battle state as JSON. All requests and responses use Content-Type: application/json. The full list of endpoints, fields, and response shapes is documented in docs/api-documentation.md.

## Major Design Decisions 

We split the game into five separate services so each part can be built and run on its own. Each service has its own database and port. PvE and PvP call the other services over HTTP when they need player data or to start a battle. We use H2 so we dont need to install a separate database and we use file storage when running and in memory when testing. The battle service stores the whole battle state as JSON so we can save and load turns and unit stats without lots of tables. Each service has its own Dockerfile so the container split matches the service split. Combat logic lives in a stateless calculator so we can test it without touching the database or HTTP.

### Why H2 instead of a shared PostgreSQL

H2 is embedded in each service JAR so there is no external database server to install or configure. Each service owns its own data files which keeps the services fully independent. If one database goes down it does not take the others with it. For a game of this scale H2 handles the load fine and makes local development and testing simpler since you just run the JAR and the database is there. The tradeoff is that H2 is not ideal for heavy concurrent writes or productionscale workloads but for this project it keeps things lightweight and portable.

### Why REST over message queues

REST is synchronous and straightforward. When the PvE service needs to start a battle it sends a POST and gets the battle ID back immediately. With a message queue we would need to handle async responses, retries, and message ordering which adds complexity without a clear benefit here. Every interservice call in our game is requestresponse (create a battle, get a party, record a score) so REST fits naturally. The tradeoff is tighter coupling between services since the caller blocks until the response comes back but for our use case the simplicity outweighs that.

### Why Java Swing instead of a web client

The whole backend is Java and Spring Boot so keeping the client in Java means one language across the entire project. Swing runs as a desktop application which avoids needing a web server, bundler, or browser framework. It connects to the API gateway over HTTP the same way a web frontend would. The tradeoff is that players need Java installed to run it and the UI is less polished than a modern web app but it meets the requirement of a functional GUI and keeps the tech stack consistent.

### Architecture tradeoffs

 **Stateless services with persistent databases** means any service instance can be replaced without losing data but interservice HTTP calls add latency compared to a monolith.
 **Perservice databases** give full independence but mean we cannot do crossservice joins. We handle this by having services call each other's APIs.
 **JSONserialized battle state** makes it easy to persist and restore an entire battle in one field but makes partial queries on battle data harder.
 **API gateway as a single entry point** simplifies the client since it only needs one URL but the gateway becomes a single point of failure.
 **Docker Compose for orchestration** is simple and works well for development and demos but for real production we would need Kubernetes for scaling and selfhealing.








