package com.example.societyhub.controller;

import com.example.societyhub.service.ExcelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class FileUpload {
    private static final Logger Log=LogManager.getLogger(FileUpload.class);

    @Autowired
    private ExcelService excelService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test endpoint working");
    }

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file, HttpSession session) {
        Integer sid = (Integer) session.getAttribute("adminSocietyId");
        System.out.println("Sid: " + sid);
        if (file.isEmpty()) {
            System.out.println("No file selected");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file selected");
        }
        System.out.println("File received: " + file.getOriginalFilename());
        try {
            // Save the file temporarily
            File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);
            Log.info("File uploaded successfully to: " + tempFile.getAbsolutePath());

            // Process the Excel file
            excelService.processExcelFile(tempFile, sid);
            System.out.println("Excel service called");

            // Optionally delete the temporary file
            tempFile.delete();

//            return ResponseEntity.ok("File uploaded and processed successfully");
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to upload file");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
