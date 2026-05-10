package com.libris;
 
import java.util.List;
 
/*
 * LibraryManager Class
 * Central service layer for the Libris system.
 * All operations are persisted in MySQL via DAO classes.
 * No in-memory lists — everything goes through the database.
 */
public class LibraryManager {
 
    // DAO instances — each handles one table in the database
    private final BookDAO bookDAO;
    private final BorrowDAO borrowDAO;
    private final UserDAO userDAO;
    private final ReservationDAO reservationDAO;
    private final ReviewDAO reviewDAO;
    private final WishListDAO wishListDAO;
 
    public LibraryManager() {
        this.bookDAO        = new BookDAO();
        this.borrowDAO      = new BorrowDAO();
        this.userDAO        = new UserDAO();
        this.reservationDAO = new ReservationDAO();
        this.reviewDAO      = new ReviewDAO();
        this.wishListDAO    = new WishListDAO();
    }
 
    // ----------------------------------------------------
    // USER OPERATIONS
    // ----------------------------------------------------
 
    /**
     * Registers a new member in the database.
     * Used by RegisterView.
     */
    public boolean registerUser(String username, String name, String email, String password) {
        return userDAO.registerUser(username, name, email, password, "MEMBER");
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
        return null;
    }
 
    // ----------------------------------------------------
    // ITEM OPERATIONS
    // ----------------------------------------------------
 
    /**
     * Returns all library items from the database.
     * Used by LibraryCatalogView to populate the grid.
     */
    public List<LibraryItem> getAllItems() {
        return bookDAO.getAllItems();
    }
 
    /**
     * Adds a new item to the database based on its type.
     */
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
 
    /**
     * Deletes an item from the database by ID.
     * Only admins should call this.
     */
    public void deleteItem(int itemId) {
        boolean success = bookDAO.deleteItem(itemId);
        if (success) {
            System.out.println("Item deleted: ID " + itemId);
        } else {
            System.err.println("Failed to delete item: ID " + itemId);
        }
    }
 
    /**
     * Searches items by title or author keyword.
     */
    public List<LibraryItem> searchItems(String query) {
        return bookDAO.searchItems(query);
    }
 
    // ----------------------------------------------------
    // BORROW OPERATIONS
    // ----------------------------------------------------
 
    /**
     * Processes a borrow request and saves it to the database.
     * Digital items (EBook, AudioBook) cannot be borrowed physically.
     */
    public void borrowMaterial(Member member, LibraryItem item) {
        if (!(item instanceof Borrowable)) {
            System.err.println("Error! " + item.getTitle() + " cannot be borrowed physically!");
            return;
        }
 
        boolean success = borrowDAO.borrowItem(member.getId(), item.getId());
        if (success) {
            System.out.println("Borrow successful: " + item.getTitle());
        } else {
            System.err.println("Borrow failed: " + item.getTitle());
        }
    }
 
    /**
     * Processes a return and updates the database.
     * Calculates penalty using the item's own calculatePenalty() method.
     */
    public double returnMaterial(int recordId, Member member, LibraryItem item) {
        double fine = borrowDAO.returnItem(recordId, member, item);
        if (fine > 0) {
            System.out.println("Late return — Fine: " + fine + " TL");
        } else {
            System.out.println("Returned on time.");
        }
        return fine;
    }
 
    /**
     * Returns all currently borrowed items for a member.
     */
    public List<String> getActiveBorrows(int userId) {
        return borrowDAO.getActiveBorrowsByUser(userId);
    }
 
    // ----------------------------------------------------
    // RESERVATION OPERATIONS
    // ----------------------------------------------------
 
    /**
     * Creates a reservation for a member on a fully borrowed item.
     */
    public void reserveItem(Member member, LibraryItem item) {
        boolean success = reservationDAO.createReservation(member.getId(), item.getId());
        if (success) {
            System.out.println(member.getName() + " reserved: " + item.getTitle());
        } else {
            System.err.println("Reservation failed for: " + item.getTitle());
        }
    }
 
    /**
     * Cancels an existing reservation.
     */
    public void cancelReservation(int reservationId) {
        boolean success = reservationDAO.cancelReservation(reservationId);
        if (success) {
            System.out.println("Reservation #" + reservationId + " cancelled.");
        }
    }
 
    /**
     * Returns all active reservations for a member.
     */
    public List<String> getActiveReservations(int userId) {
        return reservationDAO.getActiveReservationsByUser(userId);
    }
 
    // ----------------------------------------------------
    // REVIEW OPERATIONS
    // ----------------------------------------------------
 
    /**
     * Adds a review for a library item.
     */
    public void addReview(Member member, LibraryItem item, int rating, String comment) {
        boolean success = reviewDAO.addReview(member.getId(), item.getId(), rating, comment);
        if (success) {
            System.out.println("Review added by " + member.getName() + " for: " + item.getTitle());
        } else {
            System.err.println("Review failed for: " + item.getTitle());
        }
    }
 
    /**
     * Returns all reviews for a specific item.
     */
    public List<String> getReviews(int itemId) {
        return reviewDAO.getReviewsByItem(itemId);
    }
 
    /**
     * Returns the average rating for a specific item.
     */
    public double getAverageRating(int itemId) {
        return reviewDAO.getAverageRating(itemId);
    }
 
    // ----------------------------------------------------
    // WISHLIST OPERATIONS
    // ----------------------------------------------------
 
    /**
     * Adds an item to a member's wish list in the database.
     */
    public void addToWishList(Member member, LibraryItem item) {
        wishListDAO.addItem(member.getId(), item.getId());
    }
 
    /**
     * Removes an item from a member's wish list.
     */
    public void removeFromWishList(Member member, LibraryItem item) {
        wishListDAO.removeItem(member.getId(), item.getId());
    }
 
    /**
     * Returns all items in a member's wish list.
     */
    public List<String> getWishList(int userId) {
        return wishListDAO.getWishList(userId);
    }
}
 
