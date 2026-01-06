# Quick Reference Guide

## Framework Components

### Container (Dependency Injection)

```java
Container container = new Container();

// Register singleton
container.registerSingleton(MyService.class, new MyService());

// Register bean definition
container.registerBean(MyRepository.class, 
    new BeanDefinition(MyRepository.class, true));

// Get bean (auto-injects dependencies)
MyService service = container.getBean(MyService.class);
```

### HttpServer

```java
HttpServer httpServer = container.getBean(HttpServer.class);

// Add route
httpServer.addRoute("GET", "/api/resource/{id}", this::getResource);

// Add filter
httpServer.addFilter(new MyFilter());

// Start
httpServer.start(8080);
```

### Request Handler

```java
public HttpResponse getResource(HttpRequest request) {
    String id = request.getPathParam("id");
    String filter = request.getQueryParam("filter");
    String auth = request.getHeader("Authorization");
    String body = request.getBody();
    
    // Process...
    
    return HttpResponse.ok()
        .header("Content-Type", "application/json")
        .body(json);
}
```

### Filter

```java
public class MyFilter implements Filter {
    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) {
        // Before processing
        chain.doFilter(request, response);
        // After processing
    }
}
```

## Configuration (application.yml)

```yaml
application:
  name: my-service
  version: 1.0.0

server:
  port: 8080
  type: jetty

database:
  type: h2
  url: jdbc:h2:mem:my_service
  username: sa
  password: 

services:
  myService:
    implementation: com.myservice.application.service.MyUseCase
    scope: singleton

repositories:
  myRepository:
    implementation: com.myservice.infrastructure.adapter.outbound.persistence.JdbcMyRepository
    scope: singleton
```

## Architecture Pattern

```
Domain Layer (Pure POJOs)
    ↑
Application Layer (Use Cases - depends on Ports)
    ↑
Infrastructure Layer (Adapters - implements Ports)
```

## Ports

- **Inbound Ports**: What your application can do
- **Outbound Ports**: What your application needs

## Adapters

- **Inbound Adapters**: Convert external requests → use cases
- **Outbound Adapters**: Implement ports using external tech




