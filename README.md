# Portfolio Management Dashboard Backend

Production-grade Spring Boot 3.x backend for portfolio management with layered architecture, comprehensive testing, and CI readiness.

## Tech Stack
- Java 17+
- Spring Boot 3.x (Web, Data JPA, Validation)
- MySQL
- Apache POI (Excel parsing)
- Apache Commons CSV (CSV parsing)
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
| **POST** | **`/api/assets/import`** | **Import portfolio from Excel/CSV file** |
| GET | `/api/dashboard` | Portfolio summary |
| GET | `/api/prices/update` | Trigger price refresh |

## Portfolio Import Feature

Migrate your existing portfolio from Excel or CSV files! The application supports importing portfolio data from spreadsheets.

**Quick Start:**
```bash
curl -X POST http://localhost:8080/api/assets/import \
  -F "file=@portfolio.xlsx"
```

**Supported Formats:**
- Excel (.xlsx)
- CSV (.csv)

**Required Columns:**
1. Symbol
2. Name  
3. Type (STOCK, BOND, ETF, CRYPTO, CASH)
4. Quantity
5. Avg Buy Price
6. Current Price (optional)

📖 **Full Documentation:** See [docs/IMPORT_GUIDE.md](docs/IMPORT_GUIDE.md) for detailed instructions and examples.

📄 **Templates:** Download sample templates from `docs/templates/` directory.

## Configuration
Default database configuration in `application.yml`:
```yaml
url: jdbc:mysql://localhost:3306/portfolio
username: root
password: password
```

**To customize:** Edit [src/main/resources/application.yml](src/main/resources/application.yml) or override with environment variables:
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
- **FileImportServiceTest**: Import functionality tests
- 14 tests covering success and failure scenarios
- All tests pass without database dependency

## Key Features
- ✅ Layered architecture (Controller → Service → Repository)
- ✅ **Excel/CSV Portfolio Import** - Migrate from spreadsheets
- ✅ Jakarta Validation for input validation
- ✅ Global exception handling with structured JSON errors
- ✅ SLF4J logging at controller and service levels
- ✅ TDD approach with comprehensive test coverage
- ✅ No hard-coded credentials
- ✅ CI/CD ready (GitHub Actions compatible)
- ✅ Production-grade code quality
