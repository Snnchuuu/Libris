package com.libris;
 
import java.sql.*;
 
/**
 * UserDAO (Data Access Object) Class
 * Handles all database operations related to the 'users' table.
 * This includes registering new users, logging in, and fetching user info.
 */
public class UserDAO {
 
    
     //Registers a new user in the database.
     
    public boolean registerUser(String username, String name, String email, String password, String role) {
        String sql = "INSERT INTO users (username, name, email, password, role, balance, total_delays) VALUES (?, ?, ?, ?, ?, 0.0, 0)";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            pstmt.setString(1, username);
            pstmt.setString(2, name);
            pstmt.setString(3, email);
            pstmt.setString(4, password);
            pstmt.setString(5, role);
 
            return pstmt.executeUpdate() > 0;
 
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            return false;
        }
    }
 
    
     //Validates login credentials against the database using email and password.
    
    public boolean loginUser(String email, String password) {
    	String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            pstmt.setString(1, email);
            pstmt.setString(2, password);
 
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
 
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
            return false;
        }
    }
 
    /**
     * Fetches a user by username and returns it as a Member object.
     * Works for both ADMIN and MEMBER roles.
     */
    public Member getMemberByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
 
            if (rs.next()) {
                // Build Member with correct username and name fields
                Member member = new Member(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password")
                );
                member.setBalance(rs.getDouble("balance"));
                member.setTotalDelays(rs.getInt("total_delays"));
                // Set actual role (could be ADMIN or MEMBER)
                member.setRole(rs.getString("role"));
                return member;
            }
 
        } catch (SQLException e) {
            System.err.println("Error fetching member by username: " + e.getMessage());
        }
        return null;
    }
 
    
     //Fetches a user by email and returns it as a Member object.
     
    public Member getMemberByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
 
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
 
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
 
            if (rs.next()) {
                Member member = new Member(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password")
                );
                member.setBalance(rs.getDouble("balance"));
                member.setTotalDelays(rs.getInt("total_delays"));
                member.setRole(rs.getString("role"));
                return member;
            }
 
        } catch (SQLException e) {
            System.err.println("Error fetching member by email: " + e.getMessage());
        }
        return null;
    }
 
    
     //Updates the penalty balance and total delay count for a member.
     
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