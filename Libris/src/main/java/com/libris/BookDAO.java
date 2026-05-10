package com.libris;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BookDAO Class
 * Handles all database operations related to the 'library_items' table.
 * Covers all material types: Book, EBook, AudioBook, and Periodical.
 *
 * Önemli sütun farkı:
 * copy_count      → Toplam fiziksel kopya sayısı. Hiçbir zaman değişmez.
 * available_copies → Şu an ödünç verilebilir kopya sayısı. Ödünç alınınca azalır, iade edilince artar.
 * Biz grid'de ve stok kontrolünde her zaman available_copies kullanıyoruz.
 */
public class BookDAO {

    /**
     * Tüm öğeleri DB'den çeker ve LibraryItem listesi olarak döner.
     * item_type sütununa göre doğru alt sınıfı oluşturur.
     */
    public List<LibraryItem> getAllItems() {
        List<LibraryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM library_items";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
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
     * ID'ye göre tek bir öğe getirir.
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
     * Başlık veya yazara göre arama yapar (büyük/küçük harf duyarsız).
     * SQL LIKE ile kısmi eşleşme sağlar.
     */
    public List<LibraryItem> searchItems(String query) {
        List<LibraryItem> results = new ArrayList<>();
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
     * Yeni kitap ekler.
     * copy_count ve available_copies oluşturulurken eşit olur;
     * ödünç alındıkça available_copies azalır, copy_count sabit kalır.
     */
    public boolean addBook(Book book) {
        String sql = "INSERT INTO library_items " +
                     "(title, author, publication_year, copy_count, available_copies, status, item_type, isbn, page_count, genre) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'BOOK', ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setInt(3, book.getPublicationYear());
            pstmt.setInt(4, book.getCopyCount()); // copy_count: toplam, sabit
            pstmt.setInt(5, book.getCopyCount()); // available_copies: başlangıçta toplam ile eşit
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
     * Yeni süreli yayın ekler.
     */
    public boolean addPeriodical(Periodical periodical) {
        String sql = "INSERT INTO library_items " +
                     "(title, author, publication_year, copy_count, available_copies, status, item_type, issue_number, period) " +
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
     * Yeni e-kitap ekler.
     * Dijital ürün olduğu için stok kavramı yok;
     * copy_count ve available_copies 1 olarak girilir.
     */
    public boolean addEBook(EBook ebook) {
        String sql = "INSERT INTO library_items " +
                     "(title, author, publication_year, copy_count, available_copies, status, item_type, file_format, file_size) " +
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
     * Yeni sesli kitap ekler.
     * Dijital ürün olduğu için stok kavramı yok.
     */
    public boolean addAudioBook(AudioBook audioBook) {
        String sql = "INSERT INTO library_items " +
                     "(title, author, publication_year, copy_count, available_copies, status, item_type, duration, narrator) " +
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
     * ID'ye göre öğe siler.
     *
     * library_items'ı referans eden 4 tablo var (borrow_records, reservations,
     * reviews, wishlist_items). FK ihlalini önlemek için hepsini sırayla,
     * tek bir transaction içinde temizleyip en sonda kitabın kendisini siliyoruz.
     */
    public boolean deleteItem(int itemId) {

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false); // transaction başlat

            // 1) Wishlist
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM wishlist_items WHERE item_id = ?")) {
                ps.setInt(1, itemId);
                int n = ps.executeUpdate();
                System.out.println("[BookDAO] deleteItem("+itemId+") wishlist_items removed=" + n);
            } catch (SQLException ignore) {
                // tablo yoksa (henüz oluşturulmamış olabilir) — sessiz geç
            }

            // 2) Reservations
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM reservations WHERE item_id = ?")) {
                ps.setInt(1, itemId);
                int n = ps.executeUpdate();
                System.out.println("[BookDAO] deleteItem("+itemId+") reservations removed=" + n);
            }

            // 3) Reviews
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM reviews WHERE item_id = ?")) {
                ps.setInt(1, itemId);
                int n = ps.executeUpdate();
                System.out.println("[BookDAO] deleteItem("+itemId+") reviews removed=" + n);
            }

            // 4) Borrow records
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM borrow_records WHERE item_id = ?")) {
                ps.setInt(1, itemId);
                int n = ps.executeUpdate();
                System.out.println("[BookDAO] deleteItem("+itemId+") borrow_records removed=" + n);
            }

            // 5) Kitabın kendisi
            int deleted;
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM library_items WHERE item_id = ?")) {
                ps.setInt(1, itemId);
                deleted = ps.executeUpdate();
                System.out.println("[BookDAO] deleteItem("+itemId+") library_items removed=" + deleted);
            }

            if (deleted > 0) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                System.err.println("[BookDAO] deleteItem("+itemId+") library_items satırı yok, rollback.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting item " + itemId + ": "
                + e.getMessage() + " (errorCode=" + e.getErrorCode() + ")");
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignore) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignore) {}
            }
        }
    }

    /**
     * Ödünç alma (-1) veya iade (+1) sırasında available_copies günceller.
     *
     * ÖNEMLI: copy_count (toplam stok) hiç değişmez — o kitabın kaç fiziksel
     * kopyası olduğunu gösterir ve sadece admin stoğu manuel düzenlerken değişir.
     * available_copies ise kaç tanesinin şu an ödünç verilebileceğini gösterir.
     */
    public boolean updateStock(int itemId, int amount) {
        // ÖNEMLİ: MySQL UPDATE'te aynı kolona referans verirken yeni vs. eski değer
        // tuzağına düşmemek için önce eski available_copies'ı okuyup hesaplama yapıyoruz.
        // Aksi halde "available_copies + ?" CASE içinde yanlış değer verebiliyor.

        int oldAvail = -1;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT available_copies FROM library_items WHERE item_id = ?")) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                oldAvail = rs.getInt("available_copies");
            }
        } catch (SQLException e) {
            System.err.println("Stock read error: " + e.getMessage());
            return false;
        }

        int newAvail = oldAvail + amount;
        if (newAvail < 0) newAvail = 0; // negatife düşmesin
        String newStatus = newAvail > 0 ? "Available" : "Borrowed";

        String sql = "UPDATE library_items SET available_copies = ?, status = ? WHERE item_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newAvail);
            ps.setString(2, newStatus);
            ps.setInt(3, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Stock update error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Admin stoğu manuel güncellediğinde hem copy_count hem available_copies değişir.
     * Normal ödünç alma/iade için updateStock kullanılır.
     *
     * @param itemId  Güncellenecek öğenin ID'si
     * @param newTotal Yeni toplam stok adedi (admin'in girdiği değer)
     */
    /**
     * Tüm kitaplar için available_copies'ı borrow_records'tan gerçek değere yeniden hesaplar.
     * Geçmişten kalma tutarsızlıkları (örn. eski buggy updateTotalStock'lardan) onarır.
     * Uygulama açılışında bir kez çalışır, idempotent — birden fazla çalıştırmak zarar vermez.
     */
    public int resyncStockColumns() {
        // 1) Önce tüm öğelerin id ve copy_count'unu çek
        java.util.List<int[]> items = new java.util.ArrayList<>(); // [item_id, copy_count]
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT item_id, copy_count FROM library_items")) {
            while (rs.next()) {
                items.add(new int[]{ rs.getInt("item_id"), rs.getInt("copy_count") });
            }
        } catch (SQLException e) {
            System.err.println("[BookDAO] resync read error: " + e.getMessage());
            return 0;
        }

        // 2) Her öğe için aktif ödünç sayısını sayıp gerekirse UPDATE et
        int fixed = 0;
        String countSql =
            "SELECT COUNT(*) AS cnt FROM borrow_records " +
            "WHERE item_id = ? AND status = 'BORROWED' AND return_date IS NULL";
        String updateSql =
            "UPDATE library_items SET available_copies = ?, status = ? " +
            "WHERE item_id = ? AND (available_copies <> ? OR status <> ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement countPs  = conn.prepareStatement(countSql);
             PreparedStatement updatePs = conn.prepareStatement(updateSql)) {

            for (int[] row : items) {
                int itemId    = row[0];
                int copyCount = row[1];

                int borrowed = 0;
                countPs.setInt(1, itemId);
                try (ResultSet rs = countPs.executeQuery()) {
                    if (rs.next()) borrowed = rs.getInt("cnt");
                }

                int correctAvailable = Math.max(0, copyCount - borrowed);
                String correctStatus = correctAvailable > 0 ? "Available" : "Borrowed";

                updatePs.setInt(1, correctAvailable);
                updatePs.setString(2, correctStatus);
                updatePs.setInt(3, itemId);
                updatePs.setInt(4, correctAvailable);
                updatePs.setString(5, correctStatus);
                int rows = updatePs.executeUpdate();
                if (rows > 0) {
                    fixed++;
                    System.out.println("[BookDAO] resync: item=" + itemId
                        + " → available_copies=" + correctAvailable
                        + ", status=" + correctStatus);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BookDAO] resync update error: " + e.getMessage());
        }

        return fixed;
    }

    public boolean updateTotalStock(int itemId, int newTotal) {
        // SAĞLAM YAKLAŞIM: borrow_records'tan ŞU AN aktif ödünçte olan kopya sayısını
        // gerçek olarak say. Eğer geçmişte available_copies bozulduysa bile bu sayede
        // self-heal eder.
        //
        //   newAvailable = newTotal - currentlyBorrowed   (negatif olamaz)
        //
        // Status: available > 0 → 'Available', aksi halde 'Borrowed'.

        int currentlyBorrowed = 0;
        String borrowedSql =
            "SELECT COUNT(*) AS cnt FROM borrow_records " +
            "WHERE item_id = ? AND status = 'BORROWED' AND return_date IS NULL";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(borrowedSql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) currentlyBorrowed = rs.getInt("cnt");
            }
        } catch (SQLException e) {
            System.err.println("updateTotalStock count error: " + e.getMessage());
            return false;
        }

        // Admin newTotal'i, şu an ödünçte olan kopyalardan da küçük yapamaz mantıken
        // (yoksa "ödünçteki kopya sayısı toplam stoktan fazla" tutarsızlığı çıkar).
        // Bunu kabul etmek yerine yine de yazıyoruz ama available'ı 0'da tutuyoruz:
        int newAvailable = Math.max(0, newTotal - currentlyBorrowed);
        String newStatus = newAvailable > 0 ? "Available" : "Borrowed";

        String writeSql =
            "UPDATE library_items " +
            "SET copy_count = ?, available_copies = ?, status = ? " +
            "WHERE item_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(writeSql)) {
            ps.setInt(1, newTotal);
            ps.setInt(2, newAvailable);
            ps.setString(3, newStatus);
            ps.setInt(4, itemId);
            int rows = ps.executeUpdate();
            System.out.println("[BookDAO] updateTotalStock item=" + itemId
                + " currentlyBorrowed=" + currentlyBorrowed
                + " → copy_count=" + newTotal + " available_copies=" + newAvailable
                + " status=" + newStatus + " rowsAffected=" + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Total stock write error: " + e.getMessage());
            return false;
        }
    }
    public String getStatusForUser(int itemId, int userId, boolean isBorrowedByUser) {

        if (isBorrowedByUser) {
            return "Borrowed";
        }

        LibraryItem item = getItemById(itemId);

        if (item == null) return "Unknown";

        if (item.getCopyCount() <= 0) {
            return "Non Available";
        }

        return "Available";
    }
    

    /**
     * ResultSet satırını okuyarak doğru LibraryItem alt sınıfını oluşturur.
     * getAllItems(), getItemById() ve searchItems() tarafından kullanılır.
     *
     * KRİTİK: available_copies okunuyor — copy_count değil.
     * copy_count toplam stoku gösterir; available_copies şu an ödünç verilebilir
     * kopyaları gösterir. Grid ve stok kontrolü için doğru olan available_copies.
     */
    private LibraryItem buildItemFromResultSet(ResultSet rs) throws SQLException {
        int id        = rs.getInt("item_id");
        String title  = rs.getString("title");
        String author = rs.getString("author");
        int year      = rs.getInt("publication_year");
        int copies    = rs.getInt("available_copies"); // ← copy_count değil!
        String status = rs.getString("status");
        String type   = rs.getString("item_type");

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