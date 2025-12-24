# Subscription Service

A production-ready subscription management service built with **Ports and Adapters (Hexagonal Architecture)** - a custom Java framework without Spring Boot dependencies.

## 📑 Table of Contents

- [Architecture](#-architecture)
- [Quick Start](#-quick-start)
- [API Endpoints](#-api-endpoints)
- [Testing APIs](#-testing-apis)
- [Configuration](#-configuration)
- [Project Structure](#-project-structure)
- [Documentation](#-documentation)
- [Postman Collection](#postman-collection)
- [Development](#-development)
- [Docker](#-docker)
- [Security Features](#-security-features)
- [Monitoring](#-monitoring)

---

## 🔗 Quick Links

- 📖 [Architecture Documentation](ARCHITECTURE.md) - Detailed system architecture
- 📋 [API Documentation](API_DOCUMENTATION.md) - Complete API reference
- 🗄️ [Database Design](DATABASE_DESIGN.md) - Database schema and design
- 🚀 [Quick Start Guide](QUICK_START.md) - Step-by-step setup
- 📬 [Postman Setup Guide](POSTMAN_SETUP.md) - Postman collection setup
- 📦 [Postman Collection](Subscription_Service.postman_collection.json) - Import and test all APIs

## 🏗️ Architecture

This service follows **Hexagonal Architecture (Ports and Adapters)** pattern, providing a clean separation of concerns and framework independence.

### Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    Client Layer                           │
│  (React Frontend, Mobile Apps, Hardware Devices)         │
└───────────────────────┬─────────────────────────────────┘
                        │
                        │ HTTP/REST API
                        │
┌───────────────────────▼─────────────────────────────────┐
│              Infrastructure Layer (Adapters)            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
│  │ HTTP Adapter │  │  DB Adapter  │  │ Cache Adapter│ │
│  │ (Controllers)│  │ (JDBC Repos) │  │  (Redis)     │ │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘ │
└─────────┼──────────────────┼─────────────────┼─────────┘
          │                  │                 │
          │                  │                 │
┌─────────▼──────────────────▼─────────────────▼─────────┐
│          Application Layer (Use Cases)                 │
│  ┌─────────────────────────────────────────────────┐  │
│  │  Ports (Interfaces)                             │  │
│  │  • UserServicePort                              │  │
│  │  • SubscriptionServicePort                      │  │
│  │  • BillingServicePort                           │  │
│  │  • DeviceServicePort                            │  │
│  └─────────────────────────────────────────────────┘  │
└───────────────────────┬───────────────────────────────┘
                        │
┌───────────────────────▼───────────────────────────────┐
│              Domain Layer (Business Models)            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐           │
│  │   User   │  │Subscription│ │  Device  │           │
│  │  Model   │  │   Model   │ │  Model   │           │
│  └──────────┘  └──────────┘  └──────────┘           │
└───────────────────────────────────────────────────────┘
```

### Architecture Layers

- **Domain Layer**: Pure business models with no external dependencies
- **Application Layer**: Use cases and business logic (depends on ports/interfaces only)
- **Infrastructure Layer**: Adapters implementing ports (HTTP, Database, Security, Cache)

### Key Features

- ✅ **Clean Architecture** - Ports and Adapters pattern
- ✅ **Framework Independent** - Custom framework without Spring Boot
- ✅ **Security** - JWT authentication, rate limiting, password hashing
- ✅ **Performance** - Redis caching, connection pooling
- ✅ **Monitoring** - Health checks, metrics collection
- ✅ **Reliability** - Transaction management, error handling
- ✅ **Deployment** - Docker support, Docker Compose

📖 **For detailed architecture documentation, see [ARCHITECTURE.md](ARCHITECTURE.md)**

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Gradle 8.8+
- Redis (optional, for caching)
- PostgreSQL/MySQL/H2 (for database)

### Method 1: Using Gradle (Recommended)

```bash
./gradlew run
```

### Method 2: Using Docker Compose

```bash
# Start all services (app + Redis + PostgreSQL)
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

### Method 3: Using Startup Script

```bash
./run.sh
```

### Method 4: Build and Run JAR

```bash
./gradlew jar
java -jar build/libs/subscription-service-1.0.0-SNAPSHOT.jar
```

### Verify It's Running

```bash
curl http://localhost:8080/health
```

Expected response:
```json
{"status":"UP","service":"subscription-service"}
```

📖 **For detailed quick start guide, see [QUICK_START.md](QUICK_START.md)**

## 📋 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT tokens
- `POST /api/auth/refresh` - Refresh access token
- `POST /api/auth/logout` - Logout (blacklist token)

### Users
- `GET /api/users/{id}` - Get user by ID (requires auth)
- `GET /api/users` - Get all users (requires auth)

### Health & Monitoring
- `GET /health` - Basic health check
- `GET /health/detailed` - Detailed health with component status
- `GET /metrics` - Request metrics (counts, response times, errors)

## 🔧 Configuration

Configuration is in `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

database:
  type: h2  # h2, mysql, postgresql
  url: jdbc:h2:mem:subscription_service

jwt:
  secret: your-secret-key
  accessTokenExpiration: 900000  # 15 minutes
  refreshTokenExpiration: 604800000  # 7 days

redis:
  enabled: true
  host: localhost
  port: 6379

rateLimit:
  enabled: true
  maxRequestsPerMinute: 100
```

## 🧪 Testing APIs

### Using Postman (Recommended)

The easiest way to test all APIs is using the included Postman collection:

1. **Import Collection**: Open Postman → Import → Select `Subscription_Service.postman_collection.json`
2. **Setup Environment**: Create environment with `baseUrl: http://localhost:8080`
3. **Start Testing**: 
   - Begin with **Authentication → Login** (uses default admin credentials)
   - Token is automatically saved for subsequent requests
   - Explore all endpoints organized by category

📦 **Postman Collection**: [`Subscription_Service.postman_collection.json`](Subscription_Service.postman_collection.json)  
📖 **Setup Guide**: [POSTMAN_SETUP.md](POSTMAN_SETUP.md)

### Using cURL

#### Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "mobileNumber": "+1234567890"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

#### Get User (with token)
```bash
TOKEN="your-access-token"
curl http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer $TOKEN"
```

#### Health Check
```bash
curl http://localhost:8080/health/detailed
```

#### Metrics
```bash
curl http://localhost:8080/metrics
```

📖 **For complete API documentation with all endpoints, see [API_DOCUMENTATION.md](API_DOCUMENTATION.md)**

## 📁 Project Structure

```
SubscriptionService/
├── framework-core/          # Reusable framework
│   └── src/main/java/com/framework/core/
│       ├── config/         # Configuration loading
│       ├── di/             # Dependency injection
│       ├── http/           # HTTP server abstraction
│       ├── persistence/    # Database factory
│       └── bootstrap/      # Application bootstrap
│
├── src/main/java/com/subscription/subscriptionservice/
│   ├── domain/             # Domain models (pure POJOs)
│   ├── application/        # Use cases and ports
│   └── infrastructure/     # Adapters (HTTP, JDBC, JWT, Redis)
│
└── src/main/resources/
    ├── application.yml     # Configuration
    └── db/                 # Database migrations (Liquibase)
```

## 🏛️ Architecture Principles

1. **Dependency Inversion**: Application depends on interfaces (ports), not implementations
2. **Separation of Concerns**: Domain, Application, Infrastructure layers
3. **Framework Independence**: No Spring Boot, custom framework
4. **Configuration-Driven**: Everything configured in YAML
5. **Testability**: Easy to mock ports and test business logic

## 🔐 Security Features

- JWT-based authentication
- Password hashing (BCrypt)
- Token blacklisting
- Rate limiting (100 requests/minute per IP)
- Input validation
- SQL injection prevention (parameterized queries)

## 📊 Monitoring

- **Health Checks**: `/health` and `/health/detailed`
- **Metrics**: `/metrics` endpoint with request counts, response times, error rates
- **Logging**: Comprehensive logging with SLF4J/Logback

## 🐳 Docker

### Build Image
```bash
docker build -t subscription-service .
```

### Run Container
```bash
docker run -p 8080:8080 subscription-service
```

### Docker Compose
```bash
# Start all services
docker-compose up -d

# Includes:
# - Application
# - Redis (cache)
# - PostgreSQL (database)
```

## 📚 Documentation

### Essential Documentation

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Comprehensive architecture documentation
  - System architecture overview
  - Architecture patterns (Layered, DDD, Repository, Service Layer)
  - Security architecture (Authentication & Authorization flows)
  - Data flow diagrams
  - Database architecture and ER diagrams
  - Transaction management
  - Deployment architecture
  - Technology stack details

- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Complete API reference
  - All API endpoints with request/response examples
  - Code flow diagrams
  - Component interactions
  - Authentication flows
  - Role-based access control

- **[DATABASE_DESIGN.md](DATABASE_DESIGN.md)** - Database schema documentation
  - Complete database schema
  - Entity relationships
  - Table structures and indexes
  - Normalization strategy
  - Query optimization

- **[QUICK_START.md](QUICK_START.md)** - Quick start guide
  - Step-by-step setup instructions
  - Configuration guide
  - Troubleshooting tips

- **[POSTMAN_SETUP.md](POSTMAN_SETUP.md)** - Postman collection guide
  - How to import the Postman collection
  - Environment variables setup
  - Testing workflow
  - Example requests

### Postman Collection

The project includes a complete Postman collection for testing all APIs:

📦 **File**: [`Subscription_Service.postman_collection.json`](Subscription_Service.postman_collection.json)

**Features:**
- ✅ All API endpoints organized by category
- ✅ Pre-configured authentication flows
- ✅ Auto-save token functionality
- ✅ Environment variables setup
- ✅ Example requests for all endpoints
- ✅ Role-based testing scenarios

**Quick Import:**
1. Open Postman
2. Click **Import** → Select `Subscription_Service.postman_collection.json`
3. Create environment with `baseUrl: http://localhost:8080`
4. Start testing!

📖 **For detailed Postman setup instructions, see [POSTMAN_SETUP.md](POSTMAN_SETUP.md)**

## 🛠️ Development

### Build
```bash
./gradlew build
```

### Run Tests
```bash
./gradlew test
```

### Clean Build
```bash
./gradlew clean build
```

## 📦 Dependencies

- **Jetty** - HTTP server
- **HikariCP** - Connection pooling
- **Jedis** - Redis client
- **JWT (jjwt)** - JWT token handling
- **BCrypt** - Password hashing
- **Jackson** - JSON processing
- **Liquibase** - Database migrations
- **Logback** - Logging

## 🎯 Production Features

- ✅ Rate limiting
- ✅ Health checks (basic + detailed)
- ✅ Metrics collection
- ✅ Transaction management
- ✅ Redis caching
- ✅ Error handling
- ✅ Input validation
- ✅ Docker support

## 📝 License

This project is for educational/demonstration purposes.

## 🤝 Contributing

This is a demonstration project showcasing Ports and Adapters architecture.

---

**Built with ❤️ using Ports and Adapters (Hexagonal Architecture)**
