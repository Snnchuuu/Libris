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
    /**
     * İade süresi dolan bir kitap için ACİL geri iade hatırlatıcısı gönderir.
     *
     * @param toEmail   Üyenin e-posta adresi
     * @param userName  Üyenin adı (selamlama için, null olabilir)
     * @param bookTitle Kitabın başlığı
     * @param dueDate   Vade tarihi (ne zaman dolduğunu göstermek için)
     */
    public static void sendOverdueReminder(String toEmail,
                                           String userName,
                                           String bookTitle,
                                           java.time.LocalDateTime dueDate) {

        System.out.println("[EmailService] Preparing overdue reminder → "
            + toEmail + " for '" + bookTitle + "'");

        Session session = buildSession();
        session.setDebug(true); // SMTP konuşmasını konsola bas — debug için kritik

        try {
            Message message = new MimeMessage(session);
            // Sade From header — sendLoginInfo ile birebir aynı tarz
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("ACİL: İade Süreniz Doldu - " + bookTitle);

            java.time.format.DateTimeFormatter fmt =
                    java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            String greeting = (userName == null || userName.isBlank())
                    ? "Merhaba,"
                    : "Merhaba " + userName + ",";

            String body =
                    greeting + "\n\n" +
                    "Asagidaki kitabin iade suresi dolmustur. Gecikme cezasindan kacinmak icin " +
                    "lutfen kitabi en kisa surede iade ediniz.\n\n" +
                    "  - Kitap: " + bookTitle + "\n" +
                    "  - Vade tarihi: " + (dueDate != null ? dueDate.format(fmt) : "-") + "\n\n" +
                    "Bu hatirlatici sadece bir kez gonderilir.\n\n" +
                    "Libris Kutuphane Sistemi";

            // Plain setText (sendLoginInfo ile aynı) — Gmail bazen UTF-8'li versiyonda takılıyor
            message.setText(body);

            Transport.send(message);
            System.out.println("[EmailService] Overdue reminder SENT to " + toEmail + " (" + bookTitle + ")");

        } catch (Exception e) {
            System.err.println("[EmailService] Overdue mail FAILED for " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Geriye uyumluluk için eski imza — yeni metoda yönlendirir. */
    public static void sendReminder(String toEmail, String bookTitle) {
        sendOverdueReminder(toEmail, null, bookTitle, null);
    }

    /**
     * Kitap ödünç alındığında onay e-postası gönderir.
     * Kullanıcıya iade tarihi ve saati hatırlatılır.
     */
    public static void sendBorrowConfirmation(String toEmail,
                                              String userName,
                                              String bookTitle,
                                              java.time.LocalDateTime dueDate) {
        System.out.println("[EmailService] Preparing borrow confirmation -> "
            + toEmail + " for '" + bookTitle + "'");

        Session session = buildSession();
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Odunc Alma Onayi - " + bookTitle);

            java.time.format.DateTimeFormatter fmt =
                    java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            String greeting = (userName == null || userName.isBlank())
                    ? "Merhaba,"
                    : "Merhaba " + userName + ",";

            String body =
                    greeting + "\n\n" +
                    "Asagidaki kitabi basariyla odunc aldiniz. Iade tarihinde gec kalmamak icin " +
                    "lutfen sureniz dolmadan kitabi iade ediniz.\n\n" +
                    "  - Kitap: " + bookTitle + "\n" +
                    "  - Iade tarihi: " + (dueDate != null ? dueDate.format(fmt) : "-") + "\n\n" +
                    "Kitabi iade ettikten sonra bir degerlendirme yazmayi unutmayin.\n\n" +
                    "Iyi okumalar dileriz!\n\n" +
                    "Libris Kutuphane Sistemi";

            message.setText(body);
            Transport.send(message);
            System.out.println("[EmailService] Borrow confirmation SENT to " + toEmail);

        } catch (Exception e) {
            System.err.println("[EmailService] Borrow confirmation FAILED for " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Kitap iade edildiginde tesekkur e-postasi gonderir + degerlendirme istegi.
     */
    public static void sendReturnConfirmation(String toEmail,
                                              String userName,
                                              String bookTitle,
                                              java.time.LocalDateTime returnDate) {
        sendReturnConfirmation(toEmail, userName, bookTitle, returnDate, 0.0, 0L);
    }

    /**
     * İade onay e-postası — varsa gecikme cezası bilgisi de eklenir.
     *
     * @param fineAmount  bu iadeden uygulanan ceza (0 ise cezasız)
     * @param unitsLate   kaç dakika geç (PenaltyService.unitsLate)
     */
    public static void sendReturnConfirmation(String toEmail,
                                              String userName,
                                              String bookTitle,
                                              java.time.LocalDateTime returnDate,
                                              double fineAmount,
                                              long unitsLate) {
        System.out.println("[EmailService] Preparing return confirmation -> "
            + toEmail + " for '" + bookTitle + "' (fine=" + fineAmount + ")");

        Session session = buildSession();
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));

            String subject = fineAmount > 0
                    ? "Iade Alindi (CEZA: " + PenaltyService.formatAmount(fineAmount) + ") - " + bookTitle
                    : "Iade Alindi - " + bookTitle;
            message.setSubject(subject);

            java.time.format.DateTimeFormatter fmt =
                    java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            String greeting = (userName == null || userName.isBlank())
                    ? "Merhaba,"
                    : "Merhaba " + userName + ",";

            StringBuilder body = new StringBuilder();
            body.append(greeting).append("\n\n");
            body.append("Asagidaki kitabi basariyla iade ettiniz.\n\n");
            body.append("  - Kitap: ").append(bookTitle).append("\n");
            body.append("  - Iade tarihi: ")
                .append(returnDate != null ? returnDate.format(fmt) : "-").append("\n");

            if (fineAmount > 0) {
                body.append("\n");
                body.append("  ! GECIKME CEZASI !\n");
                body.append("  - Gecikme suresi: ").append(unitsLate)
                    .append(" ").append(PenaltyService.UNIT_LABEL).append("\n");
                body.append("  - Ceza tutari: ")
                    .append(PenaltyService.formatAmount(fineAmount)).append("\n");
                body.append("\nLutfen ceza bakiyenizi en kisa surede odeyiniz.\n");
            } else {
                body.append("\nIade zamaninda yapildi, ceza yok. Tesekkur ederiz!\n");
            }

            body.append("\nKitabi nasil bulduguniz hakkinda bir degerlendirme yazarsaniz cok seviniriz.\n\n");
            body.append("Libris Kutuphane Sistemi");

            message.setText(body.toString());
            Transport.send(message);
            System.out.println("[EmailService] Return confirmation SENT to " + toEmail);

        } catch (Exception e) {
            System.err.println("[EmailService] Return confirmation FAILED for " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Rezerve edilmiş bir kitap stokta tekrar mevcut olduğunda gönderilir.
     */
    public static void sendReservationAvailable(String toEmail,
                                                String userName,
                                                String bookTitle) {
        System.out.println("[EmailService] Preparing reservation-available -> "
            + toEmail + " for '" + bookTitle + "'");

        Session session = buildSession();
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Mujde! Rezerve ettiginiz kitap stokta - " + bookTitle);

            String greeting = (userName == null || userName.isBlank())
                    ? "Merhaba,"
                    : "Merhaba " + userName + ",";

            String body =
                    greeting + "\n\n" +
                    "Rezerve ettiginiz kitap suan stokta:\n\n" +
                    "  - Kitap: " + bookTitle + "\n\n" +
                    "Kataloga giderek hemen odunc alabilirsiniz. " +
                    "Ilk gelen ilk hizmet alir, gec kalmayin!\n\n" +
                    "Libris Kutuphane Sistemi";

            message.setText(body);
            Transport.send(message);
            System.out.println("[EmailService] Reservation-available SENT to " + toEmail);

        } catch (Exception e) {
            System.err.println("[EmailService] Reservation-available FAILED for " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ortak SMTP session oluşturucu — tekrar etmeyi önlemek için ayrı method.
     */
    private static Session buildSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        return Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });
    }
}