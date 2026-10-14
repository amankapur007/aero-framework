package io.aeroframework.example;

import io.aeroframework.boot.AeroApplication;

public class ExampleApp {
    public static void main(String[] args) throws Exception {
        AeroApplication app = new AeroApplication();
        app.run("io.aeroframework.example");
    }
}
