package com.libris;
 
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
 
/**
 * WishListDAO Class
 * Handles all database operations related to the 'wish_list' table.
 * Members can add and remove items from their personal wish list.
 */
public class WishListDAO {
 
    /**
     * Adds an item to a member's wish list.
     * If the item is already in the list, the database UNIQUE constraint will prevent duplicates.
     * @param userId ID of the member
     * @param itemId ID of the item to add
     * @return true if the item was added successfully
     */
    public boolean addItem(int userId, int itemId) {
        String sql = "INSERT INTO wish_list (user_id, item_id, added_date) VALUES (?, ?, ?)";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);
            // Saat-dakika dahil olsun diye Timestamp kullanıyoruz (kolon DATETIME)
            pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));

            return pstmt.executeUpdate() > 0;
 
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate")) {
                System.out.println("Item is already in the wish list.");
            } else {
                System.err.println("Error adding to wish list: " + e.getMessage());
            }
            return false;
        }
    }
 
    /**
     * Removes an item from a member's wish list.
     * @param userId ID of the member
     * @param itemId ID of the item to remove
     * @return true if the item was removed successfully
     */
    public boolean removeItem(int userId, int itemId) {
        String sql = "DELETE FROM wish_list WHERE user_id = ? AND item_id = ?";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);
            return pstmt.executeUpdate() > 0;
 
        } catch (SQLException e) {
            System.err.println("Error removing from wish list: " + e.getMessage());
            return false;
        }
    }
 
    /**
     * Returns all items in a member's wish list as formatted strings.
     * Uses JOIN with library_items to get full item details.
     * @param userId ID of the member
     * @return List of formatted strings with item details
     */
    public List<String> getWishList(int userId) {
        List<String> items = new ArrayList<>();
        String sql = "SELECT li.title, li.author, li.item_type, wl.added_date " +
                     "FROM wish_list wl " +
                     "JOIN library_items li ON wl.item_id = li.item_id " +
                     "WHERE wl.user_id = ? " +
                     "ORDER BY wl.added_date DESC";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
 
            while (rs.next()) {
                // Format: "Java Programming | Deitel | BOOK | Added: 2026-05-09"
                String entry = rs.getString("title") +
                               " | " + rs.getString("author") +
                               " | " + rs.getString("item_type") +
                               " | Added: " + rs.getDate("added_date");
                items.add(entry);
            }
 
        } catch (SQLException e) {
            System.err.println("Error fetching wish list: " + e.getMessage());
        }
        return items;
    }
 
    /**
     * Returns the item IDs in a member's wish list.
     * Useful for checking which catalog items are already wishlisted (e.g. highlight buttons in UI).
     * @param userId ID of the member
     * @return List of item IDs in the wish list
     */
    public List<Integer> getUserWishListIds(int userId) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT item_id FROM wish_list WHERE user_id = ?";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
 
            while (rs.next()) {
                list.add(rs.getInt("item_id"));
            }
 
        } catch (SQLException e) {
            System.err.println("Error fetching wish list IDs: " + e.getMessage());
        }
        return list;
    }
 
    /**
     * Checks if a specific item is already in a member's wish list.
     * @param userId ID of the member
     * @param itemId ID of the item
     * @return true if the item is already in the wish list
     */
    public boolean isInWishList(int userId, int itemId) {
        String sql = "SELECT COUNT(*) FROM wish_list WHERE user_id = ? AND item_id = ?";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);
            ResultSet rs = pstmt.executeQuery();
 
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
 
        } catch (SQLException e) {
            System.err.println("Error checking wish list: " + e.getMessage());
        }
        return false;
    }
}
