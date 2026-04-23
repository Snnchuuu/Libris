package src;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        // Starting the manager
        LibraryManager manager = new LibraryManager();

        // Declaring the users
        Admin admin = new Admin(1, "Ahmet Yılmaz", "ahmet@libris.com", "admin123");
        Member member = new Member(101, "Can Tekin", "can@mail.com", "pass123");

        // Creating the materials
        Book book1 = new Book(1, "Java Programming", "Deitel", 2024, 5, "Kutay", "123-456", 800, "Education");
        EBook ebook1 = new EBook(2, "Digital Trends", "AI Expert", 2026, 1, "PDF", "32517635", 15.5);
        Periodical mag1 = new Periodical(3, "Science Weekly", "Global Science", 2026, 10, "kUTAY", 45, "Weekly");

        // Adding materials to the system
        manager.addItems(book1);
        manager.addItems(mag1);
        manager.addItems(ebook1);

        System.out.println("--- LIBRIS TEST START ---");

        //Testing the borrowing mechanisms
        manager.borrowMaterial(member, book1);
        manager.borrowMaterial(member, ebook1); //Error message should pop up beacuse the material is digital

        // Testing the penalty methods
        BorrowRecord record = new BorrowRecord(member, 1, book1, null, 14);
        
        record.setReturnDate(LocalDate.now().plusDays(20));
        
        System.out.println("Gecikme Gunu: " + record.calculateDaysDelayed());
        
        manager.returnMaterial(record);
        
        //Testing the dashBoard
        member.showDashBoard();

        System.out.println("--- TEST END ---");
    }
}