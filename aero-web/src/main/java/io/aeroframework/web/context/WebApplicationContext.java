package io.aeroframework.web.context;

import io.aeroframework.annotations.Controller;
import io.aeroframework.core.ApplicationContext;
import io.aeroframework.web.handler.DispatcherHandler;
import io.aeroframework.web.handler.HandlerMapping;
import io.aeroframework.web.server.WebServer;
import io.aeroframework.web.server.jdk.JDKServer;

import java.util.Map;

public class WebApplicationContext {

    private final ApplicationContext applicationContext;
    private final WebServer webServer;
    private final HandlerMapping handlerMapping;

    int port=8080;

    public WebApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.handlerMapping = new  HandlerMapping();
        DispatcherHandler handler = new DispatcherHandler(handlerMapping);
        this.webServer = new JDKServer(handler, port);
        initController();
    }

    public WebApplicationContext(ApplicationContext applicationContext, int port) {
        this.applicationContext = applicationContext;
        this.handlerMapping = new  HandlerMapping();
        DispatcherHandler handler = new DispatcherHandler(handlerMapping);
        this.port = port;
        this.webServer = new JDKServer(handler, port);
        initController();
    }

    private void initController() {
        Map<String, Object> controllers = applicationContext.getAllBeans();
        for (Map.Entry<String, Object> entry : controllers.entrySet()) {
            if(entry.getValue().getClass().isAnnotationPresent(Controller.class)) {
                handlerMapping.registerController(entry.getValue());
            }
        }
    }

    public void start() throws Exception {
        webServer.start();
    }

    public void stop() throws Exception {
        webServer.stop();
    }

    public int getPort() {
        return port;
    }
}
