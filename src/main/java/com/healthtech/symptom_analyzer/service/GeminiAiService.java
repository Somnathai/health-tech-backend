package com.healthtech.symptom_analyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthtech.symptom_analyzer.model.ClinicalReport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiAiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    public ClinicalReport generateClinicalSummary(int age, String symptoms) {

        String prompt = String.format(
                "Act as a clinical consultant. A %d-year-old patient has provided the following medical intake answers: %s. " +
                        "Do not write any introductory or concluding paragraphs. " +
                        "Respond ONLY in this exact JSON format with no markdown, no code blocks, no extra text:\n" +
                        "{\n" +
                        "  \"problemsList\": [\"item1\", \"item2\"],\n" +
                        "  \"howToCure\": [\"item1\", \"item2\"],\n" +
                        "  \"suggestedTests\": [\n" +
                        "    {\"category\": \"Category Name\", \"tests\": \"Test1, Test2\"},\n" +
                        "    {\"category\": \"Category Name\", \"tests\": \"Test1, Test2\"}\n" +
                        "  ],\n" +
                        "  \"clinicalSummary\": \"A short 2-3 sentence clinical overview.\"\n" +
                        "}",
                age, symptoms
        );

        String requestBody = """
            {
              "contents": [{
                "parts": [{"text": "%s"}]
              }]
            }
            """.formatted(prompt.replace("\"", "\\\"").replace("\n", "\\n"));

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("RAW GEMINI RESPONSE: " + response.body());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.body());

            if (rootNode.has("error")) {
                return fallbackReport("Google API Error: " + rootNode.path("error").path("message").asText());
            }

            String rawText = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Strip any accidental markdown code fences if Gemini adds them
            rawText = rawText.replaceAll("```json", "").replaceAll("```", "").trim();

            return parseJsonResponse(rawText, mapper);

        } catch (Exception e) {
            e.printStackTrace();
            return fallbackReport("Error generating AI summary: " + e.getMessage());
        }
    }

    private ClinicalReport parseJsonResponse(String json, ObjectMapper mapper) {
        try {
            JsonNode root = mapper.readTree(json);

            List<String> problemsList = new ArrayList<>();
            root.path("problemsList").forEach(n -> problemsList.add(n.asText()));

            List<String> howToCure = new ArrayList<>();
            root.path("howToCure").forEach(n -> howToCure.add(n.asText()));

            List<ClinicalReport.TestCategory> suggestedTests = new ArrayList<>();
            root.path("suggestedTests").forEach(n -> {
                ClinicalReport.TestCategory cat = new ClinicalReport.TestCategory();
                cat.setCategory(n.path("category").asText());
                cat.setTests(n.path("tests").asText());
                suggestedTests.add(cat);
            });

            String summary = root.path("clinicalSummary").asText();

            ClinicalReport report = new ClinicalReport();
            report.setProblemsList(problemsList);
            report.setHowToCure(howToCure);
            report.setSuggestedTests(suggestedTests);
            report.setClinicalSummary(summary);
            return report;

        } catch (Exception e) {
            e.printStackTrace();
            return fallbackReport("Failed to parse AI response.");
        }
    }

    private ClinicalReport fallbackReport(String errorMsg) {
        ClinicalReport report = new ClinicalReport();
        report.setProblemsList(List.of(errorMsg));
        report.setHowToCure(List.of("Please retry."));
        report.setSuggestedTests(List.of());
        report.setClinicalSummary(errorMsg);
        return report;
    }
}