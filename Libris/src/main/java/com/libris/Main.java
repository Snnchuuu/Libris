package com.libris;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        // Starting the manager
        LibraryManager manager = new LibraryManager();

        // Declaring the users
        Admin admin = new Admin(1, "Ahmet Yılmaz", "ahmet@libris.com", "admin123");
        Member member1 = new Member(101, "Can Tekin", "can@mail.com", "pass123");
        Member member2 = new Member(102, "Elif Demir", "elif@mail.com", "pass456");
        
        manager.addUser(admin);
        manager.addUser(member1);
        manager.addUser(member2);

        // Creating the materials
        Book book1 = new Book(1, "Java Programming", "Deitel", 2024, 5, "Available", "123-456", 800, "Education");
        Book book2 = new Book(2, "Araba Sevdası", "Recaizade Mahmut Ekrem", 1875, 3, "Available", "456-789", 276, "Novel");
        EBook ebook1 = new EBook(3, "Digital Trends", "AI Expert", 2025, 1, "Available", "PDF", 15.5);
        AudioBook abook1 = new AudioBook(4, "Sapiens", "Harari", 2014, 2, "Available", 900, "John Smith");
        Periodical mag1 = new Periodical(5, "Science Weekly", "Global Science", 2026, 10, "Available", 45, "Weekly");

        // Adding materials to the system
        manager.addItems(book1);
        manager.addItems(book2);
        manager.addItems(ebook1);
        manager.addItems(abook1);
        manager.addItems(mag1);
        
        
        System.out.println("--- LIBRIS TEST START ---");
        
        //toString demo
        System.out.println("\n---toString demo----");
        System.out.println(admin);
        System.out.println(member1);
        System.out.println(member2);
        System.out.println(book1);
        System.out.println(book2);
        System.out.println(ebook1);
        System.out.println(abook1);
        System.out.println(mag1);
        
        //Testing the logining (polymorphism)
        System.out.println("\n---login test----");
        admin.login("ahmet@libris.com", "admin123");
        member1.login("can@mail.com", "wrongpass");
        member1.login("can@mail.com","pass123");
        
        //Testing the dashboard (polymorphism)
        System.out.println("\n----dashboard test----");
        admin.showDashBoard();
        System.out.println();
        member1.showDashBoard();
        System.out.println();
        member2.showDashBoard();
        

        //Testing the borrowing methods
        System.out.println("\n----borrow test----");
        manager.borrowMaterial(member1, book1);
        manager.borrowMaterial(member1, ebook1); //Error message should pop up beacuse the material is digital

        // Testing the penalty methods
        System.out.println("\n----penalty test----");
        BorrowRecord record1 = new BorrowRecord(member1, 1, book1, null, 14);
        record1.setBorrowDate(LocalDate.now().minusDays(20)); // borrowed 20 days ago
        manager.returnMaterial(record1);
        System.out.println(member1); //shows penalty
        //System.out.println("Gecikme Gunu: " + record.calculateDaysDelayed());
        
        //Testing the normal return
        System.out.println("\n----returning test----");
        BorrowRecord record2 = new BorrowRecord(member2, 2, mag1, null, 14);
        record2.setBorrowDate(LocalDate.now().minusDays(5)); //borrowed 5 days ago, has 14 days
        manager.returnMaterial(record2);
        System.out.println(member2);
        
        //Testing the review
        System.out.println("\n----review test----");
        book2.addReview("Bihruz Bey is hilarious, but also pathetic tbh...", 5);
        abook1.addReview("Loved the narrator.", 4);
        
        //Testing search methods
        System.out.println("\n----search test----");
        book1.search("Java");
        ebook1.search("Digital");
        abook1.search("Sapiens");
        
        //Testing Exception Handling 
        System.out.println("\n----exception handling test----");
        try {
        	Book badBook = new Book(6, "Bad Book", "Nobody", 2030, -5, "Available", "000", -100, "None");
        } catch (IllegalArgumentException e) {
        	System.out.println("Error! : "+e.getMessage());
        }
        
        try {
        	Review badReview = new Review(1, member1, book1, 8, "Too high rating");
        } catch (IllegalArgumentException e) {
        	System.out.println("Error! :"+e.getMessage());
        }

        System.out.println("--- TEST END ---");
    }
}