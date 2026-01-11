package com.example.societyhub.service;

import com.example.societyhub.controller.BillController;
import com.example.societyhub.model.Bill; // Ensure you import the Bill class
import com.example.societyhub.model.Resident;
import com.example.societyhub.model.Society;
import com.itextpdf.html2pdf.HtmlConverter;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {
    private static final Logger Log = LogManager.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;
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

    double currentMonthTotal;
    double amountDue;

    public String generatePdf(HttpSession session, Model model, int societySid) throws SQLException {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");
        System.out.println("Generate pdf sid: " + sid);

        if (sid == null) {
            model.addAttribute("error", "Society ID (SID) not found");
            return "error";
        }

        try {
            // Fetch bill details
            Bill bill = dbHandler.fetchBillDetails(sid);
            if (bill == null) {
                model.addAttribute("error", "No bill data available");
                return "error"; // Return error page if no bill data is available
            }

            // Convert Bill object to a Map<String, String>
            Map<String, String> formData = new HashMap<>();
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

            // Parse and retrieve form data
            maintenanceContribution = Double.parseDouble(formData.get("maintenance_contribution"));
            housingBoardContribution = Double.parseDouble(formData.get("housing_board_contribution"));
            propertyTaxContribution = Double.parseDouble(formData.get("property_tax_contribution"));
            sinkingFund = Double.parseDouble(formData.get("sinking_fund"));
            reserveMhadaServiceCharge = Double.parseDouble(formData.get("reserve_mhada_service_charge"));
            subcharge = Double.parseDouble(formData.get("sub_charge"));
            other = Double.parseDouble(formData.get("other"));
            buildingDevFund = Double.parseDouble(formData.get("building_dev_fund"));
            fine = Double.parseDouble(formData.get("fine"));
//            arrears = Double.parseDouble(formData.get("arrears"));

            // Perform calculations
            currentMonthTotal = maintenanceContribution + housingBoardContribution + propertyTaxContribution + sinkingFund + reserveMhadaServiceCharge + fine + subcharge + other + buildingDevFund;
            System.out.println("currentMonthTotal: " + currentMonthTotal);
            amountDue = currentMonthTotal - arrears;
            System.out.println("amount due: " + amountDue);

            // Add the calculated values to formData
            formData.put("current_month_total", String.valueOf(currentMonthTotal));
            formData.put("amount_due", String.valueOf(amountDue));
            formData.put("amount_due_in_words", BillController.convertNumberToWords((int) amountDue));

            // Add society details to formData map
            Society society = dbHandler.getSocietyBySid(sid);
            if (society == null) {
                model.addAttribute("error", "No society data found for this SID");
                return "error";
            }

            formData.put("society_name", society.getName());
            formData.put("street", society.getStreet());
            formData.put("landmark", society.getLandmark());
            formData.put("locality", society.getLocality());
            formData.put("pincode", society.getPincode());
            formData.put("city", society.getCity());

            // Set the bill date to current date in India
            String billDate = LocalDate.now(ZoneId.of("Asia/Kolkata")).toString();
            formData.put("bill_date", billDate);

            // Get the next bill number from the sequence
            Integer billNo = dbHandler.getNextBillNumber();
            formData.put("bill_no", billNo.toString());

            // Retrieve resident data from the database
            List<Map<String, String>> residentsData = dbHandler.queryResident(sid);
            if (residentsData == null || residentsData.isEmpty()) {
                model.addAttribute("error", "No resident data found for this session ID");
                return "error";
            }

            // Generate individual PDFs for each resident
            for (Map<String, String> residentData : residentsData) {
                Map<String, String> billData = new HashMap<>(formData);
                billData.putAll(residentData);

                // Generate HTML content for each resident's bill
                Context context = new Context();
                context.setVariable("formData", billData);
                String html = thymeleafViewResolver.getTemplateEngine().process("admin/final_bill", context);

                if (html == null) {
                    model.addAttribute("error", "Failed to generate HTML for resident.");
                    return "error";
                }

                // Convert the generated HTML into PDF
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                HtmlConverter.convertToPdf(new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)), byteArrayOutputStream);
                byte[] pdfBytes = byteArrayOutputStream.toByteArray();

                if (pdfBytes.length == 0) {
                    model.addAttribute("error", "Failed to generate PDF for resident. The content is empty.");
                    return "error";
                }

                // Send email with the generated PDF
                String residentName = residentData.get("name");
                String emailBody = "Please find attached your maintenance bill for this month";
                String subject = "Monthly Maintenance Bill";
                sendEmail(residentData.get("email"), subject, emailBody, pdfBytes, residentName);
            }

            return "admin/preview_bill"; // Return a view that indicates the process is complete
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to generate PDF");
            return "error"; // Return error page on failure
        }
    }

    public String generateBill(String mygateNo, String month, String status, String email, HttpSession session, Model model, int societySid) throws SQLException {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");
        System.out.println("Generate pdf sid: " + sid);

        if (sid == null) {
            model.addAttribute("error", "Society ID (SID) not found");
            return "error";
        }

        System.out.println("Status:          " + status);

        try {
            // Fetch bill details
            Bill bill = dbHandler.fetchBill(mygateNo, month, sid);
            if (bill == null) {
                model.addAttribute("error", "No bill data available");
                return "error"; // Return error page if no bill data is available
            }

            // Convert Bill object to a Map<String, String>
            Map<String, String> formData = new HashMap<>();
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

            // Parse and retrieve form data
            maintenanceContribution = Double.parseDouble(formData.get("maintenance_contribution"));
            housingBoardContribution = Double.parseDouble(formData.get("housing_board_contribution"));
            propertyTaxContribution = Double.parseDouble(formData.get("property_tax_contribution"));
            sinkingFund = Double.parseDouble(formData.get("sinking_fund"));
            reserveMhadaServiceCharge = Double.parseDouble(formData.get("reserve_mhada_service_charge"));
            subcharge = Double.parseDouble(formData.get("sub_charge"));
            other = Double.parseDouble(formData.get("other"));
            buildingDevFund = Double.parseDouble(formData.get("building_dev_fund"));
            fine = Double.parseDouble(formData.get("fine"));
//            arrears = Double.parseDouble(formData.get("arrears"));

            // Perform calculations
            currentMonthTotal = maintenanceContribution + housingBoardContribution + propertyTaxContribution + sinkingFund + reserveMhadaServiceCharge + subcharge + other + buildingDevFund;
            System.out.println("currentMonthTotal: " + currentMonthTotal);
            amountDue = currentMonthTotal - arrears;
            System.out.println("amount due: " + amountDue);

//            if(status.equals("Paid_with_fine")) {  // Use .equals() for String comparison
//                currentMonthTotal += fine;  // Add fine to currentMonthTotal
//                formData.put("fine", String.valueOf(fine));
//            }

            if (status.equals("Paid_with_fine")) {
                // Fine is treated as a percentage, so calculate it on currentMonthTotal
                double finePercentage = fine; // already parsed from formData
                double fineAmount = (finePercentage / 100.0) * currentMonthTotal;

                currentMonthTotal += fineAmount; // Add fine to total

                formData.put("fine", String.valueOf(fineAmount)); // Store actual fine amount
            }

            if(status.equals("Paid")) {  // Use .equals() for String comparison
                formData.put("fine","-");
            }

            // Add the calculated values to formData
            formData.put("current_month_total", String.valueOf(currentMonthTotal));
            formData.put("amount_due", String.valueOf(amountDue));
            formData.put("amount_due_in_words", BillController.convertNumberToWords((int) amountDue));
            formData.put("month", month);
            formData.put("status", status.replace("_", " "));

            // Add society details to formData map
            Society society = dbHandler.getSocietyBySid(sid);
            if (society == null) {
                model.addAttribute("error", "No society data found for this SID");
                return "error";
            }

            formData.put("society_name", society.getName());
            formData.put("street", society.getStreet());
            formData.put("landmark", society.getLandmark());
            formData.put("locality", society.getLocality());
            formData.put("pincode", society.getPincode());
            formData.put("city", society.getCity());

            // Set the bill date to current date in India
            String billDate = LocalDate.now(ZoneId.of("Asia/Kolkata")).toString();
            formData.put("bill_date", billDate);

            // Get the next bill number from the sequence
            Integer billNo = dbHandler.getNextBillNumber();
            formData.put("bill_no", billNo.toString());

            // Retrieve resident data from the database
            Resident residentData = dbHandler.getResident(mygateNo);
            if (residentData == null) {
                model.addAttribute("error", "No resident data found for this session ID");
                return "error";
            }

            // Generate individual PDFs for each resident

                Map<String, String> billData = new HashMap<>(formData);
                // Add resident data to billData map
                billData.put("name", residentData.getName());
                billData.put("mygateNo", residentData.getMygate_no());
                billData.put("email", residentData.getEmail());
                billData.put("room_no", String.valueOf(residentData.getRoom_no()));

                // Generate HTML content for each resident's bill
                Context context = new Context();
                context.setVariable("formData", billData);
                String html = thymeleafViewResolver.getTemplateEngine().process("admin/receipt", context);

                if (html == null) {
                    model.addAttribute("error", "Failed to generate HTML for resident.");
                    return "error";
                }

                // Convert the generated HTML into PDF
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                HtmlConverter.convertToPdf(new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)), byteArrayOutputStream);
                byte[] pdfBytes = byteArrayOutputStream.toByteArray();

                if (pdfBytes.length == 0) {
                    model.addAttribute("error", "Failed to generate PDF for resident. The content is empty.");
                    return "error";
                }

                // Send email with the generated PDF
                String residentName = residentData.getName();
                String emailBody = "Please find attached your maintenance bill for this month";
                String subject = "Monthly Maintenance Bill";
                sendEmail(email, subject, emailBody, pdfBytes, residentName);


            return "admin/preview_bill"; // Return a view that indicates the process is complete
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to generate PDF");
            return "error"; // Return error page on failure
        }
    }


    public String sendMyGate(HttpSession session, Model model, int societySid) throws SQLException {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");
        System.out.println("MyGate sid: " + sid);

        if (sid == null) {
            model.addAttribute("error", "Session ID (SID) not found");
            return "error";
        }

        try {
            // Retrieve resident data from the database
            List<Resident> residents = dbHandler.getResident(sid);
            if (residents == null || residents.isEmpty()) {
                return "error";
            }

            // Send individual emails to each resident
            for (Resident resident : residents) {
                // Send email with the MyGate no
                String mygate_no = resident.getMygate_no();
                System.out.println(mygate_no);
                String residentName = resident.getName();
                System.out.println(residentName);
                System.out.println(resident.getEmail());
                String emailBody = "Please find your MyGate number\nUse this for logging into resident dashboard\n" + mygate_no + "\n\nRegards,\n";
                String subject = "MyGate Number";
                sendEmail(resident.getEmail(), subject, emailBody, null, residentName);
            }

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to send MyGate No");
            return "error"; // Return error page on failure
        }
    }

    public void sendEmail(String to, String subject, String body, byte[] pdfBytes, String residentName) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;

        try {
            mimeMessageHelper = new MimeMessageHelper(message, true);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);

            // Customize the body to include the resident's name
            String personalizedBody = "Dear " + residentName + ",\n\n" + body;
            mimeMessageHelper.setText(personalizedBody);

//            // Attach the PDF from memory
//            ByteArrayResource pdfAttachment = new ByteArrayResource(pdfBytes);
//            mimeMessageHelper.addAttachment("Maintenance_Bill_" + residentName + ".pdf", pdfAttachment);
            // Check if there is an attachment, if present, attach it
            if (pdfBytes != null && pdfBytes.length > 0) {
                ByteArrayResource pdfAttachment = new ByteArrayResource(pdfBytes);
                mimeMessageHelper.addAttachment("Maintenance_Bill_" + residentName + ".pdf", pdfAttachment);
            }

            // Send the email
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public String sendNotice(@RequestParam("customMessage") String customMessage, HttpSession session, Model model, int societySid) throws SQLException {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");
        System.out.println("Generate pdf sid: " + sid);

        if (sid == null) {
            model.addAttribute("error", "Session ID (SID) not found");
            return "error";
        }

        try {
            // Retrieve resident data from the database
            List<Resident> residents = dbHandler.getResident(sid);
            if (residents == null || residents.isEmpty()) {
                return "error";
            }
            // Send individual emails to each resident
            for (Resident resident : residents) {
                // Send email
                String residentName = resident.getName();
                System.out.println(residentName);
                System.out.println(resident.getEmail());
                String emailBody = customMessage.replace("{name}", resident.getName());
                String subject = "Notice";
                sendEmail(resident.getEmail(), subject, emailBody, null, residentName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to send notice");
            return "error"; // Return error page on failure
        }
        return "success";
    }
}
