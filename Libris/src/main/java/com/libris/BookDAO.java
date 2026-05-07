package com.libris;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BookDAO Sınıfı
 * Kütüphanedeki kitapların (library_items) listelenmesi, eklenmesi 
 * ve stok durumlarının güncellenmesi işlemlerini yürütür.
 */
public class BookDAO {

    /**
     * Veritabanındaki tüm kitapları liste olarak döner.
     */
    public List<String> getAllBooks() {
        List<String> books = new ArrayList<>();
        String sql = "SELECT title, author FROM library_items";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                books.add(rs.getString("title") + " - " + rs.getString("author"));
            }
        } catch (SQLException e) {
            System.err.println("Kitap listeleme hatası: " + e.getMessage());
        }
        return books;
    }

    /**
     * Bir kitap ödünç alındığında veya iade edildiğinde stok miktarını günceller.
     * @param itemId Güncellenecek kitabın ID'si
     * @param amount Değişim miktarı (Örn: Ödünç alınca -1, iade edince +1)
     */
    public boolean updateStock(int itemId, int amount) {
        // Mevcut stok miktarını güncelleyen SQL sorgusu
        String sql = "UPDATE library_items SET available_copies = available_copies + ? WHERE item_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, amount);
            pstmt.setInt(2, itemId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Stok güncelleme hatası: " + e.getMessage());
            return false;
        }
    }
}
