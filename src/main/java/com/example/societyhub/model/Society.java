package com.example.societyhub.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Society {
    // Getters and setters
    private int sid;
    private String name;
    private String street;
    private String landmark;
    private String locality;
    private String pincode;
    private String city;
    private String state;
    private String country;
    private int admin_id;
    private List<Resident> residents; // Add residents list
    private Bill bills; // Add bills list
    private List<Admin> admins;
}