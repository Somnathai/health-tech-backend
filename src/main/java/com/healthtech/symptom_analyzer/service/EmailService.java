package com.healthtech.symptom_analyzer.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.Attachment;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    public void sendReportWithAttachment(String toEmail, byte[] pdfBytes) {
        try {
            Resend resend = new Resend(resendApiKey);

            String body = "Hello,\n\n"
                    + "Based on the symptoms you provided, our AI clinical engine has generated a preliminary report.\n\n"
                    + "Please find your detailed PDF summary attached to this email.\n\n"
                    + "Best regards,\n"
                    + "The Health Tech Team";

            Attachment attachment = Attachment.builder()
                    .fileName("Clinical_Report.pdf")
                    .content(Base64.getEncoder().encodeToString(pdfBytes))
                    .build();

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Health Tech <reports@flshealth.online>")
                    .to(List.of(toEmail))
                    .subject("Your Symptom Analysis & Clinical Report")
                    .text(body)
                    .attachments(List.of(attachment))
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            System.out.println("SUCCESS: Email sent to " + toEmail + " | ID: " + response.getId());

        } catch (ResendException e) {
            System.err.println("ERROR: Failed to send email to " + toEmail);
            e.printStackTrace();
        }
    }
}