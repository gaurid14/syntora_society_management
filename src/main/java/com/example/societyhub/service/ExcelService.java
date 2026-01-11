package com.example.societyhub.service;

import com.example.societyhub.controller.BillController;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class ExcelService {
    private static final Logger Log = LogManager.getLogger(ExcelService.class);

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/society_management";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "pgadmin4";


    public void processExcelFile(File file, int societySid) throws IOException {
        System.out.println("Society id: " + societySid);
        try (FileInputStream fileInputStream = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
             Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);

            XSSFSheet sheet = workbook.getSheetAt(0);

            // Get the admin's name and contact_no based on societySid
            String adminName = null;
            String adminContactNo = null;
            PreparedStatement adminSelectStmt = conn.prepareStatement(
                    "select name, contact_no from resident where sid = ? and isadmin = true"
            );
            adminSelectStmt.setInt(1, societySid);
            ResultSet adminSelectResultSet = adminSelectStmt.executeQuery();
            if (adminSelectResultSet.next()) {
                adminName = adminSelectResultSet.getString("name");
                System.out.println("adminName: " + adminName);
                adminContactNo = adminSelectResultSet.getString("contact_no");
                System.out.println("adminContactNo: " + adminContactNo);
            } else {
                throw new IOException("Admin record not found for the given session ID.");
            }

            // Validate if sid exists in society
            PreparedStatement validateSocietyStatement = conn.prepareStatement(
                    "select count(*) from society where sid = ?"
            );
            validateSocietyStatement.setInt(1, societySid);
            ResultSet societyResultSet = validateSocietyStatement.executeQuery();
            if (societyResultSet.next() && societyResultSet.getInt(1) == 0) {
                throw new IOException("The provided sid does not exist in the society table.");
            }

            // Get the current highest mem_id for this society
            PreparedStatement getMaxMemIdStatement = conn.prepareStatement(
                    "select coalesce(max(mem_id), 0) from resident where sid = ?"
            );
            getMaxMemIdStatement.setInt(1, societySid);
            ResultSet resultSet = getMaxMemIdStatement.executeQuery();
            int currentMaxMemId = 0;
            if (resultSet.next()) {
                currentMaxMemId = resultSet.getInt(1);
            }

            // Prepare for next mem_id
            int mem_id = currentMaxMemId == 0 ? Integer.parseInt(societySid + "001") : currentMaxMemId + 1;

            // Prepare SQL statements
            String updateAdminQuery = "update resident set room_no = ?, mr_ms = ?, gender = ?, age = ?, mygate_no = ?, email = ?, bhk = ? where name = ? and contact_no = ? and sid = ?";
            String insertResidentQuery = "insert into resident (mem_id, sid, name, room_no, mr_ms, gender, age, contact_no, isadmin, mygate_no, bhk, email) values (?, ?, ?, ?, ?, ?, ?, ?, false, ?, ?, ?)";
            String dataUploadQuery = "update society set data_uploaded = ? where sid = ?";

            try (PreparedStatement updateAdminStmt = conn.prepareStatement(updateAdminQuery);
                 PreparedStatement insertResidentStmt = conn.prepareStatement(insertResidentQuery);
                 PreparedStatement dataUploadStmt = conn.prepareStatement(dataUploadQuery)) {

                dataUploadStmt.setBoolean(1, true);
                dataUploadStmt.setInt(2, societySid);
                System.out.println("Updating data_uploaded for societySid: " + societySid);
                dataUploadStmt.executeUpdate();

                HashSet<String> existingMyGateNumbers = new HashSet<>();

                for (Row row : sheet) {
                    if (row.getRowNum() == 0) {
                        // Skip header row
                        continue;
                    }

                    // Handle empty rows by checking the first cell
                    if (row.getCell(0) == null || row.getCell(0).getCellType() == CellType.BLANK) {
                        continue;
                    }
//                    Objects.equals(getCellValueAsString(row.getCell(6)), adminContactNo
                    // Extract and validate cell values
                    String name = getCellValueAsString(row.getCell(3));  // Name is in column 3
                    System.out.println("Admin name from excel: " + name);
                    String contactNo = getCellValueAsString(row.getCell(6));  // Contact No is in column 6
                    System.out.println("Admin contact no from excel: " + contactNo);
                    System.out.println("Admin name from field: " + name);
                    boolean isAdmin = adminName.equals(name) && adminContactNo.equals(contactNo);
                    System.out.println("Isadmin: " + isAdmin);

                    String myGateNumber = generateUniqueMyGateNumber(existingMyGateNumbers);
                    System.out.println("MyGate no: " + myGateNumber);

                    if (isAdmin) {
                        System.out.println("Isadmin 1: " + isAdmin);
                        // Update admin details
                        updateAdminStmt.setInt(1, getCellValueAsInt(row.getCell(1)));  // room no
                        updateAdminStmt.setString(2, getCellValueAsString(row.getCell(2)));  // mr ms
                        updateAdminStmt.setString(3, getCellValueAsString(row.getCell(4)));  // gender
                        updateAdminStmt.setInt(4, getCellValueAsInt(row.getCell(5)));  // age
                        System.out.println("Mygate admin: " + myGateNumber);
                        updateAdminStmt.setString(5, myGateNumber);
                        updateAdminStmt.setString(6, getCellValueAsString(row.getCell(7)));  // email
                        updateAdminStmt.setString(7, getCellValueAsString(row.getCell(8)));  // bhk
                        updateAdminStmt.setString(8, name);
                        updateAdminStmt.setString(9, contactNo);
                        updateAdminStmt.setInt(10, societySid);
                        updateAdminStmt.executeUpdate();  // Only update admin, don't add to batch
                    } else {
                        // Add resident details
                        insertResidentStmt.setInt(1, mem_id++);  // Use and increment mem_id
                        insertResidentStmt.setInt(2, societySid);
                        insertResidentStmt.setString(3, name);
                        insertResidentStmt.setInt(4, getCellValueAsInt(row.getCell(1)));  // room no
                        insertResidentStmt.setString(5, getCellValueAsString(row.getCell(2)));  // mr ms
                        insertResidentStmt.setString(6, getCellValueAsString(row.getCell(4)));  // gender
                        insertResidentStmt.setInt(7, getCellValueAsInt(row.getCell(5)));  // age
                        insertResidentStmt.setString(8, contactNo);  // contact no
                        insertResidentStmt.setString(9, myGateNumber);  // MyGate number
                        insertResidentStmt.setString(11, getCellValueAsString(row.getCell(7)));  // email
                        insertResidentStmt.setString(10, getCellValueAsString(row.getCell(8)));  // bhk
                        insertResidentStmt.addBatch();  // Add only non-admin residents to batch
                    }
                    // Insert into resident_bill table with the default value 0 for all months
                    insertResidentBill(myGateNumber);
                }
                // Execute batch updates for residents (not admins)
                insertResidentStmt.executeBatch();
                conn.commit();
            } catch (Exception e) {
                // Rollback the transaction in case of an error
                conn.rollback();
                e.printStackTrace();
                throw new IOException("Error updating db", e);
            }

        } catch (Exception e) {

            e.printStackTrace();
            throw new IOException("Error processing Excel file", e);
        }
    }

    private void insertResidentBill(String myGateNumber) throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        conn.setAutoCommit(false);
        String insertResidentBillQuery = "insert into resident_bill (mygate_no, year) values (?, ?)";
        try (PreparedStatement insertResidentBillStmt = conn.prepareStatement(insertResidentBillQuery)) {
            int currentYear = LocalDate.now().getYear();
            insertResidentBillStmt.setString(1, myGateNumber);
            insertResidentBillStmt.setInt(2, currentYear);
            System.out.println("Inserting Resident Bill: MyGateNumber = " + myGateNumber + ", Year = " + currentYear);
            insertResidentBillStmt.executeUpdate();  // Execute insert statement
            conn.commit();
        }catch (Exception e) {
            // Rollback the transaction in case of an error
            conn.rollback();
            e.printStackTrace();
            throw new SQLException("Error ", e);
        }
    }

    // Generate a unique 6-digit MyGate number
    public String generateUniqueMyGateNumber(HashSet<String> existingMyGateNumbers) throws SQLException, IOException {
//        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
        Random random = new Random();
        String myGateNumber;
        do {
            myGateNumber = String.format("%06d", random.nextInt(999999));
        } while (existingMyGateNumbers.contains(myGateNumber) || isMyGateNumberInDatabase(myGateNumber));
        existingMyGateNumbers.add(myGateNumber);
        return myGateNumber;
//        }
    }

    // Check if MyGate number already exists in the database
    public boolean isMyGateNumberInDatabase(String myGateNumber) throws IOException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "select count(*) from resident where mygate_no = ?"
            );
            stmt.setString(1, myGateNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Helper method to get cell value as integer
    private int getCellValueAsInt(Cell cell) {
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Expected numeric value but found string", e);
                }
            default:
                return -1;
        }
    }

    // Helper method to get cell value as string
    private String getCellValueAsString(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return ""; // Handle blank cells
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Ensure that phone numbers are treated as strings
                return new BigDecimal(cell.getNumericCellValue()).toPlainString();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}