package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Map;
import java.util.Properties;

public class EmailLambdaHandler implements RequestHandler<Map<String, Object>, String> {

    private static final String FROM_EMAIL = "palleshiva2007@gmail.com";
    private static final String TO_EMAIL   = "pallesumathi18@gmail.com";

    /**
     * ==================================================
     * COMMON MAIL LOGIC
     * Works for BOTH Local & AWS
     * ==================================================
     */
    private static void sendMail() throws Exception {

        // 1️⃣ AWS Lambda way (Environment Variable)
        String resolvedPass = System.getenv("SMTP_PASS");

        // 2️⃣ Local IntelliJ fallback (VM option)
        if (resolvedPass == null || resolvedPass.isEmpty()) {
            resolvedPass = System.getProperty("SMTP_PASS");
        }

        if (resolvedPass == null || resolvedPass.isEmpty()) {
            throw new RuntimeException(
                    "SMTP_PASS not set. Set env var (AWS) or VM option (local)."
            );
        }

        final String smtpPass = resolvedPass; // must be final

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, smtpPass);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(TO_EMAIL)
        );

        message.setSubject("Email from Local & AWS Lambda");
        message.setText(
                "Hi sumathi,\n\n" +
                        "This email was sent using:\n" +
                        "• Local Java (IntelliJ)\n" +
                        "• AWS Lambda (Java 17)\n\n" +
                        "Using SMTP.\n\n" +
                        "Regards,\n" +
                        "Shiva"
        );

        Transport.send(message);
    }

    /**
     * ==================================================
     * AWS LAMBDA ENTRY POINT
     * ==================================================
     */
    @Override
    public String handleRequest(Map<String, Object> event, Context context) {

        try {
            sendMail();
            context.getLogger().log("Email sent successfully");
            return "SUCCESS";

        } catch (Exception e) {
            context.getLogger().log("Email failed: " + e.getMessage());
            return "FAILED";
        }
    }

    /**
     * ==================================================
     * LOCAL / INTELLIJ ENTRY POINT
     * ==================================================
     */
    public static void main(String[] args) {

        try {
            sendMail();
            System.out.println("Email sent successfully (local)");

        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
