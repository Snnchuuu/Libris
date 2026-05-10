package com.libris.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import com.libris.DatabaseManager;
import com.libris.Member;
import com.libris.ReservationDAO;
import com.libris.UserDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * MyReservationsView — Kullanıcının yaptığı (PENDING / FULFILLED / CANCELLED) tüm rezervasyonları.
 * Aktif (PENDING) olanlar üstte ve iptal edilebilir.
 */
@Route("my-reservations")
@PageTitle("Rezervasyonlarım | Libris")
public class MyReservationsView extends VerticalLayout implements BeforeEnterObserver {

    // Schema'da request_date zaten DATE (saatsiz) — sadece tarih gösteriyoruz.
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /** UI DTO. */
    public static class ReservationRow {
        public final int reservationId;
        public final String title;
        public final LocalDateTime requestDate;
        public final String status;

        public ReservationRow(int reservationId, String title, LocalDateTime requestDate, String status) {
            this.reservationId = reservationId;
            this.title = title;
            this.requestDate = requestDate;
            this.status = status;
        }

        public int getReservationId() { return reservationId; }
        public String getTitle()     { return title; }
        public LocalDateTime getRequestDate() { return requestDate; }
        public String getStatus()    { return status; }
        public boolean isPending()   { return "PENDING".equalsIgnoreCase(status); }
    }

    private final Grid<ReservationRow> activeGrid  = new Grid<>(ReservationRow.class, false);
    private final Grid<ReservationRow> historyGrid = new Grid<>(ReservationRow.class, false);

    private final UserDAO        userDao        = new UserDAO();
    private final ReservationDAO reservationDao = new ReservationDAO();

    private Member currentMember;

    public MyReservationsView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        Button backBtn = new Button("Kataloğa Dön", VaadinIcon.ARROW_LEFT.create());
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.addClickListener(e -> UI.getCurrent().navigate("katalog"));

        H2 pageHeader = new H2("Rezervasyonlarım");
        HorizontalLayout topBar = new HorizontalLayout(backBtn, pageHeader);
        topBar.setAlignItems(Alignment.CENTER);

        // ---- AKTİF (PENDING) ----
        H2 activeHeader = new H2("Bekleyen Rezervasyonlar");
        activeGrid.addColumn(ReservationRow::getTitle).setHeader("Kitap").setAutoWidth(true);
        activeGrid.addColumn(r -> r.getRequestDate() != null ? r.getRequestDate().format(FMT) : "—")
                  .setHeader("Talep Tarihi").setAutoWidth(true);
        activeGrid.addComponentColumn(r -> {
            Button cancelBtn = new Button("İptal Et", VaadinIcon.CLOSE.create());
            cancelBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            cancelBtn.addClickListener(e -> {
                boolean ok = reservationDao.cancelReservation(r.getReservationId());
                if (ok) {
                    Notification.show("Rezervasyon iptal edildi: " + r.getTitle(),
                        2500, Notification.Position.TOP_CENTER);
                    loadData();
                } else {
                    Notification.show("İptal başarısız.", 2500, Notification.Position.MIDDLE);
                }
            });
            return cancelBtn;
        }).setHeader("İşlem").setAutoWidth(true).setFlexGrow(0);
        activeGrid.setAllRowsVisible(true);

        // ---- GEÇMİŞ ----
        H2 historyHeader = new H2("Geçmiş");
        historyGrid.addColumn(ReservationRow::getTitle).setHeader("Kitap").setAutoWidth(true);
        historyGrid.addColumn(r -> r.getRequestDate() != null ? r.getRequestDate().format(FMT) : "—")
                   .setHeader("Talep Tarihi").setAutoWidth(true);
        historyGrid.addColumn(ReservationRow::getStatus).setHeader("Durum").setAutoWidth(true);
        historyGrid.setAllRowsVisible(true);

        add(topBar, activeHeader, activeGrid, historyHeader, historyGrid);
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

    /** Tüm (aktif + geçmiş) rezervasyonları çek. */
    private void loadData() {
        List<ReservationRow> active  = new ArrayList<>();
        List<ReservationRow> history = new ArrayList<>();

        String sql =
            "SELECT r.reservation_id, i.title, r.request_date, r.status " +
            "FROM reservations r " +
            "JOIN library_items i ON r.item_id = i.item_id " +
            "WHERE r.user_id = ? " +
            "ORDER BY r.request_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentMember.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("request_date");
                    LocalDateTime ldt = ts != null ? ts.toLocalDateTime() : null;
                    ReservationRow row = new ReservationRow(
                        rs.getInt("reservation_id"),
                        rs.getString("title"),
                        ldt,
                        rs.getString("status")
                    );
                    if (row.isPending()) active.add(row); else history.add(row);
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
}
