package io.aeroframework.web.server.jdk;

public class JDKServerTest {
    static void main() {
        try {
            JDKServer server = new JDKServer();
            server.start(8080);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
