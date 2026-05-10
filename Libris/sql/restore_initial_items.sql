-- ============================================================
-- BAŞLANGIÇ ÖGELERİNİ GERİ GETİRME (NAZİK)
-- ============================================================
-- Mevcut verilere DOKUNMAZ — sadece silinmiş başlangıç kitapları
-- ekler. Title + author kombinasyonu zaten varsa atlar (idempotent).
-- ============================================================

USE libris_db;

-- 1) Java Programming
INSERT INTO library_items
    (title, author, publication_year, copy_count, available_copies, status, item_type, isbn, page_count, genre)
SELECT 'Java Programming', 'Deitel', 2024, 5, 5, 'Available', 'BOOK', '123-456', 800, 'Education'
WHERE NOT EXISTS (
    SELECT 1 FROM library_items WHERE title = 'Java Programming' AND author = 'Deitel'
);

-- 2) Araba Sevdası
INSERT INTO library_items
    (title, author, publication_year, copy_count, available_copies, status, item_type, isbn, page_count, genre)
SELECT 'Araba Sevdası', 'Recaizade Mahmut Ekrem', 1875, 3, 3, 'Available', 'BOOK', '456-789', 276, 'Novel'
WHERE NOT EXISTS (
    SELECT 1 FROM library_items WHERE title = 'Araba Sevdası' AND author = 'Recaizade Mahmut Ekrem'
);

-- 3) Digital Trends (E-Kitap)
INSERT INTO library_items
    (title, author, publication_year, copy_count, available_copies, status, item_type, file_format, file_size)
SELECT 'Digital Trends', 'AI Expert', 2025, 1, 1, 'Available', 'EBOOK', 'PDF', 15.5
WHERE NOT EXISTS (
    SELECT 1 FROM library_items WHERE title = 'Digital Trends' AND author = 'AI Expert'
);

-- 4) Sapiens (Sesli Kitap)
INSERT INTO library_items
    (title, author, publication_year, copy_count, available_copies, status, item_type, duration, narrator)
SELECT 'Sapiens', 'Harari', 2014, 2, 2, 'Available', 'AUDIOBOOK', 900, 'John Smith'
WHERE NOT EXISTS (
    SELECT 1 FROM library_items WHERE title = 'Sapiens' AND author = 'Harari'
);

-- 5) Science Weekly (Süreli Yayın)
INSERT INTO library_items
    (title, author, publication_year, copy_count, available_copies, status, item_type, issue_number, period)
SELECT 'Science Weekly', 'Global Science', 2026, 10, 10, 'Available', 'PERIODICAL', 45, 'Weekly'
WHERE NOT EXISTS (
    SELECT 1 FROM library_items WHERE title = 'Science Weekly' AND author = 'Global Science'
);

-- Sonuç kontrolü
SELECT '---- library_items mevcut durum ----' AS info;
SELECT item_id, title, author, item_type, copy_count, available_copies, status
FROM library_items
ORDER BY item_id;
