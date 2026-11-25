# Weavo — Local Artisan Marketplace (Sem 5 Integrated Project)
**Weavo: From the loom to your living room.**

Weavo connects village craftspeople across Gujarat & India (Jamnagar Bandhani, Patan Patola, Kutchi Embroidery, Gundiyali Pottery, Dhamadka Ajrakh, Sankheda Lacquerware) directly with buyers using a **module-wise Java 21 + Spring Boot 3 architecture** backed by **Supabase PostgreSQL & Object Storage**.

## Architecture Modules (Multi-Module Maven)

1. `kalasetu-domain` — OOP `User` hierarchy (`Customer`, `Artisan`, `Admin`), Custom Exceptions (`OutOfStockException`, `PaymentFailedException`, `InvalidInputException`, `UnauthorizedActionException`), Security (`PasswordHasher`, `InputSanitizer`).
2. `kalasetu-payment` — `PaymentMethod` interface with Test-Mode-Only implementations (`UpiTestPayment`, `CardTestPayment`, `WalletTestPayment`).
3. `kalasetu-persistence` — `MarketplaceRepository` interface with live `SupabaseRestRepository` (PostgREST + RPC + Storage API) and `InMemoryMarketplaceRepository` fallback.
4. `kalasetu-concurrency` — `ExecutorService` thread pool + per-product `ReentrantLock` order processor + `BackgroundNotificationWorker` daemon thread.
5. `kalasetu-fileio` — CSV catalog export/import, text Order Invoice Receipts, and File Audit Logs with path-traversal protection.
6. `kalasetu-web` — Spring Boot 3 REST API, Actuator monitoring (`/actuator/health`), and interactive multi-module frontend.

## Quick Start

```bash
# 1. Configure environment (NEVER commit .env!)
cp .env.example .env
# Edit .env with your SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY

# 2. Build, run JUnit 5 tests, and generate JaCoCo coverage reports
mvn clean verify

# 3. Run locally with live Supabase
set -a; source .env; set +a
java -jar kalasetu-web/target/kalasetu-web-1.0.0.jar
```

Open `http://localhost:8080`.
