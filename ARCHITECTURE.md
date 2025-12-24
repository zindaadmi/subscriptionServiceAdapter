# Subscription Service - Architecture Documentation

## 🏗️ System Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Client Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │ React Web UI │  │ Mobile Apps  │  │ Hardware     │     │
│  │ (shadcn/ui)  │  │             │  │ Devices      │     │
│  └──────┬───────┘  └──────┬──────┘  └──────┬──────┘     │
└─────────┼──────────────────┼─────────────────┼────────────┘
          │                  │                 │
          │ HTTP/REST        │ HTTP/REST       │ API Key Auth
          │                  │                 │
┌─────────▼──────────────────▼─────────────────▼────────────┐
│              API Gateway / Spring Boot Application      │
│  ┌──────────────────────────────────────────────────┐  │
│  │              Security Layer                       │  │
│  │  • JWT Authentication                            │  │
│  │  • OAuth2 (Google) - Optional                    │  │
│  │  • Role-Based Access Control (RBAC)              │  │
│  │  • API Key Authentication (Devices)              │  │
│  │  • CSRF Protection (Selective)                   │  │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │              Caching Layer (Optional)             │  │
│  │  • Redis Cache (when configured)                 │  │
│  │  • In-Memory Cache (fallback)                    │  │
│  │  • Cache Names: users, subscriptions, devices    │  │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │              Controller Layer                     │  │
│  │  • AuthController                                │  │
│  │  • AdminController                               │  │
│  │  • AgentController                               │  │
│  │  • UserController                                │  │
│  │  • BillingController                             │  │
│  │  • DeviceVerificationController                  │  │
│  │  • AuditLogController                            │  │
│  │  • MigrationController                           │  │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │              Service Layer (Business Logic)       │  │
│  │  • UserService                                   │  │
│  │  • DeviceService                                 │  │
│  │  • SubscriptionService                            │  │
│  │  • UserDeviceService                              │  │
│  │  • BillingService                                │  │
│  │  • DeviceAuthService                              │  │
│  │  • AuditLogService                                │  │
│  │  • MigrationService                               │  │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │              Repository Layer (Data Access)       │  │
│  │  • JPA Repositories                              │  │
│  │  • Custom Queries                                 │  │
│  └──────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
          │
          │ JPA/Hibernate
          │
┌─────────▼────────────────────────────────────────────┐
│              Database Layer                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐           │
│  │  MySQL   │  │PostgreSQL│  │   H2     │           │
│  │(Production)│ │(Production)│ │  (Dev)   │           │
│  └──────────┘  └──────────┘  └──────────┘           │
└───────────────────────────────────────────────────────┘
          │
          │ Optional
          │
┌─────────▼────────────────────────────────────────────┐
│              Cache Layer (Optional)                    │
│  ┌──────────┐  ┌──────────┐                          │
│  │  Redis   │  │ In-Memory│                          │
│  │(Optional)│ │ (Fallback)│                          │
│  └──────────┘  └──────────┘                          │
└───────────────────────────────────────────────────────┘
```

## 📐 Architecture Patterns

### 1. Layered Architecture (N-Tier)

```
┌─────────────────────────────────────┐
│   Presentation Layer (Controllers)   │  ← REST API Endpoints
├─────────────────────────────────────┤
│   Business Logic Layer (Services)    │  ← Domain Logic
├─────────────────────────────────────┤
│   Data Access Layer (Repositories)   │  ← Database Operations
├─────────────────────────────────────┤
│   Database Layer                     │  ← MySQL/PostgreSQL/H2
└─────────────────────────────────────┘
```

### 2. Domain-Driven Design (DDD)

**Domain Models:**
- `User` - User aggregate root
- `Device` - Device aggregate root
- `Subscription` - Subscription aggregate root
- `UserSubscription` - User subscription aggregate
- `UserDevice` - User device aggregate
- `Billing` - Billing aggregate
- `Feature` - Feature value object
- `AuditLog` - Audit log aggregate

**Bounded Contexts:**
- **Identity & Access Management** - Users, Roles, Authentication
- **Subscription Management** - Subscriptions, Features, Plans
- **Device Management** - Devices, Device Authentication
- **Billing** - Bills, Payments, Pro-rata calculations
- **Audit & Compliance** - Audit logs, Soft deletes

### 3. Repository Pattern

All data access through repositories:
```java
UserRepository extends JpaRepository<User, Long>
DeviceRepository extends JpaRepository<Device, Long>
SubscriptionRepository extends JpaRepository<Subscription, Long>
```

### 4. Service Layer Pattern

Business logic encapsulated in services:
- Transaction management
- Business rule enforcement
- Cross-cutting concerns (audit, logging)

### 5. DTO Pattern

Data Transfer Objects for API communication:
- `LoginRequest`, `RegisterRequest`
- `SubscriptionRequest`, `AssignSubscriptionRequest`
- `DeviceRequest`, `FeatureRequest`

## 🔐 Security Architecture

### Authentication Flow

```
┌─────────┐         ┌──────────────┐         ┌──────────┐
│ Client │─────────▶│ AuthController│────────▶│ JWT Token│
└────────┘  Login   └──────────────┘  Generate └────┬─────┘
                                                     │
                                                     ▼
                                            ┌────────────────┐
                                            │  Store Token   │
                                            │  (LocalStorage)│
                                            └────────────────┘
```

### Authorization Flow

```
┌─────────┐    Request + JWT    ┌──────────────┐
│ Client │─────────────────────▶│JWT Filter   │
└────────┘                       └──────┬──────┘
                                        │
                                        ▼
                              ┌──────────────────┐
                              │ Extract Roles    │
                              └──────┬───────────┘
                                     │
                                     ▼
                        ┌────────────────────────┐
                        │ SecurityConfig        │
                        │ Role-Based Access     │
                        └──────┬────────────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │ Controller Method   │
                    │ @PreAuthorize       │
                    └──────────────────────┘
```

### API Key Authentication (Devices)

```
┌──────────┐    API Key Header    ┌──────────────────┐
│ Hardware │─────────────────────▶│DeviceAuthService │
│ Device   │                       └────────┬─────────┘
└──────────┘                                │
                                            ▼
                                  ┌──────────────────┐
                                  │ Verify API Key   │
                                  │ Check Device     │
                                  └──────────────────┘
```

## 📊 Data Flow Architecture

### User Registration Flow

```
1. Client → POST /api/auth/register
2. AuthController → UserService.registerUser()
3. UserService → PasswordEncoder.encode()
4. UserService → UserRepository.save()
5. UserService → RoleRepository (assign default role)
6. Response → JWT Token + User Info
```

### Subscription Assignment Flow

```
1. Agent/Admin → POST /api/agent/user-subscriptions/assign
2. AgentController → SubscriptionService.assignSubscriptionToUser()
3. SubscriptionService → Validate negotiated price
4. SubscriptionService → UserSubscriptionRepository.save()
5. AuditLogService → Log assignment
6. Response → UserSubscription object
```

### Billing Generation Flow

```
1. Scheduled Task (1st of month, 2 AM)
2. BillingService.generateMonthlyBills()
3. For each active UserSubscription:
   a. Calculate pro-rata amount
   b. Create Billing record
   c. Generate PDF
   d. Send email with PDF
4. Update billing status
```

### Device Verification Flow

```
1. Hardware Device → POST /api/device/verify-subscription
2. DeviceVerificationController → DeviceAuthService.authenticateDevice()
3. DeviceAuthService → Verify API key
4. DeviceVerificationController → UserDeviceService.getUserDeviceBySerial()
5. DeviceVerificationController → BillingService.isDeviceAuthorized()
6. Check:
   - Subscription active?
   - Bills paid?
   - Not overdue?
7. Response → authorized: true/false
```

## 🗄️ Database Architecture

### Entity Relationship Diagram

```
┌──────────┐      ┌──────────────┐      ┌──────────┐
│   User   │──────│ user_roles   │──────│  Role   │
│          │◄─────│ (Many-to-Many)│─────│          │
└────┬─────┘      └──────────────┘      └──────────┘
     │
     │ 1:N
     │
┌────▼──────────────────┐
│ user_subscriptions     │
│ - user_id (FK)        │
│ - subscription_id (FK) │
│ - negotiated_price    │
│ - status              │
│ - billing_start_date  │
└────┬──────────────────┘
     │ N:1              │ N:1
     │                  │
┌────▼──────────┐  ┌────▼──────────┐
│ Subscription  │  │  Device       │
│ - device_id   │──│  (Template)   │
│ - base_price  │  │  - api_key    │
│ - level       │  └───────────────┘
└────┬──────────┘
     │ N:M
     │
┌────▼──────────┐      ┌──────────────┐      ┌──────────┐
│subscription_  │──────│ Feature       │      │ Feature  │
│  _features    │      │ - feature_code│      │          │
│ (Join Table)  │      └───────────────┘      └──────────┘
└───────────────┘

┌─────────────────────────────────────────────┐
│ user_devices                                 │
│ - device_id (FK) - Main mapping             │
│ - user_subscription_id (FK)                 │
│ - device_serial (UNIQUE) - Physical device   │
│ - active                                     │
└────┬─────────────────────────────────────────┘
     │ N:1
     │
┌────▼──────────────────┐
│ billings              │
│ - user_subscription_id│
│ - status              │
│ - due_date            │
│ - payment_method      │
└───────────────────────┘

┌──────────────────────┐
│ audit_logs           │
│ - entity_type        │
│ - entity_id          │
│ - user_id            │
│ - action             │
│ - timestamp          │
└──────────────────────┘
```

### Complete Database Schema

#### 1. **users** Table
```sql
- id (PK, BIGINT, AUTO_INCREMENT)
- username (VARCHAR, UNIQUE, NOT NULL)
- email (VARCHAR, UNIQUE)
- password (VARCHAR, NOT NULL)
- mobile_number (VARCHAR, UNIQUE)
- phone_number (VARCHAR)
- address (VARCHAR(500))
- city, state, zip_code, country (VARCHAR)
- deleted (BOOLEAN, DEFAULT FALSE)
- deleted_at (TIMESTAMP)
- deleted_by (BIGINT)
- provider (ENUM: LOCAL, GOOGLE)
- provider_id (VARCHAR)
- enabled (BOOLEAN, DEFAULT TRUE)
- created_at, updated_at (TIMESTAMP)

Indexes:
- PRIMARY KEY (id)
- UNIQUE (username)
- UNIQUE (email)
- UNIQUE (mobile_number)
- INDEX idx_users_deleted (deleted)
```

#### 2. **roles** Table
```sql
- id (PK, BIGINT, AUTO_INCREMENT)
- name (VARCHAR, UNIQUE, NOT NULL) - ADMIN, AGENT, USER

Indexes:
- PRIMARY KEY (id)
- UNIQUE (name)
```

#### 3. **user_roles** Table (Join Table)
```sql
- user_id (FK → users.id)
- role_id (FK → roles.id)
- PRIMARY KEY (user_id, role_id)

Indexes:
- PRIMARY KEY (user_id, role_id)
- INDEX (user_id)
- INDEX (role_id)
```

#### 4. **devices** Table
```sql
- id (PK, BIGINT, AUTO_INCREMENT)
- name (VARCHAR, NOT NULL)
- description (VARCHAR(1000))
- device_type (VARCHAR, NOT NULL)
- active (BOOLEAN, DEFAULT TRUE)
- deleted (BOOLEAN, DEFAULT FALSE)
- deleted_at (TIMESTAMP)
- deleted_by (BIGINT)
- api_key (VARCHAR(64), UNIQUE)
- created_at, updated_at (TIMESTAMP)

Indexes:
- PRIMARY KEY (id)
- UNIQUE (api_key)
- INDEX idx_devices_deleted (deleted)
- INDEX idx_devices_active_deleted (active, deleted)
```

#### 5. **subscriptions** Table
```sql
- id (PK, BIGINT, AUTO_INCREMENT)
- name (VARCHAR, NOT NULL)
- description (VARCHAR(1000))
- device_id (FK → devices.id, NOT NULL)
- base_price (DECIMAL(10,2), NOT NULL)
- subscription_level (ENUM: BASIC, STANDARD, PREMIUM, ENTERPRISE)
- billing_cycle (ENUM: MONTHLY, QUARTERLY, YEARLY)
- active (BOOLEAN, DEFAULT TRUE)
- deleted (BOOLEAN, DEFAULT FALSE)
- deleted_at (TIMESTAMP)
- deleted_by (BIGINT)
- created_at, updated_at (TIMESTAMP)

Indexes:
- PRIMARY KEY (id)
- INDEX (device_id)
- INDEX idx_subscriptions_deleted (deleted)
- INDEX idx_subscriptions_active_deleted (active, deleted)
```

#### 6. **features** Table
```sql
- id (PK, BIGINT, AUTO_INCREMENT)
- name (VARCHAR, NOT NULL, UNIQUE)
- description (VARCHAR(1000))
- feature_code (VARCHAR, UNIQUE, NOT NULL)
- active (BOOLEAN, DEFAULT TRUE)
- created_at, updated_at (TIMESTAMP)

Indexes:
- PRIMARY KEY (id)
- UNIQUE (name)
- UNIQUE (feature_code)
```

#### 7. **subscription_features** Table (Join Table)
```sql
- subscription_id (FK → subscriptions.id)
- feature_id (FK → features.id)
- PRIMARY KEY (subscription_id, feature_id)

Indexes:
- PRIMARY KEY (subscription_id, feature_id)
- INDEX (subscription_id)
- INDEX (feature_id)
```

#### 8. **user_subscriptions** Table
```sql
- id (PK, BIGINT, AUTO_INCREMENT)
- user_id (FK → users.id, NOT NULL)
- subscription_id (FK → subscriptions.id, NOT NULL)
- negotiated_price (DECIMAL(10,2), NOT NULL)
- start_date (DATE, NOT NULL)
- end_date (DATE)
- billing_start_date (DATE, NOT NULL)
- status (ENUM: ACTIVE, INACTIVE, CANCELLED, EXPIRED)
- duration_months (INTEGER, DEFAULT 1)
- assigned_by (BIGINT)
- created_at, updated_at (TIMESTAMP)

Indexes:
- PRIMARY KEY (id)
- INDEX (user_id)
- INDEX (subscription_id)
- INDEX (status)
- INDEX idx_user_subscriptions_user_status (user_id, status)
- INDEX idx_user_subscriptions_billing_start_date (billing_start_date)
```

#### 9. **user_devices** Table
```sql
- id (PK, BIGINT, AUTO_INCREMENT)
- device_id (FK → devices.id, NOT NULL) - Main mapping
- user_subscription_id (FK → user_subscriptions.id, NOT NULL)
- device_serial (VARCHAR(255), UNIQUE) - Physical device serial
- purchase_date (DATE, NOT NULL)
- active (BOOLEAN, DEFAULT TRUE)
- created_at, updated_at (TIMESTAMP)

Indexes:
- PRIMARY KEY (id)
- UNIQUE (device_serial)
- INDEX (device_id)
- INDEX (user_subscription_id)
- INDEX idx_user_devices_device_active (device_id, active)
- INDEX idx_user_devices_user_subscription (user_subscription_id)
```

#### 10. **billings** Table
```sql
- id (PK, BIGINT, AUTO_INCREMENT)
- user_subscription_id (FK → user_subscriptions.id, NOT NULL)
- billing_period_start (DATE, NOT NULL)
- billing_period_end (DATE, NOT NULL)
- base_amount (DECIMAL(10,2), NOT NULL)
- negotiated_amount (DECIMAL(10,2), NOT NULL)
- pro_rata_amount (DECIMAL(10,2), NOT NULL)
- total_amount (DECIMAL(10,2), NOT NULL)
- bill_date (DATE, NOT NULL)
- due_date (DATE, NOT NULL)
- paid_date (DATE)
- payment_method (VARCHAR(50))
- status (ENUM: PENDING, PAID, OVERDUE, CANCELLED)
- pdf_path (VARCHAR(500))
- email_sent (BOOLEAN, DEFAULT FALSE)
- email_sent_at (TIMESTAMP)
- created_at, updated_at (TIMESTAMP)

Indexes:
- PRIMARY KEY (id)
- INDEX (user_subscription_id)
- INDEX (status)
- INDEX (due_date)
- INDEX idx_billings_user_subscription_status (user_subscription_id, status)
- INDEX idx_billings_status_due_date (status, due_date)
```

#### 11. **audit_logs** Table
```sql
- id (PK, BIGINT, AUTO_INCREMENT)
- entity_type (VARCHAR, NOT NULL)
- entity_id (BIGINT, NOT NULL)
- user_id (BIGINT)
- action (VARCHAR, NOT NULL)
- description (TEXT)
- old_value (TEXT)
- new_value (TEXT)
- ip_address (VARCHAR(45))
- user_agent (VARCHAR(500))
- success (BOOLEAN, DEFAULT TRUE)
- error_message (TEXT)
- timestamp (TIMESTAMP, NOT NULL)

Indexes:
- PRIMARY KEY (id)
- INDEX idx_entity_type (entity_type)
- INDEX idx_entity_id (entity_id)
- INDEX idx_user_id (user_id)
- INDEX idx_action (action)
- INDEX idx_timestamp (timestamp)
```

### Database Schema Layers

1. **Core Entities:**
   - Users, Roles, Devices, Features

2. **Business Entities:**
   - Subscriptions, UserSubscriptions, UserDevices

3. **Financial Entities:**
   - Billings

4. **Audit Entities:**
   - AuditLogs

### Database Design Principles

#### ✅ Normalization
- **3NF (Third Normal Form)**: All tables are normalized
- **No Redundancy**: Data stored once, accessed via relationships
- **Referential Integrity**: All foreign keys properly defined

#### ✅ Indexing Strategy
- **Primary Keys**: All tables have auto-increment primary keys
- **Foreign Keys**: Indexed for join performance
- **Unique Constraints**: On username, email, mobile_number, device_serial, api_key
- **Composite Indexes**: For common query patterns
  - `(user_id, status)` for user subscription queries
  - `(device_id, active)` for active device queries
  - `(status, due_date)` for overdue billing queries
- **Date Indexes**: On billing_start_date, due_date for range queries

#### ✅ Performance Optimizations
- **Composite Indexes**: Added for frequently queried column combinations
- **Soft Delete Indexes**: For filtering deleted records
- **Status Indexes**: For filtering by status
- **Date Range Indexes**: For billing period queries

#### ✅ Data Integrity
- **Foreign Key Constraints**: All relationships enforced
- **Unique Constraints**: Prevent duplicate data
- **NOT NULL Constraints**: Required fields enforced
- **Check Constraints**: Enum values validated

### Query Optimization

**Common Query Patterns Optimized:**
1. `findByUserAndStatus` → Index on (user_id, status)
2. `findByDeviceIdAndActiveTrue` → Index on (device_id, active)
3. `findByBillingStartDate` → Index on billing_start_date
4. `findByStatusAndDueDate` → Index on (status, due_date)
5. `findByUserSubscriptionAndStatus` → Index on (user_subscription_id, status)

### Database Migration

**Liquibase Changelog Structure:**
- Version-controlled schema changes
- Rollback support
- Data migration scripts
- Index optimization migrations

## 🔄 Transaction Management

### Transaction Boundaries

```java
@Transactional  // Service layer methods
public UserSubscription assignSubscriptionToUser(...) {
    // All operations in single transaction
    // Rollback on any error
}
```

### Transaction Isolation

- Default: `READ_COMMITTED`
- Ensures data consistency
- Prevents dirty reads

## 📝 Logging Architecture

### Log Layers

```
┌─────────────────────────────────────┐
│   Application Logs                  │  ← General app logs
├─────────────────────────────────────┤
│   Audit Logs                        │  ← All operations
├─────────────────────────────────────┤
│   Security Logs                     │  ← Auth events
├─────────────────────────────────────┤
│   Billing Logs                      │  ← Billing ops
└─────────────────────────────────────┘
```

### Log Flow

```
Service Method
    │
    ├─→ AOP Aspect (AuditAspect)
    │       │
    │       └─→ AuditLogService.logAction()
    │               │
    │               └─→ AuditLogRepository.save()
    │
    └─→ LoggerUtil.logAudit()
            │
            └─→ logback-spring.xml
                    │
                    └─→ audit.log file
```

## 🚀 Deployment Architecture

### Development Environment

```
┌─────────────┐
│   Developer │
│   Machine   │
└──────┬──────┘
       │
       ├─→ Spring Boot App (Port 8080)
       │       │
       │       └─→ H2 Database (In-Memory)
       │
       └─→ React Frontend (Port 3000)
               │
               └─→ Proxy to /api → :8080
```

### Production Environment

```
┌──────────────┐
│   Load       │
│   Balancer   │
└──────┬───────┘
       │
       ├─→ App Server 1 (Spring Boot)
       ├─→ App Server 2 (Spring Boot)
       └─→ App Server N (Spring Boot)
              │
              └─→ Database Cluster
                    ├─→ MySQL Master
                    └─→ MySQL Replicas
```

## 🔧 Technology Stack

### Backend
- **Framework:** Spring Boot 3.2.0
- **Security:** Spring Security + JWT + OAuth2
- **Persistence:** JPA/Hibernate
- **Database:** MySQL/PostgreSQL/H2
- **Migrations:** Liquibase
- **Logging:** Logback + SLF4J
- **PDF:** iText7
- **Email:** Spring Mail

### Frontend
- **Framework:** React 18 + TypeScript
- **Build Tool:** Vite
- **UI Library:** shadcn/ui
- **Styling:** Tailwind CSS
- **Routing:** React Router
- **HTTP Client:** Axios

## 📦 Package Structure

```
com.security.securityDemo
├── config/              # Configuration classes
│   ├── SecurityConfig
│   ├── DataInitializer
│   ├── ScheduledTasks
│   └── AuditAspect
├── controller/          # REST Controllers
│   ├── AuthController
│   ├── AdminController
│   ├── AgentController
│   ├── UserController
│   ├── BillingController
│   ├── DeviceVerificationController
│   ├── AuditLogController
│   └── MigrationController
├── service/             # Business Logic
│   ├── UserService
│   ├── DeviceService
│   ├── SubscriptionService
│   ├── BillingService
│   ├── AuditLogService
│   └── ...
├── repository/          # Data Access
│   ├── UserRepository
│   ├── DeviceRepository
│   └── ...
├── model/               # Domain Models
│   ├── User
│   ├── Device
│   ├── Subscription
│   └── ...
├── dto/                 # Data Transfer Objects
│   ├── LoginRequest
│   ├── RegisterRequest
│   └── ...
├── security/            # Security Components
│   └── JwtAuthenticationFilter
├── util/                # Utilities
│   ├── JwtTokenUtil
│   └── LoggerUtil
└── SubscriptionServiceApplication
```

## 🔄 Request/Response Flow

### Typical API Request Flow

```
1. Client Request
   ↓
2. Security Filter (JWT Validation)
   ↓
3. Controller (Request Mapping)
   ↓
4. Service Layer (Business Logic)
   ↓
5. Repository (Data Access)
   ↓
6. Database
   ↓
7. Response (DTO)
   ↓
8. Client
```

### Error Handling Flow

```
Exception Thrown
   ↓
@ControllerAdvice (Global Exception Handler)
   ↓
Error Response DTO
   ↓
HTTP Status Code
   ↓
Client
```

## 🎯 Design Principles Applied

1. **SOLID Principles**
   - Single Responsibility
   - Open/Closed
   - Liskov Substitution
   - Interface Segregation
   - Dependency Inversion

2. **DRY (Don't Repeat Yourself)**
   - Reusable services
   - Common utilities
   - Base repositories

3. **Separation of Concerns**
   - Controllers → HTTP handling
   - Services → Business logic
   - Repositories → Data access

4. **Dependency Injection**
   - Spring's IoC container
   - Constructor injection preferred

5. **Transaction Management**
   - Declarative transactions
   - Service layer boundaries

## 📈 Scalability Considerations

### Horizontal Scaling
- Stateless JWT authentication
- Database connection pooling
- Stateless services

### Caching Strategy
- **Optional Redis Caching** - Automatically enabled when Redis is configured
  - Redis cache manager (when `spring.redis.host` is set)
  - In-memory cache manager (fallback when Redis not available)
  - Pre-configured cache names: `users`, `subscriptions`, `devices`, `features`
  - Default TTL: 1 hour (configurable)
  - Uses Spring Cache abstraction with `@Cacheable`, `@CacheEvict` annotations
  - Application works seamlessly with or without Redis

### Database Optimization
- Indexes on foreign keys
- Indexes on frequently queried fields
- Soft delete for audit trail

## 🔒 Security Considerations

1. **Authentication**
   - JWT tokens (stateless)
   - OAuth2 for social login
   - API keys for devices

2. **Authorization**
   - Role-based access control
   - Method-level security
   - URL-level security

3. **Data Protection**
   - Password encryption (BCrypt)
   - SQL injection prevention (JPA)
   - XSS prevention (input validation)
   - CSRF protection (selective - enabled for OAuth2/web, disabled for JWT API endpoints)

4. **Audit Trail**
   - All operations logged
   - Soft delete for recovery
   - IP address tracking

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [Liquibase Documentation](https://www.liquibase.org/documentation)
- [React Documentation](https://react.dev/)
- [Redis Documentation](https://redis.io/documentation)

## 👤 Author

**Nikhil Parakh**

This architecture document describes the enterprise subscription service system built with Spring Boot, React, and modern security practices.
