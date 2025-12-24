package com.framework.core.di;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple Dependency Injection Container
 * Supports singleton and prototype scopes
 */
public class Container {
    
    private final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();
    private final Map<Class<?>, BeanDefinition> definitions = new ConcurrentHashMap<>();
    
    public <T> void registerSingleton(Class<T> clazz, T instance) {
        singletons.put(clazz, instance);
    }
    
    public <T> void registerBean(Class<T> clazz, BeanDefinition definition) {
        definitions.put(clazz, definition);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        // Check singleton cache first
        Object singleton = singletons.get(clazz);
        if (singleton != null) {
            return (T) singleton;
        }
        
        // Check bean definitions
        BeanDefinition definition = definitions.get(clazz);
        if (definition != null) {
            T instance = (T) definition.createInstance(this);
            if (definition.isSingleton()) {
                singletons.put(clazz, instance);
            }
            return instance;
        }
        
        // Try to find by interface (for port implementations)
        for (Map.Entry<Class<?>, BeanDefinition> entry : definitions.entrySet()) {
            if (clazz.isAssignableFrom(entry.getKey())) {
                BeanDefinition def = entry.getValue();
                T instance = (T) def.createInstance(this);
                if (def.isSingleton()) {
                    singletons.put(entry.getKey(), instance);
                }
                return instance;
            }
        }
        
        // Try to instantiate directly (for simple classes with no-arg constructor)
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            singletons.put(clazz, instance);
            return instance;
        } catch (NoSuchMethodException e) {
            // No no-arg constructor, try to find constructor with dependencies
            try {
                java.lang.reflect.Constructor<?>[] constructors = clazz.getConstructors();
                if (constructors.length > 0) {
                    java.lang.reflect.Constructor<?> constructor = constructors[0];
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    Object[] args = new Object[paramTypes.length];
                    for (int i = 0; i < paramTypes.length; i++) {
                        args[i] = getBean(paramTypes[i]);
                    }
                    T instance = (T) constructor.newInstance(args);
                    singletons.put(clazz, instance);
                    return instance;
                }
            } catch (Exception ex) {
                throw new RuntimeException("Failed to create bean: " + clazz.getName() + 
                    ". Make sure it's registered in application.yml or has a no-arg constructor.", ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + clazz.getName() + 
                ". Make sure it's registered in application.yml.", e);
        }
        
        throw new RuntimeException("Bean not found: " + clazz.getName() + 
            ". Register it in application.yml or ensure it has a no-arg constructor.");
    }
    
    public <T> T getBean(String name, Class<T> clazz) {
        return getBean(clazz);
    }
    
    public boolean containsBean(Class<?> clazz) {
        return singletons.containsKey(clazz) || definitions.containsKey(clazz);
    }
}

