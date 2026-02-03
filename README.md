# Portfolio Management Dashboard Backend

Production-grade Spring Boot 3.x backend for portfolio management with layered architecture, comprehensive testing, and CI readiness.

## Tech Stack
- Java 17+
- Spring Boot 3.x (Web, Data JPA, Validation)
- MySQL
- Lombok
- SLF4J + Logback
- Maven
- JUnit 5 + Mockito

## Architecture
```
Controller → Service → Repository → Database
```
- Controllers: HTTP handling only
- Services: Business logic
- Repositories: Data access
- DTOs: API responses (no entity leakage)
- Centralized exception handling with structured error responses

## Database Schema
Create MySQL database and run:
```sql
CREATE TABLE assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    asset_type VARCHAR(20) NOT NULL,
    quantity DECIMAL(15,4) NOT NULL,
    avg_buy_price DECIMAL(15,2) NOT NULL,
    current_price DECIMAL(15,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## REST API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/assets` | List all assets (optional `?type=STOCK`) |
| POST | `/api/assets` | Create new asset |
| PUT | `/api/assets/{id}` | Update asset |
| DELETE | `/api/assets/{id}` | Delete asset |
| GET | `/api/dashboard` | Portfolio summary |
| GET | `/api/prices/update` | Trigger price refresh |

## Configuration
Set environment variables:
```bash
export DB_URL=jdbc:mysql://localhost:3306/portfolio
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```

## Build & Test
```bash
# Run tests
/Users/sruthi/.maven/maven-3.9.12/bin/mvn clean test

# Build project
/Users/sruthi/.maven/maven-3.9.12/bin/mvn clean install
```

## Run Application
```bash
/Users/sruthi/.maven/maven-3.9.12/bin/mvn spring-boot:run
```

## Testing
- **AssetServiceTest**: Unit tests with Mockito
- **AssetControllerTest**: Integration tests with MockMvc
- 9 tests covering success and failure scenarios
- All tests pass without database dependency

## Key Features
- ✅ Layered architecture (Controller → Service → Repository)
- ✅ Jakarta Validation for input validation
- ✅ Global exception handling with structured JSON errors
- ✅ SLF4J logging at controller and service levels
- ✅ TDD approach with comprehensive test coverage
- ✅ No hard-coded credentials
- ✅ CI/CD ready (GitHub Actions compatible)
- ✅ Production-grade code quality
