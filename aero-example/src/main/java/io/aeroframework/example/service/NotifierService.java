package io.aeroframework.example.service;

import io.aeroframework.annotations.Autowired;
import io.aeroframework.annotations.Service;
import io.aeroframework.example.component.EmailSender;
import io.aeroframework.example.model.Email;

@Service
public class NotifierService {
    @Autowired
    EmailSender emailSender;

    public Email send(String email, String message) {
        return emailSender.sendEmail(email, message);
    }
}
