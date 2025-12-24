package com.framework.core.di;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean definition for dependency injection
 */
public class BeanDefinition {
    private final Class<?> beanClass;
    private final boolean singleton;
    private final List<Class<?>> constructorDependencies;
    
    public BeanDefinition(Class<?> beanClass, boolean singleton) {
        this.beanClass = beanClass;
        this.singleton = singleton;
        this.constructorDependencies = new ArrayList<>();
    }
    
    public BeanDefinition(Class<?> beanClass, boolean singleton, List<Class<?>> dependencies) {
        this.beanClass = beanClass;
        this.singleton = singleton;
        this.constructorDependencies = new ArrayList<>(dependencies);
    }
    
    public Object createInstance(Container container) {
        try {
            if (constructorDependencies.isEmpty()) {
                Constructor<?> constructor = beanClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } else {
                // Get dependencies from container
                Object[] args = new Object[constructorDependencies.size()];
                for (int i = 0; i < constructorDependencies.size(); i++) {
                    args[i] = container.getBean(constructorDependencies.get(i));
                }
                
                Class<?>[] paramTypes = constructorDependencies.toArray(new Class<?>[0]);
                Constructor<?> constructor = beanClass.getDeclaredConstructor(paramTypes);
                constructor.setAccessible(true);
                return constructor.newInstance(args);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of: " + beanClass.getName(), e);
        }
    }
    
    public Class<?> getBeanClass() {
        return beanClass;
    }
    
    public boolean isSingleton() {
        return singleton;
    }
    
    public List<Class<?>> getConstructorDependencies() {
        return constructorDependencies;
    }
}

