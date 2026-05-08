package com.libris;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ReviewDAO Class
 * Handles all database operations related to the 'reviews' table.
 * Members can add, update, delete, and read reviews for any library item.
 */
public class ReviewDAO {

    /**
     * Adds a new review for a library item by a member.
     * A member can only review the same item once (enforced by the database UNIQUE constraint).
     * @param userId  ID of the member writing the review
     * @param itemId  ID of the item being reviewed
     * @param rating  Rating score between 1 and 5
     * @param comment Written comment for the item
     * @return true if the review was added successfully
     */
    public boolean addReview(int userId, int itemId, int rating, String comment) {
        // Validate rating before hitting the database
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5. Given: " + rating);
        }

        String sql = "INSERT INTO reviews (user_id, item_id, rating, comment, review_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);
            pstmt.setInt(3, rating);
            pstmt.setString(4, comment);
            pstmt.setDate(5, Date.valueOf(LocalDate.now()));

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding review: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all reviews for a specific library item.
     * Used to display reviews on an item's detail page.
     * @param itemId ID of the item
     * @return List of review details as formatted strings
     */
    public List<String> getReviewsByItem(int itemId) {
        List<String> reviews = new ArrayList<>();
        // JOIN with users table to get the reviewer's username
        String sql = "SELECT u.username, r.rating, r.comment, r.review_date " +
                     "FROM reviews r " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r.item_id = ? " +
                     "ORDER BY r.review_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Format: "Can Tekin | 5/5 | Great book! | 2026-05-08"
                String entry = rs.getString("username") +
                               " | " + rs.getInt("rating") + "/5" +
                               " | " + rs.getString("comment") +
                               " | " + rs.getDate("review_date");
                reviews.add(entry);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching reviews: " + e.getMessage());
        }
        return reviews;
    }

    /**
     * Calculates the average rating for a library item.
     * Useful for showing a star rating on item cards.
     * @param itemId ID of the item
     * @return Average rating as a double, or 0.0 if no reviews exist
     */
    public double getAverageRating(int itemId) {
        String sql = "SELECT AVG(rating) as avg_rating FROM reviews WHERE item_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }

        } catch (SQLException e) {
            System.err.println("Error calculating average rating: " + e.getMessage());
        }
        return 0.0; // Default if no reviews yet
    }

    /**
     * Deletes a review from the database.
     * Can be used by admins to moderate inappropriate reviews.
     * @param reviewId ID of the review to delete
     * @return true if deletion was successful
     */
    public boolean deleteReview(int reviewId) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reviewId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting review: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates the rating and comment of an existing review.
     * @param reviewId   ID of the review to update
     * @param newRating  New rating score (1-5)
     * @param newComment New comment text
     * @return true if the update was successful
     */
    public boolean updateReview(int reviewId, int newRating, String newComment) {
        if (newRating < 1 || newRating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5. Given: " + newRating);
        }

        String sql = "UPDATE reviews SET rating = ?, comment = ? WHERE review_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newRating);
            pstmt.setString(2, newComment);
            pstmt.setInt(3, reviewId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating review: " + e.getMessage());
            return false;
        }
    }
}
