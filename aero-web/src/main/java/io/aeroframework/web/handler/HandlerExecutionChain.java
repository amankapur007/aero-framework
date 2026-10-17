package io.aeroframework.web.handler;

import io.aeroframework.web.handler.route.Route;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HandlerExecutionChain {
    private Route route;
    private Map<String, String> pathVariables;

    HandlerExecutionChain(Route route, Map<String, String> pathVariables) {
        this.route = route;
        this.pathVariables = pathVariables;
    }

    public Route getRoute() {
        return route;
    }

    public Map<String, String> getPathVariables() {
        return Collections.unmodifiableMap(pathVariables);
    }
}
