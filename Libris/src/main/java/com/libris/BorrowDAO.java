package com.libris;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * BorrowDAO — borrow_records tablosunu yöneten DAO sınıfı.
 * Ödünç alma, iade ve "bu üye bu kitabı zaten aldı mı?" kontrolü burada.
 */
public class BorrowDAO {

    /**
     * Belirli bir üyenin belirli bir kitabı aktif olarak ödünç alıp almadığını kontrol eder.
     * "Aynı kitabı 1'den fazla alamazsın" kuralı buradan uygulanıyor.
     *
     * @param userId Üyenin ID'si
     * @param itemId Kitabın ID'si
     * @return true ise zaten ödünç almış (tekrar alamaz)
     */
    public boolean isAlreadyBorrowed(int userId, int itemId) {
        // return_date NULL ve status BORROWED ise kitap hâlâ iade edilmemiş demektir
        String sql = "SELECT COUNT(*) FROM borrow_records " +
                     "WHERE user_id = ? AND item_id = ? " +
                     "AND status = 'BORROWED' AND return_date IS NULL";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; // 0'dan büyükse zaten ödünç almış
            }

        } catch (SQLException e) {
            System.err.println("Borrow check error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Yeni bir ödünç alma kaydı oluşturur.
     * borrow_date: bugün, due_date: 14 gün sonra (2 haftalık standart süre)
     *
     * @param userId Ödünç alan üyenin ID'si
     * @param itemId Ödünç alınan kitabın ID'si
     * @return true ise kayıt başarıyla oluşturuldu
     */
    public boolean borrowItem(int userId, int itemId) {
        String sql = "INSERT INTO borrow_records (user_id, item_id, borrow_date, due_date, status) " +
                     "VALUES (?, ?, ?, ?, 'BORROWED')";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

        	Timestamp now = new Timestamp(System.currentTimeMillis());
        	Timestamp dueDate = new Timestamp(System.currentTimeMillis() + 3 * 60 * 1000);

        	pstmt.setInt(1, userId);
        	pstmt.setInt(2, itemId);
        	pstmt.setTimestamp(3, now);
        	pstmt.setTimestamp(4, dueDate);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Borrow insert error: " + e.getMessage());
            return false;
        }
    }
    /**
     * Kullanıcının ödünç aldığı kitabı iade eder.
     * return_date bugünün tarihi olur
     * status = RETURNED yapılır
     *
     * @param userId İade eden üyenin ID'si
     * @param itemId İade edilen kitabın ID'si
     * @return true ise iade başarılı
     */
    public boolean returnItem(int userId, int itemId) {

        String sql = "UPDATE borrow_records " +
                     "SET return_date = ?, status = 'RETURNED' " +
                     "WHERE user_id = ? AND item_id = ? " +
                     "AND status = 'BORROWED' AND return_date IS NULL";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(2, userId);
            pstmt.setInt(3, itemId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Return item error: " + e.getMessage());
            return false;
        }
    }

    /** İade işleminin tüm sonuçlarını taşıyan basit DTO. */
    public static class ReturnResult {
        public final boolean success;
        public final double fineAmount;       // bu iadeden uygulanan ceza
        public final long   unitsLate;        // kaç dakika geç (PenaltyService.unitsLate)
        public final double newBalance;      // güncel toplam bakiye

        public ReturnResult(boolean success, double fineAmount, long unitsLate, double newBalance) {
            this.success    = success;
            this.fineAmount = fineAmount;
            this.unitsLate  = unitsLate;
            this.newBalance = newBalance;
        }

        public static ReturnResult failure() {
            return new ReturnResult(false, 0.0, 0, 0.0);
        }
    }

    /**
     * İade + gecikme cezası hesaplama + bakiye güncelleme — tek transaction'da.
     *
     *   1) Aktif borrow_records satırını bul (user+item, status=BORROWED, return_date NULL)
     *   2) due_date ve şu an arasından PenaltyService ile ceza hesapla
     *   3) borrow_records: status=RETURNED, return_date=now, fine_amount=ceza
     *   4) users: balance += ceza, total_delays += (ceza > 0 ? 1 : 0)
     *
     * Hata durumunda transaction rollback olur.
     */
    public ReturnResult returnAndPenalize(int userId, int itemId) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // 1) Aktif kaydı bul (record_id ve due_date'i lazım)
            int recordId = -1;
            LocalDateTime dueDate = null;
            String findSql =
                "SELECT record_id, due_date FROM borrow_records " +
                "WHERE user_id = ? AND item_id = ? " +
                "AND status = 'BORROWED' AND return_date IS NULL " +
                "ORDER BY borrow_date DESC LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, itemId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        System.err.println("[BorrowDAO] returnAndPenalize: aktif kayıt yok user="
                            + userId + " item=" + itemId);
                        return ReturnResult.failure();
                    }
                    recordId = rs.getInt("record_id");
                    Timestamp ts = rs.getTimestamp("due_date");
                    if (ts != null) dueDate = ts.toLocalDateTime();
                }
            }

            // 2) Ceza hesapla
            LocalDateTime now = LocalDateTime.now();
            long unitsLate = PenaltyService.unitsLate(dueDate, now);
            double fine    = PenaltyService.computeFine(dueDate, now);

            // 3) borrow_records güncelle
            String updateRecord =
                "UPDATE borrow_records " +
                "SET status = 'RETURNED', return_date = ?, fine_amount = ? " +
                "WHERE record_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateRecord)) {
                ps.setTimestamp(1, Timestamp.valueOf(now));
                ps.setDouble(2, fine);
                ps.setInt(3, recordId);
                ps.executeUpdate();
            }

            // 4) users.balance ve total_delays
            double newBalance = 0.0;
            String updateUser =
                "UPDATE users " +
                "SET balance = balance + ?, " +
                "    total_delays = total_delays + ? " +
                "WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateUser)) {
                ps.setDouble(1, fine);
                ps.setInt(2, fine > 0 ? 1 : 0);
                ps.setInt(3, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT balance FROM users WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) newBalance = rs.getDouble("balance");
                }
            }

            conn.commit();
            System.out.println("[BorrowDAO] returnAndPenalize record=" + recordId
                + " unitsLate=" + unitsLate + " fine=" + fine
                + " newBalance=" + newBalance);
            return new ReturnResult(true, fine, unitsLate, newBalance);

        } catch (SQLException e) {
            System.err.println("[BorrowDAO] returnAndPenalize error: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignore) {}
            }
            return ReturnResult.failure();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignore) {}
            }
        }
    }

    /** Kullanıcının şu anki bakiyesini döner (TL cinsinden). */
    public double getBalance(int userId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT balance FROM users WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            System.err.println("getBalance error: " + e.getMessage());
        }
        return 0.0;
    }

    /** Kullanıcının ceza bakiyesini sıfırlar (ödeme simülasyonu). */
    public boolean clearBalance(int userId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE users SET balance = 0 WHERE user_id = ?")) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("clearBalance error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kullanıcının user_id'sini username'den çeker.
     * Session'da sadece username tutulduğu için bu dönüşüm gerekiyor.
     *
     * @param username Oturumdaki kullanıcı adı
     * @return user_id, bulunamazsa -1
     */
    public int getUserIdByUsername(String username) {
        String sql = "SELECT user_id FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("user_id");
            }

        } catch (SQLException e) {
            System.err.println("User lookup error: " + e.getMessage());
        }
        return -1; // Kullanıcı bulunamadı
    }
}