package io.aeroframework.web.handler;

import io.aeroframework.web.server.HttpRequest;
import io.aeroframework.web.server.HttpResponse;

@FunctionalInterface
public interface WebHandler {
    void handle(HttpRequest httpRequest, HttpResponse httpResponse);
}
