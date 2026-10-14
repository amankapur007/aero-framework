package io.aeroframework.example.component;

import io.aeroframework.annotations.Component;

@Component
public class EmailSender {
    public String sendEmail(String msg) {
        System.out.println("Email sending...");
        return msg;
    }
}
