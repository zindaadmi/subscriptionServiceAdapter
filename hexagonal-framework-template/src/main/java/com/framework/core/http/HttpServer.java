package com.framework.core.http;

import java.util.Map;

/**
 * HTTP Server interface
 */
public interface HttpServer {
    void start(int port) throws Exception;
    void stop() throws Exception;
    void addRoute(String method, String path, RequestHandler handler);
    void addFilter(Filter filter);
    boolean isRunning();
}

