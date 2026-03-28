package com.healthtech.symptom_analyzer.service;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class EmailService {

    @Value("${mailjet.api.key}")
    private String apiKey;

    @Value("${mailjet.api.secret}")
    private String apiSecret;

    public void sendReportWithAttachment(String toEmail, byte[] pdfBytes) {
        try {
            ClientOptions options = ClientOptions.builder()
                    .apiKey(apiKey)
                    .apiSecretKey(apiSecret)
                    .build();

            MailjetClient client = new MailjetClient(options);

            String body = "Hello,\n\n"
                    + "Based on the symptoms you provided, our AI clinical engine has generated a preliminary report.\n\n"
                    + "Please find your detailed PDF summary attached to this email.\n\n"
                    + "Best regards,\n"
                    + "The Health Tech Team";

            String base64Pdf = Base64.getEncoder().encodeToString(pdfBytes);

            MailjetRequest request = new MailjetRequest(Emailv31.resource)
                    .property(Emailv31.MESSAGES, new JSONArray()
                            .put(new JSONObject()
                                    .put(Emailv31.Message.FROM, new JSONObject()
                                            .put("Email", "reports@flshealth.online")
                                            .put("Name", "Health Tech"))
                                    .put(Emailv31.Message.TO, new JSONArray()
                                            .put(new JSONObject()
                                                    .put("Email", toEmail)))
                                    .put(Emailv31.Message.SUBJECT, "Your Symptom Analysis & Clinical Report")
                                    .put(Emailv31.Message.TEXTPART, body)
                                    .put(Emailv31.Message.ATTACHMENTS, new JSONArray()
                                            .put(new JSONObject()
                                                    .put("ContentType", "application/pdf")
                                                    .put("Filename", "Clinical_Report.pdf")
                                                    .put("Base64Content", base64Pdf)))));

            MailjetResponse response = client.post(request);
            System.out.println("SUCCESS: Email sent to " + toEmail + " | Status: " + response.getStatus());

        } catch (Exception e) {
            System.err.println("ERROR: Failed to send email to " + toEmail);
            e.printStackTrace();
        }
    }
}