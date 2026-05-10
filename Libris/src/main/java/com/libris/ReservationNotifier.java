package com.libris;

import com.libris.ReservationDAO.PendingReserver;
import java.util.List;

/**
 * Rezervasyon bildirim yardımcısı.
 * Bir kitap stokta tekrar mevcut olduğunda PENDING rezervasyon sahiplerinin
 * hepsine "kitap müsait" e-postası göndermek için kullanılır.
 *
 * View'lerde tekrar tekrar aynı kodu yazmamak için statik helper olarak duruyor.
 */
public class ReservationNotifier {

    private static final ReservationDAO RESERVATION_DAO = new ReservationDAO();

    /**
     * itemId'si verilen kitap için PENDING rezervasyon sahibi tüm kullanıcılara
     * "rezerve ettiğiniz kitap stokta" maili gönderir.
     *
     * @param itemId    kitabın id'si
     * @param bookTitle e-postada gösterilecek kitap adı
     * @return mail gönderilen kişi sayısı
     */
    public static int notifyAllPending(int itemId, String bookTitle) {
        List<PendingReserver> reservers = RESERVATION_DAO.getPendingReservers(itemId);
        int sent = 0;
        for (PendingReserver r : reservers) {
            if (r.email == null || r.email.isBlank()) continue;
            try {
                EmailService.sendReservationAvailable(r.email, r.name, bookTitle);
                sent++;
            } catch (Exception e) {
                System.err.println("[ReservationNotifier] Mail failed for "
                    + r.email + ": " + e.getMessage());
            }
        }
        if (!reservers.isEmpty()) {
            System.out.println("[ReservationNotifier] item=" + itemId
                + " '" + bookTitle + "' — pending=" + reservers.size()
                + ", emailed=" + sent);
        }
        return sent;
    }
}
