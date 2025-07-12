package com.example.societyhub.controller;

import com.example.societyhub.service.DBHandler;
import com.example.societyhub.service.Bill;
import com.example.societyhub.service.Society;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api")
@SessionAttributes("formData")
public class BillController {
    private static final Logger Log = LogManager.getLogger(BillController.class);

    @Autowired
    private DBHandler dbHandler;

    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;
    double maintenanceContribution, subcharge, fine, other, buildingDevFund;
    double housingBoardContribution;
    double propertyTaxContribution;
    double sinkingFund;
    double reserveMhadaServiceCharge;
    double arrears;
    double creditBill;

    double currentMonthTotal;
    double amountDue;

    String due_date;

    @GetMapping("/form")
    public String showForm(Model model, HttpSession session, HttpServletRequest request) throws SQLException {
//        Integer sid = (Integer) session.getAttribute("sid");
//        String userName = (String) session.getAttribute("userName");
        Integer sid = (Integer) session.getAttribute("adminSocietyId");
        Integer society_name = (Integer) session.getAttribute("adminSocietyId");

        if (sid == null) {
            return "error"; // Redirect to an error page or handle accordingly
        }

        // Add session data to the model if needed
//        model.addAttribute("userName", userName);
        model.addAttribute("adminSocietyId", sid);
        if (!model.containsAttribute("formData")) {
            Map<String, String> formData = new HashMap<>();
            // Initialize formData with default empty values
            initializeFormData(sid, formData);
            model.addAttribute("formData", formData);
        }
        model.addAttribute("role", "admin"); // or dynamically fetched
        model.addAttribute("requestURI", request.getRequestURI());
        return "admin/bill_form"; // Thymeleaf template for the form
    }

    @PostMapping("/form/submit")
    public String handleSubmit(@RequestParam Map<String, String> formData, Model model, HttpSession session) throws Exception {
//        Integer sid = (Integer) session.getAttribute("sid");
        // Add form data to the session attributes
        Integer sid = (Integer) session.getAttribute("adminSocietyId");

        if (sid == null) {
            return "error"; // Redirect to an error page or handle accordingly
        }

        Society society = dbHandler.getSocietyBySid(sid);
        System.out.println("Name: " + society.getName());
//        formData.put("society_name", society.getName());
//        formData.put("street", society.getStreet());
//        formData.put("landmark", society.getLandmark());
//        formData.put("locality", society.getLocality());
//        formData.put("pincode", society.getPincode());
//        formData.put("city", society.getCity());

        // Add session data to the model if needed
//        model.addAttribute("userName", userName);
        model.addAttribute("adminSocietyId", sid);
        model.addAttribute("society_name", society.getName());
        model.addAttribute("formData", formData);

        // Add form data to the model to be displayed in bill.html
//        model.addAttribute("roomNo", formData.get("room_no"));
//        model.addAttribute("billNo", formData.get("bill_no"));
//        model.addAttribute("billDate", formData.get("bill_date"));
//        model.addAttribute("maintenanceContribution", formData.get("maintenance_contribution"));
//        model.addAttribute("sub_charge", formData.get("sub_charge"));
//        model.addAttribute("housing_board_contribution", formData.get("housing_board_contribution"));
//        model.addAttribute("fine", formData.get("fine"));
//        model.addAttribute("property_tax_contribution", formData.get("property_tax_contribution"));
//        model.addAttribute("building_dev_fund", formData.get("building_dev_fund"));
//        model.addAttribute("sinking_fund", formData.get("sinking_fund"));
//        model.addAttribute("other", formData.get("other"));
//        model.addAttribute("reserve_mhada_service_charge", formData.get("reserve_mhada_service_charge"));
//        model.addAttribute("bill_for", formData.get("bill_for"));
//        model.addAttribute("current_month_total", formData.get("current_month_total"));
//        model.addAttribute("amount_due_in_words", formData.get("amount_due_in_words"));
//        model.addAttribute("arrears", formData.get("arrears"));
//        model.addAttribute("bldg_fund_due", formData.get("bldg_fund_due"));
//        model.addAttribute("amount_due", formData.get("amount_due"));

        // Parse and retrieve form data
        maintenanceContribution = parseDoubleSafely(formData.get("maintenance_contribution"));
        housingBoardContribution = parseDoubleSafely(formData.get("housing_board_contribution"));
        propertyTaxContribution = parseDoubleSafely(formData.get("property_tax_contribution"));
        sinkingFund = parseDoubleSafely(formData.get("sinking_fund"));
        reserveMhadaServiceCharge = parseDoubleSafely(formData.get("reserve_mhada_service_charge"));
        subcharge = parseDoubleSafely(formData.get("sub_charge"));
        other = parseDoubleSafely(formData.get("other"));
        buildingDevFund = parseDoubleSafely(formData.get("building_dev_fund"));
        fine = parseDoubleSafely(formData.get("fine"));
        arrears = parseDoubleSafely(formData.get("arrears"));
        creditBill = parseDoubleSafely(formData.get("credit_bill"));

        // Perform calculations
        currentMonthTotal = maintenanceContribution + housingBoardContribution + propertyTaxContribution + sinkingFund + reserveMhadaServiceCharge + subcharge + other + buildingDevFund;
        System.out.println("currentMonthTotal: " + currentMonthTotal);
        amountDue = (currentMonthTotal + arrears) - creditBill;
        System.out.println("amount due: " + amountDue);

//        if()

        // Add attributes to the model
        model.addAttribute("maintenanceContribution", maintenanceContribution);
        model.addAttribute("sub_charge", formData.get("sub_charge"));
        model.addAttribute("housing_board_contribution", housingBoardContribution);
        model.addAttribute("fine", 0);
        model.addAttribute("property_tax_contribution", propertyTaxContribution);
        model.addAttribute("building_dev_fund", buildingDevFund);
        model.addAttribute("sinking_fund", sinkingFund);
        model.addAttribute("other", other);
        model.addAttribute("reserve_mhada_service_charge", reserveMhadaServiceCharge);
        model.addAttribute("bill_for", formData.get("bill_for"));
        model.addAttribute("due_date", formData.get("due_date"));
        model.addAttribute("current_month_total", currentMonthTotal);
        model.addAttribute("amount_due_in_words", formData.get("amount_due_in_words"));
        model.addAttribute("arrears", arrears);
        model.addAttribute("bldg_fund_due", formData.get("bldg_fund_due"));
        model.addAttribute("amount_due", amountDue);



        return "admin/bill"; // Thymeleaf template for the bill
    }


    @PostMapping("/bill/modify")
    public String modifyBill(@ModelAttribute("formData") Map<String, String> formData, Model model, HttpSession session) {
//        Integer sid = (Integer) session.getAttribute("sid");
        Integer sid = (Integer) session.getAttribute("adminSocietyId");

        if (sid == null) {
            return "error"; // Redirect to an error page or handle accordingly
        }

        // Add session data to the model if needed
//        model.addAttribute("userName", userName);
        model.addAttribute("adminSocietyId", sid);
        model.addAllAttributes(formData);
        return "bill_form"; // Return to the form with pre-filled values
    }


    public String prepareHtmlForPdf(Integer sid, Map<String, String> formData, Model model, double currentMonthTotal, double amountDue) throws Exception {
        // Retrieve society details from the database
        Society society = dbHandler.getSocietyBySid(sid);
        formData.put("society_name", society.getName());
        formData.put("street", society.getStreet());
        formData.put("landmark", society.getLandmark());
        formData.put("locality", society.getLocality());
        formData.put("pincode", society.getPincode());
        formData.put("city", society.getCity());

        // Add the calculated values to formData
        formData.put("current_month_total", String.valueOf(currentMonthTotal));
        formData.put("amount_due", String.valueOf(amountDue));
        formData.put("amount_due_in_words", convertNumberToWords((int) amountDue));
        formData.put("fine", String.valueOf(0));


        // Set the bill_date to the current date in India
        String billDate = LocalDate.now(ZoneId.of("Asia/Kolkata")).toString();
        formData.put("bill_date", billDate);

        // Get the next bill number
        Integer billNo = dbHandler.getNextBillNumber();
        formData.put("bill_no", billNo.toString());

        // Retrieve resident data from the database
        List<Map<String, String>> residentsData = dbHandler.queryResident(sid);
        if (residentsData == null || residentsData.isEmpty()) {
            model.addAttribute("error", "No resident data found for this session ID");
            return null; // Return error if no resident data is found
        }

        // Combine form data and resident data to generate bills for all residents
        StringBuilder htmlBuilder = new StringBuilder();
        for (Map<String, String> residentData : residentsData) {
            Map<String, String> billData = new HashMap<>(formData);
            billData.putAll(residentData);

            // Generate HTML content for each resident's bill
            Context context = new Context();
            context.setVariable("formData", billData);
            String html = thymeleafViewResolver.getTemplateEngine().process("admin/final_bill", context);
            htmlBuilder.append(html);

        }

        return htmlBuilder.toString();
    }

    public byte[] convertHtmlToPdf(String htmlContent) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(new ByteArrayInputStream(htmlContent.getBytes(StandardCharsets.UTF_8)), byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    @GetMapping("/preview-pdf")
    public String previewPdf(@ModelAttribute("formData") Map<String, String> formData, HttpServletRequest request, Model model, HttpSession session) {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");

        if (sid == null) {
            model.addAttribute("error", "Session ID not found");
            return "error"; // Handle missing session ID error
        }

        try {
            saveBillDetails(formData, request, model, session);
            // Use the service to prepare and generate the HTML for the bill
            String htmlContent = prepareHtmlForPdf(sid, formData, model, currentMonthTotal, amountDue);
            if (htmlContent == null) {
                return "error"; // Handle error when formData or resident data is missing
            }

            // Convert HTML to PDF
            byte[] pdfBytes = convertHtmlToPdf(htmlContent);

            // Encode PDF bytes to Base64 and add to session
            String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
            session.setAttribute("pdfBytes", base64Pdf);
            model.addAttribute("pdfBytes", base64Pdf);

            return "admin/preview_bill"; // Thymeleaf template for previewing the bill
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to generate PDF preview");
            return "error"; // Thymeleaf template for the error page
        }
    }

    //    @PostMapping("/bill/save")
    public String saveBillDetails(@ModelAttribute("formData") Map<String, String> formData, HttpServletRequest request, Model model, HttpSession session) {
//        session = request.getSession();
        try {
            // Retrieve sid from session
            Integer sid = (Integer) session.getAttribute("adminSocietyId");
            System.out.println("Sid: " + sid);
            if (sid == null) {
                model.addAttribute("error", "No SID available");
                return "error"; // Thymeleaf template for the error page
            }

//            dbHandler.getSocietyBySid(sid);

            // Create Bill object from formData
//            Bill bill = new Bill();
//            bill.setSid(sid); // Use SID from session
//            bill.setMaintenance_contribution(Integer.parseInt(formData.get("maintenance_contribution")));
//            bill.setHousing_board_contribution(Integer.parseInt(formData.get("housing_board_contribution")));
//            bill.setProperty_tax_contribution(Integer.parseInt(formData.get("property_tax_contribution")));
//            bill.setSinking_fund(Integer.parseInt(formData.get("sinking_fund")));
//            bill.setReserve_mhada_service_charge(Integer.parseInt(formData.get("reserve_mhada_service_charge")));
//            bill.setSub_charge(Integer.parseInt(formData.get("sub_charge")));
//            bill.setFine(Integer.parseInt(formData.get("fine")));
//            bill.setBuilding_dev_fund(Integer.parseInt(formData.get("building_dev_fund")));
//            bill.setOther(Integer.parseInt(formData.get("other")));
            Bill bill = new Bill();
            bill.setSid(sid); // Use SID from session
            bill.setMaintenance_contribution(parseIntSafely(formData.get("maintenance_contribution")));
            bill.setHousing_board_contribution(parseIntSafely(formData.get("housing_board_contribution")));
            bill.setProperty_tax_contribution(parseIntSafely(formData.get("property_tax_contribution")));
            bill.setSinking_fund(parseIntSafely(formData.get("sinking_fund")));
            bill.setReserve_mhada_service_charge(parseIntSafely(formData.get("reserve_mhada_service_charge")));
            bill.setSub_charge(parseIntSafely(formData.get("sub_charge")));
            bill.setFine(parseIntSafely(formData.get("fine")));
            bill.setBuilding_dev_fund(parseIntSafely(formData.get("building_dev_fund")));
            bill.setOther(parseIntSafely(formData.get("other")));
            bill.setDue_date(formData.get("due_date"));

            // Save or update bill details to the database
            dbHandler.insertOrUpdateBill(bill);

            model.addAttribute("message", "Bill details saved successfully");
            return "success"; // Thymeleaf template for success page
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to save bill details");
            return "error"; // Thymeleaf template for the error page
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int parseIntSafely(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return 0;
            }
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    @GetMapping("/generate-pdf")
    public String generatePdf(HttpServletRequest request, Model model, HttpSession session) {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");
        System.out.println("Generate pdf sid: " + sid);

        if (sid == null) {
            model.addAttribute("error", "Session ID (SID) not found");
            return "error"; // Return error page if sid is not available
        }

        try {
            // Fetch Bill details as a Bill object
            Bill bill = dbHandler.fetchBillDetails(sid);
            if (bill == null) {
                model.addAttribute("error", "No bill data available");
                return "admin/empty_bill"; // Return error page if no bill data is available
            }

            else {
                // Convert Bill object to a Map<String, String>
                Map<String, String> formData = new HashMap<>();
                formData.put("sid", String.valueOf(bill.getSid()));
                formData.put("maintenance_contribution", String.valueOf(bill.getMaintenance_contribution()));
                formData.put("housing_board_contribution", String.valueOf(bill.getHousing_board_contribution()));
                formData.put("property_tax_contribution", String.valueOf(bill.getProperty_tax_contribution()));
                formData.put("sinking_fund", String.valueOf(bill.getSinking_fund()));
                formData.put("reserve_mhada_service_charge", String.valueOf(bill.getReserve_mhada_service_charge()));
                formData.put("sub_charge", String.valueOf(bill.getSub_charge()));
                formData.put("fine", String.valueOf(0));
                formData.put("building_dev_fund", String.valueOf(bill.getBuilding_dev_fund()));
                formData.put("other", String.valueOf(bill.getOther()));
                formData.put("due_date", String.valueOf(bill.getDue_date()));

                // Retrieve values from formData and convert to double for calculations
                maintenanceContribution = parseDoubleSafely(formData.get("maintenance_contribution"));
                housingBoardContribution = parseDoubleSafely(formData.get("housing_board_contribution"));
                propertyTaxContribution = parseDoubleSafely(formData.get("property_tax_contribution"));
                sinkingFund = parseDoubleSafely(formData.get("sinking_fund"));
                reserveMhadaServiceCharge = parseDoubleSafely(formData.get("reserve_mhada_service_charge"));
                subcharge = parseDoubleSafely(formData.get("sub_charge"));
                fine = parseDoubleSafely(formData.get("fine"));
                buildingDevFund = parseDoubleSafely(formData.get("building_dev_fund"));
                other = parseDoubleSafely(formData.get("other"));

                // Assuming arrears value is known or retrieved from somewhere else
                double arrears = 0.0; // Set arrears value

                // Perform calculations for currentMonthTotal and amountDue
                currentMonthTotal = maintenanceContribution + housingBoardContribution +
                        propertyTaxContribution + sinkingFund +
                        reserveMhadaServiceCharge + subcharge +
                        buildingDevFund + other;

                double amountDue = currentMonthTotal - arrears;

                // Output results
                System.out.println("Current Month Total: " + currentMonthTotal);
                System.out.println("Amount Due: " + amountDue);
                // Use the service to generate HTML for PDF
                String htmlContent = prepareHtmlForPdf(sid, formData, model, currentMonthTotal, amountDue);
                System.out.println(currentMonthTotal + " " + amountDue);
//            System.out.println(htmlContent);
                if (htmlContent == null) {
                    return "error"; // Return error page if resident data is missing
                }

                // Convert the generated HTML into PDF
                byte[] pdfBytes = convertHtmlToPdf(htmlContent);

                // Store PDF in session or return it as needed (assuming you want to preview it)
                String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
                session.setAttribute("pdfBytes", base64Pdf);
                model.addAttribute("pdfBytes", base64Pdf);

                return "admin/preview_bill"; // Thymeleaf template for previewing the bill
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to generate PDF");
            return "error"; // Return error page on failure
        }
    }

    @GetMapping("/download-pdf")
    public void downloadPdf(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        session = request.getSession();
        try {
            Integer sid = (Integer) session.getAttribute("sid");
            String base64Pdf = (String) session.getAttribute("pdfBytes");
            if (base64Pdf == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No PDF available for download");
                return;
            }

            // Decode Base64 string to byte array
            byte[] pdfBytes = Base64.getDecoder().decode(base64Pdf);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=Maintenance_Bills.pdf");
            response.setContentLength(pdfBytes.length);

            try (OutputStream outputStream = response.getOutputStream()) {
                outputStream.write(pdfBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to download PDF");
        }
    }

    private void initializeFormData(int sid, Map<String, String> formData) throws SQLException {
        try {
            // Fetch Bill details as a Bill object
            Bill bill = dbHandler.fetchBillDetails(sid);
            if (bill == null) {
                formData.put("sid", ""); // Add this for the ID field
                formData.put("maintenance_contribution", "");
                formData.put("sub_charge", "");
                formData.put("housing_board_contribution", "");
                formData.put("fine", "");
                formData.put("property_tax_contribution", "");
                formData.put("building_dev_fund", "");
                formData.put("sinking_fund", "");
                formData.put("other", "");
                formData.put("reserve_mhada_service_charge", "");
                formData.put("bill_for", "");
                formData.put("due_date", "");
                formData.put("current_month_total", "");
                formData.put("amount_due_in_words", "");
                formData.put("arrears", "");
                formData.put("bldg_fund_due", "");
                formData.put("amount_due", "");
                formData.put("credit_bill", "");
            } else {
                // Convert Bill object to a Map<String, String>
//            Map<String, String> formData = new HashMap<>();
                formData.put("sid", String.valueOf(bill.getSid()));
                formData.put("maintenance_contribution", String.valueOf(bill.getMaintenance_contribution()));
                formData.put("housing_board_contribution", String.valueOf(bill.getHousing_board_contribution()));
                formData.put("property_tax_contribution", String.valueOf(bill.getProperty_tax_contribution()));
                formData.put("sinking_fund", String.valueOf(bill.getSinking_fund()));
                formData.put("reserve_mhada_service_charge", String.valueOf(bill.getReserve_mhada_service_charge()));
                formData.put("sub_charge", String.valueOf(bill.getSub_charge()));
                formData.put("fine", String.valueOf(bill.getFine()));
                formData.put("building_dev_fund", String.valueOf(bill.getBuilding_dev_fund()));
                formData.put("other", String.valueOf(bill.getOther()));
                formData.put("bill_for", "");
                formData.put("due_date", "");
                formData.put("credit_bill", "");

                // Retrieve values from formData and convert to double for calculations
                maintenanceContribution = parseDoubleSafely(formData.get("maintenance_contribution"));
                housingBoardContribution = parseDoubleSafely(formData.get("housing_board_contribution"));
                propertyTaxContribution = parseDoubleSafely(formData.get("property_tax_contribution"));
                sinkingFund = parseDoubleSafely(formData.get("sinking_fund"));
                reserveMhadaServiceCharge = parseDoubleSafely(formData.get("reserve_mhada_service_charge"));
                subcharge = parseDoubleSafely(formData.get("sub_charge"));
                fine = parseDoubleSafely(formData.get("fine"));
                buildingDevFund = parseDoubleSafely(formData.get("building_dev_fund"));
                other = parseDoubleSafely(formData.get("other"));

                // Assuming arrears value is known or retrieved from somewhere else
                double arrears = 0.0; // Set arrears value

                formData.put("amount_due_in_words", "");
                formData.put("arrears", String.valueOf(arrears));
                formData.put("bldg_fund_due", "");

                // Perform calculations for currentMonthTotal and amountDue
                currentMonthTotal = maintenanceContribution + housingBoardContribution +
                        propertyTaxContribution + sinkingFund +
                        reserveMhadaServiceCharge + subcharge +
                        buildingDevFund + other;

                formData.put("current_month_total", String.valueOf(currentMonthTotal));

                double amountDue = currentMonthTotal - arrears;
                formData.put("amount_due", String.valueOf(amountDue));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String convertNumberToWords(int number) {
        if (number == 0) {
            return "Zero";
        }

        String[] units = {
                "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
                "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
                "Seventeen", "Eighteen", "Nineteen"
        };

        String[] tens = {
                "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
        };

        String[] thousands = {
                "", "Thousand", "Million", "Billion"
        };

        String words = "";
        int i = 0;

        while (number > 0) {
            if (number % 1000 != 0) {
                words = convertHundreds(number % 1000, units, tens) + thousands[i] + " " + words;
            }
            number /= 1000;
            i++;
        }

        return words.trim();
    }

    private static String convertHundreds(int number, String[] units, String[] tens) {
        String words = "";

        if (number >= 100) {
            words += units[number / 100] + " Hundred ";
            number %= 100;
        }

        if (number >= 20) {
            words += tens[number / 10] + " ";
            number %= 10;
        }

        if (number > 0) {
            words += units[number] + " ";
        }

        return words;
    }
//    private double parseDoubleSafely(String value) {
//        if (value == null || value.trim().isEmpty()) {
//            return 0.0; // Default value if the field is empty
//        }
//        return Double.parseDouble(value);
//    }

    private double parseDoubleSafely(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return 0.0;
            }
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }


}




//    @GetMapping("/generate-pdf")
//    public String generatePdf(HttpServletRequest request, Model model, HttpSession session) {
//        session = request.getSession();
//        Integer sid = (Integer) session.getAttribute("sid");
//
//        // Check if sid is null
//        if (sid == null) {
//            model.addAttribute("error", "Session ID (SID) not found");
//            return "error"; // Return error page if sid is not available
//        }
//
//        try {
//            // Retrieve formData from session attributes
//            Map<String, String> formData = (Map<String, String>) dbHandler.fetchBillDetails(sid);
//            if (formData == null) {
//                model.addAttribute("error", "No form data available");
//                return "error"; // Return error page if form data is not available
//            }
//
//            // Retrieve resident data from the database
//            List<Map<String, String>> residentsData = dbHandler.queryResident(sid);
//
//            // Combine form data and resident data to generate bills for all residents
//            StringBuilder htmlBuilder = new StringBuilder();
//            for (Map<String, String> residentData : residentsData) {
//                Map<String, String> billData = new HashMap<>(formData);
//                billData.putAll(residentData);
//
//                // Generate HTML content for each resident's bill
//                Context context = new Context();
//                context.setVariable("formData", billData);
//                String html = thymeleafViewResolver.getTemplateEngine().process("final_bill", context);
//                htmlBuilder.append(html);
//            }
//
//            // Convert the generated HTML into PDF
//            String finalHtml = htmlBuilder.toString();
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            HtmlConverter.convertToPdf(new ByteArrayInputStream(finalHtml.getBytes()), byteArrayOutputStream);
//            byte[] pdfBytes = byteArrayOutputStream.toByteArray();
//
//            // Encode PDF bytes to Base64 and store in session
//            String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
//            session.setAttribute("pdfBytes", base64Pdf);
//            model.addAttribute("pdfBytes", base64Pdf);
//
//            return "preview_bill"; // Thymeleaf template for previewing the bill
//        } catch (Exception e) {
//            e.printStackTrace();
//            model.addAttribute("error", "Failed to generate PDF preview");
//            return "error"; // Thymeleaf template for the error page
//        }
//    }



//    @GetMapping("/preview-pdf")
//    public String previewPdf(@ModelAttribute("formData") Map<String, String> formData, HttpServletRequest request, Model model, HttpSession session) {
////        session = request.getSession();
////        Integer sid = (Integer) session.getAttribute("sid");
//        Integer sid = (Integer) session.getAttribute("adminSocietyId");
//        saveBillDetails(formData, request, model, session);
//
//        if (sid == null) {
//            return "error"; // Redirect to an error page or handle accordingly
//        }
//
//        // Add session data to the model if needed
////        model.addAttribute("userName", userName);
//        model.addAttribute("adminSocietyId", sid);
//        System.out.println("Session attribute: "+ sid);
//        // Check if sid is null
//
//        try {
//            // Retrieve formData from session attributes
////            Map<String, String> formData = (Map<String, String>) session.getAttribute("formData");
//            formData = (Map<String, String>) session.getAttribute("formData");
//            if (formData == null) {
//                model.addAttribute("error", "No form data available");
//                return "error"; // Return error page if form data is not available
//            }
//
//            // Retrieve society details from the database
//            Society society;
//            try {
//                society = dbHandler.getSocietyBySid(sid);
//            } catch (Exception e) {
//                model.addAttribute("error", "Error retrieving society data");
//                return "error";
//            }
//
//            // Add society details to formData map
//            formData.put("society_name", society.getName());
//            formData.put("street", society.getStreet());
//            formData.put("landmark", society.getLandmark());
//            formData.put("locality", society.getLocality());
//            formData.put("pincode", society.getPincode());
//            formData.put("city", society.getCity());
//
//            // Set the bill_date to current date in India
//            String billDate = LocalDate.now(ZoneId.of("Asia/Kolkata")).toString();
//            formData.put("bill_date", billDate);
//
//            // Get the next bill number from the sequence
//            Integer billNo = dbHandler.getNextBillNumber(); // Call method to get the next value from the sequence
//            formData.put("bill_no", billNo.toString());
//
////            String currentMonthTotal = formData.get("current_month_total");
////            String currentMonthTotalInWords = WordUtils.capitalizeFully(NumberToWords.convert(Integer.parseInt(currentMonthTotal)));
////            formData.put("amount_due_in_words", currentMonthTotalInWords);
//
//            // Check if formData is present in the session, if not set it
//            session.setAttribute("formData", formData);
//
//            // Retrieve resident data from the database
//            List<Map<String, String>> residentsData = dbHandler.queryResident(sid);
//            System.out.println("Residents data: " + residentsData);
//            if (residentsData == null || residentsData.isEmpty()) {
//                model.addAttribute("error", "No resident data found for this session ID");
//                return "error"; // Return error page if no resident data is found
//            }
//
//            // Combine form data and resident data to generate bills for all residents
//            StringBuilder htmlBuilder = new StringBuilder();
//            for (Map<String, String> residentData : residentsData) {
//                Map<String, String> billData = new HashMap<>(formData);
//                billData.putAll(residentData);
//
//                System.out.println("Hiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
//
//                // Generate HTML content for each resident's bill
//                Context context = new Context();
//                context.setVariable("formData", billData);
//                String html = thymeleafViewResolver.getTemplateEngine().process("final_bill", context);
//                htmlBuilder.append(html);
//            }
//
//            // Debug: Print the generated HTML content for verification
//            String finalHtml = htmlBuilder.toString();
////            System.out.println("Generated HTML: " + finalHtml); // Debugging purposes
//
//            // Convert the generated HTML into PDF
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            HtmlConverter.convertToPdf(new ByteArrayInputStream(finalHtml.getBytes(StandardCharsets.UTF_8)), byteArrayOutputStream);
//            byte[] pdfBytes = byteArrayOutputStream.toByteArray();
//
//            if (pdfBytes.length == 0) {
//                model.addAttribute("error", "Failed to generate PDF. The content is empty.");
//                return "error"; // Return error page if PDF generation fails
//            }
//
//            // Encode PDF bytes to Base64 and store in session
//            String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);
//            session.setAttribute("pdfBytes", base64Pdf);
//            model.addAttribute("pdfBytes", base64Pdf);
//
//            return "preview_bill"; // Thymeleaf template for previewing the bill
//        } catch (Exception e) {
//            e.printStackTrace();
//            model.addAttribute("error", "Failed to generate PDF preview due to an error: " + e.getMessage());
//            return "error"; // Thymeleaf template for the error page
//        }
//    }