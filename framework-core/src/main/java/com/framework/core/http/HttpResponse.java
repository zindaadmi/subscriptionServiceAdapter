package com.framework.core.http;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Response representation
 */
public class HttpResponse {
    private int statusCode = 200;
    private Map<String, String> headers = new HashMap<>();
    private String body;
    
    public HttpResponse() {
        headers.put("Content-Type", "application/json");
    }
    
    public HttpResponse(int statusCode) {
        this();
        this.statusCode = statusCode;
    }
    
    public HttpResponse(int statusCode, String body) {
        this(statusCode);
        this.body = body;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public static HttpResponse ok(String body) {
        return new HttpResponse(200, body);
    }
    
    public static HttpResponse created(String body) {
        return new HttpResponse(201, body);
    }
    
    public static HttpResponse badRequest(String body) {
        return new HttpResponse(400, body);
    }
    
    public static HttpResponse unauthorized(String body) {
        return new HttpResponse(401, body);
    }
    
    public static HttpResponse forbidden(String body) {
        return new HttpResponse(403, body);
    }
    
    public static HttpResponse notFound(String body) {
        return new HttpResponse(404, body);
    }
    
    public static HttpResponse internalServerError(String body) {
        return new HttpResponse(500, body);
    }
}

