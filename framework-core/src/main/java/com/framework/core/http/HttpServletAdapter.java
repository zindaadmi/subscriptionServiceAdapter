package com.framework.core.http;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Jetty Handler adapter to bridge Jetty with our HTTP abstraction
 */
public class HttpServletAdapter extends AbstractHandler {
    
    private final JettyHttpServer httpServer;
    
    public HttpServletAdapter(JettyHttpServer httpServer) {
        this.httpServer = httpServer;
    }
    
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse resp) 
            throws java.io.IOException, ServletException {
        try {
            HttpRequest request = convertRequest(req);
            HttpResponse response = httpServer.handleRequest(request);
            writeResponse(resp, response);
            baseRequest.setHandled(true);
        } catch (Exception e) {
            resp.setStatus(500);
            resp.setContentType("application/json");
            PrintWriter writer = resp.getWriter();
            writer.write("{\"error\":\"" + e.getMessage() + "\"}");
            writer.flush();
            baseRequest.setHandled(true);
        }
    }
    
    private HttpRequest convertRequest(HttpServletRequest req) throws IOException {
        HttpRequest request = new HttpRequest();
        request.setMethod(req.getMethod());
        request.setPath(req.getRequestURI());
        
        // Headers
        Map<String, String> headers = new HashMap<>();
        req.getHeaderNames().asIterator().forEachRemaining(name -> 
            headers.put(name, req.getHeader(name))
        );
        request.setHeaders(headers);
        
        // Query parameters
        Map<String, String> queryParams = new HashMap<>();
        req.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                queryParams.put(key, values[0]);
            }
        });
        request.setQueryParams(queryParams);
        
        // Body
        if (req.getContentLength() > 0) {
            try (BufferedReader reader = req.getReader()) {
                request.setBody(reader.lines().collect(Collectors.joining("\n")));
            }
        }
        
        return request;
    }
    
    private void writeResponse(HttpServletResponse resp, HttpResponse response) throws IOException {
        resp.setStatus(response.getStatusCode());
        response.getHeaders().forEach(resp::setHeader);
        if (response.getBody() != null) {
            PrintWriter writer = resp.getWriter();
            writer.write(response.getBody());
            writer.flush();
        }
    }
}

