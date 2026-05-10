-- ============================================================
-- TAM DEMO RESET (NÜKLEER)
-- ============================================================
-- DİKKAT: Bu script kullanıcı dışındaki TÜM verileri siler!
--   - Tüm ödünç kayıtları, rezervasyonlar, yorumlar, wishlist
--   - Tüm kitap/kütüphane öğeleri
--   - Sonra başlangıç 5 kitabı yeniden ekler
--
-- Kullanıcılar (admin/üye hesapları) korunur.
-- ============================================================

USE libris_db;

START TRANSACTION;

-- 1) Bağımlı tablolardan veri sil (çocuk → ebeveyn sırası)
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM wishlist_items;
DELETE FROM reservations;
DELETE FROM reviews;
DELETE FROM borrow_records;
DELETE FROM library_items;
SET FOREIGN_KEY_CHECKS = 1;

-- 2) AUTO_INCREMENT'i sıfırla (item_id 1'den başlasın)
ALTER TABLE library_items   AUTO_INCREMENT = 1;
ALTER TABLE borrow_records  AUTO_INCREMENT = 1;
ALTER TABLE reservations    AUTO_INCREMENT = 1;
ALTER TABLE reviews         AUTO_INCREMENT = 1;
ALTER TABLE wishlist_items  AUTO_INCREMENT = 1;

-- 3) Başlangıç ögelerini ekle
INSERT INTO library_items
    (title, author, publication_year, copy_count, available_copies, status, item_type, isbn, page_count, genre)
VALUES
    ('Java Programming', 'Deitel',                  2024, 5, 5, 'Available', 'BOOK', '123-456', 800, 'Education'),
    ('Araba Sevdası',    'Recaizade Mahmut Ekrem',  1875, 3, 3, 'Available', 'BOOK', '456-789', 276, 'Novel');

INSERT INTO library_items
    (title, author, publication_year, copy_count, available_copies, status, item_type, file_format, file_size)
VALUES
    ('Digital Trends', 'AI Expert', 2025, 1, 1, 'Available', 'EBOOK', 'PDF', 15.5);

INSERT INTO library_items
    (title, author, publication_year, copy_count, available_copies, status, item_type, duration, narrator)
VALUES
    ('Sapiens', 'Harari', 2014, 2, 2, 'Available', 'AUDIOBOOK', 900, 'John Smith');

INSERT INTO library_items
    (title, author, publication_year, copy_count, available_copies, status, item_type, issue_number, period)
VALUES
    ('Science Weekly', 'Global Science', 2026, 10, 10, 'Available', 'PERIODICAL', 45, 'Weekly');

COMMIT;

-- Sonuç kontrolü
SELECT '---- library_items reset SONRASI ----' AS info;
SELECT item_id, title, author, item_type, copy_count, available_copies, status
FROM library_items
ORDER BY item_id;
