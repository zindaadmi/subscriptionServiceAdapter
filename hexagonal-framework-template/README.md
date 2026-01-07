# Hexagonal Framework - Reusable Template

A lightweight, framework-independent Java framework implementing **Ports and Adapters (Hexagonal Architecture)** pattern. Use this as a template to build your own services without Spring Boot dependencies.

## 🎯 What is This?

This is a **reusable framework template** that provides:
- ✅ Dependency Injection Container
- ✅ HTTP Server Abstraction (Jetty-based)
- ✅ Configuration Management (YAML)
- ✅ Database Connection Pooling
- ✅ Filter Chain Support
- ✅ Ports and Adapters Architecture

## 📁 Framework Structure

```
hexagonal-framework-template/
├── src/main/java/com/framework/core/
│   ├── bootstrap/          # Application bootstrap
│   │   └── ApplicationBootstrap.java
│   ├── config/            # Configuration loading
│   │   ├── ConfigurationLoader.java
│   │   ├── YamlConfigurationLoader.java
│   │   └── ConfigurationException.java
│   ├── di/                # Dependency Injection
│   │   ├── Container.java
│   │   └── BeanDefinition.java
│   ├── http/              # HTTP Server Abstraction
│   │   ├── HttpServer.java
│   │   ├── JettyHttpServer.java
│   │   ├── HttpRequest.java
│   │   ├── HttpResponse.java
│   │   ├── RequestHandler.java
│   │   ├── Filter.java
│   │   ├── FilterChain.java
│   │   └── HttpServletAdapter.java
│   └── persistence/       # Database Factory
│       └── DataSourceFactory.java
└── README.md
```

## 🚀 How to Use This Template

### Step 1: Copy Framework to Your Project

```bash
# Copy the framework-core folder to your new project
cp -r hexagonal-framework-template/src/main/java/com/framework/core /path/to/your/project/src/main/java/com/framework/
```

### Step 2: Create Your Service Structure

Create your service following Hexagonal Architecture:

```
your-service/
├── src/main/java/com/yourservice/
│   ├── domain/                    # Domain Layer (Pure Business Logic)
│   │   ├── model/                 # Domain Models (POJOs)
│   │   │   └── YourEntity.java
│   │   └── exception/             # Domain Exceptions
│   │       └── YourException.java
│   │
│   ├── application/               # Application Layer (Use Cases)
│   │   ├── port/
│   │   │   ├── inbound/           # Primary Ports (What your app can do)
│   │   │   │   └── YourServicePort.java
│   │   │   └── outbound/          # Secondary Ports (What your app needs)
│   │   │       ├── YourRepositoryPort.java
│   │   │       └── SecurityPort.java
│   │   └── service/               # Use Case Implementations
│   │       └── YourUseCase.java
│   │
│   └── infrastructure/            # Infrastructure Layer (Adapters)
│       ├── adapter/
│       │   ├── inbound/           # Primary Adapters (HTTP, CLI, etc.)
│       │   │   └── http/
│       │   │       ├── YourController.java
│       │   │       └── filter/
│       │   │           └── YourFilter.java
│       │   └── outbound/          # Secondary Adapters
│       │       ├── persistence/
│       │       │   └── JdbcYourRepository.java
│       │       ├── security/
│       │       │   └── JwtSecurityAdapter.java
│       │       └── cache/
│       │           └── RedisCacheAdapter.java
│       └── YourServiceApplication.java
│
└── src/main/resources/
    └── application.yml
```

### Step 3: Configure Your Application

Create `application.yml`:

```yaml
application:
  name: your-service
  version: 1.0.0

server:
  port: 8080
  type: jetty

database:
  type: h2  # h2, mysql, postgresql
  url: jdbc:h2:mem:your_service
  username: sa
  password: 
  pool:
    maxSize: 10
    minIdle: 5

# Register your services
services:
  yourService:
    implementation: com.yourservice.application.service.YourUseCase
    scope: singleton

# Register your repositories
repositories:
  yourRepository:
    implementation: com.yourservice.infrastructure.adapter.outbound.persistence.JdbcYourRepository
    scope: singleton

# Register your adapters
adapters:
  securityAdapter:
    implementation: com.yourservice.infrastructure.adapter.outbound.security.JwtSecurityAdapter
    scope: singleton
```

### Step 4: Create Your Main Application

```java
package com.yourservice;

import com.framework.core.bootstrap.ApplicationBootstrap;
import com.framework.core.di.Container;
import com.framework.core.http.HttpServer;
import com.yourservice.infrastructure.adapter.inbound.http.YourController;
import com.yourservice.infrastructure.adapter.inbound.http.filter.YourFilter;

public class YourServiceApplication {
    
    public static void main(String[] args) {
        // Initialize framework
        ApplicationBootstrap bootstrap = new ApplicationBootstrap();
        bootstrap.initialize("application.yml");
        
        Container container = bootstrap.getContainer();
        HttpServer httpServer = container.getBean(HttpServer.class);
        
        // Register your filters
        httpServer.addFilter(new YourFilter(container));
        
        // Register your routes
        YourController controller = new YourController(container);
        httpServer.addRoute("GET", "/api/your-resource", controller::getResource);
        httpServer.addRoute("POST", "/api/your-resource", controller::createResource);
        
        // Start server
        httpServer.start(8080);
        
        System.out.println("Your Service started on http://localhost:8080");
    }
}
```

## 📚 Core Components Explained

### 1. Container (Dependency Injection)

```java
Container container = new Container();

// Register a singleton
container.registerSingleton(YourService.class, new YourService());

// Register a bean definition
container.registerBean(YourRepository.class, 
    new BeanDefinition(YourRepository.class, true));

// Get a bean (auto-injects dependencies)
YourService service = container.getBean(YourService.class);
```

### 2. HttpServer (HTTP Abstraction)

```java
HttpServer httpServer = container.getBean(HttpServer.class);

// Add routes
httpServer.addRoute("GET", "/api/users/{id}", this::getUser);
httpServer.addRoute("POST", "/api/users", this::createUser);

// Add filters
httpServer.addFilter(new AuthenticationFilter());
httpServer.addFilter(new RateLimitFilter());

// Start server
httpServer.start(8080);
```

### 3. Request Handlers

```java
public HttpResponse getUser(HttpRequest request) {
    // Extract path parameters
    String id = request.getPathParam("id");
    
    // Extract query parameters
    String filter = request.getQueryParam("filter");
    
    // Get headers
    String authHeader = request.getHeader("Authorization");
    
    // Get body
    String body = request.getBody();
    
    // Process request
    User user = userService.findById(Long.parseLong(id));
    
    // Return response
    return HttpResponse.ok()
        .header("Content-Type", "application/json")
        .body(objectMapper.writeValueAsString(user));
}
```

### 4. Filters

```java
public class AuthenticationFilter implements Filter {
    
    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) {
        String token = request.getHeader("Authorization");
        
        if (token == null || !isValidToken(token)) {
            response.setStatusCode(401);
            response.setBody("{\"error\":\"Unauthorized\"}");
            return;
        }
        
        // Continue to next filter or handler
        chain.doFilter(request, response);
    }
}
```

## 🔧 Configuration Options

### Database Configuration

```yaml
database:
  type: mysql  # h2, mysql, postgresql
  url: jdbc:mysql://localhost:3306/your_db
  username: root
  password: password
  pool:
    maxSize: 20
    minIdle: 5
    connectionTimeout: 30000
```

### Server Configuration

```yaml
server:
  port: 8080
  type: jetty
```

## 📦 Dependencies

Add these to your `build.gradle`:

```gradle
dependencies {
    // YAML Configuration
    implementation 'org.yaml:snakeyaml:2.2'
    
    // HTTP Server (Jetty)
    implementation 'org.eclipse.jetty:jetty-server:11.0.20'
    implementation 'org.eclipse.jetty:jetty-servlet:11.0.20'
    implementation 'jakarta.servlet:jakarta.servlet-api:6.0.0'
    
    // JSON Processing
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.16.1'
    
    // Database Connection Pool
    implementation 'com.zaxxer:HikariCP:5.1.0'
    
    // Database Drivers (choose one or more)
    runtimeOnly 'com.h2database:h2:2.2.224'
    runtimeOnly 'com.mysql:mysql-connector-j:8.2.0'
    runtimeOnly 'org.postgresql:postgresql:42.7.1'
}
```

## 🎯 Architecture Principles

### 1. Ports and Adapters

- **Ports (Interfaces)**: Define contracts
  - **Inbound Ports**: What your application can do
  - **Outbound Ports**: What your application needs

- **Adapters (Implementations)**: Implement ports
  - **Inbound Adapters**: Convert external requests → use cases
  - **Outbound Adapters**: Implement ports using external technologies

### 2. Dependency Inversion

- Application layer depends on ports (interfaces)
- Infrastructure layer implements ports
- Domain layer has no dependencies

### 3. Separation of Concerns

- **Domain**: Pure business logic (no dependencies)
- **Application**: Use cases (depends on ports only)
- **Infrastructure**: Adapters (implements ports)

## 📝 Example: Complete Service

See the `examples/` directory for a complete example service implementation.

## 🔍 Key Features

- ✅ **No Spring Boot** - Lightweight, minimal dependencies
- ✅ **Dependency Injection** - Simple container-based DI
- ✅ **HTTP Server** - Jetty-based, easy to extend
- ✅ **Configuration** - YAML-based configuration
- ✅ **Database Support** - H2, MySQL, PostgreSQL
- ✅ **Filter Chain** - Request/response filtering
- ✅ **Ports & Adapters** - Clean architecture support

## 🤝 Contributing

This is a template framework. Feel free to:
- Copy and modify for your needs
- Add new features
- Improve documentation
- Share your implementations

## 📄 License

This framework template is provided as-is for educational and commercial use.

---

**Built with ❤️ using Ports and Adapters (Hexagonal Architecture)**





