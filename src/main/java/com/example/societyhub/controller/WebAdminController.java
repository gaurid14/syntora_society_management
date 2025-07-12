package com.example.societyhub.controller;

import com.example.societyhub.service.DBHandler;
import com.example.societyhub.service.Society;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class WebAdminController {
    @Autowired
    private DBHandler dbHandler;

    @GetMapping("/web_admin")
    public String getAdminPage(Model model, HttpSession session) throws SQLException {
        String username = (String) session.getAttribute("webAdminUsername");

        // Add session attributes to the model if needed
        model.addAttribute("webAdminUsername", username);
        List<Society> societies = dbHandler.getAllSocieties();

        // Populate residents and bills for each society
        for (Society society : societies) {
            society.setResidents(dbHandler.getResident(society.getSid()));
            society.setBills(dbHandler.fetchBillDetails(society.getSid()));
            society.setAdmins(dbHandler.getAdmin(society.getSid()));
        }

        model.addAttribute("societies", societies);
        return "webadmin/web_admin";
    }
}