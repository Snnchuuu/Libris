package com.libris.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import com.libris.BookDAO;
import com.libris.BorrowDAO;
import com.libris.DatabaseManager;
import com.libris.EmailService;
import com.libris.Member;
import com.libris.PenaltyService;
import com.libris.ReviewDAO;
import com.libris.UserDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * MyBorrowedView — Kullanıcının kendi ödünç aldığı kitapları gösterir.
 *
 *   • Üstte: AKTİF ödünçler (status = BORROWED) — canlı kalan süre + "İade Et" butonu
 *   • Altta: GEÇMİŞ (status = RETURNED) — iade edilmiş eski kayıtlar
 *
 * İade edince:
 *   - Onay e-postası gönderilir
 *   - "Kitabı değerlendir" diyaloğu açılır (yıldız + yorum)
 */
@Route("my-borrows")
@PageTitle("Ödünç Aldıklarım | Libris")
public class MyBorrowedView extends VerticalLayout implements BeforeEnterObserver {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /** UI için DTO. */
    public static class BorrowView {
        private final int itemId;
        private final String title;
        private final LocalDateTime borrowDate;
        private final LocalDateTime dueDate;
        private final LocalDateTime returnDate; // null ise henüz iade edilmemiş
        private final String status;
        private final double fineAmount; // geçmiş kayıtlar için kalıcı ceza

        public BorrowView(int itemId, String title,
                          LocalDateTime borrowDate, LocalDateTime dueDate,
                          LocalDateTime returnDate, String status, double fineAmount) {
            this.itemId = itemId;
            this.title = title;
            this.borrowDate = borrowDate;
            this.dueDate = dueDate;
            this.returnDate = returnDate;
            this.status = status;
            this.fineAmount = fineAmount;
        }

        public int getItemId() { return itemId; }
        public String getTitle() { return title; }
        public LocalDateTime getBorrowDate() { return borrowDate; }
        public LocalDateTime getDueDate() { return dueDate; }
        public LocalDateTime getReturnDate() { return returnDate; }
        public String getStatus() { return status; }
        public double getFineAmount() { return fineAmount; }

        /** Aktif kayıt için anlık potansiyel ceza (canlı sayaçla güncellenir). */
        public String getCurrentFineDisplay() {
            if (!isActive() || dueDate == null) return "—";
            double f = PenaltyService.computeCurrentFine(dueDate);
            return f > 0 ? PenaltyService.formatAmount(f) : "—";
        }

        public boolean isActive() {
            return "BORROWED".equalsIgnoreCase(status) && returnDate == null;
        }

        public boolean isOverdue() {
            return isActive() && dueDate != null && LocalDateTime.now().isAfter(dueDate);
        }

        public String getRemainingTime() {
            if (!isActive() || dueDate == null) return "—";
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(dueDate)) {
                Duration overdue = Duration.between(dueDate, now);
                return "GECİKTİ — " + format(overdue);
            }
            return format(Duration.between(now, dueDate));
        }

        private String format(Duration d) {
            long days    = d.toDays();
            long hours   = d.toHoursPart();
            long minutes = d.toMinutesPart();
            long seconds = d.toSecondsPart();
            if (days > 0)  return String.format("%dg %02ds %02dd", days, hours, minutes);
            if (hours > 0) return String.format("%02ds %02dd %02dsn", hours, minutes, seconds);
            return String.format("%02dd %02dsn", minutes, seconds);
        }
    }

    private final Grid<BorrowView> activeGrid  = new Grid<>(BorrowView.class, false);
    private final Grid<BorrowView> historyGrid = new Grid<>(BorrowView.class, false);

    private final BorrowDAO       borrowDao      = new BorrowDAO();
    private final BookDAO         bookDao        = new BookDAO();
    private final UserDAO         userDao        = new UserDAO();
    private final ReviewDAO       reviewDao      = new ReviewDAO();

    private String currentUsername;
    private Member currentMember;

    public MyBorrowedView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // ---- ÜST BAR — geri dönüş & başlık ----
        Button backBtn = new Button("Kataloğa Dön", VaadinIcon.ARROW_LEFT.create());
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.addClickListener(e -> UI.getCurrent().navigate("katalog"));

        H2 pageHeader = new H2("Ödünç Aldıklarım");
        HorizontalLayout topBar = new HorizontalLayout(backBtn, pageHeader);
        topBar.setAlignItems(Alignment.CENTER);
        topBar.setSpacing(true);

        // ---- AKTİF ÖDÜNÇLER ----
        H2 activeHeader = new H2("Aktif Ödünçler");
        activeGrid.addColumn(BorrowView::getTitle).setHeader("Kitap").setAutoWidth(true);
        activeGrid.addColumn(b -> b.getBorrowDate().format(DATE_FMT)).setHeader("Alınma").setAutoWidth(true);
        activeGrid.addColumn(b -> b.getDueDate().format(DATE_FMT)).setHeader("Bitiş").setAutoWidth(true);
        activeGrid.addColumn(BorrowView::getRemainingTime).setHeader("Kalan Süre").setAutoWidth(true);
        activeGrid.addColumn(BorrowView::getCurrentFineDisplay)
                  .setHeader("Olası Ceza").setAutoWidth(true);

        activeGrid.addComponentColumn(b -> {
            Button returnBtn = new Button("İade Et", e -> handleReturn(b));
            returnBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            return returnBtn;
        }).setHeader("İşlem").setAutoWidth(true).setFlexGrow(0);

        activeGrid.setClassNameGenerator(b -> b.isOverdue() ? "overdue-row" : null);
        activeGrid.setAllRowsVisible(true);

        // ---- GEÇMİŞ ----
        H2 historyHeader = new H2("Geçmiş");
        historyGrid.addColumn(BorrowView::getTitle).setHeader("Kitap").setAutoWidth(true);
        historyGrid.addColumn(b -> b.getBorrowDate().format(DATE_FMT)).setHeader("Alınma").setAutoWidth(true);
        historyGrid.addColumn(b -> b.getDueDate().format(DATE_FMT)).setHeader("Bitiş").setAutoWidth(true);
        historyGrid.addColumn(b -> b.getReturnDate() != null ? b.getReturnDate().format(DATE_FMT) : "—")
                   .setHeader("İade Tarihi").setAutoWidth(true);
        historyGrid.addColumn(b -> b.getFineAmount() > 0
                ? PenaltyService.formatAmount(b.getFineAmount()) : "—")
                   .setHeader("Ceza").setAutoWidth(true);

        // Geçmişte bir kitap için tekrar değerlendirme yapabilsin
        historyGrid.addComponentColumn(b -> {
            Button reviewBtn = new Button("Değerlendir", VaadinIcon.STAR.create());
            reviewBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            reviewBtn.addClickListener(e -> openReviewDialog(b.getItemId(), b.getTitle()));
            return reviewBtn;
        }).setHeader("İnceleme").setAutoWidth(true).setFlexGrow(0);

        historyGrid.setAllRowsVisible(true);

        // Inline CSS — gecikmiş satırlar kırmızı
        getElement().executeJs(
            "if (!document.getElementById('libris-overdue-style')) {" +
            "  const s = document.createElement('style');" +
            "  s.id = 'libris-overdue-style';" +
            "  s.textContent = '" +
            "    vaadin-grid::part(row).overdue-row { background-color: #ffe5e5; color: #b00020; }" +
            "  ';" +
            "  document.head.appendChild(s);" +
            "}"
        );

        add(topBar, activeHeader, activeGrid, historyHeader, historyGrid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Object username = VaadinSession.getCurrent().getAttribute("username");
        if (username == null) {
            event.forwardTo("login");
            return;
        }
        currentUsername = (String) username;
        currentMember   = userDao.getMemberByUsername(currentUsername);
        loadData();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();
        ui.setPollInterval(1000);
        ui.addPollListener(e -> activeGrid.getDataProvider().refreshAll());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        UI ui = detachEvent.getUI();
        if (ui != null) ui.setPollInterval(-1);
        super.onDetach(detachEvent);
    }

    // ============================================================
    // İADE ETME
    // ============================================================
    private void handleReturn(BorrowView b) {
        if (currentUsername == null || currentMember == null) {
            Notification.show("Oturum bulunamadı.", 3000, Notification.Position.MIDDLE);
            return;
        }
        int userId = currentMember.getId();

        // İade + ceza hesaplama atomik
        BorrowDAO.ReturnResult result = borrowDao.returnAndPenalize(userId, b.getItemId());
        if (!result.success) {
            Notification.show("İade başarısız: " + b.getTitle(),
                              3000, Notification.Position.MIDDLE);
            return;
        }

        // Stoğu 1 arttır
        bookDao.updateStock(b.getItemId(), +1);

        // Bekleyen rezervasyon sahiplerine bildir
        int notified = com.libris.ReservationNotifier.notifyAllPending(
            b.getItemId(), b.getTitle());
        if (notified > 0) {
            Notification.show("📬 " + notified + " kişiye rezervasyon e-postası gönderildi.",
                              3000, Notification.Position.BOTTOM_END);
        }

        // Sonuç bildirimi — ceza varsa göster
        String msg;
        if (result.fineAmount > 0) {
            msg = "İade alındı: " + b.getTitle()
                + " — Gecikme cezası: " + PenaltyService.formatAmount(result.fineAmount)
                + " (Bakiye: " + PenaltyService.formatAmount(result.newBalance) + ")";
        } else {
            msg = "İade edildi: " + b.getTitle();
        }
        Notification.show(msg, 4500, Notification.Position.TOP_CENTER);

        // İade onay e-postası — ceza bilgisiyle
        sendReturnEmailQuiet(b.getTitle(), result.fineAmount, result.unitsLate);

        // Tabloyu yenile
        loadData();

        // Değerlendirme diyaloğunu aç
        openReviewDialog(b.getItemId(), b.getTitle());
    }

    private void sendReturnEmailQuiet(String title, double fineAmount, long unitsLate) {
        try {
            if (currentMember != null && currentMember.getEmail() != null) {
                EmailService.sendReturnConfirmation(
                    currentMember.getEmail(),
                    currentMember.getName(),
                    title,
                    LocalDateTime.now(),
                    fineAmount,
                    unitsLate
                );
            }
        } catch (Exception ignore) {
            // sessiz — mail başarısız olsa da kullanıcıya hata gösterme
        }
    }

    // ============================================================
    // DEĞERLENDİRME DİYALOĞU (review)
    // ============================================================
    private void openReviewDialog(int itemId, String title) {
        if (currentMember == null) return;

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Değerlendir: " + title);

        Select<Integer> ratingSelect = new Select<>();
        ratingSelect.setLabel("Puan (1-5)");
        ratingSelect.setItems(1, 2, 3, 4, 5);
        ratingSelect.setValue(5);

        TextArea commentArea = new TextArea("Yorum");
        commentArea.setWidthFull();
        commentArea.setMaxLength(500);
        commentArea.setHelperText("İsteğe bağlı");

        dialog.add(new VerticalLayout(
            new Span("Bu kitap hakkındaki düşüncelerin diğer üyeler için değerli."),
            ratingSelect,
            commentArea
        ));

        Button skipBtn = new Button("Atla", e -> dialog.close());
        Button saveBtn = new Button("Gönder", e -> {
            Integer rating = ratingSelect.getValue();
            String comment = commentArea.getValue();
            if (rating == null) {
                Notification.show("Lütfen bir puan seç.");
                return;
            }
            try {
                boolean ok = reviewDao.addReview(currentMember.getId(), itemId, rating,
                                                 comment == null ? "" : comment.trim());
                if (ok) {
                    Notification.show("Değerlendirmen kaydedildi. Teşekkürler!",
                                      3000, Notification.Position.TOP_CENTER);
                } else {
                    // Genelde UNIQUE ihlali (zaten daha önce yazılmış)
                    Notification.show("Bu kitap için zaten bir değerlendirmen var.",
                                      3000, Notification.Position.MIDDLE);
                }
            } catch (IllegalArgumentException iae) {
                Notification.show("Geçersiz puan: " + iae.getMessage());
            }
            dialog.close();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(skipBtn, saveBtn);
        dialog.open();
    }

    // ============================================================
    // VERİ
    // ============================================================
    private void loadData() {
        List<BorrowView> active  = new ArrayList<>();
        List<BorrowView> history = new ArrayList<>();

        String sql =
                "SELECT i.item_id, i.title, br.borrow_date, br.due_date, br.return_date, br.status, br.fine_amount " +
                "FROM borrow_records br " +
                "JOIN users u ON br.user_id = u.user_id " +
                "JOIN library_items i ON br.item_id = i.item_id " +
                "WHERE u.username = ? " +
                "ORDER BY br.borrow_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, currentUsername);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDateTime borrow = toLdt(rs.getTimestamp("borrow_date"));
                    LocalDateTime due    = toLdt(rs.getTimestamp("due_date"));
                    LocalDateTime ret    = toLdt(rs.getTimestamp("return_date"));
                    BorrowView bv = new BorrowView(
                            rs.getInt("item_id"),
                            rs.getString("title"),
                            borrow, due, ret,
                            rs.getString("status"),
                            rs.getDouble("fine_amount")
                    );
                    if (bv.isActive()) active.add(bv); else history.add(bv);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Veriler yüklenemedi: " + e.getMessage(),
                              4000, Notification.Position.MIDDLE);
        }

        activeGrid.setItems(active);
        historyGrid.setItems(history);
    }

    private static LocalDateTime toLdt(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
