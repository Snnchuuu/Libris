-- ============================================================
-- TEŞHİS: Araba Sevdası kitabıyla ilgili durumu görelim.
-- Hiçbir şeyi DEĞİŞTİRMEZ, sadece okur. Önce bunu çalıştır.
-- ============================================================

USE libris_db;

-- 1) Kitabın kendi durumu (library_items)
SELECT item_id, title, copy_count, available_copies, status
FROM library_items
WHERE title = 'Araba Sevdası';

-- 2) Bu kitapla ilgili TÜM ödünç kayıtları (kim, ne zaman, status)
SELECT
    br.record_id,
    u.user_id,
    u.username,
    u.name,
    u.email,
    br.borrow_date,
    br.due_date,
    br.return_date,
    br.status
FROM borrow_records br
JOIN users u           ON br.user_id = u.user_id
JOIN library_items i   ON br.item_id = i.item_id
WHERE i.title = 'Araba Sevdası'
ORDER BY br.borrow_date DESC;

-- 3) Sadece HÂLÂ AKTİF görünen (BORROWED ve return_date NULL) kayıtlar
SELECT
    br.record_id,
    u.username,
    u.email,
    br.borrow_date,
    br.due_date
FROM borrow_records br
JOIN users u           ON br.user_id = u.user_id
JOIN library_items i   ON br.item_id = i.item_id
WHERE i.title = 'Araba Sevdası'
  AND br.status = 'BORROWED'
  AND br.return_date IS NULL;

-- 4) Aslında orphan/geçersiz kayıt var mı? (user_id veya item_id bozuk)
SELECT br.*
FROM borrow_records br
LEFT JOIN users u         ON br.user_id = u.user_id
LEFT JOIN library_items i ON br.item_id = i.item_id
WHERE u.user_id IS NULL OR i.item_id IS NULL;
