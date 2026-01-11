package com.example.societyhub.controller;

import com.example.societyhub.service.DBHandler;
import com.example.societyhub.model.Note;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {
    private static final Logger Log = LogManager.getLogger(ResidentController.class);

    @Autowired
    private DBHandler dbHandler;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;
    @GetMapping("/admin")
    public String getAdminPage(Model model, HttpSession session, HttpServletRequest request) throws SQLException {
        Integer societyId = (Integer) session.getAttribute("adminSocietyId");
        String adminName = (String) session.getAttribute("adminName");

        System.out.println("SocietyId: " + societyId);

        try {
            List<Note> noteItems = dbHandler.getNotes(societyId);
            model.addAttribute("notes", noteItems);
            System.out.println("Notes retrieved from DB:");
            for (Note note : noteItems) {
                System.out.println(note);
            }
        } catch (SQLException e) {
            Log.error("Error fetching notes", e);
            System.out.println("Error fetching notes");
            model.addAttribute("notes", List.of()); // empty list fallback
        }

        // Add session attributes and the list of To-Do items to the model
        model.addAttribute("societyId", societyId);
        model.addAttribute("adminName", adminName);
//        model.addAttribute("notes", noteItems);  // MISSING in your code
//        model.addAttribute("role", "admin");
        model.addAttribute("newNote", new Note());
        model.addAttribute("role", "admin"); // or dynamically fetched
        model.addAttribute("requestURI", request.getRequestURI());

//        System.out.println("Role: " + model.getAttribute("role"));

        return "admin/admin";  // Resolves to `admin.html`
    }

    @GetMapping("/upload")
    public String getUploadPage(Model model, HttpSession session) {
        Integer societyId = (Integer) session.getAttribute("adminSocietyId");
        String adminName = (String) session.getAttribute("adminName");

        // Add session attributes to the model if needed
        model.addAttribute("societyId", societyId);
        model.addAttribute("adminName", adminName);
        return "upload";  // This will resolve to `admin.html` in `src/main/resources/templates/`
    }

    @PostMapping("/admin/add_note")
    @ResponseBody
    public Map<String, Object> addNote(@RequestBody Map<String, String> formData, HttpSession session) {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");
        Map<String, Object> response = new HashMap<>();
        try {
            String title = formData.get("title");
            String message = formData.get("message");

            // Call DB insert function
            dbHandler.addNote(title, message, sid);  // <-- you need this

            System.out.println("Note added");

            response.put("success", true);
            response.put("message", "Note added successfully!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error adding note: " + e.getMessage());
        }
        return response;
    }


    @PostMapping("/admin/delete_note")
    @ResponseBody
    public String deleteNote(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String sidString = request.get("sid");

        if (sidString == null || title == null) {
            return "Invalid request: sid or title missing.";
        }

        try {
            int sid = Integer.parseInt(sidString);
            dbHandler.deleteNote(sid, title);
            System.out.println("Note deleted: " + title);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @PostMapping("/admin/delete_announcement")
    @ResponseBody
    public String deleteAnnouncement(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String sidString = request.get("sid");

        if (sidString == null || title == null) {
            return "Invalid request: sid or title missing.";
        }

        try {
            int sid = Integer.parseInt(sidString);
            dbHandler.deleteAnnouncement(sid, title);
            System.out.println("Announcement deleted: " + title);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
}

