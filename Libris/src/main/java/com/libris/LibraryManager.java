package com.libris;
 
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
 
/*
 * Info:
 * This class is responsible for managing users, authentication and registration.
 * It acts as a central service layer for the library system.
 * It handles user creation, login and username recovery operations.
 * Also provides database-backed item management via DAO classes.
 */
 
public class LibraryManager {
 
    private List<User> users;
    private List<BorrowRecord> borrowRecords;
 
    private UserDAO userDAO;
    private BookDAO bookDAO;
    private BorrowDAO borrowDAO;
 
    public LibraryManager() {
        this.users = new ArrayList<>();
        this.borrowRecords = new ArrayList<>();
        this.userDAO  = new UserDAO();
        this.bookDAO  = new BookDAO();
        this.borrowDAO = new BorrowDAO();
    }
 
    // ----------------------------------------------------
    // USER MANAGEMENT SYSTEM
    // ----------------------------------------------------
 
    public void addUser(User user) {
        users.add(user);
        if (user instanceof Admin) {
            userDAO.registerUser(user.getUsername(), user.getName(), user.getEmail(), user.getPassword(), "ADMIN");
        } else {
            userDAO.registerUser(user.getUsername(), user.getName(), user.getEmail(), user.getPassword(), "MEMBER");
        }
        System.out.println("User added: " + user.getUsername());
    }
 
    // ----------------------------------------------------
    // ITEM MANAGEMENT — database backed via BookDAO
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
     * Legacy method — kept for backward compatibility.
     * New code should use addItem() instead.
     */
    public void addItems(LibraryItem item) {
        addItem(item);
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
    // REGISTRATION SYSTEM
    // ----------------------------------------------------
 
    public boolean registerUser(String username, String name, String email, String password) {
        boolean success = userDAO.registerUser(username, name, email, password, "MEMBER");
 
        if (!success) {
            System.out.println("Error! Registration failed in database.");
            return false;
        }
 
        Member newUser = new Member(users.size() + 1, username, name, email, password);
        users.add(newUser);
 
        System.out.println("User registered successfully: " + username);
        return true;
    }
 
    // ----------------------------------------------------
    // LOGIN SYSTEM
    // ----------------------------------------------------
 
    public User login(String username, String password) {
        // 1. RAM CHECK (cache-first)
        for (User u : users) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                System.out.println("Login from RAM: " + username);
                return u;
            }
        }
 
        // 2. DB CHECK (fallback)
        boolean ok = userDAO.loginUser(username, password);
        if (!ok) {
            System.out.println("Login failed!");
            return null;
        }
 
        Member member = userDAO.getMemberByUsername(username);
        if (member != null) {
            users.add(member);
            System.out.println("Login from DB: " + username);
            return member;
        }
 
        return null;
    }
 
    // ----------------------------------------------------
    // BORROW SYSTEM
    // ----------------------------------------------------
 
    /**
     * Processes a borrow request and saves it to the database.
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
 
    // ----------------------------------------------------
    // USER SEARCH
    // ----------------------------------------------------
 
    public User findByUsername(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }
 
    public List<User> getAllUsers() {
        return users;
    }
}