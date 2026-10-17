package io.aeroframework.example.controller;

import io.aeroframework.annotations.Autowired;
import io.aeroframework.annotations.Controller;
import io.aeroframework.annotations.web.GetMapping;
import io.aeroframework.annotations.web.PathVariable;
import io.aeroframework.annotations.web.RequestMapping;
import io.aeroframework.example.model.Email;
import io.aeroframework.example.service.NotifierService;

@Controller
@RequestMapping(value = "/notify")
public class NotifierController {
    @Autowired
    private NotifierService notifierService;

    @GetMapping(value = "/send/{emailId}/msg/{message}")
    public Email sendEmail(@PathVariable("emailId") String emailId, @PathVariable("message") String message) {
        return notifierService.send(emailId, message);
    }
}
