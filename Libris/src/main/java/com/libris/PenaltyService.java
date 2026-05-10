package com.libris;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * PenaltyService — Gecikme cezası hesaplamalarının tek merkezi.
 *
 * Test modu için ödünç süresi 3 dakika olduğundan ceza oranı dakika bazında.
 * Gerçek ortama geçtiğinde {@link #FINE_PER_UNIT} ve {@link #UNIT_LABEL} değerlerini
 * gün bazına çevirmek yeterli (örn. 5 TL/gün).
 */
public final class PenaltyService {

    /** Dakika başına ceza miktarı (TL). */
    public static final double FINE_PER_UNIT = 1.00;

    /** İnsan-okur ifade için: "dakika" / "gün" vb. */
    public static final String UNIT_LABEL = "dakika";

    /** Para birimi etiketi. */
    public static final String CURRENCY = "TL";

    private PenaltyService() {}

    /**
     * Verilen vade tarihi ile iade tarihi arasındaki gecikmeye göre cezayı hesaplar.
     * İade vadesinde veya öncesinde ise 0 döner.
     */
    public static double computeFine(LocalDateTime dueDate, LocalDateTime returnDate) {
        long units = unitsLate(dueDate, returnDate);
        return units * FINE_PER_UNIT;
    }

    /** Aktif (henüz iade edilmemiş) bir ödünç için şu ana kadarki potansiyel cezayı döner. */
    public static double computeCurrentFine(LocalDateTime dueDate) {
        return computeFine(dueDate, LocalDateTime.now());
    }

    /**
     * Kaç birim (dakika) gecikmiş — hesaplama için.
     * Yukarı yuvarlama: 1 saniye bile gecikse 1 dakika sayılır,
     * 61 saniye → 2 dakika. Demo için cömert/anlaşılır davranır.
     */
    public static long unitsLate(LocalDateTime dueDate, LocalDateTime returnOrNow) {
        if (dueDate == null || returnOrNow == null) return 0;
        if (!returnOrNow.isAfter(dueDate)) return 0;
        long seconds = Duration.between(dueDate, returnOrNow).getSeconds();
        if (seconds <= 0) return 0;
        // Ceiling division: her başlanmış dakika 1 birim sayılır
        return (seconds + 59) / 60;
    }

    /** Tutarı kullanıcıya göstermek için: "5.00 TL". */
    public static String formatAmount(double amount) {
        return String.format("%.2f %s", amount, CURRENCY);
    }
}
