package com.libris;
 
import java.time.LocalDate;
 
public class Main {
    public static void main(String[] args) {
        // Starting the manager
        LibraryManager manager = new LibraryManager();
 
        // Declaring the users — IDs match database (1=admin, 2=cantek, 3=elifdemir)
        Admin admin = new Admin(1, "ahmetadmin", "Ahmet Yılmaz", "ahmet@libris.com", "admin123");
        Member member1 = new Member(2, "cantek", "Can Tekin", "can@mail.com", "pass123");
        Member member2 = new Member(3, "elifdemir", "Elif Demir", "elif@mail.com", "pass456");
 
        // Creating the materials — IDs match database
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
 
        // Testing login — uses username, not email
        System.out.println("\n--- login test ---");
        admin.login("ahmetadmin", "admin123");       // success
        member1.login("cantek", "wrongpass");        // fail
        member1.login("cantek", "pass123");          // success
 
        // Testing dashboard (polymorphism)
        System.out.println("\n--- dashboard test ---");
        admin.showDashBoard();
        System.out.println();
        member1.showDashBoard();
        System.out.println();
        member2.showDashBoard();
 
        // Testing borrow
        System.out.println("\n--- borrow test ---");
        manager.borrowMaterial(member1, book1);   // success — saved to DB
        manager.borrowMaterial(member1, ebook1);  // fail — digital item
 
        // Testing penalty — in-memory calculation
        System.out.println("\n--- penalty test ---");
        BorrowRecord record1 = new BorrowRecord(member1, 1, book1, 14);
        record1.setBorrowDate(LocalDate.now().minusDays(20)); // 6 days late
        int daysDelayed1 = record1.calculateDaysDelayed();
        double penalty1 = book1.calculatePenalty(daysDelayed1, member1.getTotalDelays());
        member1.setBalance(member1.getBalance() + penalty1);
        member1.setTotalDelays(member1.getTotalDelays() + 1);
        System.out.println("Days late: " + daysDelayed1 + " | Fine: " + penalty1 + " TL");
        System.out.println(member1);
 
        // Testing on-time return
        System.out.println("\n--- returning test ---");
        BorrowRecord record2 = new BorrowRecord(member2, 2, mag1, 14);
        record2.setBorrowDate(LocalDate.now().minusDays(5)); // still on time
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
 
        System.out.println("--- TEST END ---");
    }
}
 
