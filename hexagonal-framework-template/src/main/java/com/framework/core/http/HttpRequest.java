package com.framework.core.http;

import java.util.Map;

/**
 * HTTP Request representation
 */
public class HttpRequest {
    private String method;
    private String path;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private String body;
    private Map<String, String> pathParams;
    
    public HttpRequest() {
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public String getHeader(String name) {
        return headers != null ? headers.get(name) : null;
    }
    
    public Map<String, String> getQueryParams() {
        return queryParams;
    }
    
    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }
    
    public String getQueryParam(String name) {
        return queryParams != null ? queryParams.get(name) : null;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public Map<String, String> getPathParams() {
        return pathParams;
    }
    
    public void setPathParams(Map<String, String> pathParams) {
        this.pathParams = pathParams;
    }
    
    public String getPathParam(String name) {
        return pathParams != null ? pathParams.get(name) : null;
    }
}

