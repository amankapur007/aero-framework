package io.aeroframework.web.server;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface HttpRequest {
    String getMethod();
    String getUri();
    Map<String, List<String>> getHeaders();
    Map<String,String> getPathVariables();
    Map<String,String> getQueryParameters();
    InputStream getBody();
}
