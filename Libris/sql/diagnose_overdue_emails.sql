-- ============================================================
-- TEŞHİS: Geç-iade e-postası neden gitmiyor?
-- ============================================================

USE libris_db;

-- 1) reminder_sent kolonu eklenmiş mi?
SHOW COLUMNS FROM borrow_records LIKE 'reminder_sent';

-- 2) Şu an "süresi dolmuş ve aktif" görünen tüm kayıtlar
SELECT
    br.record_id,
    u.user_id, u.username, u.email, u.name,
    i.title,
    br.borrow_date, br.due_date, br.return_date,
    br.status,
    COALESCE(br.reminder_sent, -1) AS reminder_sent,   -- -1 = kolon yoksa
    NOW() AS db_now,
    TIMESTAMPDIFF(SECOND, br.due_date, NOW()) AS overdue_seconds
FROM borrow_records br
JOIN users u           ON br.user_id = u.user_id
JOIN library_items i   ON br.item_id = i.item_id
WHERE br.status = 'BORROWED'
  AND br.return_date IS NULL
  AND br.due_date <= NOW()
ORDER BY br.due_date;

-- 3) Asıl scheduler sorgusu — bunlar mail için aday
SELECT
    br.record_id, u.email, u.name, i.title, br.due_date
FROM borrow_records br
JOIN users u           ON br.user_id = u.user_id
JOIN library_items i   ON br.item_id = i.item_id
WHERE br.status = 'BORROWED'
  AND br.return_date IS NULL
  AND br.due_date <= NOW()
  AND br.reminder_sent = 0;

-- 4) RESET — eğer hiçbiri çıkmıyorsa ama (2) dolu, demek ki reminder_sent zaten 1.
--    Test için sıfırlamak istersen aşağıdaki UPDATE'i ÇALIŞTIR (yorum kaldır):
-- UPDATE borrow_records
-- SET reminder_sent = 0
-- WHERE status = 'BORROWED' AND return_date IS NULL;
