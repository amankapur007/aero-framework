package io.aeroframework.web.server.jdk;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.aeroframework.web.server.HttpRequest;
import io.aeroframework.web.server.WebServer;

import java.io.InputStream;
import java.net.URI;
import java.util.*;


public class JDKHttpRequest implements HttpRequest {
    private final HttpExchange exchange;
    private final Map<String, String> pathVariables = new LinkedHashMap<>();
    private final Map<String, String> queryParameters = new LinkedHashMap<>();

    public JDKHttpRequest(HttpExchange exchange) {
        this.exchange = exchange;
        extractQueryParams(exchange.getRequestURI().getRawQuery());
    }

    private void extractQueryParams(String rawQuery) {
     if (rawQuery == null) return;

     String[] queryParams = rawQuery.split("&");
     for (String queryParam : queryParams) {
         int idx = queryParam.indexOf("=");
         String key = queryParam.substring(0, idx);
         String value = queryParam.length()>idx+1?queryParam.substring(idx + 1):"";
         queryParameters.put(key, value);
     }
    }

    public HttpExchange getExchange() {
        return exchange;
    }

    @Override
    public String getHttpMethod() {
        return exchange.getRequestMethod();
    }

    @Override
    public URI getUri() {
        return exchange.getRequestURI();
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return Collections.unmodifiableMap(exchange.getRequestHeaders());
    }

    @Override
    public Map<String, String> getPathVariables() {
        return Collections.unmodifiableMap(pathVariables);
    }

    @Override
    public Map<String, String> getQueryParameters() {
        return Collections.unmodifiableMap(queryParameters);
    }

    @Override
    public String getQueryParameter(String name) {
        return this.queryParameters.get(name);
    }

    @Override
    public InputStream getBody() {
        return exchange.getRequestBody();
    }

    @Override
    public void setPathVariables(String key, String value) {
        this.pathVariables.put(key, value);
    }

}
