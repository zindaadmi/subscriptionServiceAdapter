package com.framework.core.bootstrap;

import com.framework.core.config.ConfigurationLoader;
import com.framework.core.config.YamlConfigurationLoader;
import com.framework.core.di.BeanDefinition;
import com.framework.core.di.Container;
import com.framework.core.http.HttpServer;
import com.framework.core.http.JettyHttpServer;
import com.framework.core.persistence.DataSourceFactory;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

/**
 * Application bootstrap - loads configuration and initializes the application
 */
public class ApplicationBootstrap {
    
    private final Container container;
    private final ConfigurationLoader configLoader;
    private Map<String, Object> configuration;
    
    public ApplicationBootstrap() {
        this.container = new Container();
        this.configLoader = new YamlConfigurationLoader();
    }
    
    public void initialize(String configFile) {
        // Load configuration
        configuration = configLoader.loadConfiguration(configFile);
        
        // Store configuration in container
        container.registerSingleton(Map.class, configuration);
        
        // Initialize database connection pool
        initializeDatabase();
        
        // Register adapters first (they may be dependencies)
        registerAdapters();
        
        // Register repositories
        registerRepositories();
        
        // Register services (they depend on repositories and adapters)
        registerServices();
        
        // Initialize HTTP server
        initializeHttpServer();
    }
    
    private void initializeDatabase() {
        @SuppressWarnings("unchecked")
        Map<String, Object> dbConfig = (Map<String, Object>) configuration.get("database");
        if (dbConfig != null) {
            // Create DataSource from configuration
            DataSource dataSource = DataSourceFactory.createDataSource(dbConfig);
            container.registerSingleton(DataSource.class, dataSource);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void registerAdapters() {
        Map<String, Object> adapters = (Map<String, Object>) configuration.get("adapters");
        if (adapters != null) {
            for (Map.Entry<String, Object> entry : adapters.entrySet()) {
                String beanName = entry.getKey();
                Map<String, Object> beanConfig = (Map<String, Object>) entry.getValue();
                
                // Check if adapter is enabled
                Boolean enabled = (Boolean) beanConfig.get("enabled");
                if (enabled != null && !enabled) {
                    System.out.println("Skipping disabled adapter: " + beanName);
                    continue;
                }
                
                // Special handling for JwtSecurityAdapter - needs JWT config
                if ("securityAdapter".equals(beanName)) {
                    registerSecurityAdapter(beanConfig);
                } else if ("cacheAdapter".equals(beanName)) {
                    registerCacheAdapter(beanConfig);
                } else {
                    registerBean(beanName, beanConfig);
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void registerSecurityAdapter(Map<String, Object> beanConfig) {
        try {
            // Get JWT configuration
            Map<String, Object> jwtConfig = (Map<String, Object>) configuration.get("jwt");
            if (jwtConfig == null) {
                throw new RuntimeException("JWT configuration not found in application.yml");
            }
            
            String secret = (String) jwtConfig.get("secret");
            Long accessTokenExpiration = jwtConfig.get("accessTokenExpiration") != null ?
                ((Number) jwtConfig.get("accessTokenExpiration")).longValue() : 900000L;
            Long refreshTokenExpiration = jwtConfig.get("refreshTokenExpiration") != null ?
                ((Number) jwtConfig.get("refreshTokenExpiration")).longValue() : 604800000L;
            
            // Create JwtSecurityAdapter instance
            Class<?> clazz = Class.forName((String) beanConfig.get("implementation"));
            Constructor<?> constructor = clazz.getConstructor(String.class, long.class, long.class);
            Object instance = constructor.newInstance(secret, accessTokenExpiration, refreshTokenExpiration);
            
            // Register as singleton
            container.registerSingleton(clazz, instance);
            System.out.println("Registered adapter: securityAdapter -> " + beanConfig.get("implementation"));
        } catch (Exception e) {
            System.err.println("Warning: Failed to register security adapter: " + e.getMessage());
            throw new RuntimeException("Failed to register security adapter", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void registerCacheAdapter(Map<String, Object> beanConfig) {
        try {
            // Get Redis configuration
            Map<String, Object> redisConfig = (Map<String, Object>) configuration.get("redis");
            if (redisConfig == null) {
                System.out.println("Warning: Redis configuration not found, skipping cache adapter");
                return;
            }
            
            Boolean enabled = redisConfig.get("enabled") != null ? 
                ((Boolean) redisConfig.get("enabled")) : false;
            if (!enabled) {
                System.out.println("Redis cache is disabled, skipping cache adapter");
                return;
            }
            
            String host = (String) redisConfig.get("host");
            if (host == null) host = "localhost";
            
            Integer port = redisConfig.get("port") != null ?
                ((Number) redisConfig.get("port")).intValue() : 6379;
            
            String password = (String) redisConfig.get("password");
            String keyPrefix = (String) redisConfig.get("keyPrefix");
            Integer maxConnections = redisConfig.get("maxConnections") != null ?
                ((Number) redisConfig.get("maxConnections")).intValue() : 10;
            
            // Create RedisCacheAdapter instance
            Class<?> clazz = Class.forName((String) beanConfig.get("implementation"));
            Constructor<?> constructor = clazz.getConstructor(String.class, int.class, String.class, String.class, int.class);
            Object instance = constructor.newInstance(host, port, password, keyPrefix, maxConnections);
            
            // Register as singleton
            container.registerSingleton(clazz, instance);
            System.out.println("Registered adapter: cacheAdapter -> " + beanConfig.get("implementation"));
        } catch (Exception e) {
            System.err.println("Warning: Failed to register cache adapter: " + e.getMessage());
            // Don't throw - cache is optional
        }
    }
    
    @SuppressWarnings("unchecked")
    private void registerRepositories() {
        Map<String, Object> repos = (Map<String, Object>) configuration.get("repositories");
        if (repos != null) {
            for (Map.Entry<String, Object> entry : repos.entrySet()) {
                String beanName = entry.getKey();
                Map<String, Object> beanConfig = (Map<String, Object>) entry.getValue();
                registerBean(beanName, beanConfig);
            }
        }
        
        // Register transaction manager
        Map<String, Object> transactionManagerConfig = (Map<String, Object>) configuration.get("transactionManager");
        if (transactionManagerConfig != null) {
            registerBean("transactionManager", transactionManagerConfig);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void registerServices() {
        Map<String, Object> services = (Map<String, Object>) configuration.get("services");
        if (services != null) {
            for (Map.Entry<String, Object> entry : services.entrySet()) {
                String beanName = entry.getKey();
                Map<String, Object> beanConfig = (Map<String, Object>) entry.getValue();
                registerBean(beanName, beanConfig);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void registerBean(String beanName, Map<String, Object> beanConfig) {
        try {
            String implementation = (String) beanConfig.get("implementation");
            if (implementation == null || implementation.isEmpty()) {
                System.out.println("Warning: Bean '" + beanName + "' has no implementation, skipping...");
                return;
            }
            
            String scope = (String) beanConfig.get("scope");
            boolean singleton = "singleton".equals(scope);
            
            Class<?> clazz = Class.forName(implementation);
            
            // Try to find constructor dependencies
            Constructor<?>[] constructors = clazz.getConstructors();
            if (constructors.length == 0) {
                throw new RuntimeException("No public constructor found for: " + implementation);
            }
            
            Constructor<?> constructor = constructors[0]; // Use first public constructor
            
            List<Class<?>> dependencies = new java.util.ArrayList<>();
            for (Class<?> paramType : constructor.getParameterTypes()) {
                dependencies.add(paramType);
            }
            
            BeanDefinition definition = new BeanDefinition(clazz, singleton, dependencies);
            container.registerBean(clazz, definition);
            
            // If singleton, create instance now (will be cached)
            if (singleton) {
                try {
                    Object instance = container.getBean(clazz);
                    System.out.println("Registered bean: " + beanName + " -> " + implementation);
                    
                    // Special handling: Set cache port in UserUseCase if cache is available
                    if ("userService".equals(beanName) && instance instanceof com.subscription.subscriptionservice.application.service.UserUseCase) {
                        try {
                            com.subscription.subscriptionservice.application.port.outbound.CachePort cachePort = 
                                container.getBean(com.subscription.subscriptionservice.application.port.outbound.CachePort.class);
                            if (cachePort != null) {
                                ((com.subscription.subscriptionservice.application.service.UserUseCase) instance).setCachePort(cachePort);
                                System.out.println("Cache port set in UserUseCase");
                            }
                        } catch (Exception e) {
                            // Cache is optional - ignore if not available
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Failed to instantiate bean '" + beanName + "': " + e.getMessage());
                    // Don't throw - allow other beans to register
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Warning: Class not found for bean '" + beanName + "': " + 
                beanConfig.get("implementation") + ". Skipping...");
            // Don't throw - allow application to start with missing optional beans
        } catch (Exception e) {
            System.err.println("Warning: Failed to register bean '" + beanName + "': " + e.getMessage());
            // Don't throw - allow application to start
        }
    }
    
    private void initializeHttpServer() {
        HttpServer server = new JettyHttpServer();
        container.registerSingleton(HttpServer.class, server);
    }
    
    public Container getContainer() {
        return container;
    }
    
    public Map<String, Object> getConfiguration() {
        return configuration;
    }
    
    public void start() throws Exception {
        HttpServer server = container.getBean(HttpServer.class);
        Map<String, Object> serverConfig = (Map<String, Object>) configuration.get("server");
        int port = serverConfig != null && serverConfig.get("port") != null 
            ? ((Number) serverConfig.get("port")).intValue() : 8080;
        server.start(port);
    }
    
    public void stop() throws Exception {
        HttpServer server = container.getBean(HttpServer.class);
        server.stop();
    }
}
