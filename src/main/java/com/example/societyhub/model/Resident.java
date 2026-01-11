package com.example.societyhub.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Resident {
    // Getters and setters
    private String mem_id;
    private Integer sid;
    private String name;
    private Integer room_no;
    private String mr_ms;
    private String gender;
    private Integer age;
    private String contactNo;
    private Boolean isAdmin;
    private String status;
    private String mygate_no;
    private String email;
    private String bhk;
    private String residentPassword;
    private String month;
    private Boolean isMonthStatus;
}
