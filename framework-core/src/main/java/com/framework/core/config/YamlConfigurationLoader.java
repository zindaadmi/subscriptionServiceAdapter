package com.framework.core.config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * YAML-based configuration loader
 */
public class YamlConfigurationLoader implements ConfigurationLoader {
    
    private final Yaml yaml;
    
    public YamlConfigurationLoader() {
        this.yaml = new Yaml();
    }
    
    @Override
    public Map<String, Object> loadConfiguration(String source) throws ConfigurationException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(source)) {
            if (inputStream == null) {
                throw new ConfigurationException("Configuration file not found: " + source);
            }
            return loadConfiguration(inputStream);
        } catch (Exception e) {
            throw new ConfigurationException("Error loading configuration from: " + source, e);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadConfiguration(InputStream inputStream) throws ConfigurationException {
        try {
            Object loaded = yaml.load(inputStream);
            if (loaded instanceof Map) {
                return (Map<String, Object>) loaded;
            }
            return new HashMap<>();
        } catch (Exception e) {
            throw new ConfigurationException("Error parsing YAML configuration", e);
        }
    }
}

