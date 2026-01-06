package com.framework.core.http;

/**
 * HTTP Filter interface
 */
public interface Filter {
    void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) throws Exception;
}

