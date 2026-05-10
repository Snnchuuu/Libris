package com.libris.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
import com.libris.ReservationDAO;
import com.libris.UserDAO;
import com.libris.WishListDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * MyWishlistView — Kullanıcının istek listesi.
 * Her satırda: kitap, durum, "Ödünç Al" / "Rezerve Et" / "Listeden Çıkar".
 *
 * NOT: Takım arkadaşının {@link WishListDAO} sınıfı sade bir API sunuyor (basit
 * ekleme/silme + formatted string list). Bu view UI için detay (itemId, stok)
 * gerektiğinden DB sorgusunu kendi içinde yapıyor.
 */
@Route("my-wishlist")
@PageTitle("İstek Listem | Libris")
public class MyWishlistView extends VerticalLayout implements BeforeEnterObserver {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // BorrowDAO'da loan period ile uyumlu olsun
    private static final int LOAN_PERIOD_MINUTES = 3;

    /** UI için zengin DTO — view kendi SQL query'siyle dolduruyor. */
    public static class WishRow {
        public final int itemId;
        public final String title;
        public final String author;
        public final int copyCount;
        public final int availableCopies;
        public final Timestamp addedDate;

        public WishRow(int itemId, String title, String author,
                       int copyCount, int availableCopies, Timestamp addedDate) {
            this.itemId = itemId;
            this.title = title;
            this.author = author;
            this.copyCount = copyCount;
            this.availableCopies = availableCopies;
            this.addedDate = addedDate;
        }

        public int       getItemId()         { return itemId; }
        public String    getTitle()          { return title; }
        public String    getAuthor()         { return author; }
        public int       getCopyCount()      { return copyCount; }
        public int       getAvailableCopies(){ return availableCopies; }
        public Timestamp getAddedDate()      { return addedDate; }
    }

    private final Grid<WishRow> grid = new Grid<>(WishRow.class, false);

    private final UserDAO        userDao        = new UserDAO();
    private final WishListDAO    wishListDao    = new WishListDAO();
    private final BorrowDAO      borrowDao      = new BorrowDAO();
    private final BookDAO        bookDao        = new BookDAO();
    private final ReservationDAO reservationDao = new ReservationDAO();

    private Member currentMember;

    /** Liste boşsa kullanıcıya gösterilen yardım metni. */
    private final Span emptyHint = new Span(
        "İstek listen boş — kataloğa dönüp kalp ikonuyla kitap ekleyebilirsin.");
    {
        emptyHint.getStyle().set("color", "#666").set("font-style", "italic");
    }

    public MyWishlistView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        Button backBtn = new Button("Kataloğa Dön", VaadinIcon.ARROW_LEFT.create());
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.addClickListener(e -> UI.getCurrent().navigate("katalog"));

        H2 pageHeader = new H2("İstek Listem");
        HorizontalLayout topBar = new HorizontalLayout(backBtn, pageHeader);
        topBar.setAlignItems(Alignment.CENTER);

        grid.addColumn(WishRow::getTitle).setHeader("Kitap").setAutoWidth(true);
        grid.addColumn(WishRow::getAuthor).setHeader("Yazar").setAutoWidth(true);
        grid.addColumn(w -> w.getAvailableCopies() > 0 ? "Mevcut" : "Stokta yok")
            .setHeader("Durum").setAutoWidth(true);
        grid.addColumn(w -> {
            Timestamp t = w.getAddedDate();
            return t != null ? t.toLocalDateTime().format(FMT) : "—";
        }).setHeader("Eklendi").setAutoWidth(true);

        grid.addComponentColumn(this::buildActionButton)
            .setHeader("İşlem").setAutoWidth(true).setFlexGrow(0);

        grid.addComponentColumn(w -> {
            Button removeBtn = new Button("Çıkar", VaadinIcon.TRASH.create());
            removeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR,
                                       ButtonVariant.LUMO_SMALL);
            removeBtn.addClickListener(e -> {
                if (wishListDao.removeItem(currentMember.getId(), w.getItemId())) {
                    Notification.show("Listeden çıkarıldı: " + w.getTitle(),
                                      2000, Notification.Position.BOTTOM_END);
                    loadData();
                }
            });
            return removeBtn;
        }).setHeader("Liste").setAutoWidth(true).setFlexGrow(0);

        grid.setAllRowsVisible(true);

        add(topBar, emptyHint, grid);
    }

    /** Satıra göre dinamik buton: stok varsa "Ödünç Al", yoksa "Rezerve Et". */
    private Button buildActionButton(WishRow w) {
        if (w.getAvailableCopies() > 0) {
            Button borrowBtn = new Button("Ödünç Al", VaadinIcon.BOOK.create());
            borrowBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            borrowBtn.addClickListener(e -> {
                if (borrowDao.isAlreadyBorrowed(currentMember.getId(), w.getItemId())) {
                    Notification.show("Bu kitabı zaten ödünç almışsın.",
                                      2500, Notification.Position.MIDDLE);
                    return;
                }
                boolean ok = borrowDao.borrowItem(currentMember.getId(), w.getItemId());
                if (ok) {
                    bookDao.updateStock(w.getItemId(), -1);
                    // Bu kitap için PENDING rezervasyonu varsa otomatik FULFILLED yap
                    reservationDao.fulfillByUserAndItem(currentMember.getId(), w.getItemId());
                    Notification.show("Ödünç alındı: " + w.getTitle(),
                                      2500, Notification.Position.TOP_CENTER);
                    try {
                        EmailService.sendBorrowConfirmation(
                            currentMember.getEmail(),
                            currentMember.getName(),
                            w.getTitle(),
                            LocalDateTime.now().plusMinutes(LOAN_PERIOD_MINUTES)
                        );
                    } catch (Exception ignore) {}
                    loadData();
                } else {
                    Notification.show("Ödünç alma başarısız.",
                                      2500, Notification.Position.MIDDLE);
                }
            });
            return borrowBtn;
        }

        Button reserveBtn = new Button("Rezerve Et", VaadinIcon.CLOCK.create());
        reserveBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
        reserveBtn.addClickListener(e -> {
            boolean ok = reservationDao.createReservation(currentMember.getId(), w.getItemId());
            if (ok) {
                Notification.show("Rezervasyon oluşturuldu: " + w.getTitle(),
                                  2500, Notification.Position.TOP_CENTER);
            } else {
                Notification.show("Rezervasyon başarısız.",
                                  2500, Notification.Position.MIDDLE);
            }
        });
        return reserveBtn;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Object username = VaadinSession.getCurrent().getAttribute("username");
        if (username == null) {
            event.forwardTo("login");
            return;
        }
        currentMember = userDao.getMemberByUsername((String) username);
        if (currentMember == null) {
            event.forwardTo("login");
            return;
        }
        loadData();
    }

    /**
     * Kullanıcının istek listesini detaylı olarak çeker.
     * WishListDAO basit bir API sunuyor; UI için kitap detayı + stok durumu lazım,
     * onu da burada doğrudan SQL ile alıyoruz.
     */
    private void loadData() {
        List<WishRow> rows = new ArrayList<>();
        String sql =
            "SELECT i.item_id, i.title, i.author, i.copy_count, i.available_copies, w.added_date " +
            "FROM wish_list w " +
            "JOIN library_items i ON w.item_id = i.item_id " +
            "WHERE w.user_id = ? " +
            "ORDER BY w.added_date DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentMember.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new WishRow(
                        rs.getInt("item_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("copy_count"),
                        rs.getInt("available_copies"),
                        rs.getTimestamp("added_date")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Notification.show("Liste yüklenemedi: " + e.getMessage(),
                              4000, Notification.Position.MIDDLE);
        }

        grid.setItems(rows);
        emptyHint.setVisible(rows.isEmpty());
    }
}
