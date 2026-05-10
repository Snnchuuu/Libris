package com.libris;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class WishListDAO {

    // İstek listesine ekle
    public boolean addWish(int userId, int itemId) {
        String query = "INSERT INTO wish_list (user_id, item_id, added_date) VALUES (?, ?, CURDATE())";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, itemId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("WishList Ekleme Hatası: " + e.getMessage());
            return false;
        }
    }

    // İstek listesinden çıkar
    public boolean removeWish(int userId, int itemId) {
        String query = "DELETE FROM wish_list WHERE user_id = ? AND item_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, itemId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("WishList Silme Hatası: " + e.getMessage());
            return false;
        }
    }

    // Kullanıcının istek listesindeki kitap ID'lerini getir
    public List<Integer> getUserWishListIds(int userId) {
        List<Integer> list = new ArrayList<>();
        String query = "SELECT item_id FROM wish_list WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("item_id"));
                }
            }
        } catch (Exception e) {
            System.out.println("WishList Getirme Hatası: " + e.getMessage());
        }
        return list;
    }
}
