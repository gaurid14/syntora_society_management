package com.example.societyhub.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SMSService {
    public static final String ACCOUNT_SID = "";
    public static final String AUTH_TOKEN = "";

    @PostMapping("/sendSMS")
    public ResponseEntity<String> sendSMS(){
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber("+"),
                "", "Hello user")
                .create();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
