package io.aeroframework.core;

import io.aeroframework.annotations.Autowired;
import io.aeroframework.annotations.Component;
import io.aeroframework.annotations.Service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IOCTest {
    @Component
    public static class EmailSender{
        public String send(String msg){
            return msg;
        }
    }

    @Service
    public static class NotificationService{
        @Autowired
        private EmailSender emailSender;

        public String sendEmail(String email){
            return emailSender.send(email);
        }
    }

    @Test
    public void test(){
        ApplicationContext applicationContext = new ApplicationContext();
        applicationContext.run("io.aeroframework.core");
        NotificationService notificationService = applicationContext.getBean(NotificationService.class);
        String message = "Email Send";
        Assertions.assertNotNull(notificationService);
        String msg = notificationService.sendEmail(message);
        Assertions.assertNotNull(msg);
        Assertions.assertEquals(message, msg);
    }
}
