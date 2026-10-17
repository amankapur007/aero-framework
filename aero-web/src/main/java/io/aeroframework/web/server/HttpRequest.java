package io.aeroframework.web.server;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

public interface HttpRequest {
    String getHttpMethod();
    URI getUri();
    Map<String, List<String>> getHeaders();
    Map<String,String> getPathVariables();
    Map<String,String> getQueryParameters();
    String getQueryParameter(String name);
    InputStream getBody();
    void setPathVariables(String key, String value);
}
