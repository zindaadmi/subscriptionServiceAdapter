package com.framework.core.http;

/**
 * Filter chain for processing filters
 */
public interface FilterChain {
    void doFilter(HttpRequest request, HttpResponse response) throws Exception;
}

