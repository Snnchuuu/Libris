package com.libris;
 
import java.time.LocalDate;
 
public class Main {
    public static void main(String[] args) {
 
        // Starting the manager (now connected to the database via DAOs)
        LibraryManager manager = new LibraryManager();
 
        // Creating user objects for local testing
        Admin admin = new Admin(1, "Ahmet Yılmaz", "ahmet@libris.com", "admin123");
        Member member1 = new Member(101, "Can Tekin", "can@mail.com", "pass123");
        Member member2 = new Member(102, "Elif Demir", "elif@mail.com", "pass456");
 
        // Creating material objects for local testing
        Book book1 = new Book(1, "Java Programming", "Deitel", 2024, 5, "Available", "123-456", 800, "Education");
        Book book2 = new Book(2, "Araba Sevdası", "Recaizade Mahmut Ekrem", 1875, 3, "Available", "456-789", 276, "Novel");
        EBook ebook1 = new EBook(3, "Digital Trends", "AI Expert", 2025, 1, "Available", "PDF", 15.5);
        AudioBook abook1 = new AudioBook(4, "Sapiens", "Harari", 2014, 2, "Available", 900, "John Smith");
        Periodical mag1 = new Periodical(5, "Science Weekly", "Global Science", 2026, 10, "Available", 45, "Weekly");
 
        System.out.println("--- LIBRIS TEST START ---");
 
        // toString demo
        System.out.println("\n--- toString demo ---");
        System.out.println(admin);
        System.out.println(member1);
        System.out.println(member2);
        System.out.println(book1);
        System.out.println(book2);
        System.out.println(ebook1);
        System.out.println(abook1);
        System.out.println(mag1);
 
        // Testing login (polymorphism)
        System.out.println("\n--- login test ---");
        admin.login("ahmet@libris.com", "admin123");
        member1.login("can@mail.com", "wrongpass");
        member1.login("can@mail.com", "pass123");
 
        // Testing dashboard (polymorphism)
        System.out.println("\n--- dashboard test ---");
        admin.showDashBoard();
        System.out.println();
        member1.showDashBoard();
        System.out.println();
        member2.showDashBoard();
 
        // Testing borrow (digital items should be blocked)
        System.out.println("\n--- borrow test ---");
        manager.borrowMaterial(member1, book1);
        manager.borrowMaterial(member1, ebook1); // Should print error: digital item
 
        // Testing penalty calculation (late return)
        System.out.println("\n--- penalty test ---");
        BorrowRecord record1 = new BorrowRecord(member1, 1, book1, 14); // Fixed: removed null
        record1.setBorrowDate(LocalDate.now().minusDays(20)); // Borrowed 20 days ago, 6 days late
        int daysDelayed1 = record1.calculateDaysDelayed();
        double penalty1 = book1.calculatePenalty(daysDelayed1, member1.getTotalDelays());
        member1.setBalance(member1.getBalance() + penalty1);
        member1.setTotalDelays(member1.getTotalDelays() + 1);
        System.out.println("Days late: " + daysDelayed1 + " | Fine: " + penalty1 + " TL");
        System.out.println(member1);
 
        // Testing on-time return
        System.out.println("\n--- returning test ---");
        BorrowRecord record2 = new BorrowRecord(member2, 2, mag1, 14); // Fixed: removed null
        record2.setBorrowDate(LocalDate.now().minusDays(5)); // Borrowed 5 days ago, still on time
        int daysDelayed2 = record2.calculateDaysDelayed();
        if (daysDelayed2 == 0) {
            System.out.println("RETURNED ON TIME: No penalty.");
        }
        System.out.println(member2);
 
        // Testing reviews
        System.out.println("\n--- review test ---");
        book2.addReview("Bihruz Bey is hilarious, but also pathetic tbh...", 5);
        abook1.addReview("Loved the narrator.", 4);
 
        // Testing search
        System.out.println("\n--- search test ---");
        book1.search("Java");
        ebook1.search("Digital");
        abook1.search("Sapiens");
 
        // Testing WishList
        System.out.println("\n--- wishlist test ---");
        WishList wishList = new WishList(1, member1);
        wishList.addItem(book1);
        wishList.addItem(ebook1);
        wishList.addItem(book1); // Should print: already in wishlist
        wishList.removeItem(ebook1);
        System.out.println(wishList);
 
        // Testing Notification
        System.out.println("\n--- notification test ---");
        Notification notif = new Notification(1, member1, "Your reserved item is now available!");
        System.out.println(notif);
        notif.markAsRead();
        System.out.println("Is read: " + notif.getIsRead());
 
        // Testing exception handling
        System.out.println("\n--- exception handling test ---");
        try {
            Book badBook = new Book(6, "Bad Book", "Nobody", 2030, -5, "Available", "000", -100, "None");
        } catch (IllegalArgumentException e) {
            System.out.println("Error! : " + e.getMessage());
        }
 
        try {
            Review badReview = new Review(1, member1, book1, 8, "Too high rating");
        } catch (IllegalArgumentException e) {
            System.out.println("Error! : " + e.getMessage());
        }
 
        try {
            Notification badNotif = new Notification(-1, member1, "test");
        } catch (IllegalArgumentException e) {
            System.out.println("Error! : " + e.getMessage());
        }
 
        System.out.println("--- TEST END ---");
    }
}