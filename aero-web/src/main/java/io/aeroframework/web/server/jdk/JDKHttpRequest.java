package io.aeroframework.web.server.jdk;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.aeroframework.web.server.HttpRequest;
import io.aeroframework.web.server.WebServer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class JDKHttpRequest implements HttpRequest {
    private final HttpExchange exchange;
    private final String uri;
    private final String method;
    private Map<String, List<String>> headers;
    private Map<String, String> pathVariables;
    private Map<String, List<String>> queryParameters;

    public JDKHttpRequest(HttpExchange exchange) {
        this.exchange = exchange;
        this.method = exchange.getRequestMethod();
        this.uri = exchange.getRequestURI().toString();
        this.pathVariables = new LinkedHashMap<>();
        this.queryParameters = new LinkedHashMap<>();
        extractQueryParams(exchange.getRequestURI().getRawQuery());
        populateHeaders(exchange.getRequestHeaders());
    }

    private void populateHeaders(Headers requestHeaders) {
        this.headers = requestHeaders;
    }

    private void extractQueryParams(String rawQuery) {

        if (rawQuery == null) return;
//        System.out.println(rawQuery);
//        if (rawQuery.contains("?")) {
        String[] queriesParam = rawQuery.split("&");
        for (String queryParam : queriesParam) {
            String[] param = queryParam.split("=");
            String key = param[0].trim();
            String value = param[1].trim();
            if (this.queryParameters.containsKey(key)) {
                this.queryParameters.get(key).add(value);
            } else {
                this.queryParameters.put(key, new ArrayList<>());
                this.queryParameters.get(key).add(value);
            }
//            }
        }
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    @Override
    public String getUri() {
        return this.uri;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }

    @Override
    public Map<String, String> getPathVariables() {
        return Map.of();
    }

    @Override
    public Map<String, List<String>> getQueryParameters() {
        return queryParameters;
    }

    @Override
    public InputStream getBody() {
        return null;
    }
}
