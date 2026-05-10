package com.libris;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * BorrowScheduler — Arka planda çalışan zamanlayıcı.
 * <p>
 * Her birkaç saniyede bir veritabanını kontrol eder; iade süresi dolmuş ama
 * henüz hatırlatma maili gönderilmemiş kayıtlar için kullanıcıya e-posta atar.
 * <p>
 * Spam'i önlemek için {@code borrow_records.reminder_sent} kolonu kullanılır:
 * mail başarıyla gönderildiğinde 1 yapılır, böylece aynı kayıt için tekrar gönderilmez.
 * Bu kolon yoksa uygulama açılışında otomatik eklenir (basit migrasyon).
 */
public class BorrowScheduler {

    private static final long CHECK_INTERVAL_MS = 10_000L; // 10 sn

    private static volatile Thread schedulerThread;

    /** Scheduler'ı bir kez başlatır (çift çağrıya karşı korunur). */
    public static synchronized void start() {

        if (schedulerThread != null && schedulerThread.isAlive()) {
            System.out.println("[BorrowScheduler] Already running, skip.");
            return;
        }

        System.out.println("[BorrowScheduler] start() called. Ensuring schema...");
        ensureSchema();

        schedulerThread = new Thread(() -> {
            System.out.println("[BorrowScheduler] Loop started, checking every "
                + (CHECK_INTERVAL_MS / 1000) + " seconds.");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    checkOverdue();
                } catch (Exception e) {
                    System.err.println("[BorrowScheduler] Loop error: " + e.getMessage());
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(CHECK_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            System.out.println("[BorrowScheduler] Loop stopped.");
        }, "BorrowScheduler");

        schedulerThread.setDaemon(true); // JVM kapanmasını engellemesin
        schedulerThread.start();
    }

    /**
     * borrow_records tablosunda reminder_sent kolonu yoksa ekler.
     * MySQL "duplicate column" (errorCode 1060) hatasını yutarak idempotent yapıyoruz.
     */
    private static void ensureSchema() {
        String alter =
            "ALTER TABLE borrow_records " +
            "ADD COLUMN reminder_sent TINYINT(1) NOT NULL DEFAULT 0";

        try (Connection conn = DatabaseManager.getConnection();
             Statement st   = conn.createStatement()) {
            try {
                st.executeUpdate(alter);
                System.out.println("[BorrowScheduler] Column 'reminder_sent' added to borrow_records.");
            } catch (SQLException e) {
                if (e.getErrorCode() == 1060) { // Duplicate column name
                    System.out.println("[BorrowScheduler] Column 'reminder_sent' already exists, OK.");
                } else {
                    throw e;
                }
            }
        } catch (SQLException e) {
            System.err.println("[BorrowScheduler] Schema check failed: "
                + e.getMessage() + " (code=" + e.getErrorCode() + ")");
        }
    }

    /** Süresi dolmuş ama maili henüz gönderilmemiş kayıtları çek, mail at, flag'le. */
    private static void checkOverdue() {

        String sql =
            "SELECT br.record_id, u.email, u.name, i.title, br.due_date " +
            "FROM borrow_records br " +
            "JOIN users u ON br.user_id = u.user_id " +
            "JOIN library_items i ON br.item_id = i.item_id " +
            "WHERE br.status = 'BORROWED' " +
            "  AND br.return_date IS NULL " +
            "  AND br.due_date <= NOW() " +
            "  AND br.reminder_sent = 0";

        int candidateCount = 0;
        int sentCount = 0;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                candidateCount++;
                int recordId   = rs.getInt("record_id");
                String email   = rs.getString("email");
                String name    = rs.getString("name");
                String title   = rs.getString("title");
                Timestamp due  = rs.getTimestamp("due_date");
                LocalDateTime dueLdt = (due != null) ? due.toLocalDateTime() : null;

                System.out.println("[BorrowScheduler] Overdue candidate: recordId=" + recordId
                    + ", email=" + email + ", title=" + title);

                try {
                    EmailService.sendOverdueReminder(email, name, title, dueLdt);
                    markReminderSent(recordId);
                    sentCount++;
                } catch (Exception mailErr) {
                    // Mail başarısız olursa flag'lemiyoruz; sonraki turda tekrar denenir.
                    System.err.println("[BorrowScheduler] Mail failed for record " + recordId
                                     + " (" + email + "): " + mailErr.getMessage());
                    mailErr.printStackTrace();
                }
            }

        } catch (SQLException e) {
            System.err.println("[BorrowScheduler] Query error: "
                + e.getMessage() + " (code=" + e.getErrorCode() + ")");
            e.printStackTrace();
            return;
        }

        if (candidateCount == 0) {
            // Sessiz tut, her 10 sn'de bir log basmasın — sadece debug için açabilirsin:
            // System.out.println("[BorrowScheduler] No overdue candidates this cycle.");
        } else {
            System.out.println("[BorrowScheduler] Cycle done: candidates=" + candidateCount
                + ", sent=" + sentCount);
        }
    }

    /** Tek bir kayıt için reminder_sent = 1 yapar. */
    private static void markReminderSent(int recordId) {
        String sql = "UPDATE borrow_records SET reminder_sent = 1 WHERE record_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recordId);
            int updated = ps.executeUpdate();
            System.out.println("[BorrowScheduler] Marked record " + recordId
                + " as reminder_sent (rows updated: " + updated + ").");
        } catch (SQLException e) {
            System.err.println("[BorrowScheduler] Could not mark record " + recordId
                             + " as reminded: " + e.getMessage());
        }
    }
}
