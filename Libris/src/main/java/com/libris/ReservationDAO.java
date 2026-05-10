package com.libris;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ReservationDAO Class
 * Handles all database operations related to the 'reservations' table.
 * Allows members to join a waitlist when all copies of an item are borrowed.
 */
public class ReservationDAO {

    /**
     * Creates a new reservation for a member on a fully borrowed item.
     * @param userId ID of the member making the reservation
     * @param itemId ID of the item being reserved
     * @return true if the reservation was created successfully
     */
    public boolean createReservation(int userId, int itemId) {
        String sql = "INSERT INTO reservations (user_id, item_id, request_date, status) VALUES (?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);
            pstmt.setDate(3, Date.valueOf(LocalDate.now()));

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Reservation creation error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cancels an existing reservation by updating its status to 'CANCELLED'.
     * @param reservationId ID of the reservation to cancel
     * @return true if cancellation was successful
     */
    public boolean cancelReservation(int reservationId) {
        String sql = "UPDATE reservations SET status = 'CANCELLED' WHERE reservation_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reservationId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Reservation cancellation error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Marks a reservation as 'FULFILLED' when the item becomes available
     * and is assigned to the waiting member.
     * @param reservationId ID of the reservation to fulfill
     * @return true if the update was successful
     */
    public boolean fulfillReservation(int reservationId) {
        String sql = "UPDATE reservations SET status = 'FULFILLED' WHERE reservation_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reservationId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Reservation fulfillment error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves the next PENDING reservation for an item (the one who waited the longest).
     * Called when an item is returned to notify the next person in the queue.
     * @param itemId ID of the returned item
     * @return The oldest PENDING reservation ID, or -1 if no one is waiting
     */
    public int getNextReservationForItem(int itemId) {
        // ORDER BY request_date ASC ensures the member who waited longest gets priority
        String sql = "SELECT reservation_id FROM reservations WHERE item_id = ? AND status = 'PENDING' ORDER BY request_date ASC LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("reservation_id");
            }

        } catch (SQLException e) {
            System.err.println("Error fetching next reservation: " + e.getMessage());
        }
        return -1; // No pending reservation found
    }

    /**
     * Bir kitap için bekleyen tüm rezervasyon sahiplerinin temel bilgileri.
     * E-posta gönderirken kullanılır.
     */
    public static class PendingReserver {
        public final int reservationId;
        public final int userId;
        public final String email;
        public final String name;

        public PendingReserver(int reservationId, int userId, String email, String name) {
            this.reservationId = reservationId;
            this.userId = userId;
            this.email = email;
            this.name = name;
        }
    }

    /**
     * Belirli bir kitap için PENDING rezervasyonu olan tüm kullanıcıları döndürür.
     * Kitap stokta tekrar mevcut olduğunda hepsine bilgilendirme maili göndermek için kullanılır.
     * Sıralama: en uzun bekleyen ilk sırada (request_date ASC).
     */
    public List<PendingReserver> getPendingReservers(int itemId) {
        List<PendingReserver> result = new ArrayList<>();
        String sql =
            "SELECT r.reservation_id, u.user_id, u.email, u.name " +
            "FROM reservations r " +
            "JOIN users u ON r.user_id = u.user_id " +
            "WHERE r.item_id = ? AND r.status = 'PENDING' " +
            "ORDER BY r.request_date ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new PendingReserver(
                        rs.getInt("reservation_id"),
                        rs.getInt("user_id"),
                        rs.getString("email"),
                        rs.getString("name")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending reservers: " + e.getMessage());
        }
        return result;
    }

    /**
     * Bu kullanıcı + kitap için PENDING rezervasyonu varsa FULFILLED yapar.
     * Kullanıcı kendi rezerve ettiği kitabı ödünç aldığında çağrılır.
     */
    public boolean fulfillByUserAndItem(int userId, int itemId) {
        String sql =
            "UPDATE reservations SET status = 'FULFILLED' " +
            "WHERE user_id = ? AND item_id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, itemId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("[ReservationDAO] Auto-fulfilled " + rows
                    + " reservation(s) for user=" + userId + ", item=" + itemId);
            }
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Reservation auto-fulfill error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns all active (PENDING) reservations made by a specific member.
     * Used in the member dashboard to show their reservation list.
     * @param userId ID of the member
     * @return List of reservation details as formatted strings
     */
    public List<String> getActiveReservationsByUser(int userId) {
        List<String> reservations = new ArrayList<>();
        String sql = "SELECT r.reservation_id, li.title, r.request_date " +
                     "FROM reservations r " +
                     "JOIN library_items li ON r.item_id = li.item_id " +
                     "WHERE r.user_id = ? AND r.status = 'PENDING' " +
                     "ORDER BY r.request_date ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Format: "Reservation #2 | Java Programming | Requested: 2026-05-01"
                String entry = "Reservation #" + rs.getInt("reservation_id") +
                               " | " + rs.getString("title") +
                               " | Requested: " + rs.getDate("request_date");
                reservations.add(entry);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching reservations: " + e.getMessage());
        }
        return reservations;
    }
}
