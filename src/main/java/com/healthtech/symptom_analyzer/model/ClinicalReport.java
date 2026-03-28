package com.healthtech.symptom_analyzer.model;

import java.util.List;

public class ClinicalReport {

    private List<String> problemsList;
    private List<String> howToCure;
    private List<TestCategory> suggestedTests;
    private String clinicalSummary;

    public static class TestCategory {
        private String category;
        private String tests;

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getTests() { return tests; }
        public void setTests(String tests) { this.tests = tests; }
    }

    public List<String> getProblemsList() { return problemsList; }
    public void setProblemsList(List<String> problemsList) { this.problemsList = problemsList; }

    public List<String> getHowToCure() { return howToCure; }
    public void setHowToCure(List<String> howToCure) { this.howToCure = howToCure; }

    public List<TestCategory> getSuggestedTests() { return suggestedTests; }
    public void setSuggestedTests(List<TestCategory> suggestedTests) { this.suggestedTests = suggestedTests; }

    public String getClinicalSummary() { return clinicalSummary; }
    public void setClinicalSummary(String clinicalSummary) { this.clinicalSummary = clinicalSummary; }
}