package io.aeroframework.example.component;

import io.aeroframework.annotations.Component;
import io.aeroframework.example.model.Email;

@Component
public class EmailSender {
    public Email sendEmail(String email, String msg) {
        return new  Email(email, msg);
    }
}
