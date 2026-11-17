# ATA Vehicle Booking System

A microservices-based vehicle booking platform built with Spring Boot, Spring Cloud, and React.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     React Frontend                       │
│                    (Port: 5173)                          │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                    API Gateway                           │
│                    (Port: 8080)                          │
│              JWT Auth Filter + Routing                   │
└──┬──────────┬──────────┬──────────┬──────────────────────┘
   │          │          │          │
┌──▼──┐  ┌───▼──┐  ┌────▼──┐  ┌───▼──────┐
│Auth │  │Admin │  │Booking│  │Customer  │
│8081 │  │8082  │  │8083   │  │8084      │
└──┬──┘  └───┬──┘  └────┬──┘  └───┬──────┘
   │          │          │          │
   └──────────┴──────────┴──────────┘
                    │
         ┌──────────▼──────────┐
         │    Eureka Server     │
         │    (Port: 8761)      │
         └─────────────────────┘
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| eureka-server | 8761 | Service discovery |
| api-gateway | 8080 | Single entry point, JWT routing |
| auth-service | 8081 | Registration, login, JWT issuance |
| admin-service | 8082 | Manage customers, drivers, vehicles, routes |
| booking-service | 8083 | Create and manage bookings |
| customer-service | 8084 | Customer profile management |
| frontend | 5173 | React + Vite + Tailwind UI |

## Tech Stack

**Backend**
- Java 17, Spring Boot 3.2, Spring Cloud 2023
- Spring Security + JWT (JJWT 0.11.5)
- Spring Data JPA + PostgreSQL
- Spring Cloud Gateway, Eureka, OpenFeign
- SpringDoc OpenAPI (Swagger UI)
- Lombok, JaCoCo, PMD

**Frontend**
- React 19, TypeScript, Vite
- Tailwind CSS v4, React Router v7
- Axios, Lucide React

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Node.js 18+

## Database Setup

Create the required PostgreSQL databases:

```sql
CREATE DATABASE ata_auth;
CREATE DATABASE ata_admin;
CREATE DATABASE ata_booking;
CREATE DATABASE ata_customer;
```

Default credentials used in `application.yml`:
- Username: `postgres`
- Password: `sam123`

> Update credentials in each service's `application.yml` before running.

## Running Locally

### 1. Start Eureka Server
```bash
cd eureka-server
mvn spring-boot:run
```
Access: http://localhost:8761 (user: `eureka`, pass: `eureka123`)

### 2. Start Backend Services (in order)
```bash
cd api-gateway && mvn spring-boot:run
cd auth-service && mvn spring-boot:run
cd admin-service && mvn spring-boot:run
cd booking-service && mvn spring-boot:run
cd customer-service && mvn spring-boot:run
```

### 3. Start Frontend
```bash
cd frontend
npm install
npm run dev
```
Access: http://localhost:5173

### Build All (from root)
```bash
mvn clean install
```

## API Endpoints

### Auth Service (`/api/auth`)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, returns JWT |
| POST | `/api/auth/refresh` | Refresh token |
| POST | `/api/auth/forgot-password` | Forgot password |
| POST | `/api/auth/change-password` | Change password |

### Admin Service (`/api/admin`) — requires JWT
| Method | Path | Description |
|--------|------|-------------|
| GET/POST | `/api/admin/customers` | List / create customers |
| GET/PUT/DELETE | `/api/admin/customers/{id}` | Get / update / delete |
| GET/POST | `/api/admin/drivers` | List / create drivers |
| GET/PUT/DELETE | `/api/admin/drivers/{id}` | Get / update / delete |
| GET/POST | `/api/admin/vehicles` | List / create vehicles |
| GET/PUT/DELETE | `/api/admin/vehicles/{id}` | Get / update / delete |
| GET/POST | `/api/admin/routes` | List / create routes |
| GET/PUT/DELETE | `/api/admin/routes/{id}` | Get / update / delete |

### Booking Service (`/api/booking`) — requires JWT
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/booking/search` | Search available routes |
| POST | `/api/booking` | Create booking |
| GET | `/api/booking/my` | My bookings |
| PUT | `/api/booking/{id}/cancel` | Cancel booking |

### Customer Service (`/api/customers`) — requires JWT
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/customers/profile` | Get profile |
| PUT | `/api/customers/profile` | Update profile |
| GET | `/api/customers/{id}/bookings` | Customer bookings |

## Swagger UI

Each service exposes Swagger UI at `/swagger-ui.html`:
- Auth: http://localhost:8081/swagger-ui.html
- Admin: http://localhost:8082/swagger-ui.html
- Booking: http://localhost:8083/swagger-ui.html
- Customer: http://localhost:8084/swagger-ui.html

## Running Tests

```bash
# All services
mvn test

# Single service
cd admin-service && mvn test

# With coverage report
mvn verify
# Report: target/site/jacoco/index.html
```

## Project Structure

```
ata-vehicle-booking/
├── eureka-server/
├── api-gateway/
├── auth-service/
├── admin-service/
├── booking-service/
├── customer-service/
├── frontend/
└── pom.xml
```

## Environment Variables

For production, override these via environment variables or a config server:

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/ata_*` | DB URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | `sam123` | DB password |
| `JWT_SECRET` | (see yml) | JWT signing key |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | `http://eureka:eureka123@localhost:8761/eureka/` | Eureka URL |

## License

MIT
