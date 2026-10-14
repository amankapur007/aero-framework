package io.aeroframework.boot;

import io.aeroframework.core.ApplicationContext;
import io.aeroframework.web.context.WebApplicationContext;

public class AeroApplication {

    public WebApplicationContext run(String packageName) {
        long startTime = System.currentTimeMillis();
        System.out.println("Starting application...");

        ApplicationContext app = new ApplicationContext();
        app.run(packageName);
        WebApplicationContext wac = new WebApplicationContext(app);
        try {
            wac.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start application", e);
        }
        long timeTaken = System.currentTimeMillis() - startTime;
        System.out.println("Application started in " + timeTaken + " ms");
        return wac;
    }
}
