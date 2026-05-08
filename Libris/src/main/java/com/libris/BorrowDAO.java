package com.libris;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * BorrowDAO Class
 * Handles all database operations related to the 'borrow_records' table.
 * Manages borrowing, returning, penalty calculation, and borrow history.
 */
public class BorrowDAO {

    /**
     * Records a new borrow transaction in the database.
     * Due date is automatically set to 15 days from today.
     * Also decreases the available stock for the borrowed item.
     * @param userId ID of the member borrowing the item
     * @param itemId ID of the item being borrowed
     * @return true if the borrow was recorded successfully
     */
    public boolean borrowItem(int userId, int itemId) {
        String sql = "INSERT INTO borrow_records (user_id, item_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, 'BORROWED')";

        LocalDate today   = LocalDate.now();
        LocalDate dueDate = today.plusDays(15); // Loan period: 15 days

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);
            pstmt.setDate(3, Date.valueOf(today));
            pstmt.setDate(4, Date.valueOf(dueDate));

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                // Decrease available stock by 1 after a successful borrow
                BookDAO bookDAO = new BookDAO();
                return bookDAO.updateStock(itemId, -1);
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Borrow error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Processes a return: calculates the fine using the item's own penalty logic,
     * updates the borrow record, restores stock, and updates the member's penalty balance.
     *
     * NOTE: Penalty rates come from the Java item classes (Book = 2 TL/day, Periodical = 5 TL/day)
     * not hardcoded here — this keeps the business logic consistent with the rest of the project.
     *
     * @param recordId ID of the borrow record being closed
     * @param member   The Member who is returning the item
     * @param item     The LibraryItem being returned (needed for calculatePenalty())
     * @return The fine amount charged (0.0 if returned on time)
     */
    public double returnItem(int recordId, Member member, LibraryItem item) {
        String selectSql = "SELECT due_date FROM borrow_records WHERE record_id = ?";
        String updateSql = "UPDATE borrow_records SET return_date = ?, status = 'RETURNED', fine_amount = ? WHERE record_id = ?";

        double fine = 0.0;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement selectPstmt = conn.prepareStatement(selectSql);
             PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {

            // Step 1: Get the due date for this borrow record
            selectPstmt.setInt(1, recordId);
            ResultSet rs = selectPstmt.executeQuery();

            if (rs.next()) {
                LocalDate dueDate    = rs.getDate("due_date").toLocalDate();
                LocalDate returnDate = LocalDate.now();

                // Step 2: Calculate fine if returned late
                if (returnDate.isAfter(dueDate)) {
                    long daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);

                    // Use the item's own calculatePenalty() so each type applies its own rate
                    fine = item.calculatePenalty((int) daysLate, member.getTotalDelays());
                }

                // Step 3: Update the borrow record with return info
                updatePstmt.setDate(1, Date.valueOf(returnDate));
                updatePstmt.setDouble(2, fine);
                updatePstmt.setInt(3, recordId);
                updatePstmt.executeUpdate();

                // Step 4: Restore available stock
                BookDAO bookDAO = new BookDAO();
                bookDAO.updateStock(item.getId(), 1);

                // Step 5: Update member's penalty balance and delay count in the database
                if (fine > 0) {
                    UserDAO userDAO = new UserDAO();
                    userDAO.updateMemberPenalty(
                        member.getId(),
                        member.getBalance() + fine,
                        member.getTotalDelays() + 1
                    );
                    // Also update the in-memory Member object
                    member.setBalance(member.getBalance() + fine);
                    member.setTotalDelays(member.getTotalDelays() + 1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Return error: " + e.getMessage());
        }

        return fine; // Returns 0.0 if there was no delay
    }

    /**
     * Fetches all active (not yet returned) borrow records for a specific member.
     * Useful for displaying "My Borrowed Items" in the member dashboard.
     * @param userId ID of the member
     * @return List of active borrow record IDs and item IDs as String entries
     */
    public List<String> getActiveBorrowsByUser(int userId) {
        List<String> records = new ArrayList<>();
        String sql = "SELECT br.record_id, li.title, br.due_date " +
                     "FROM borrow_records br " +
                     "JOIN library_items li ON br.item_id = li.item_id " +
                     "WHERE br.user_id = ? AND br.status = 'BORROWED'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Format: "Record #3 | Java Programming | Due: 2026-05-20"
                String entry = "Record #" + rs.getInt("record_id") +
                               " | " + rs.getString("title") +
                               " | Due: " + rs.getDate("due_date");
                records.add(entry);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching active borrows: " + e.getMessage());
        }
        return records;
    }

    /**
     * Checks if a specific item is currently borrowed by a specific user.
     * Useful before allowing another borrow of the same item.
     * @param userId ID of the member
     * @param itemId ID of the item
     * @return true if the item is currently borrowed by this user
     */
    public boolean isItemBorrowedByUser(int userId, int itemId) {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE user_id = ? AND item_id = ? AND status = 'BORROWED'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, itemId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error checking borrow status: " + e.getMessage());
        }
        return false;
    }
}
