package com.example.societyhub.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
public class WebAdmin {
    private String username;
    private String password;
}
