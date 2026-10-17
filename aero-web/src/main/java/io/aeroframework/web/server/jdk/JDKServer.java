package io.aeroframework.web.server.jdk;

import com.sun.net.httpserver.HttpServer;
import io.aeroframework.web.handler.WebHandler;
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
    private WebHandler handler;
    private int port=0;

    public JDKServer(WebHandler handler, int port) {
        this.handler = handler;
        this.port = port;
    }

    @Override
    public void start() {
        try {
            this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            this.httpServer.createContext("/", exchange -> {
                HttpRequest httpRequest = new JDKHttpRequest(exchange);
                HttpResponse httpResponse = new JDKHttpResponse(exchange);
                handler.handle(httpRequest, httpResponse);
                httpResponse.commit();
                exchange.close();
            });
            this.httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            this.httpServer.start();
            System.out.println("Server started on port " + this.httpServer.getAddress().getPort());
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

    public WebHandler getHandler() {
        return handler;
    }
}
