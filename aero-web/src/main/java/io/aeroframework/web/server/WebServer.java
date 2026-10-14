package io.aeroframework.web.server;

public interface WebServer {
    void start(int port) throws Exception;
    void stop();
}
