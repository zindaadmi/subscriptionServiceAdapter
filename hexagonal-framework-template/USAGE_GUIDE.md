# Framework Usage Guide

## Quick Start Example

### 1. Create Your Domain Model

```java
package com.yourservice.domain.model;

public class Product {
    private Long id;
    private String name;
    private BigDecimal price;
    
    // Getters and setters
}
```

### 2. Create Ports (Interfaces)

**Inbound Port (What your app can do):**
```java
package com.yourservice.application.port.inbound;

public interface ProductServicePort {
    Product createProduct(String name, BigDecimal price);
    Product findById(Long id);
    List<Product> findAll();
}
```

**Outbound Port (What your app needs):**
```java
package com.yourservice.application.port.outbound;

public interface ProductRepositoryPort {
    Product save(Product product);
    Optional<Product> findById(Long id);
    List<Product> findAll();
}
```

### 3. Implement Use Case

```java
package com.yourservice.application.service;

import com.yourservice.application.port.inbound.ProductServicePort;
import com.yourservice.application.port.outbound.ProductRepositoryPort;
import com.yourservice.domain.model.Product;

public class ProductUseCase implements ProductServicePort {
    
    private final ProductRepositoryPort repository;
    
    public ProductUseCase(ProductRepositoryPort repository) {
        this.repository = repository;
    }
    
    @Override
    public Product createProduct(String name, BigDecimal price) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        return repository.save(product);
    }
    
    @Override
    public Product findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
    }
    
    @Override
    public List<Product> findAll() {
        return repository.findAll();
    }
}
```

### 4. Implement Repository Adapter

```java
package com.yourservice.infrastructure.adapter.outbound.persistence;

import com.yourservice.application.port.outbound.ProductRepositoryPort;
import com.yourservice.domain.model.Product;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcProductRepository implements ProductRepositoryPort {
    
    private final DataSource dataSource;
    
    public JdbcProductRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public Product save(Product product) {
        try (Connection conn = dataSource.getConnection()) {
            if (product.getId() == null) {
                // Insert
                String sql = "INSERT INTO products (name, price) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, product.getName());
                    stmt.setBigDecimal(2, product.getPrice());
                    stmt.executeUpdate();
                    
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            product.setId(rs.getLong(1));
                        }
                    }
                }
            } else {
                // Update
                String sql = "UPDATE products SET name = ?, price = ? WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, product.getName());
                    stmt.setBigDecimal(2, product.getPrice());
                    stmt.setLong(3, product.getId());
                    stmt.executeUpdate();
                }
            }
            return product;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save product", e);
        }
    }
    
    @Override
    public Optional<Product> findById(Long id) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT * FROM products WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    }
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find product", e);
        }
    }
    
    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT * FROM products";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find products", e);
        }
        return products;
    }
    
    private Product mapRow(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));
        product.setPrice(rs.getBigDecimal("price"));
        return product;
    }
}
```

### 5. Create HTTP Controller

```java
package com.yourservice.infrastructure.adapter.inbound.http;

import com.framework.core.di.Container;
import com.framework.core.http.HttpRequest;
import com.framework.core.http.HttpResponse;
import com.yourservice.application.port.inbound.ProductServicePort;
import com.yourservice.domain.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProductController {
    
    private final Container container;
    private final ObjectMapper objectMapper;
    
    public ProductController(Container container) {
        this.container = container;
        this.objectMapper = new ObjectMapper();
    }
    
    public HttpResponse createProduct(HttpRequest request) {
        try {
            ProductRequest dto = objectMapper.readValue(request.getBody(), ProductRequest.class);
            
            ProductServicePort service = container.getBean(ProductServicePort.class);
            Product product = service.createProduct(dto.getName(), dto.getPrice());
            
            return HttpResponse.created()
                .header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(product));
        } catch (Exception e) {
            return HttpResponse.badRequest()
                .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    public HttpResponse getProduct(HttpRequest request) {
        try {
            String id = request.getPathParam("id");
            ProductServicePort service = container.getBean(ProductServicePort.class);
            Product product = service.findById(Long.parseLong(id));
            
            return HttpResponse.ok()
                .header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(product));
        } catch (Exception e) {
            return HttpResponse.notFound()
                .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    public HttpResponse getAllProducts(HttpRequest request) {
        try {
            ProductServicePort service = container.getBean(ProductServicePort.class);
            List<Product> products = service.findAll();
            
            return HttpResponse.ok()
                .header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(products));
        } catch (Exception e) {
            return HttpResponse.internalServerError()
                .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
```

### 6. Configure application.yml

```yaml
application:
  name: product-service
  version: 1.0.0

server:
  port: 8080
  type: jetty

database:
  type: h2
  url: jdbc:h2:mem:product_service
  username: sa
  password: 
  pool:
    maxSize: 10
    minIdle: 5

services:
  productService:
    implementation: com.yourservice.application.service.ProductUseCase
    scope: singleton

repositories:
  productRepository:
    implementation: com.yourservice.infrastructure.adapter.outbound.persistence.JdbcProductRepository
    scope: singleton
```

### 7. Create Main Application

```java
package com.yourservice;

import com.framework.core.bootstrap.ApplicationBootstrap;
import com.framework.core.di.Container;
import com.framework.core.http.HttpServer;
import com.yourservice.infrastructure.adapter.inbound.http.ProductController;

public class ProductServiceApplication {
    
    public static void main(String[] args) {
        // Initialize framework
        ApplicationBootstrap bootstrap = new ApplicationBootstrap();
        bootstrap.initialize("application.yml");
        
        Container container = bootstrap.getContainer();
        HttpServer httpServer = container.getBean(HttpServer.class);
        
        // Register routes
        ProductController controller = new ProductController(container);
        httpServer.addRoute("POST", "/api/products", controller::createProduct);
        httpServer.addRoute("GET", "/api/products/{id}", controller::getProduct);
        httpServer.addRoute("GET", "/api/products", controller::getAllProducts);
        
        // Start server
        httpServer.start(8080);
        
        System.out.println("Product Service started on http://localhost:8080");
    }
}
```

## Advanced Examples

### Custom Filter

```java
public class LoggingFilter implements Filter {
    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) {
        System.out.println("Request: " + request.getMethod() + " " + request.getPath());
        long start = System.currentTimeMillis();
        
        chain.doFilter(request, response);
        
        long duration = System.currentTimeMillis() - start;
        System.out.println("Response: " + response.getStatusCode() + " (" + duration + "ms)");
    }
}
```

### Transaction Management

```java
public class ProductUseCase implements ProductServicePort {
    private final ProductRepositoryPort repository;
    private final TransactionManager transactionManager;
    
    public Product createProductWithInventory(String name, BigDecimal price, int stock) {
        return transactionManager.executeInTransaction(() -> {
            Product product = createProduct(name, price);
            inventoryRepository.save(new Inventory(product.getId(), stock));
            return product;
        });
    }
}
```

---

This framework provides a solid foundation for building services with clean architecture!




