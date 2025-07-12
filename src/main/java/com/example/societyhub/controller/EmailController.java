package com.example.societyhub.controller;

import com.example.societyhub.service.DBHandler;
import com.example.societyhub.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Map;

@Controller
@RequestMapping("/api")
public class EmailController {
    private static final Logger Log = LogManager.getLogger(EmailController.class);

    @Autowired
    private DBHandler dbHandler;

    @Autowired
    private EmailService emailService;

    @GetMapping("/notify_resident")
    public String handleResidentData(Model model, HttpSession session, HttpServletRequest request) {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");

        if (sid == null) {
            model.addAttribute("error", "Admin society ID not found");
            return "error"; // Redirect to an error page or handle accordingly
        }

        model.addAttribute("role", "admin"); // or dynamically fetched
        model.addAttribute("requestURI", request.getRequestURI());

        return "admin/notify_resident"; // Thymeleaf template for the details
    }

//    @PostMapping("/emailBill")
//    public String emailBill(@RequestParam("mygate_no") String mygateNo,@RequestParam("month") String month, @RequestParam("status") String status, @RequestParam("email") String email,
//                                HttpSession session, Model model) {
//        Integer sid = (Integer) session.getAttribute("adminSocietyId");
//        if (sid == null) {
//            model.addAttribute("error", "Session ID not found.");
//            return "error";
//        }
//
//        try {
//            System.out.println("Email Bill : " + mygateNo + " " + month + status + email);
//            // Generate and send email with the bill as PDF
//            emailService.generateBill(mygateNo, month, status, email, session, model, sid);
//            model.addAttribute("message", "Bill emailed successfully to resident.");
//
//            return "generate_bill"; // Return success page
//        } catch (SQLException e) {
//            Log.error("Error during email sending", e);
//            model.addAttribute("error", "Failed to send emails.");
//            return "error";
//        }
//    }
    @PostMapping("/emailBill")
    public ResponseEntity<Map<String, String>> emailBill(@RequestBody Map<String, String> requestBody, HttpSession session, Model model) {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");
        if (sid == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Session ID not found."));
        }

        String mygateNo = requestBody.get("mygate_no");
        String month = requestBody.get("selectedMonth");
        String status = requestBody.get("status");
        String email = requestBody.get("email");

        try {
            System.out.println("Email Bill : " + mygateNo + " " + month + " " + status + " " + email);
            emailService.generateBill(mygateNo, month, status, email, session, model, sid);

            // ✅ Return a JSON response instead of plain text
            return ResponseEntity.ok(Map.of("message", "Bill emailed successfully."));
        } catch (SQLException e) {
            Log.error("Error during email sending", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to send emails."));
        }
    }



    @PostMapping("/sendBill") // Change this to @PostMapping to handle form submission
    public String sendBill(HttpSession session, Model model) {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");
        if (sid == null) {
            model.addAttribute("error", "Session ID not found.");
            return "notify_resident"; // Return to the same Thymeleaf template
        }

        try {
            // Generate and send email with the bill as PDF
            emailService.generatePdf(session, model, sid);
            model.addAttribute("message", "Emails sent successfully✅");
        } catch (SQLException e) {
            Log.error("Error during email sending", e);
            model.addAttribute("error", "Failed to send emails.");
        }

        return "admin/notify_resident"; // Return to the same Thymeleaf template
    }
    @PostMapping("/sendMyGate") // Change this to @PostMapping to handle form submission
    public String emailMyGate(HttpSession session, Model model) {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");
        if (sid == null) {
            model.addAttribute("error", "Session ID not found.");
            return "admin/notify_resident"; // Return to the same Thymeleaf template
        }

        try {
            // Generate and send email with the bill as PDF
            emailService.sendMyGate(session, model, sid);
            model.addAttribute("message", "Emails sent successfully✅");
        } catch (SQLException e) {
            Log.error("Error during email sending", e);
            model.addAttribute("error", "Failed to send emails.");
        }

        return "admin/notify_resident"; // Return to the same Thymeleaf template
    }

    @PostMapping("/sendNotice") // Change this to @PostMapping to handle form submission
    public String emailNotice(@RequestParam("customMessage") String customMessage, HttpSession session, Model model) {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");
        if (sid == null) {
            model.addAttribute("error", "Session ID not found.");
            return "admin/notify_resident"; // Return to the same Thymeleaf template
        }

        try {
            // Generate and send email with the bill as PDF
            emailService.sendNotice(customMessage,session, model, sid);
            model.addAttribute("message", "Emails sent successfully✅");
        } catch (SQLException e) {
            Log.error("Error during email sending", e);
            model.addAttribute("error", "Failed to send emails.");
        }

        return "admin/notify_resident"; // Return to the same Thymeleaf template
    }
}
