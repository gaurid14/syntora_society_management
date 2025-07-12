package com.example.societyhub.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
public class Admin {
    // Getters and setters
    private int mem_id;
    private String name;
    private String contact_no;
    private String email_id;
    private String adminPassword;
    private int societyId;
    private String mygate_no;
}
