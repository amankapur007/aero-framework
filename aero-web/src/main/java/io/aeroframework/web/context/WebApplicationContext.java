package io.aeroframework.web.context;

import io.aeroframework.annotations.Controller;
import io.aeroframework.core.ApplicationContext;
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
        this.webServer = new JDKServer();
        this.handlerMapping = new  HandlerMapping();
        initController();
    }

    public WebApplicationContext(ApplicationContext applicationContext, int port) {
        this.applicationContext = applicationContext;
        this.webServer = new JDKServer();
        this.handlerMapping = new  HandlerMapping();
        this.port = port;
        initController();
    }

    private void initController() {
        Map<String, Object> controllers = applicationContext.getBeans(Controller.class);
        for (Map.Entry<String, Object> entry : controllers.entrySet()) {
            handlerMapping.registerController(entry.getValue());
        }
    }

    public void start() throws Exception {
        webServer.start(port);
    }
}
