package com.example.societyhub.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
public class Note {
    private String title;
    private int sid;
    private String message;
}
