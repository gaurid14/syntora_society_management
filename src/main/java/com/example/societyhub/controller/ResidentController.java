//package com.example.societyhub.controller;
//
//import com.example.societyhub.service.Bill;
//import com.example.societyhub.service.DBHandler;
//import com.example.societyhub.service.Resident;
//import com.example.societyhub.service.Society;
//import jakarta.servlet.http.HttpSession;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.thymeleaf.spring6.view.ThymeleafViewResolver;
//
//import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Controller
//@RequestMapping("/api")
//@SessionAttributes("formData")
//public class ResidentController {
//    private static final Logger Log = LogManager.getLogger(ResidentController.class);
//
//    @Autowired
//    private DBHandler dbHandler;
//
//    @Autowired
//    private ThymeleafViewResolver thymeleafViewResolver;
//
//    @GetMapping("/resident_details")
//    public String handleResidentData(Map<String, String> formData, Model model, HttpSession session) throws Exception {
////        Integer sid = (Integer) session.getAttribute("sid");
//        // Add form data to the session attributes
//        Integer sid = (Integer) session.getAttribute("adminSocietyId");
//
//        if (sid == null) {
//            model.addAttribute("error", "Admin society ID not found");
//            return "error"; // Redirect to an error page or handle accordingly
//        }
//
//        // Add session data to the model if needed
////        model.addAttribute("userName", userName);
//        model.addAttribute("adminSocietyId", sid);
//        try {
//            Resident resident = dbHandler.getResidentDetails(sid);
//            if (resident == null) {
//                model.addAttribute("error", "No bill data available");
//                return "error"; // Return error page if no bill data is available
//            }
//
//            // Initialize formData if it's null
//            if (formData == null) {
//                formData = new HashMap<>();
//                model.addAttribute("formData", formData); // Save it back to the model
//            }
//            formData.put("roomNo", String.valueOf(resident.getRoom_no()));
//            formData.put("name", String.valueOf(resident.getName()));
//            formData.put("gender", String.valueOf(resident.getGender()));
//            formData.put("age", String.valueOf(resident.getAge()));
//            formData.put("contactNo", String.valueOf(resident.getContactNo()));
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//        return "residents_details"; // Thymeleaf template for the details
//    }
//}

package com.example.societyhub.controller;

import com.example.societyhub.service.DBHandler;
import com.example.societyhub.service.Resident;
import com.example.societyhub.service.Society;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api")
@SessionAttributes("formData")
public class ResidentController {
    private static final Logger Log = LogManager.getLogger(ResidentController.class);

    @Autowired
    private DBHandler dbHandler;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    @GetMapping("/resident_dashboard")
    public String getResidentDashboard(Model model, HttpSession session) throws SQLException {
        System.out.println("Hello");
        String mygate_no = (String) session.getAttribute("residentMygate");
        System.out.println("Mygate no: " + mygate_no);
        Integer sid = (Integer) session.getAttribute("adminSocietyId");
//        System.out.println("Sid: " + sid);
//
//        if (sid == null) {
//            model.addAttribute("error", "Admin society ID not found");
//            return "error"; // Redirect to an error page
//        }

        try {
//            Society society = dbHandler.getSocietyBySid(sid);
//            model.addAttribute("society_name", society.getName());
//            System.out.println("Society name: " + society.getName());

            if (mygate_no == null) {
                model.addAttribute("error", "Session expired or invalid access. Please login again.");
                return "error";
            }

            Resident resident = dbHandler.getResident(mygate_no);

            if (resident == null) {
                model.addAttribute("error", "Resident details not found for mygate_no: " + mygate_no);
                return "error";
            }
            System.out.println("Bhk: " + resident.getBhk());

            // Add resident details to the model for rendering
            model.addAttribute("resident", resident);

            // Logging for debugging
            System.out.println("Resident details: " + resident);

            return "resident_dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/resident_details")
    public String handleResidentData(Model model, HttpSession session, HttpServletRequest request) {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");

        if (sid == null) {
            model.addAttribute("error", "Admin society ID not found");
            return "error"; // Redirect to an error page or handle accordingly
        }

        try {
            Society society = dbHandler.getSocietyBySid(sid);
            model.addAttribute("society_name", society.getName());
            List<Resident> residents = dbHandler.getResident(sid); // Method to get all residents based on society ID
            if (residents == null || residents.isEmpty()) {
                model.addAttribute("error", "No resident data available");
                return "error"; // Return error page if no residents are found
            }
            for (Resident r : residents) {
//                System.out.println("Resident Room No: " + r.getRoom_no());
//                System.out.println("Resident Status: " + r.getStatus());
            }

            model.addAttribute("residents", residents); // Add the list of residents to the model

            // Initialize formData for any specific resident update (if needed)
            model.addAttribute("formData", new HashMap<String, String>());
            model.addAttribute("role", "admin"); // or dynamically fetched
            model.addAttribute("requestURI", request.getRequestURI());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "admin/resident_details"; // Thymeleaf template for the details
    }

    @PostMapping("/add_resident")
    @ResponseBody
    public Map<String, Object> addResident(@RequestBody Map<String, String> formData, HttpSession session) {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");
        Map<String, Object> response = new HashMap<>();
        try {
            // Extract data from the formData
            Resident resident = new Resident();
            resident.setSid(sid);
            System.out.println("Sid: " + resident.getSid());
            resident.setRoom_no(Integer.parseInt(formData.get("room_no")));
            System.out.println("Room no: " + resident.getRoom_no());
            resident.setMr_ms(formData.get("mr_ms"));
            System.out.println("Mr_Ms: " + resident.getMr_ms());
            resident.setName(formData.get("name"));
            System.out.println("Name: " + resident.getName());
            resident.setGender(formData.get("gender"));
            System.out.println("Gender: " + resident.getGender());
            resident.setAge(Integer.parseInt(formData.get("age")));
            resident.setContactNo(formData.get("contactNo"));
            resident.setBhk(formData.get("bhk"));
            System.out.println("BHK: " + resident.getBhk());
            resident.setEmail(formData.get("email"));
            System.out.println("Email: " + resident.getEmail());
//            resident.setStatus(formData.get("status"));
//            System.out.println("Status: " + resident.getStatus());

            // Call service method to add the resident
            dbHandler.addResident(resident); // Method to add the resident to the database

            Log.info("Resident added: Name: " + resident.getName() + " Email: " + resident.getEmail());

            response.put("success", true);
            response.put("message", "Resident added successfully!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error adding resident: " + e.getMessage());
        }
        return response; // Return JSON response
    }

    @PostMapping("/delete_residents")
    @ResponseBody
    public String deleteResidents(@RequestBody Map<String, List<String>> request) {
        List<String> mygateNos = request.get("mygateNos");
        System.out.println("mygateNos: " + mygateNos);
        for (String mygateNo : mygateNos) {
            try {
                System.out.println("mygateNo: " + mygateNo);
                dbHandler.deleteResident(mygateNo);
                Log.info("Resident deleted: Mygate No: " + mygateNo);
            } catch (Exception e) {
                return "Error deleting resident: " + e.getMessage();
            }
        }
        return "success"; // Return success message
    }


    @PostMapping("/update_resident")
    @ResponseBody
    public Map<String, Object> updateResident(@RequestBody Map<String, String> formData, HttpSession session) {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");
        Map<String, Object> response = new HashMap<>();
        try {
            // Extract data from the formData
            Resident resident = new Resident();
            resident.setSid(sid);
            System.out.println("Sid: " + resident.getSid());
            resident.setRoom_no(Integer.parseInt(formData.get("room_no")));
            System.out.println("Room no: " + resident.getRoom_no());
            resident.setMr_ms(formData.get("mr_ms"));
            System.out.println("Mr_Ms: " + resident.getMr_ms());
            resident.setName(formData.get("name"));
            System.out.println("Name: " + resident.getName());
            resident.setGender(formData.get("gender"));
            System.out.println("Gender: " + resident.getGender());
            resident.setAge(Integer.parseInt(formData.get("age")));
            resident.setContactNo(formData.get("contactNo"));
            resident.setBhk(formData.get("bhk"));
            System.out.println("BHK: " + resident.getBhk());
            resident.setEmail(formData.get("email"));
            System.out.println("Email: " + resident.getEmail());
            resident.setMonth(formData.get("month"));
//            String month = resident.getMonth();
//            String status = resident.getStatus();

//            int flag = determineFlag(month, status);
//            System.out.println("Month: " + resident.getMonth());
//            resident.setStatus(formData.get("status"));
//            System.out.println("Status: " + resident.getStatus());
            resident.setMygate_no(formData.get("mygate_no"));
            System.out.println("MyGate no: " + resident.getMygate_no());

//            boolean statusValue = "Paid".equalsIgnoreCase(resident.getStatus()) || "Paid with fine".equalsIgnoreCase(resident.getStatus());
//            String selectedMonth = resident.getMonth();
//            dbHandler.updateResidentBill(resident.getMygate_no(), selectedMonth, statusValue);

            // Call service method to update the resident data
            dbHandler.updateResident(resident); // Method to update the resident in the database

            response.put("success", true);
            response.put("message", "Resident added successfully!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error adding resident: " + e.getMessage());
        }
        return response;
    }

    private int determineFlag(String month, String status) {
        System.out.println("Processing month: " + month + ", status: " + status);

        if ("Paid".equals(status)) {
            return 1;
        } else if ("Unpaid".equals(status)) {
            return 0;
        } else if ("Paid_with_fine".equals(status)) {
            return 2;
        } else {
            return -1; // Invalid status
        }
    }

//    @GetMapping("/generateResidentBill")
//    public String getResidentBill(Model model, HttpSession session) throws SQLException {
//        Integer sid = (Integer) session.getAttribute("adminSocietyId");
//        System.out.println("Sid:               " + sid);
//
//        if (sid == null) {
//            model.addAttribute("error", "Admin society ID not found");
//            return "error"; // Redirect to an error page or handle accordingly
//        }
//        try {
//            Society society = dbHandler.getSocietyBySid(sid);
//            model.addAttribute("society_name", society.getName());
//            List<Resident> residents = dbHandler.getResident(sid); // Method to get all residents based on society ID
//            if (residents == null || residents.isEmpty()) {
//                model.addAttribute("error", "No resident data available");
//                return "error"; // Return error page if no residents are found
//            }
//            for (Resident r : residents) {
//                System.out.println("Resident Room No: " + r.getRoom_no());
//                System.out.println("Resident Month: " + r.getMonth());
//                System.out.println("Resident Status: " + r.getStatus());
//            }
//
//            model.addAttribute("residents", residents); // Add the list of residents to the model
//
//            // Initialize formData for any specific resident update (if needed)
//            model.addAttribute("formData", new HashMap<String, String>());
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        return "generate_bill";
//    }

    @GetMapping("/generateResidentBill")
    public String getResidentBill(@RequestParam(value = "month", required = false) String month, Model model, HttpSession session, HttpServletRequest request) throws SQLException {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");
        System.out.println("Sid: " + sid);

        if (sid == null) {
            model.addAttribute("error", "Admin society ID not found");
            return "error"; // Redirect to an error page or handle accordingly
        }

        try {
            // If month is null, set a default value
            if (month == null || month.isEmpty()) {
                month = "January"; // Default month
            }

//            System.out.println("Selected Month: " + month); // Print selected month

            Society society = dbHandler.getSocietyBySid(sid);
            model.addAttribute("society_name", society.getName());

            List<Resident> residents = dbHandler.getResidentBillDetails(month, sid); // Fetch residents for selected month

            if (residents == null || residents.isEmpty()) {
                model.addAttribute("error", "No resident data available");
                return "error"; // Return error page if no residents are found
            }

            for (Resident r : residents) {
                System.out.println("Helloooooooooooooooooooooooooooooooooooooooooooooo");
                String statusString = r.getStatus();

                if (statusString == null || statusString.isEmpty()) {
                    System.out.println("Status is null or empty for Resident Room No: " + r.getRoom_no());
                    continue; // Skip this iteration if status is invalid
                }

                try {
                    int statusValue = Integer.parseInt(statusString); // Get the status from the database (0, 1, or 2)
                    System.out.println("Status:::::::::::::::::: " + statusValue);

                    // Display the corresponding text based on the status
                    String status = switch (statusValue) {
                        case 0 -> "Unpaid";
                        case 1 -> "Paid";
                        case 2 -> "Paid_with_fine";
                        default -> "Unknown Status";
                    };

                    // Debugging output
//                    System.out.println("Resident Room No: " + r.getRoom_no());
//                    System.out.println("Resident Month: " + r.getMonth());
//                    System.out.println("Resident Status: " + status);
                    r.setStatus(status);

                } catch (NumberFormatException e) {
                    System.out.println("Failed to parse status for Resident Room No: " + r.getRoom_no() + ", status: " + r.getStatus());
                }
            }


            model.addAttribute("residents", residents); // Add the list of residents to the model
            model.addAttribute("formData", new HashMap<String, String>()); // Initialize formData
            model.addAttribute("role", "admin"); // or dynamically fetched
            model.addAttribute("requestURI", request.getRequestURI());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "admin/generate_bill";
    }

    @PostMapping("/update_status")
    @ResponseBody
    public Map<String, Object> updateResidentStatus(@RequestBody Map<String, String> formData, HttpSession session) {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");
        Map<String, Object> response = new HashMap<>();
        try {
            // Extract data from the formData
            Resident resident = new Resident();
            resident.setSid(sid);
            System.out.println("Sid: " + resident.getSid());
//            resident.setRoom_no(Integer.parseInt(formData.get("room_no")));
//            System.out.println("Room no: " + resident.getRoom_no());
//            resident.setName(formData.get("name"));
//            System.out.println("Name: " + resident.getName());
//            resident.setEmail(formData.get("email"));
//            System.out.println("Email: " + resident.getEmail());
            resident.setMonth(formData.get("month"));
            String month = resident.getMonth();
//            String status = resident.getStatus();



            System.out.println("Month: " + resident.getMonth());
            resident.setStatus(formData.get("status"));
            String status = resident.getStatus().replace(" ", "_");
            System.out.println("Statusssssssssssssssssssssssss: " + status);
            int flag = determineFlag(month, status);
            System.out.println("Flag: " + flag);
            System.out.println("Status: " + status);
            resident.setMygate_no(formData.get("mygate_no"));
            System.out.println("MyGate no: " + resident.getMygate_no());

//            boolean statusValue = "Paid".equalsIgnoreCase(resident.getStatus()) || "Paid_with_fine".equalsIgnoreCase(resident.getStatus());
            String selectedMonth = resident.getMonth();
            dbHandler.updateResidentBill(resident.getMygate_no(), selectedMonth, flag);

            response.put("success", true);
            response.put("message", "Resident status updated successfully!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error updating resident status: " + e.getMessage());
        }
        return response;
    }
}












