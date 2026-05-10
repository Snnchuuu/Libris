-- ============================================================
-- TEMİZLEME: Araba Sevdası kitabı için aktif görünen tüm
-- ödünç kayıtlarını "iade edildi" olarak işaretler ve kitabın
-- mevcut sayısını / status'unu sıfırlar.
--
-- ÖNCE diagnose_araba_sevdasi.sql ile durumu gör, ondan sonra
-- bu dosyayı çalıştır.
--
-- Transaction içinde sarılı: hata olursa hiçbir şey değişmez.
-- ============================================================

USE libris_db;

START TRANSACTION;

-- 1) Araba Sevdası'na ait AKTİF kayıtları RETURNED'a çek.
--    return_date'i şu an yapıyoruz, fine'ı 0'a çekiyoruz (kim olduğu belli değil, ceza vermiyoruz).
UPDATE borrow_records br
JOIN library_items i ON br.item_id = i.item_id
SET
    br.status      = 'RETURNED',
    br.return_date = NOW(),
    br.fine_amount = 0
WHERE i.title = 'Araba Sevdası'
  AND br.status = 'BORROWED'
  AND br.return_date IS NULL;

-- 2) Kitabın stok durumunu yeniden hesapla:
--    available_copies = copy_count - (hâlâ aktif olan kayıt sayısı)
UPDATE library_items i
SET
    i.available_copies = i.copy_count - (
        SELECT COUNT(*)
        FROM borrow_records br
        WHERE br.item_id = i.item_id
          AND br.status = 'BORROWED'
          AND br.return_date IS NULL
    ),
    i.status = CASE
        WHEN i.copy_count - (
            SELECT COUNT(*)
            FROM borrow_records br
            WHERE br.item_id = i.item_id
              AND br.status = 'BORROWED'
              AND br.return_date IS NULL
        ) > 0 THEN 'Available'
        ELSE 'Unavailable'
    END
WHERE i.title = 'Araba Sevdası';

-- 3) Doğrulama — sonuç beklenenle eşleşiyor mu? (auto-commit'ten ÖNCE göster)
SELECT 'AFTER UPDATE - library_items' AS info;
SELECT item_id, title, copy_count, available_copies, status
FROM library_items
WHERE title = 'Araba Sevdası';

SELECT 'AFTER UPDATE - aktif kayıt kalmış mı?' AS info;
SELECT COUNT(*) AS still_active
FROM borrow_records br
JOIN library_items i ON br.item_id = i.item_id
WHERE i.title = 'Araba Sevdası'
  AND br.status = 'BORROWED'
  AND br.return_date IS NULL;

-- Sonuçları gördükten sonra COMMIT et.
-- Eğer beklenmedik bir şey varsa COMMIT yerine ROLLBACK yaz.
COMMIT;
-- ROLLBACK;
