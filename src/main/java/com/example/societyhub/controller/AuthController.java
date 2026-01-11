package com.example.societyhub.controller;

import com.example.societyhub.model.Admin;
import com.example.societyhub.model.Resident;
import com.example.societyhub.model.WebAdmin;
import com.example.societyhub.service.*;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private DBHandler dbHandler;
    @Autowired
    private ExcelService excelService;
    @Autowired
    private EmailService emailService;
    private String generatedOtp;

    private static final Logger Log = LogManager.getLogger(AuthController.class);

    @PostMapping("/register")
    public ResponseEntity<String> registerAdmin(@RequestBody Admin admin, HttpSession session) {
        try {
            Integer societyId = (Integer) session.getAttribute("societyId"); // Get from session
            if (societyId == null) {
                return ResponseEntity.status(400).body("Society ID not found in session.");
            }
            System.out.println("One");
            // Log incoming data
            System.out.println("Attempting to register admin: " + admin.getEmail_id());
            String name = admin.getName();
            String contact_no = admin.getContact_no();
            String email_id = admin.getEmail_id();
            System.out.println("Admin side society id: " + societyId);

            // Check if the user already exists
            if (dbHandler.adminExists(admin.getEmail_id())) {
                System.out.println("Admin already exists: " + admin.getEmail_id());
                return ResponseEntity.badRequest().body("Admin with this email already exists.");
            }

            // Hash the password and log it
            String hashedPassword = hashPassword(admin.getAdminPassword());
            System.out.println("Hashed password: " + hashedPassword);

            // Store the user in the database
            dbHandler.registerAdmin(admin.getEmail_id(), hashedPassword);
            Log.info("Admin registered: Name: " + admin.getName() + " Email: " + admin.getEmail_id());
            dbHandler.update(societyId, name, contact_no, email_id);
            Log.info("Admin details updated: Name: " + admin.getName() + " Email: " + admin.getEmail_id());

            System.out.println("Admin registered successfully");
            return ResponseEntity.ok("Admin registered successfully.");
        } catch (Exception e) {
            e.printStackTrace();  // Log full error details
            return ResponseEntity.status(500).body("Registration failed.");
        }
    }


    // Login Endpoint
    @PostMapping("/residentLogin")
    public ResponseEntity<Map<String, Object>> residentLogin(@RequestBody Resident resident, HttpSession session) {
        try {
            // Check if the user exists and fetch the hashed password
            String storedHashedPassword = dbHandler.getPasswordByMyGateNo(resident.getMygate_no());
            if (storedHashedPassword == null) {
                return ResponseEntity.status(401).body(Map.of("message", "User does not exist"));
            }

            // Hash the provided password and compare
            if (storedHashedPassword.equals(hashPassword(resident.getResidentPassword()))) {
                // Fetch the complete admin details and assign back to admin object
                Resident completeResident = dbHandler.getResident(resident.getMygate_no());

                if (completeResident != null) {
                    // Copy details from completeAdmin to the passed admin object
                    resident.setMr_ms(completeResident.getMr_ms());
                    resident.setName(completeResident.getName());
                    resident.setRoom_no(completeResident.getRoom_no());
                    resident.setEmail(completeResident.getEmail());
                    resident.setContactNo(completeResident.getContactNo());

                    // Set session attributes
                    session.setAttribute("residentMygate", resident.getMygate_no());
//                    session.setAttribute("adminName", admin.getName());
//                    session.setAttribute("adminContactNo", admin.getContact_no());
//                    session.setAttribute("adminSocietyId", admin.getSocietyId());
//                    session.setAttribute("adminMemId", admin.getMem_id());

                    // Debugging statements to confirm values
                    System.out.println(resident.getName());
                    System.out.println(resident.getContactNo());
                    System.out.println(resident.getEmail());

                    // Prepare the response
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Login successful");

                    Log.info("Resident login successful: Name: " + resident.getName() + " Email: " + resident.getEmail());

                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(401).body(Map.of("message", "Admin details not found"));
                }
            } else {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Server error"));
        }
    }

    @PostMapping("/adminLogin")
    public ResponseEntity<Map<String, Object>> adminLogin(@RequestBody Admin admin, HttpSession session) {
        try {
            // Check if the user exists and fetch the hashed password
            String storedHashedPassword = dbHandler.getPasswordByEmail(admin.getEmail_id());
            if (storedHashedPassword == null) {
                return ResponseEntity.status(401).body(Map.of("message", "User does not exist"));
            }

            System.out.println("Hello"+ admin.getAdminPassword());

            // Hash the provided password and compare
            if (storedHashedPassword.equals(hashPassword(admin.getAdminPassword()))) {
                // Fetch the complete admin details and assign back to admin object
                Admin completeAdmin = dbHandler.getAdminDetails(admin.getEmail_id());
                Boolean data_uploaded = dbHandler.isDataUploaded(completeAdmin.getSocietyId());

                if (completeAdmin != null) {
                    // Copy details from completeAdmin to the passed admin object
                    admin.setName(completeAdmin.getName());
                    admin.setContact_no(completeAdmin.getContact_no());
                    admin.setMem_id(completeAdmin.getMem_id());
                    admin.setSocietyId(completeAdmin.getSocietyId());

                    // Set session attributes
                    session.setAttribute("adminEmail", admin.getEmail_id());
                    session.setAttribute("adminName", admin.getName());
                    session.setAttribute("adminContactNo", admin.getContact_no());
                    session.setAttribute("adminSocietyId", admin.getSocietyId());
                    session.setAttribute("adminMemId", admin.getMem_id());
                    session.setAttribute("dataUploaded", data_uploaded);

                    // Debugging statements to confirm values
                    System.out.println(admin.getName());
                    System.out.println(admin.getContact_no());
                    System.out.println(admin.getEmail_id());
                    System.out.println(admin.getSocietyId());
                    System.out.println(admin.getMem_id());
                    System.out.println(data_uploaded);

                    // Prepare the response
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Login successful");
                    Log.info("Admin login successful: Name: " + admin.getName() + " Email: " + admin.getEmail_id());
                    response.put("dataUploaded", data_uploaded);

                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(401).body(Map.of("message", "Admin details not found"));
                }
            } else {
                return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Server error"));
        }
    }


    @PostMapping("/admin/login")
    public ResponseEntity<Map<String, Object>> adminLogin(@RequestBody WebAdmin webAdmin, HttpSession session) {
        try {
            // Check if the user exists and fetch the hashed password
            String storedHashedPassword = dbHandler.getAdminPassword(webAdmin.getUsername());
            if (storedHashedPassword == null) {
                return ResponseEntity.status(401).body(Map.of("message", "User does not exist"));
            }

            // Hash the provided password and compare
            if (storedHashedPassword.equals(hashPassword(webAdmin.getPassword()))) {

                // Set session attributes
                session.setAttribute("webAdminUsername", webAdmin.getUsername());

                // Debugging statements to confirm values
                System.out.println(webAdmin.getUsername());

                // Prepare the response
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login successful");

                Log.info("WebAdmin login successful: Username: " + webAdmin.getUsername());

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(401).body(Map.of("message", "WebAdmin details not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Server error"));
        }
    }

    // Logout Endpoint
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();  // Destroy session
        return ResponseEntity.ok("Logged out successfully");
//        return "index";
    }

    // Helper method to hash passwords using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            System.out.println("Hashed password: " + hexString);
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody Map<String, String> request, HttpSession session) {
        Integer societyId = (Integer) session.getAttribute("societyId"); // Get societyId from session
        String mygate_no = request.get("mygate_no");
        System.out.println("OTP sent to mygate: " + mygate_no);

        try {
            Resident resident = dbHandler.getResident(mygate_no);

            if (resident != null) {
                // Generate 4-digit OTP
                generatedOtp = String.format("%04d", new Random().nextInt(9999));
                System.out.println("generatedOtp: " + generatedOtp);
                session.setAttribute("otp", generatedOtp); // Save OTP in session

                // Send OTP to resident email
                String to = resident.getEmail();
                System.out.println("Email: " + to);
                String subject = "MyGate Authentication OTP";
                String body = "OTP for MyGate number " + mygate_no + " is " + generatedOtp;
                emailService.sendEmail(to, subject, body, null, resident.getName());
                System.out.println("OTP sent to: " + to);

                Log.info("OTP sent successfully for MyGate No: " + mygate_no);

                return ResponseEntity.ok("OTP sent successfully");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Resident not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while sending OTP");
        }
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<Map<String, Object>> validateOtp(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String otp = request.get("otp");
        System.out.println("OTP: " + otp);
        boolean isValid = otp.equals(generatedOtp);
        System.out.println("boolean isValid: " + isValid);
        if (isValid) {
            System.out.println("isvalid true");
            response.put("status", "ok");
            response.put("message", "OTP validated.");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Invalid OTP.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/validate-mygate_no")
    public ResponseEntity<Map<String, Object>> validateMyGate(@RequestBody Map<String, String> request) {
        String mygate_no = request.get("mygate_no");
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("Received MyGate number: " + mygate_no);

            // Validate MyGate number from the database
            boolean residentExists = excelService.isMyGateNumberInDatabase(mygate_no);
            System.out.println("Does MyGate number exist in database? " + residentExists);

            if (residentExists) {
                // If valid, respond with a success message
                response.put("status", "ok");
                response.put("message", "MyGate number validated.");
                return ResponseEntity.ok(response);
            } else {
                // MyGate number not found
                response.put("status", "error");
                response.put("message", "Invalid MyGate number.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An error occurred during validation.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/create-password")
    public ResponseEntity<Map<String, Object>> createPassword(@RequestBody Map<String, String> request, HttpSession session) {
        System.out.println("createPassword method called");
        Map<String, Object> response = new HashMap<>();

        String mygate_no = request.get("mygate_no");
        System.out.println("mygate:" + mygate_no);
        String password = request.get("password");
        System.out.println("Password:" + password);
        String comPassword = request.get("comPassword"); // Change from "confirmPassword" to "comPassword"

        System.out.println("Password is been created: " + comPassword);

        try {
            // Ensure the passwords match
            if (!password.equals(comPassword)) {
                response.put("status", "error");
                response.put("message", "Passwords do not match.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Hash the password
            String hashedPassword = hashPassword(password);
            System.out.println("Hashed password: " + hashedPassword);

            // Update password in the database
            boolean updateSuccess = dbHandler.updateResidentPassword(mygate_no, hashedPassword);

            if (updateSuccess) {
                response.put("status", "ok");
                response.put("message", "Password created successfully.");
                session.setAttribute("mygate_no", mygate_no);
                Log.info("Password set successfully for MyGate No: " + mygate_no);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Failed to update password. Resident not found.");
                Log.error("Failed to create password for MyGate No: " + mygate_no);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An error occurred while creating password.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

