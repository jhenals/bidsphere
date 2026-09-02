# BidSphere — Distributed Real-Time Auction Platform
BidSphere is a distributed auction platform that uses Java/Spring Boot, Redis, Kafka, WebSockets, MySQL, and Docker, implementing concurrency-safe bid processing, distributed locking, event-driven workflows, auction expiration, simulated payment processing, and real-time client updates. Developed concurrent load tests to validate system consistency under high bid contention and implemented idempotent event processing for eventual-consistency scenarios.


# Run the Project
I’ll verify the two local prerequisites that control startup, then give you the exact PowerShell sequence for PostgreSQL and the backend, including the Java 25 issue found in the current workspace.

Ran terminal command: docker --version; docker compose version

Ran terminal command: java -version; Get-ChildItem 'C:\Program Files\Java' -Directory -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Name

**Start the project**

Open PowerShell in the repository root:

```powershell
cd C:\progetti\bidsphere\bidsphere
docker compose -f infrastructure\docker-compose.yml up -d postgres
```

The PostgreSQL database will run on `localhost:5432`.

The project requires Java 25. Java 25 is installed locally, so configure it for the current PowerShell session:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.2"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Start the backend:

```powershell
cd bidsphere-backend
.\mvnw.cmd spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```

Flyway will automatically create the database schema from `V1__create_core_schema.sql`.

**Create a test user**

There is currently no user REST controller, so insert one manually:

```powershell
docker exec -it bidsphere-postgres psql -U bidsphere -d bidsphere
```

Then run:

```sql
INSERT INTO app_users (email, display_name)
VALUES ('seller@example.com', 'Seller')
RETURNING id;
```

Use the returned UUID as `X-Seller-Id` or `X-Bidder-Id`.

To stop everything:

```powershell
docker compose -f infrastructure\docker-compose.yml down
```
