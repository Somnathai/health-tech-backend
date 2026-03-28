package com.healthtech.symptom_analyzer.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendReportWithAttachment(String toEmail, byte[] pdfBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("somnathbhakta475@gmail.com"); // just a display address
            helper.setTo(toEmail);
            helper.setSubject("Your Symptom Analysis & Clinical Report");

            String body = "Hello,\n\n"
                    + "Based on the symptoms you provided, our AI clinical engine has generated a preliminary report.\n\n"
                    + "Please find your detailed PDF summary attached to this email.\n\n"
                    + "Best regards,\n"
                    + "The Health Tech Team";

            helper.setText(body);
            helper.addAttachment("Clinical_Report.pdf", new ByteArrayResource(pdfBytes));

            mailSender.send(message);
            System.out.println("SUCCESS: Email sent to " + toEmail);

        } catch (Exception e) {
            System.err.println("ERROR: Failed to send email to " + toEmail);
            e.printStackTrace();
        }
    }
}