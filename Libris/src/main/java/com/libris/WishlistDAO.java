package com.libris;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * WishlistDAO — Kullanicinin istek listesini (wishlist_items) yoneten DAO.
 * Tablo {@link StartupHooks} tarafindan yoksa otomatik olusturulur.
 */
public class WishlistDAO {

    /** Bir kitabi istek listesine ekler. UNIQUE kisitlamasi sayesinde cift ekleme engellenir. */
    public boolean add(int userId, int itemId) {
        String sql = "INSERT IGNORE INTO wishlist_items (user_id, item_id, added_date) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, itemId);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[WishlistDAO] add error: " + e.getMessage());
            return false;
        }
    }

    /** Bir kitabi istek listesinden cikarir. */
    public boolean remove(int userId, int itemId) {
        String sql = "DELETE FROM wishlist_items WHERE user_id = ? AND item_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[WishlistDAO] remove error: " + e.getMessage());
            return false;
        }
    }

    /** Bu kitap zaten kullanicinin listesinde mi? */
    public boolean isInWishlist(int userId, int itemId) {
        String sql = "SELECT 1 FROM wishlist_items WHERE user_id = ? AND item_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[WishlistDAO] isInWishlist error: " + e.getMessage());
            return false;
        }
    }

    /** Bir DTO — view'da gridde gostermek icin. */
    public static class WishlistEntry {
        public final int itemId;
        public final String title;
        public final String author;
        public final int copyCount;
        public final int availableCopies;
        public final Timestamp addedDate;

        public WishlistEntry(int itemId, String title, String author,
                             int copyCount, int availableCopies, Timestamp addedDate) {
            this.itemId = itemId;
            this.title = title;
            this.author = author;
            this.copyCount = copyCount;
            this.availableCopies = availableCopies;
            this.addedDate = addedDate;
        }

        public int    getItemId()         { return itemId; }
        public String getTitle()          { return title; }
        public String getAuthor()         { return author; }
        public int    getCopyCount()      { return copyCount; }
        public int    getAvailableCopies(){ return availableCopies; }
        public Timestamp getAddedDate()   { return addedDate; }
    }

    /** Kullanicinin istek listesindeki tum kitaplar. */
    public List<WishlistEntry> getByUser(int userId) {
        List<WishlistEntry> result = new ArrayList<>();
        String sql =
            "SELECT i.item_id, i.title, i.author, i.copy_count, i.available_copies, w.added_date " +
            "FROM wishlist_items w " +
            "JOIN library_items i ON w.item_id = i.item_id " +
            "WHERE w.user_id = ? " +
            "ORDER BY w.added_date DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new WishlistEntry(
                        rs.getInt("item_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("copy_count"),
                        rs.getInt("available_copies"),
                        rs.getTimestamp("added_date")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[WishlistDAO] getByUser error: " + e.getMessage());
        }
        return result;
    }
}
