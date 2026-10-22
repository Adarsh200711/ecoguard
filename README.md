# EcoGuard - Pallikaranai Marshland Waste Management System

A digital waste reporting and wetland conservation platform built with **Java 21**, **Spring Boot 4**, **Spring Data JPA**, and **MySQL**. EcoGuard connects citizens, environmental volunteers, and wetland conservation rangers to monitor, report, and remediate illegal dumping, plastic waste, and industrial effluents in Chennai's Ramsar Wetland Site #2484 (Pallikaranai Marshland).

---

## Prerequisites

- **Java JDK**: Version 21 LTS installed (`java -version`)
- **Maven**: Included via Maven Wrapper (`.\mvnw.cmd`)
- **MySQL Server**: Version 8.0+ (optional for production mode; in-memory H2 profile available for standalone/offline runs)
- **MySQL Workbench**: Recommended for viewing and verifying database records

---

## Database Setup (MySQL)

1. Open **MySQL Workbench** and connect to your local MySQL instance (`localhost:3306`).
2. Open the included script: `src/main/resources/schema-mysql.sql`.
3. Execute the script. It creates the database and required tables non-destructively:
   - Database: `ecoguard`
   - Tables:
     - `waste_reports`: Core incident reports (category, location, zone, severity, photo reference, status)
     - `status_history`: Remediation audit trail recording status transitions and officer notes
     - `waste_categories`: Standardized categories (Plastic, Industrial, E-Waste, Organic, Construction, General)
     - `users`: User credentials for Admin, Volunteer, and Citizen roles

---

## Configuration & Credentials

Configuration is defined in [`src/main/resources/application.properties`](src/main/resources/application.properties).

Environment variables are supported to avoid exposing credentials in source code:

| Variable | Description | Default Value |
|---|---|---|
| `MYSQL_URL` | JDBC Connection URL | `jdbc:mysql://localhost:3306/ecoguard?...` |
| `MYSQL_USER` | MySQL Username | `root` |
| `MYSQL_PASSWORD` | MySQL Password | *(empty / provided at runtime)* |

---

## How to Run the Application on Windows

### Mode 1: With MySQL Database (Production Mode)

1. Ensure MySQL Server is running on port 3306.
2. In **Windows PowerShell**, set your credentials and start the application:
   ```powershell
   $env:MYSQL_USER = "root"
   $env:MYSQL_PASSWORD = "your_actual_mysql_password"
   .\mvnw.cmd spring-boot:run
   ```
3. Open your browser and navigate to:
   - **Home Page**: [http://localhost:8080/index.html](http://localhost:8080/index.html)
   - **Report Waste Incident**: [http://localhost:8080/report.html](http://localhost:8080/report.html)
   - **Monitoring Dashboard**: [http://localhost:8080/dashboard.html](http://localhost:8080/dashboard.html)

---

### Mode 2: Standalone / Offline Mode (In-Memory H2 Database)

If MySQL Server is not currently installed or running, you can run the application immediately in standalone mode using the H2 profile:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"
```

- Pre-seeds sample reports and categories via `DataInitializer`.
- Web Console accessible at: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - JDBC URL: `jdbc:h2:mem:ecoguard`
  - User: `sa`
  - Password: *(blank)*

---

## REST API Reference

### 1. Submit Waste Report (JSON)
- **Endpoint**: `POST /api/reports`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "category": "Plastic Waste",
    "description": "Accumulation of bottles near culvert 4 beneath Radial Road flyover",
    "location": "200 Feet Radial Road, Pillar #14",
    "zone": "Zone A - Radial Road Corridor",
    "severity": "HIGH",
    "reporterName": "Adarsh S",
    "reporterEmail": "adarsh@gmail.com",
    "reporterPhone": "+91 98400 12345"
  }
  ```
- **Response**: `201 Created` with generated ticket ID and saved record.

### 2. Submit Waste Report with Photo (Multipart)
- **Endpoint**: `POST /api/reports`
- **Content-Type**: `multipart/form-data`
- **Fields**:
  - `category` (required)
  - `description` (required, min 10 chars)
  - `location` (required)
  - `zone` (optional)
  - `severity` (optional: LOW, MEDIUM, HIGH, CRITICAL)
  - `reporterName`, `reporterEmail`, `reporterPhone` (optional)
  - `photo` (optional: JPG, PNG, WebP image up to 10MB)

### 3. List Waste Reports
- **Endpoint**: `GET /api/reports`
- **Query Parameters**:
  - `status`: SUBMITTED, UNDER_REVIEW, IN_PROGRESS, RESOLVED, REJECTED
  - `category`: Filter by category name
  - `zone`: Filter by sector / zone
  - `search`: Keyword search matching location or description

### 4. Get Report by ID
- **Endpoint**: `GET /api/reports/{id}`
- **Response**: `200 OK` with report details, or `404 Not Found`.

### 5. Get Report Status Audit History
- **Endpoint**: `GET /api/reports/{id}/history`
- **Response**: List of status changes, who made the update, and comments.

### 6. Update Report Status
- **Endpoint**: `PATCH /api/reports/{id}/status`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "status": "IN_PROGRESS",
    "updatedBy": "Wetland Patrol Squad A",
    "comment": "Dispatched crew with containment booms"
  }
  ```

### 7. Dashboard Metrics Summary
- **Endpoint**: `GET /api/dashboard/summary`
- **Response**: Real-time KPI counts, resolution rate, category breakdown, and sector heat density.

---

## Automated Testing

Run the automated test suite with the Maven wrapper:

```powershell
.\mvnw.cmd test
```

All integration tests use the dedicated H2 profile (`@ActiveProfiles("h2")`), ensuring that test runs are completely self-contained and do not require a live MySQL server.

---

## Building the Production JAR

To compile, test, and package the application into an executable JAR:

```powershell
.\mvnw.cmd clean package
```

The resulting standalone JAR file is located at:
`target/ecoguard-0.0.1-SNAPSHOT.jar`

Run it with:
```powershell
java -jar target/ecoguard-0.0.1-SNAPSHOT.jar
```
