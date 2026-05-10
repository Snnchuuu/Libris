package com.libris;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Spring Boot tamamen ayağa kalktığında çalışacak başlangıç adımları.
 *  - BorrowScheduler'ı başlatır (geç-iade hatırlatma e-postaları için)
 *  - Wishlist tablosunu yoksa oluşturur
 */
@Component
public class StartupHooks {

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("[StartupHooks] Application ready — running startup tasks...");
        ensureWishlistTable();
        ensureReturnDateIsDateTime();
        resyncStockColumns();
        BorrowScheduler.start();
    }

    /**
     * Eski schema'da {@code borrow_records.return_date} DATE olarak oluşturulmuştu;
     * setTimestamp yazsak bile saat kırpılıyordu. Yeni schema DATETIME olarak
     * tanımlıyor ama mevcut tabloyu bozmamak için burada idempotent bir
     * ALTER TABLE çalıştırıyoruz.
     */
    private void ensureReturnDateIsDateTime() {
        // 1) Mevcut sütun tipini kontrol et
        String currentType = null;
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                 "WHERE TABLE_SCHEMA = DATABASE() " +
                 "AND TABLE_NAME = 'borrow_records' " +
                 "AND COLUMN_NAME = 'return_date'")) {
            if (rs.next()) {
                currentType = rs.getString("DATA_TYPE");
            }
        } catch (SQLException e) {
            System.err.println("[StartupHooks] return_date type check failed: " + e.getMessage());
            return;
        }

        if (currentType == null) {
            System.err.println("[StartupHooks] return_date column not found, skipping migration.");
            return;
        }

        if ("datetime".equalsIgnoreCase(currentType)) {
            System.out.println("[StartupHooks] return_date already DATETIME, OK.");
            return;
        }

        // 2) DATE veya başka bir şey ise → DATETIME'a çevir
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(
                "ALTER TABLE borrow_records MODIFY COLUMN return_date DATETIME NULL");
            System.out.println("[StartupHooks] Migrated return_date from "
                + currentType + " to DATETIME.");
        } catch (SQLException e) {
            System.err.println("[StartupHooks] return_date migration failed: " + e.getMessage());
        }
    }

    /** library_items.available_copies'ı borrow_records'tan gerçek değere göre senkronize eder. */
    private void resyncStockColumns() {
        try {
            int fixed = new BookDAO().resyncStockColumns();
            if (fixed > 0) {
                System.out.println("[StartupHooks] Stock resync: fixed " + fixed + " row(s).");
            } else {
                System.out.println("[StartupHooks] Stock resync: all items already in sync.");
            }
        } catch (Exception e) {
            System.err.println("[StartupHooks] Stock resync failed: " + e.getMessage());
        }
    }

    /**
     * wish_list tablosunu yoksa oluşturur. added_date DATETIME (saat-dakika dahil)
     * olacak şekilde tanımlanır. Eski tabloda DATE ise migrasyonla DATETIME'a çevirir.
     */
    private void ensureWishlistTable() {
        String sql =
            "CREATE TABLE IF NOT EXISTS wish_list (" +
            "  wish_id     INT AUTO_INCREMENT PRIMARY KEY," +
            "  user_id     INT       NOT NULL," +
            "  item_id     INT       NOT NULL," +
            "  added_date  DATETIME  NOT NULL," +
            "  UNIQUE KEY unique_wish (user_id, item_id)," +
            "  FOREIGN KEY (user_id) REFERENCES users(user_id)," +
            "  FOREIGN KEY (item_id) REFERENCES library_items(item_id) ON DELETE CASCADE" +
            ") ENGINE=InnoDB";

        try (Connection conn = DatabaseManager.getConnection();
             Statement st   = conn.createStatement()) {
            st.executeUpdate(sql);
            System.out.println("[StartupHooks] wish_list table ready.");
        } catch (SQLException e) {
            System.err.println("[StartupHooks] wish_list create failed: " + e.getMessage());
        }

        // Mevcut tabloda added_date DATE ise DATETIME'a migrate et
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                 "WHERE TABLE_SCHEMA = DATABASE() " +
                 "AND TABLE_NAME = 'wish_list' AND COLUMN_NAME = 'added_date'")) {
            if (rs.next()) {
                String t = rs.getString("DATA_TYPE");
                if (!"datetime".equalsIgnoreCase(t)) {
                    try (Statement st2 = conn.createStatement()) {
                        st2.executeUpdate(
                            "ALTER TABLE wish_list MODIFY COLUMN added_date DATETIME NOT NULL");
                        System.out.println("[StartupHooks] Migrated wish_list.added_date from "
                            + t + " to DATETIME.");
                    }
                } else {
                    System.out.println("[StartupHooks] wish_list.added_date already DATETIME, OK.");
                }
            }
        } catch (SQLException e) {
            System.err.println("[StartupHooks] wish_list.added_date migration failed: " + e.getMessage());
        }

        // Eski wishlist_items tablosu varsa (önceki StartupHooks versiyonundan kalan)
        // — temizleme amaçlı düşür. Boş, kullanılmıyor.
        try (Connection conn = DatabaseManager.getConnection();
             Statement st   = conn.createStatement()) {
            st.executeUpdate("DROP TABLE IF EXISTS wishlist_items");
        } catch (SQLException e) {
            // sessiz: tablo zaten yoksa sorun değil
        }
    }
}
