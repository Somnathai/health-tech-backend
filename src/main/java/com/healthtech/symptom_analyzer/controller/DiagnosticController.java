package com.healthtech.symptom_analyzer.controller;

import com.healthtech.symptom_analyzer.model.ClinicalReport;
import com.healthtech.symptom_analyzer.model.SymptomReport;
import com.healthtech.symptom_analyzer.repository.SymptomReportRepository;
import com.healthtech.symptom_analyzer.service.EmailService;
import com.healthtech.symptom_analyzer.service.GeminiAiService;
import com.healthtech.symptom_analyzer.service.PdfGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://health-tech-frontend-pi.vercel.app",
        "https://flshealth.online",
        "https://www.flshealth.online"
})
@RestController
@RequestMapping("/api/diagnostics")
public class DiagnosticController {

    @Autowired
    private SymptomReportRepository repository;

    @Autowired
    private GeminiAiService aiService;

    @Autowired
    private PdfGeneratorService pdfService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeSymptoms(@RequestBody SymptomReport request) {

        // Step 1: Get structured AI data (parsed JSON → ClinicalReport)
        ClinicalReport clinicalData = aiService.generateClinicalSummary(
                request.getAge(),
                request.getReportedSymptoms()
        );

        // Step 2: Store the summary string for DB persistence
        request.setAiGeneratedSummary(clinicalData.getClinicalSummary());

        // Step 3: Save to database
        repository.save(request);

        // Step 4: Generate PDF with structured card layout
        byte[] pdfBytes = pdfService.generateClinicalReport(request, clinicalData);

        // Step 5: Email the PDF
        emailService.sendReportWithAttachment(request.getCustomerEmail(), pdfBytes);

        return ResponseEntity.ok("Success! AI analyzed symptoms, created PDF, and emailed the report.");
    }
}