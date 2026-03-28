package com.healthtech.symptom_analyzer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "symptom_reports")
public class SymptomReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerEmail;
    private Integer age;

    @Column(columnDefinition = "TEXT")
    private String reportedSymptoms;

    // ADDED THIS: The variable to hold the AI's response
    @Column(columnDefinition = "TEXT")
    private String aiGeneratedSummary;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getReportedSymptoms() { return reportedSymptoms; }
    public void setReportedSymptoms(String reportedSymptoms) { this.reportedSymptoms = reportedSymptoms; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    // ADDED THIS: The Getter and Setter for the AI Summary
    public String getAiGeneratedSummary() {
        return aiGeneratedSummary;
    }

    public void setAiGeneratedSummary(String aiGeneratedSummary) {
        this.aiGeneratedSummary = aiGeneratedSummary;
    }
}