package com.libris;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

/*
 * Info:
 * This class is responsible for sending emails using Gmail SMTP.
 * It is used for account recovery (forgot password feature).
 * NOTE: This is a basic student-level implementation, not production secure.
 */

public class EmailService {

    // System Gmail account (sender)
    private static final String FROM_EMAIL = "libris31.app@gmail.com";

    // IMPORTANT:
    // This is NOT the Gmail password, it is an App Password generated from Google
    private static final String APP_PASSWORD = "fbpruayqebdgznvt";

    /*
     * Sends login credentials to the user's email address.
     * WARNING: In real systems, passwords should NEVER be sent via email.
     */
    public static void sendLoginInfo(String toEmail, String username, String password) {

        // SMTP configuration for Gmail
        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        // Creating mail session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        // DEBUG: enables SMTP logs in console (useful for troubleshooting)
        session.setDebug(true);

        try {

            // Creating email message
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));

            message.setSubject("Libris Hesap Bilgilerin");

            // Email body content
            message.setText(
                    "Kullanıcı adın: " + username + "\n" +
                    "Şifren: " + password
            );

            // Sending email
            Transport.send(message);

            System.out.println("Mail başarıyla gönderildi!");

        } catch (MessagingException e) {

            // If something goes wrong, print full error trace
            e.printStackTrace();
        }
    }
}