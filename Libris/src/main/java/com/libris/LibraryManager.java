package com.libris;

import java.util.ArrayList;   //Importing ArrayList
import java.util.List;        //Importing list
import java.time.LocalDate;   //Importing LocalDate for date calculations

/*
 * Info:
 * This class is responsible for managing users, authentication and registration.
 * It acts as a central service layer for the library system.
 * It handles user creation, login and username recovery operations.
 */

public class LibraryManager {   //Start of class LibraryManager

    private List<User> users;   //users list stores all registered users
    private List<LibraryItem> items;  //items in system
    private List<BorrowRecord> borrowRecords;

    private UserDAO userDAO; //DATABASE ACCESS OBJECT

    public LibraryManager() {   //Constructor of our class
        this.users = new ArrayList<>();
        this.items = new ArrayList<>();
        this.borrowRecords = new ArrayList<>();
        this.userDAO = new UserDAO(); //init DAO
    }

    // ----------------------------------------------------
    // USER MANAGEMENT SYSTEM
    // ----------------------------------------------------

    public void addUser(User user) {  
        //Method for adding existing user objects (Admin, Member etc.)

        users.add(user);

        //SYNC TO DATABASE
        if (user instanceof Admin) {
            userDAO.registerUser(user.getUsername(), user.getName(), user.getEmail(), user.getPassword(), "ADMIN");
        } else {
            userDAO.registerUser(user.getUsername(), user.getName(), user.getEmail(), user.getPassword(), "MEMBER");
        }

        System.out.println("User added: " + user.getUsername());
    }

    public void addItems(LibraryItem item) {  
        //Method for adding library items into system

        items.add(item);
        System.out.println("Item added: " + item.getTitle());
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

        // FIX: DB’den çekmek yerine direkt RAM cache oluşturuyoruz
        Member newUser = new Member(
                users.size() + 1,
                username,
                name,
                email,
                password
        );

        users.add(newUser); // RAM CACHE

        System.out.println("User registered successfully: " + username);
        return true;
    }

    // ----------------------------------------------------
    // LOGIN SYSTEM
    // ----------------------------------------------------

    public User login(String username, String password) {  

        // 1. RAM CHECK (cache-first)
        for (User u : users) {
            if (u.getUsername().equals(username) &&
                u.getPassword().equals(password)) {

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

        // FIX: username yerine email kullanman gerekiyorsa burada netleşmeli
        Member member = userDAO.getMemberByUsername(username);

        if (member != null) {
            users.add(member); // cache update
            System.out.println("Login from DB: " + username);
            return member;
        }

        return null;
    }

    // ----------------------------------------------------
    // BORROW SYSTEM
    // ----------------------------------------------------

    public void borrowMaterial(Member member, LibraryItem item) {  

        if (!(item instanceof Borrowable)) {
            System.err.println("Error! This item cannot be borrowed physically!");
            return;
        }

        BorrowRecord record = new BorrowRecord(
                member,
                borrowRecords.size() + 1,
                item,
                14
        );

        borrowRecords.add(record);

        ((Borrowable) item).borrowItem();

        System.out.println("Borrow successful: " + item.getTitle());
    }

    public void returnMaterial(BorrowRecord record) {

        record.setReturnDate(LocalDate.now());

        int delay = record.calculateDaysDelayed();

        if (delay > 0) {
            System.out.println("Late return: " + delay + " days");
        } else {
            System.out.println("Returned on time");
        }

        if (record.getItem() instanceof Borrowable) {
            ((Borrowable) record.getItem()).returnItem();
        }

        //SYNC PENALTY UPDATE TO DB
        Member m = record.getMember();
        userDAO.updateMemberPenalty(
            m.getId(),
            m.getBalance(),
            m.getTotalDelays()
        );
    }

    // ----------------------------------------------------
    // USER SEARCH (RECOVERY PURPOSE)
    // ----------------------------------------------------

    public User findByUsername(String username) {  

        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }

        return null;
    }

    // ----------------------------------------------------
    // DEBUG
    // ----------------------------------------------------

    public List<User> getAllUsers() {
        return users;
    }

}   //End of class LibraryManager