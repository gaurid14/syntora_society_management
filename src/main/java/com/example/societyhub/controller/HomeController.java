package com.example.societyhub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String languagePage() {
        return "language";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }
}

