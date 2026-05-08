package com.libris;

import java.sql.*;

/**
 * UserDAO (Data Access Object) Class
 * Handles all database operations related to the 'users' table.
 * This includes registering new users, logging in, and fetching user info.
 */
public class UserDAO {

    /**
     * Registers a new user in the database.
     * @param username  Display name of the user
     * @param email     Email address (must be unique)
     * @param password  Password (should be hashed in production!)
     * @param role      'ADMIN' or 'MEMBER'
     * @return true if registration was successful, false otherwise
     */
    public boolean registerUser(String username, String email, String password, String role) {
        // Using '?' placeholders to prevent SQL Injection attacks
        String sql = "INSERT INTO users (username, email, password, role, balance, total_delays) VALUES (?, ?, ?, ?, 0.0, 0)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, role);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validates login credentials against the database.
     * @param email    Email entered by the user
     * @param password Password entered by the user
     * @return true if credentials match, false otherwise
     */
    public boolean loginUser(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            // If rs.next() returns true, a matching user was found
            return rs.next();

        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Fetches a Member object from the database using their email.
     * Useful after login to load the full Member into the application.
     * @param email Email of the user to fetch
     * @return Member object if found, null otherwise
     */
    public Member getMemberByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ? AND role = 'MEMBER'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Build and return a Member object from the database row
                Member member = new Member(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password")
                );
                member.setBalance(rs.getDouble("balance"));
                member.setTotalDelays(rs.getInt("total_delays"));
                return member;
            }

        } catch (SQLException e) {
            System.err.println("Error fetching member: " + e.getMessage());
        }
        return null; // Return null if no user was found
    }

    /**
     * Updates the penalty balance and total delay count for a member.
     * Called after a late return is processed.
     * @param userId       ID of the member to update
     * @param newBalance   Updated penalty balance
     * @param totalDelays  Updated total delay count
     * @return true if the update was successful
     */
    public boolean updateMemberPenalty(int userId, double newBalance, int totalDelays) {
        String sql = "UPDATE users SET balance = ?, total_delays = ? WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, newBalance);
            pstmt.setInt(2, totalDelays);
            pstmt.setInt(3, userId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating member penalty: " + e.getMessage());
            return false;
        }
    }
}
