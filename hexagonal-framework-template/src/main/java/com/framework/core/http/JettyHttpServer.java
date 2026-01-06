package com.framework.core.http;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Jetty-based HTTP Server implementation
 */
public class JettyHttpServer implements HttpServer {
    
    private Server server;
    private final Map<String, RequestHandler> routes = new HashMap<>();
    private final List<Filter> filters = new ArrayList<>();
    private final HttpServletAdapter servletAdapter;
    
    public JettyHttpServer() {
        this.servletAdapter = new HttpServletAdapter(this);
    }
    
    @Override
    public void start(int port) throws Exception {
        server = new Server(port);
        server.setHandler(servletAdapter);
        server.start();
    }
    
    @Override
    public void stop() throws Exception {
        if (server != null && server.isRunning()) {
            server.stop();
        }
    }
    
    @Override
    public void addRoute(String method, String path, RequestHandler handler) {
        String key = method.toUpperCase() + ":" + path;
        routes.put(key, handler);
    }
    
    @Override
    public void addFilter(Filter filter) {
        filters.add(filter);
    }
    
    @Override
    public boolean isRunning() {
        return server != null && server.isRunning();
    }
    
    public HttpResponse handleRequest(HttpRequest request) throws Exception {
        // Apply filters
        FilterChainImpl chain = new FilterChainImpl(filters, request);
        HttpResponse response = new HttpResponse();
        chain.doFilter(request, response);
        return response;
    }
    
    public RequestHandler findHandler(String method, String path, HttpRequest request) {
        // Exact match first
        String exactKey = method.toUpperCase() + ":" + path;
        RequestHandler handler = routes.get(exactKey);
        if (handler != null) {
            return handler;
        }
        
        // Pattern matching for path parameters
        for (Map.Entry<String, RequestHandler> entry : routes.entrySet()) {
            String routeKey = entry.getKey();
            String[] parts = routeKey.split(":", 2);
            if (parts.length == 2 && parts[0].equalsIgnoreCase(method)) {
                String routePath = parts[1];
                Map<String, String> pathParams = extractPathParams(routePath, path);
                if (pathParams != null) {
                    if (request != null) {
                        request.setPathParams(pathParams);
                    }
                    return entry.getValue();
                }
            }
        }
        
        return null;
    }
    
    private Map<String, String> extractPathParams(String routePath, String requestPath) {
        // Extract path parameters from route pattern like /api/users/{id}
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            routePath.replaceAll("\\{[^}]+\\}", "([^/]+)")
        );
        java.util.regex.Matcher matcher = pattern.matcher(requestPath);
        
        if (matcher.matches()) {
            Map<String, String> params = new HashMap<>();
            // Extract parameter names from route
            java.util.regex.Pattern paramPattern = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
            java.util.regex.Matcher paramMatcher = paramPattern.matcher(routePath);
            int groupIndex = 1;
            while (paramMatcher.find()) {
                String paramName = paramMatcher.group(1);
                if (groupIndex <= matcher.groupCount()) {
                    params.put(paramName, matcher.group(groupIndex));
                }
                groupIndex++;
            }
            return params;
        }
        
        return null;
    }
    
    private class FilterChainImpl implements FilterChain {
        private final List<Filter> filters;
        private final HttpRequest request;
        private int index = 0;
        
        public FilterChainImpl(List<Filter> filters, HttpRequest request) {
            this.filters = filters;
            this.request = request;
        }
        
        @Override
        public void doFilter(HttpRequest request, HttpResponse response) throws Exception {
            if (index < filters.size()) {
                Filter filter = filters.get(index++);
                filter.doFilter(request, response, this);
            } else {
                // All filters processed, handle request
                RequestHandler handler = findHandler(request.getMethod(), request.getPath(), request);
                if (handler != null) {
                    HttpResponse handlerResponse = handler.handle(request);
                    response.setStatusCode(handlerResponse.getStatusCode());
                    response.setBody(handlerResponse.getBody());
                    response.getHeaders().putAll(handlerResponse.getHeaders());
                } else {
                    response.setStatusCode(404);
                    response.setBody("{\"error\":\"Not Found\"}");
                }
            }
        }
    }
}

