//package com.example.societyhub.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//
//    @Autowired
//    private DBHandler dbHandler;
//
//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        // Fetch user by email from the database
//        Admin admin = dbHandler.getUserByEmail(email);
//
//        if (admin == null) {
//            throw new UsernameNotFoundException("User not found");
//        }
//
//        // Return a Spring Security User object with the necessary details
//        return org.springframework.security.core.userdetails.User
//                .withUsername(admin.getEmail())
//                .password(admin.getPassword()) // hashed password
//                .roles("USER") // Assign default role
//                .build();
//    }
//}
//
