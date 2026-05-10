-- ============================================================
-- TEŞHİS: Geçmiş iade kayıtları nasıl saklanmış?
-- ============================================================
-- Hangi iadelerin saat bilgisi var, hangileri 00:00:00 olarak yazılı,
-- ceza miktarları ne, kullanıcı bakiyesi ne — hepsini gösterir.
-- ============================================================

USE libris_db;

-- 1) Senin (veya tüm üyelerin) son 20 iade kaydı — saat detayıyla
SELECT
    br.record_id,
    u.username,
    i.title,
    br.borrow_date,
    br.due_date,
    br.return_date,
    br.status,
    br.fine_amount,
    -- iade saati 00:00:00 mi? → eski kayıt belirtisi
    CASE
        WHEN br.return_date IS NULL THEN 'AKTIF'
        WHEN TIME(br.return_date) = '00:00:00' THEN 'ESKI (saatsiz)'
        ELSE 'YENI (saatli)'
    END AS kayit_tipi,
    -- gerçek gecikme saniyesi
    CASE
        WHEN br.return_date IS NULL THEN NULL
        ELSE TIMESTAMPDIFF(SECOND, br.due_date, br.return_date)
    END AS gecikme_saniye
FROM borrow_records br
JOIN users u ON br.user_id = u.user_id
JOIN library_items i ON br.item_id = i.item_id
ORDER BY br.borrow_date DESC
LIMIT 20;

-- 2) Kullanıcı bakiyeleri ve gecikme sayıları
SELECT '---- KULLANICI BAKİYELERİ ----' AS info;
SELECT user_id, username, name, email, balance, total_delays
FROM users
ORDER BY user_id;

-- 3) borrow_records'taki sütunların tipi (return_date DATETIME mi gerçekten?)
SELECT '---- borrow_records SÜTUN TİPLERİ ----' AS info;
SHOW COLUMNS FROM borrow_records;
