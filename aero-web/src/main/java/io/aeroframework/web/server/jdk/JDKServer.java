package io.aeroframework.web.server.jdk;

import com.sun.net.httpserver.HttpServer;
import io.aeroframework.web.server.HttpRequest;
import io.aeroframework.web.server.HttpResponse;
import io.aeroframework.web.server.WebServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class JDKServer implements WebServer {
    private HttpServer httpServer;
    @Override
    public void start(int port) {
        try {
            this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            this.httpServer.createContext("/", exchange -> {
                HttpRequest request = new JDKHttpRequest(exchange);
                HttpResponse response = new JDKHttpResponse(exchange);

                response.setStatus(200);
                response.setHeader("Content-Type", "application/json");
                response.setBody("""
                        {"message":"Hello World!"}"
                        """);
                response.commit();
            });
            this.httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            this.httpServer.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start http server", e);
        }
    }

    /**
     * Stopping the server
     */
    @Override
    public void stop() {
        if(httpServer!=null){
            httpServer.stop(0);
        }
    }
}
