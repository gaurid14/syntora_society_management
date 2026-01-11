package com.example.societyhub.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Announcement {
    private Integer sid;
    private String title;
    private String message;
    private String category;
    private LocalDateTime createdAt;
    private Boolean isActive;
}
