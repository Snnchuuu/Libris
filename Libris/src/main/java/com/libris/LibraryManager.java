package com.libris;

import java.util.List;

/**
 * LibraryManager Class
 * Central management class for the Libris system.
 * All operations (borrow, return, add item, etc.) are persisted in MySQL.
 */
public class LibraryManager {

    // DAO instances handle all communication with the database
    private final BookDAO bookDAO;
    private final BorrowDAO borrowDAO;
    private final UserDAO userDAO;
    private final ReservationDAO reservationDAO;
    private final ReviewDAO reviewDAO;

    public LibraryManager() {
        this.bookDAO        = new BookDAO();
        this.borrowDAO      = new BorrowDAO();
        this.userDAO        = new UserDAO();
        this.reservationDAO = new ReservationDAO();
        this.reviewDAO      = new ReviewDAO();
    }

    // ITEM OPERATIONS

    
     // Adds a new item to the database based on its type.
  
    public void addItem(LibraryItem item) {
        boolean success = false;

        if (item instanceof Book) {
            success = bookDAO.addBook((Book) item);
        } else if (item instanceof EBook) {
            success = bookDAO.addEBook((EBook) item);
        } else if (item instanceof AudioBook) {
            success = bookDAO.addAudioBook((AudioBook) item);
        } else if (item instanceof Periodical) {
            success = bookDAO.addPeriodical((Periodical) item);
        }

        if (success) {
            System.out.println("Item added to database: " + item.getTitle());
        } else {
            System.err.println("Failed to add item: " + item.getTitle());
        }
    }

    
     //Returns all library items from the database.
    
     
    public List<LibraryItem> getAllItems() {
        return bookDAO.getAllItems();
    }

    /**
     * Searches for items by title or author keyword.
     * @param query The search keyword
     */
    public List<LibraryItem> searchItems(String query) {
        return bookDAO.searchItems(query);
    }

    /**
     * Deletes an item from the database by ID.
     * Only admins should be able to call this.
     */
    public void deleteItem(int itemId) {
        boolean success = bookDAO.deleteItem(itemId);
        if (success) {
            System.out.println("Item deleted: ID " + itemId);
        } else {
            System.err.println("Failed to delete item: ID " + itemId);
        }
    }

    // USER OPERATIONS

    
    //Registers a new member in the database.
    public void registerMember(String username, String email, String password) {
        boolean success = userDAO.registerUser(username, email, password, "MEMBER");
        if (success) {
            System.out.println("Member registered: " + username);
        } else {
            System.err.println("Registration failed for: " + username);
        }
    }

    /**
     * Logs in a user by checking credentials against the database.
     * Returns the Member object if successful, null if login fails.
     */
    public Member loginMember(String email, String password) {
        boolean valid = userDAO.loginUser(email, password);
        if (valid) {
            return userDAO.getMemberByEmail(email);
        }
        System.out.println("Login failed for: " + email);
        return null;
    }

    // BORROW & RETURN OPERATIONS

    /**
     * Processes a borrow request.
     * Checks if the item is borrowable, then records it in the database.
     * Replaces the old in-memory borrow logic.
     * @param member The member borrowing the item
     * @param item   The item to borrow
     */
    public void borrowMaterial(Member member, LibraryItem item) {
        // Digital items (EBook, AudioBook) cannot be borrowed physically
        if (!(item instanceof Borrowable)) {
            System.err.println("Error! " + item.getTitle() + " is digital and cannot be borrowed physically!");
            return;
        }

        boolean success = borrowDAO.borrowItem(member.getId(), item.getId());
        if (success) {
            System.out.println(member.getName() + " borrowed: " + item.getTitle());
        } else {
            System.err.println("Borrow failed for: " + item.getTitle());
        }
    }

    /**
     * Processes a return.
     * Calculates penalty using the item's own calculatePenalty() method,
     * updates the database, and prints the result.
     * @param recordId The borrow record ID to close
     * @param member   The member returning the item
     * @param item     The item being returned
     */
    public void returnMaterial(int recordId, Member member, LibraryItem item) {
        double fine = borrowDAO.returnItem(recordId, member, item);

        if (fine > 0) {
            System.out.println("LATE RETURN — Fine: " + fine + " TL added to " + member.getName() + "'s account.");
        } else {
            System.out.println("RETURNED ON TIME — No penalty for " + member.getName() + ".");
        }
    }

    /**
     * Returns all currently borrowed items for a member.
     * Useful for the member dashboard.
     */
    public List<String> getActiveBorrows(int userId) {
        return borrowDAO.getActiveBorrowsByUser(userId);
    }

    // RESERVATION OPERATIONS

    
    //Creates a reservation for a member on a fully borrowed item.
     
    public void reserveItem(Member member, LibraryItem item) {
        boolean success = reservationDAO.createReservation(member.getId(), item.getId());
        if (success) {
            System.out.println(member.getName() + " reserved: " + item.getTitle());
        } else {
            System.err.println("Reservation failed for: " + item.getTitle());
        }
    }

    
    //Cancels an existing reservation.
    
    public void cancelReservation(int reservationId) {
        boolean success = reservationDAO.cancelReservation(reservationId);
        if (success) {
            System.out.println("Reservation #" + reservationId + " cancelled.");
        }
    }

   
    //Returns all active reservations for a member.
    
    public List<String> getActiveReservations(int userId) {
        return reservationDAO.getActiveReservationsByUser(userId);
    }

    // REVIEW OPERATIONS
 
    /**
     * Adds a review for a library item.
     * @param member  The member writing the review
     * @param item    The item being reviewed
     * @param rating  Score between 1 and 5
     * @param comment Written comment
     */
    public void addReview(Member member, LibraryItem item, int rating, String comment) {
        boolean success = reviewDAO.addReview(member.getId(), item.getId(), rating, comment);
        if (success) {
            System.out.println("Review added by " + member.getName() + " for: " + item.getTitle());
        } else {
            System.err.println("Review failed for: " + item.getTitle());
        }
    }

    
    //Returns all reviews for a specific item.
    
    public List<String> getReviews(int itemId) {
        return reviewDAO.getReviewsByItem(itemId);
    }

   
    //Returns the average rating for a specific item.
    
    public double getAverageRating(int itemId) {
        return reviewDAO.getAverageRating(itemId);
    }
}
