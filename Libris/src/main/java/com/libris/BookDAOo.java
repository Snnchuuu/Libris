package com.libris;

import java.sql.*;  //Used for sql operations
import java.time.LocalDate; //Java library for date operations
import java.time.temporal.ChronoUnit; // For date calculations between two different day

/**
 * BorrowDAO Class
 * Controls borrowing, returning and penalty calculation operations.
 * This class creates the bussiness logic in the project.
 */
public class BorrowDAO {

    // Daily penalty cost: 1.5 TL
    private static final double DAILY_FINE_AMOUNT = 1.5;

    /**
     * Starts the borrowing proccess
     * @param userId Id of the person who borrowed the item
     * @param itemId Id of the borrowed item
     * @return if the proccess was successful it returns true
     */
    public boolean borrowItem(int userId, int itemId) {
        // SQL sorgusu: Ödünç alma tarihi (BUGÜN) ve İade tarihi (BUGÜN + 15 GÜN)
        String sql = "INSERT INTO borrow_records (user_id, item_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, 'BORROWED')";
        
        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusDays(15); // 15 gün kuralı burada uygulanıyor

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);
            pstmt.setDate(3, Date.valueOf(today));
            pstmt.setDate(4, Date.valueOf(dueDate));

            int rows = pstmt.executeUpdate();
            
            // Eğer kayıt başarılıysa, kitabın stok miktarını 1 azaltmalıyız
            if (rows > 0) {
                BookDAO bookDAO = new BookDAO();
                return bookDAO.updateStock(itemId, -1);
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("Borrowing Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kitap iade edildiğinde cezayı hesaplar ve kaydı günceller.
     * @param recordId borrow_records tablosundaki işlemin ID'si
     */
    public double returnItem(int recordId, int itemId) {
        String selectSql = "SELECT due_date FROM borrow_records WHERE record_id = ?";
        String updateSql = "UPDATE borrow_records SET return_date = ?, status = 'RETURNED', fine_amount = ? WHERE record_id = ?";
        
        double fine = 0.0;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement selectPstmt = conn.prepareStatement(selectSql);
             PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
            
            // 1. Önce iade tarihini (due_date) öğrenelim
            selectPstmt.setInt(1, recordId);
            ResultSet rs = selectPstmt.executeQuery();
            
            if (rs.next()) {
                LocalDate dueDate = rs.getDate("due_date").toLocalDate();
                LocalDate returnDate = LocalDate.now();

                // 2. Eğer iade tarihi geçtiyse ceza hesapla
                if (returnDate.isAfter(dueDate)) {
                    long daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);
                    fine = daysLate * DAILY_FINE_AMOUNT;
                }

                // 3. Kaydı güncelle
                updatePstmt.setDate(1, Date.valueOf(returnDate));
                updatePstmt.setDouble(2, fine);
                updatePstmt.setInt(3, recordId);
                updatePstmt.executeUpdate();

                // 4. Kitap iade edildiği için stok miktarını 1 artır
                BookDAO bookDAO = new BookDAO();
                bookDAO.updateStock(itemId, 1);
            }
            
        } catch (SQLException e) {
            System.err.println("Return Error: " + e.getMessage());
        }
        
        return fine; // Hesaplanan cezayı döner (0.0 ise ceza yok demektir)
    }
}
