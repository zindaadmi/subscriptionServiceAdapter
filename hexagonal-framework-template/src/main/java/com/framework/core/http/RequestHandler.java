package com.framework.core.http;

/**
 * HTTP request handler
 */
public interface RequestHandler {
    HttpResponse handle(HttpRequest request) throws Exception;
}

