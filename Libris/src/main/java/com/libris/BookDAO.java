package com.libris;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BookDAO Class
 * Handles all database operations related to the 'library_items' table.
 * Covers all material types: Book, EBook, AudioBook, and Periodical.
 */
public class BookDAO {

    /**
     * Retrieves all items from the database and returns them as LibraryItem objects.
     * Uses the 'item_type' column to construct the correct subclass.
     * @return List of all LibraryItem objects (Books, EBooks, AudioBooks, Periodicals)
     */
    public List<LibraryItem> getAllItems() {
        List<LibraryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM library_items";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Build the correct subclass based on item_type
                LibraryItem item = buildItemFromResultSet(rs);
                if (item != null) {
                    items.add(item);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching items: " + e.getMessage());
        }
        return items;
    }

    /**
     * Fetches a single item by its ID.
     * @param itemId The ID of the item to fetch
     * @return The LibraryItem if found, null otherwise
     */
    public LibraryItem getItemById(int itemId) {
        String sql = "SELECT * FROM library_items WHERE item_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return buildItemFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching item by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Searches for items by title or author (case-insensitive).
     * @param query The search keyword
     * @return List of matching LibraryItem objects
     */
    public List<LibraryItem> searchItems(String query) {
        List<LibraryItem> results = new ArrayList<>();
        // LIKE with % allows partial matching on both title and author
        String sql = "SELECT * FROM library_items WHERE title LIKE ? OR author LIKE ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String pattern = "%" + query + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                LibraryItem item = buildItemFromResultSet(rs);
                if (item != null) results.add(item);
            }

        } catch (SQLException e) {
            System.err.println("Search error: " + e.getMessage());
        }
        return results;
    }

    /**
     * Adds a new Book to the database.
     * @param book The Book object to insert
     * @return true if the insert was successful
     */
    public boolean addBook(Book book) {
        String sql = "INSERT INTO library_items (title, author, publication_year, copy_count, available_copies, status, item_type, isbn, page_count, genre) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'BOOK', ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setInt(3, book.getPublicationYear());
            pstmt.setInt(4, book.getCopyCount());
            pstmt.setInt(5, book.getCopyCount()); // available = total on creation
            pstmt.setString(6, book.getStatus());
            pstmt.setString(7, book.getIsbn());
            pstmt.setInt(8, book.getPageCount());
            pstmt.setString(9, book.getGenre());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds a new Periodical to the database.
     * @param periodical The Periodical object to insert
     * @return true if the insert was successful
     */
    public boolean addPeriodical(Periodical periodical) {
        String sql = "INSERT INTO library_items (title, author, publication_year, copy_count, available_copies, status, item_type, issue_number, period) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'PERIODICAL', ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, periodical.getTitle());
            pstmt.setString(2, periodical.getAuthor());
            pstmt.setInt(3, periodical.getPublicationYear());
            pstmt.setInt(4, periodical.getCopyCount());
            pstmt.setInt(5, periodical.getCopyCount());
            pstmt.setString(6, periodical.getStatus());
            pstmt.setInt(7, periodical.getIssueNumber());
            pstmt.setString(8, periodical.getPeriod());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding periodical: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds a new EBook to the database.
     * @param ebook The EBook object to insert
     * @return true if the insert was successful
     */
    public boolean addEBook(EBook ebook) {
        String sql = "INSERT INTO library_items (title, author, publication_year, copy_count, available_copies, status, item_type, file_format, file_size) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'EBOOK', ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ebook.getTitle());
            pstmt.setString(2, ebook.getAuthor());
            pstmt.setInt(3, ebook.getPublicationYear());
            pstmt.setInt(4, ebook.getCopyCount());
            pstmt.setInt(5, ebook.getCopyCount());
            pstmt.setString(6, ebook.getStatus());
            pstmt.setString(7, ebook.getFileFormat());
            pstmt.setDouble(8, ebook.getFileSize());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding EBook: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds a new AudioBook to the database.
     * @param audioBook The AudioBook object to insert
     * @return true if the insert was successful
     */
    public boolean addAudioBook(AudioBook audioBook) {
        String sql = "INSERT INTO library_items (title, author, publication_year, copy_count, available_copies, status, item_type, duration, narrator) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'AUDIOBOOK', ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, audioBook.getTitle());
            pstmt.setString(2, audioBook.getAuthor());
            pstmt.setInt(3, audioBook.getPublicationYear());
            pstmt.setInt(4, audioBook.getCopyCount());
            pstmt.setInt(5, audioBook.getCopyCount());
            pstmt.setString(6, audioBook.getStatus());
            pstmt.setInt(7, audioBook.getDuration());
            pstmt.setString(8, audioBook.getNarrator());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error adding AudioBook: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes an item from the database by its ID.
     * @param itemId ID of the item to delete
     * @return true if deletion was successful
     */
    public boolean deleteItem(int itemId) {
        String sql = "DELETE FROM library_items WHERE item_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting item: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates the available copy count when an item is borrowed (-1) or returned (+1).
     * @param itemId ID of the item
     * @param amount Change in stock (use -1 for borrow, +1 for return)
     * @return true if update was successful
     */
    public boolean updateStock(int itemId, int amount) {
        String sql = "UPDATE library_items SET available_copies = available_copies + ? WHERE item_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, amount);
            pstmt.setInt(2, itemId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Stock update error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Private helper method that reads a ResultSet row and builds the correct LibraryItem subclass.
     * Called internally by getAllItems(), getItemById(), and searchItems().
     * @param rs The current row in the ResultSet
     * @return The correct LibraryItem subclass, or null if item_type is unknown
     */
    private LibraryItem buildItemFromResultSet(ResultSet rs) throws SQLException {
        int id               = rs.getInt("item_id");
        String title         = rs.getString("title");
        String author        = rs.getString("author");
        int year             = rs.getInt("publication_year");
        int copies           = rs.getInt("copy_count");
        String status        = rs.getString("status");
        String type          = rs.getString("item_type");

        // Build the correct subclass depending on item_type
        switch (type) {
            case "BOOK":
                return new Book(id, title, author, year, copies, status,
                        rs.getString("isbn"),
                        rs.getInt("page_count"),
                        rs.getString("genre"));

            case "EBOOK":
                return new EBook(id, title, author, year, copies, status,
                        rs.getString("file_format"),
                        rs.getDouble("file_size"));

            case "AUDIOBOOK":
                return new AudioBook(id, title, author, year, copies, status,
                        rs.getInt("duration"),
                        rs.getString("narrator"));

            case "PERIODICAL":
                return new Periodical(id, title, author, year, copies, status,
                        rs.getInt("issue_number"),
                        rs.getString("period"));

            default:
                System.err.println("Unknown item type: " + type);
                return null;
        }
    }
}
