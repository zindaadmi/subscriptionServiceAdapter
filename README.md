# Subscription Service

A production-ready subscription management service built with **Ports and Adapters (Hexagonal Architecture)** - a custom Java framework without Spring Boot dependencies.

## 🏗️ Architecture

This service follows **Hexagonal Architecture (Ports and Adapters)** pattern:

- **Domain Layer**: Pure business models (no dependencies)
- **Application Layer**: Use cases and business logic (depends on ports/interfaces)
- **Infrastructure Layer**: Adapters (HTTP, Database, Security, Cache)

### Key Features

- ✅ **Clean Architecture** - Ports and Adapters pattern
- ✅ **Framework Independent** - No Spring Boot, custom framework
- ✅ **Security** - JWT authentication, rate limiting, password hashing
- ✅ **Performance** - Redis caching, connection pooling
- ✅ **Monitoring** - Health checks, metrics collection
- ✅ **Reliability** - Transaction management, error handling
- ✅ **Deployment** - Docker support, Docker Compose

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Gradle 8.8+
- Redis (optional, for caching)
- PostgreSQL/MySQL/H2 (for database)

### Run with Gradle

```bash
./gradlew run
```

### Run with Docker

```bash
# Start all services (app + Redis + PostgreSQL)
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

### Run Locally

```bash
# Start Redis (if using caching)
docker run -d -p 6379:6379 --name redis redis:latest

# Run application
./gradlew run
```

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

### Register User
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

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### Get User (with token)
```bash
TOKEN="your-access-token"
curl http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Health Check
```bash
curl http://localhost:8080/health/detailed
```

### Metrics
```bash
curl http://localhost:8080/metrics
```

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

- `ARCHITECTURE.md` - Detailed architecture documentation
- `DATABASE_DESIGN.md` - Database schema and design
- `API_DOCUMENTATION.md` - Complete API documentation
- `QUICK_START.md` - Quick start guide
- `POSTMAN_SETUP.md` - Postman collection setup

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
