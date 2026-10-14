package io.aeroframework.web.server;

import java.io.OutputStream;
import java.util.Map;

public interface HttpResponse {
    void setStatus(int status);
    OutputStream getBody();
    void setHeader(String name, String value);
}
