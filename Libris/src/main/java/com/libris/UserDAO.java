package com.libris;

import java.sql.*;

/**
 * UserDAO (Data Access Object) Class
 * Bu sınıf, veritabanındaki 'users' tablosuyla ilgili tüm işlemleri (Login, Register vb.) yönetir.
 * SQL sorgularını Java içinden güvenli bir şekilde çalıştırmak için kullanılır.
 */
public class UserDAO {
    
    /**
     * Yeni bir kullanıcıyı veritabanına kaydeder.
     * @param username Kullanıcı adı
     * @param password Şifre (Gerçek projelerde hash'lenmelidir!)
     * @param role 'ADMIN' veya 'MEMBER'
     * @return Kayıt başarılıysa true döner.
     */
    public boolean registerUser(String username, String password, String role) {
        // SQL Injection saldırılarını önlemek için '?' (placeholder) kullanıyoruz.
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        
        // try-with-resources yapısı: İşlem bitince bağlantı otomatik olarak kapanır.
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Soru işaretlerinin yerine gelecek gerçek verileri set ediyoruz
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            
            // Sorguyu çalıştır ve kaç satırın etkilendiğini al
            int rowsAffected = pstmt.executeUpdate();
            
            // Eğer en az 1 satır eklendiyse işlem başarılıdır
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            // Bir hata oluşursa (örneğin aynı kullanıcı adından varsa) konsola yazdır
            System.err.println("Kullanıcı kayıt hatası: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kullanıcı adı ve şifre kontrolü yapar (Login işlemi).
     * @param username Giriş yapmaya çalışan kullanıcı adı
     * @param password Giriş yapmaya çalışan şifre
     * @return Bilgiler eşleşiyorsa true döner.
     */
    public boolean loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            // Sorguyu çalıştır ve sonuçları ResultSet içine al
            ResultSet rs = pstmt.executeQuery();
            
            // Eğer rs.next() true dönüyorsa, veritabanında böyle bir kullanıcı var demektir
            return rs.next(); 
            
        } catch (SQLException e) {
            System.err.println("Giriş hatası: " + e.getMessage());
            return false;
        }
    }
}