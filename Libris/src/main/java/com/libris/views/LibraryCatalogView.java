package com.libris.views;
 
import com.libris.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.server.VaadinSession;
import java.util.List;
 
// Catalog page — shows all library items, handles borrow/add/delete
 
@Route("katalog")
public class LibraryCatalogView extends VerticalLayout implements BeforeEnterObserver {
 
    // LibraryManager handles all database communication via DAOs
    private LibraryManager manager = new LibraryManager();
 
    // Item list loaded fresh from the database each time (not static!)
    private List<LibraryItem> items;
 
    public LibraryCatalogView() {
        setSizeFull();
 
        // Get session info
        String role       = (String) VaadinSession.getCurrent().getAttribute("role");
        String activeUser = (String) VaadinSession.getCurrent().getAttribute("username");
        Integer userId    = (Integer) VaadinSession.getCurrent().getAttribute("userId");
        boolean isAdmin   = "ADMIN".equals(role);
 
        // Load items fresh from the database — not static, not shared between users
        items = manager.getAllItems();
 
        // Grid setup
        Grid<LibraryItem> grid = new Grid<>(LibraryItem.class, false);
        grid.addColumn(LibraryItem::getTitle).setHeader("Kitap Adı");
        grid.addColumn(LibraryItem::getAuthor).setHeader("Yazar");
        grid.addColumn(LibraryItem::getPublicationYear).setHeader("Yayın Yılı");
        grid.addColumn(LibraryItem::getStatus).setHeader("Durum");
        grid.addColumn(item -> item.getClass().getSimpleName()).setHeader("Tür");
        grid.setItems(items);
        grid.setSizeFull();
 
        // Header with welcome message and logout button
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
 
        H3 welcomeText = new H3("Kütüphane Sistemi - Hoş geldin, " + (activeUser != null ? activeUser : "Misafir"));
 
        Button logoutBtn = new Button("Güvenli Çıkış", VaadinIcon.SIGN_OUT.create());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        logoutBtn.addClickListener(e -> {
            // Clear session and go back to login
            VaadinSession.getCurrent().getSession().invalidate();
            UI.getCurrent().navigate("login");
        });
 
        header.add(welcomeText);
        header.expand(welcomeText);
        header.add(logoutBtn);
        add(header);
 
        // --- ADMIN TOOLBAR ---
        // Only shown when the logged-in user has ADMIN role
        if (isAdmin) {
            Button addBtn    = new Button("Yeni Kitap Ekle", VaadinIcon.PLUS.create());
            Button deleteBtn = new Button("Seçileni Sil", VaadinIcon.TRASH.create());
 
            addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
 
            HorizontalLayout adminActions = new HorizontalLayout(addBtn, deleteBtn);
            add(adminActions);
 
            // Delete selected item from database
            deleteBtn.addClickListener(e -> {
                grid.asSingleSelect().getOptionalValue().ifPresent(selectedItem -> {
                    manager.deleteItem(selectedItem.getId()); // Delete from database
                    items = manager.getAllItems();            // Refresh from database
                    grid.setItems(items);
                    Notification.show(selectedItem.getTitle() + " sistemden kaldırıldı.");
                });
                if (grid.asSingleSelect().isEmpty()) {
                    Notification.show("Lütfen silmek istediğiniz kitabı tablodan seçin!");
                }
            });
 
            // Add new book via dialog
            addBtn.addClickListener(e -> {
                Dialog dialog = new Dialog();
                dialog.setHeaderTitle("Yeni Kitap Ekle");
 
                TextField titleIn = new TextField("Kitap Adı");
                TextField authIn  = new TextField("Yazar");
                TextField yearIn  = new TextField("Yayın Yılı");
                TextField isbnIn  = new TextField("ISBN");
 
                dialog.add(new VerticalLayout(titleIn, authIn, yearIn, isbnIn));
 
                Button saveBtn = new Button("Kaydet", saveEvt -> {
                    try {
                        int year = Integer.parseInt(yearIn.getValue());
 
                        // Create a temporary Book object to pass to the DAO
                        Book newBook = new Book(
                            0,                      // ID will be assigned by the database
                            titleIn.getValue(),
                            authIn.getValue(),
                            year,
                            1,
                            "Available",
                            isbnIn.getValue(),
                            100,
                            "General"
                        );
 
                        manager.addItem(newBook);       // Save to database
                        items = manager.getAllItems();   // Refresh list from database
                        grid.setItems(items);
                        dialog.close();
                        Notification.show("Yeni kitap başarıyla eklendi!");
 
                    } catch (Exception ex) {
                        Notification.show("Hata: Geçersiz veri girişi!");
                    }
                });
 
                saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                dialog.getFooter().add(new Button("İptal", i -> dialog.close()), saveBtn);
                dialog.open();
            });
        }
 
        // Search bar
        TextField searchField = new TextField("Katalogda Ara...");
        searchField.setPlaceholder("Kitap adı veya yazar...");
        searchField.setWidthFull();
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
 
        searchField.addValueChangeListener(event -> {
            String filtre = event.getValue().toLowerCase();
            grid.setItems(items.stream()
                .filter(item ->
                    item.getTitle().toLowerCase().contains(filtre) ||
                    item.getAuthor().toLowerCase().contains(filtre)
                )
                .toList());
        });
 
        // --- BORROW BUTTONS (only for members) ---
        if (!isAdmin) {
            grid.addComponentColumn(item -> {
                Button borrowBtn = new Button("Ödünç Al");
 
                // Disable button for digital items
                if (item instanceof EBook || item instanceof AudioBook) {
                    borrowBtn.setText("Dijital Ürün");
                    borrowBtn.setEnabled(false);
                } else if (item.getCopyCount() <= 0) {
                    // No copies available
                    borrowBtn.setText("Stokta Yok");
                    borrowBtn.setEnabled(false);
                    borrowBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
                }
 
                borrowBtn.addClickListener(e -> {
                    if (userId == null) {
                        Notification.show("Oturum hatası, lütfen tekrar giriş yapın!");
                        return;
                    }
 
                    // Check if already borrowed by this user
                    BorrowDAO borrowDAO = new BorrowDAO();
                    if (borrowDAO.isItemBorrowedByUser(userId, item.getId())) {
                        Notification.show("Bu kitabı zaten ödünç aldınız!");
                        return;
                    }
 
                    // Save borrow to database
                    boolean success = borrowDAO.borrowItem(userId, item.getId());
 
                    if (success) {
                        Notification.show(item.getTitle() + " başarıyla ödünç alındı!", 3000, Notification.Position.TOP_CENTER);
                        // Refresh list from database to show updated stock
                        items = manager.getAllItems();
                        grid.setItems(items);
                    } else {
                        Notification.show("Ödünç alma işlemi başarısız!");
                    }
                });
 
                return borrowBtn;
            }).setHeader("İşlemler");
        }
 
        add(searchField, grid);
    }
 
    // Redirect to login if not logged in
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Object role = VaadinSession.getCurrent().getAttribute("role");
        if (role == null) {
            event.rerouteTo(LoginView.class);
            Notification.show("Lütfen önce giriş yapın!", 3000, Notification.Position.MIDDLE);
        }
    }
}