-- ============================================================
-- TEŞHİS: library_items'da copy_count vs available_copies vs
--         gerçek aktif ödünç sayısı (borrow_records) tutarlılığı
-- ============================================================

USE libris_db;

-- Tüm kitaplar için: stoktaki vs gerçekte ödünçte olan
SELECT
    i.item_id,
    i.title,
    i.copy_count           AS stored_total,
    i.available_copies     AS stored_available,
    COALESCE(br.borrowed_count, 0) AS actually_borrowed,
    (i.copy_count - COALESCE(br.borrowed_count, 0)) AS expected_available,
    CASE
        WHEN i.available_copies = (i.copy_count - COALESCE(br.borrowed_count, 0))
        THEN 'OK'
        ELSE 'MISMATCH'
    END AS state,
    i.status
FROM library_items i
LEFT JOIN (
    SELECT item_id, COUNT(*) AS borrowed_count
    FROM borrow_records
    WHERE status = 'BORROWED' AND return_date IS NULL
    GROUP BY item_id
) br ON i.item_id = br.item_id
ORDER BY state DESC, i.item_id;

-- Sadece bozuk olanlar
SELECT '---- SADECE BOZUK SATIRLAR ----' AS info;
SELECT
    i.item_id,
    i.title,
    i.copy_count           AS stored_total,
    i.available_copies     AS stored_available,
    COALESCE(br.borrowed_count, 0) AS actually_borrowed,
    (i.copy_count - COALESCE(br.borrowed_count, 0)) AS expected_available
FROM library_items i
LEFT JOIN (
    SELECT item_id, COUNT(*) AS borrowed_count
    FROM borrow_records
    WHERE status = 'BORROWED' AND return_date IS NULL
    GROUP BY item_id
) br ON i.item_id = br.item_id
WHERE i.available_copies <> (i.copy_count - COALESCE(br.borrowed_count, 0));
