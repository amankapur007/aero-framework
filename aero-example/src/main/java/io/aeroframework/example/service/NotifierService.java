package io.aeroframework.example.service;

import io.aeroframework.annotations.Autowired;
import io.aeroframework.annotations.Service;
import io.aeroframework.example.component.EmailSender;

@Service
public class NotifierService {
    @Autowired
    EmailSender emailSender;

    public String send(String message) {
        return emailSender.sendEmail(message);
    }
}
