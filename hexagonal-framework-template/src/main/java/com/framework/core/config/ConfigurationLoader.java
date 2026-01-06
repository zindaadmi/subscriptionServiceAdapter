package com.framework.core.config;

import java.io.InputStream;
import java.util.Map;

/**
 * Configuration loader interface for loading configuration from various sources
 */
public interface ConfigurationLoader {
    Map<String, Object> loadConfiguration(String source) throws ConfigurationException;
    Map<String, Object> loadConfiguration(InputStream inputStream) throws ConfigurationException;
}

