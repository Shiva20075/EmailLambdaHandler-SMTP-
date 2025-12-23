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

/**
 * EmailLambdaHandler is a Java 17 program used to send emails through Gmail SMTP.
 * It is designed to work in BOTH environments: Local execution (IntelliJ) and
 * AWS Lambda, using the same source code and same build artifact (JAR/ZIP).
 *
 * The program uses Jakarta Mail (JavaMail) API, which provides classes such as
 * Properties, Session, Authenticator, Message, MimeMessage, and Transport to
 * communicate with an SMTP server and send emails.
 *
 * Properties:
 * The Properties object is used to define SMTP configuration details such as
 * enabling authentication, enabling TLS security, specifying the SMTP host
 * (smtp.gmail.com), and the port number (587). These properties control how the
 * Java application connects to Gmail’s mail server.
 *
 * Session:
 * The Session object represents a mail session between the Java application and
 * the SMTP server. It is created using the SMTP properties and an Authenticator.
 * The session manages the overall communication settings for sending emails.
 *
 * Authenticator:
 * The Authenticator object is responsible for authentication. It provides Gmail
 * with the sender’s email address and the App Password. This is done by overriding
 * getPasswordAuthentication(), which returns a PasswordAuthentication object.
 *
 * PasswordAuthentication:
 * This object stores the sender’s email (username) and the SMTP password. Gmail
 * verifies these credentials before allowing the email to be sent.
 *
 * Message and MimeMessage:
 * Message is an abstract representation of an email. MimeMessage is its concrete
 * implementation, allowing the program to set the sender address, recipient
 * address, subject, and message body in text format.
 *
 * Transport:
 * The Transport class is responsible for actually sending the email. Once the
 * message is fully constructed, Transport.send(message) connects to Gmail SMTP
 * and delivers the email.
 *
 * sendMail() Method:
 * This method contains the complete email-sending logic. It first reads the
 * SMTP password from an environment variable (SMTP_PASS) for AWS Lambda.
 * If not found, it falls back to a JVM VM option (SMTP_PASS) for local execution.
 * It then configures SMTP properties, creates the mail session, builds the email,
 * and sends it using the Transport class.
 *
 * handleRequest() Method:
 * This is the AWS Lambda entry point. AWS automatically calls this method when the
 * Lambda function is triggered. It invokes sendMail() and logs success or failure
 * using the Lambda Context logger.
 *
 * main() Method:
 * This is the local execution entry point used for testing in IntelliJ or from
 * the command line. It allows developers to verify the email functionality
 * locally before deploying the same code to AWS Lambda.
 *
 */


public class EmailLambdaHandler implements RequestHandler<Map<String, Object>, String> {
    
    private static final String FROM_EMAIL = "palleshiva2007@gmail.com";
    private static final String TO_EMAIL   = "pallesumathi18@gmail.com";
    
    private static void sendMail() throws Exception {
        String resolvedPass = System.getenv("SMTP_PASS");
        
        if (resolvedPass == null || resolvedPass.isEmpty()) {
            resolvedPass = System.getProperty("SMTP_PASS");
        }

        if (resolvedPass == null || resolvedPass.isEmpty()) {
            throw new RuntimeException(
                    "SMTP_PASS not set. Set env var (AWS) or VM option (local)."
            );
        }

        final String smtpPass = resolvedPass; 
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
