package com.libris.views;
 
import com.libris.*;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.VaadinSession;
import java.util.List;
 
@Route("katalog")
public class LibraryCatalogView extends VerticalLayout implements BeforeEnterObserver {
 
    private LibraryManager manager = new LibraryManager();
    private BorrowDAO borrowDAO = new BorrowDAO();
    private List<LibraryItem> items;
 
    public LibraryCatalogView() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
 
        String role       = (String) VaadinSession.getCurrent().getAttribute("role");
        String activeUser = (String) VaadinSession.getCurrent().getAttribute("username");
        Integer userId    = (Integer) VaadinSession.getCurrent().getAttribute("userId");
        boolean isAdmin   = "ADMIN".equals(role);
 
        items = manager.getAllItems();
 
        // -------------------------------------------------------
        // HEADER — logo + welcome + logout
        // -------------------------------------------------------
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setPadding(true);
        header.getStyle()
        .set("background-color", "#E8DFC8")  // antik parşömen
        .set("border-bottom", "2px solid #7A5520")
        .set("padding", "10px 20px")
        .set("box-shadow", "0 2px 8px rgba(74,48,16,0.15)");
 
        // Logo — sol üst köşe
        Image logo = new Image("images/logo.png", "Libris");
        logo.setHeight("48px");
        logo.getStyle().set("cursor", "pointer");
 
        // Kullanıcı bilgisi
        Span userInfo = new Span("Hoş geldin, " + (activeUser != null ? activeUser : "Misafir")
                + (isAdmin ? " (Admin)" : ""));
        userInfo.getStyle()
        .set("color", "#6B4C11")  // logonun altın kahverengisi
        .set("font-family", "'Lato', sans-serif")
        .set("font-size", "0.95rem");
 
        Button logoutBtn = new Button("Çıkış", VaadinIcon.SIGN_OUT.create());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutBtn.getStyle()
        .set("color", "#6B4C11")
        .set("border", "1px solid #6B4C11");
        logoutBtn.addClickListener(e -> {
            VaadinSession.getCurrent().getSession().invalidate();
            UI.getCurrent().navigate("login");
        });
 
        header.add(logo, userInfo);
        header.expand(userInfo);
        header.add(logoutBtn);
        add(header);
 
        // -------------------------------------------------------
        // CONTENT AREA
        // -------------------------------------------------------
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.getStyle().set("padding", "20px");
 
        // -------------------------------------------------------
        // TAB BUTTONS (only for members)
        // -------------------------------------------------------
        VerticalLayout catalogSection  = new VerticalLayout();
        VerticalLayout borrowedSection = new VerticalLayout();
        borrowedSection.setVisible(false);
 
        if (!isAdmin) {
            Button catalogTab  = new Button("📚 Katalog");
            Button borrowedTab = new Button("📖 Ödünç Aldıklarım");
 
            catalogTab.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
 
            catalogTab.addClickListener(e -> {
                catalogSection.setVisible(true);
                borrowedSection.setVisible(false);
                catalogTab.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                borrowedTab.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            });
 
            borrowedTab.addClickListener(e -> {
                catalogSection.setVisible(false);
                borrowedSection.setVisible(true);
                borrowedTab.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                catalogTab.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                refreshBorrowedSection(borrowedSection, userId);
            });
 
            HorizontalLayout tabs = new HorizontalLayout(catalogTab, borrowedTab);
            tabs.getStyle().set("margin-bottom", "12px");
            content.add(tabs);
        }
 
        // -------------------------------------------------------
        // ADMIN TOOLBAR
        // -------------------------------------------------------
        if (isAdmin) {
            Button addBtn    = new Button("Yeni Kitap Ekle", VaadinIcon.PLUS.create());
            Button deleteBtn = new Button("Seçileni Sil", VaadinIcon.TRASH.create());
 
            addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
 
            HorizontalLayout adminActions = new HorizontalLayout(addBtn, deleteBtn);
            adminActions.getStyle().set("margin-bottom", "12px");
            content.add(adminActions);
 
            Grid<LibraryItem> catalogGrid = buildCatalogGrid(isAdmin, userId);
 
            deleteBtn.addClickListener(e -> {
    // 1. Seçili öğeyi en başta alıp var mı yok mu diye kontrol ediyoruz
    catalogGrid.asSingleSelect().getOptionalValue().ifPresentOrElse(selectedItem -> {
        // --- SEÇİM VARSA ÇALIŞACAK KISIM ---
        
        // Backend'den sil
        manager.deleteItem(selectedItem.getId());
        
        // Listeyi güncelle ve Grid'e tekrar bas
        items = manager.getAllItems();
        catalogGrid.setItems(items);
        
        // Başarı mesajı
        Notification.show(selectedItem.getTitle() + " sistemden kaldırıldı.", 3000, Notification.Position.TOP_END);
        
    }, () -> {
        // --- SEÇİM YOKSA ÇALIŞACAK KISIM ---
        Notification.show("Lütfen silmek istediğiniz kitabı tablodan seçin!", 3000, Notification.Position.MIDDLE);
    });
});
 
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
                        Book newBook = new Book(0, titleIn.getValue(), authIn.getValue(),
                                year, 1, "Available", isbnIn.getValue(), 100, "General");
                        manager.addItem(newBook);
                        items = manager.getAllItems();
                        catalogGrid.setItems(items);
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
 
            TextField searchField = buildSearchField(catalogGrid);
            content.add(searchField, catalogGrid);
 
        } else {
            Grid<LibraryItem> catalogGrid = buildCatalogGrid(isAdmin, userId);
            TextField searchField = buildSearchField(catalogGrid);
            catalogSection.add(new H4("📚 Kütüphane Kataloğu"), searchField, catalogGrid);
            content.add(catalogSection);
 
            refreshBorrowedSection(borrowedSection, userId);
            content.add(borrowedSection);
        }
 
        add(content);
    }
 
    private void refreshBorrowedSection(VerticalLayout section, Integer userId) {
        section.removeAll();
        section.add(new H4("📖 Ödünç Aldıklarım"));
 
        if (userId == null) {
            section.add(new Span("Oturum hatası!"));
            return;
        }
 
        List<String> borrowedItems = borrowDAO.getActiveBorrowsByUser(userId);
 
        if (borrowedItems.isEmpty()) {
            section.add(new Span("Şu an ödünç aldığınız kitap bulunmuyor."));
            return;
        }
 
        Grid<String> borrowedGrid = new Grid<>();
        borrowedGrid.setHeight("300px");
        borrowedGrid.addColumn(s -> s.split("\\|")[1].trim()).setHeader("Kitap Adı");
        borrowedGrid.addColumn(s -> s.split("\\|")[2].trim()).setHeader("İade Tarihi");
 
        borrowedGrid.addComponentColumn(record -> {
            Button returnBtn = new Button("İade Et", VaadinIcon.ARROW_BACKWARD.create());
            returnBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
 
            returnBtn.addClickListener(e -> {
                try {
                    String recordIdStr = record.split("\\|")[0].replace("Record #", "").trim();
                    int recordId = Integer.parseInt(recordIdStr);
 
                    BorrowDAO dao = new BorrowDAO();
                    boolean success = dao.returnItemSimple(recordId);
 
                    if (success) {
                        Notification.show("Kitap başarıyla iade edildi!", 3000, Notification.Position.TOP_CENTER);
                        refreshBorrowedSection(section, userId);
                    } else {
                        Notification.show("İade işlemi başarısız!");
                    }
                } catch (Exception ex) {
                    Notification.show("Hata: " + ex.getMessage());
                }
            });
 
            return returnBtn;
        }).setHeader("İşlem");
 
        borrowedGrid.setItems(borrowedItems);
        section.add(borrowedGrid);
    }
 
    private Grid<LibraryItem> buildCatalogGrid(boolean isAdmin, Integer userId) {
        Grid<LibraryItem> grid = new Grid<>(LibraryItem.class, false);
        grid.addColumn(LibraryItem::getTitle).setHeader("Kitap Adı");
        grid.addColumn(LibraryItem::getAuthor).setHeader("Yazar");
        grid.addColumn(LibraryItem::getPublicationYear).setHeader("Yayın Yılı");
        grid.addColumn(LibraryItem::getStatus).setHeader("Durum");
        grid.addColumn(item -> item.getClass().getSimpleName()).setHeader("Tür");
        grid.setItems(items);
        grid.setHeight("400px");
 
        if (!isAdmin) {
            grid.addComponentColumn(item -> {
                Button borrowBtn = new Button("Ödünç Al");
 
                if (item instanceof EBook || item instanceof AudioBook) {
                    borrowBtn.setText("Dijital Ürün");
                    borrowBtn.setEnabled(false);
                } else if (item.getCopyCount() <= 0) {
                    borrowBtn.setText("Stokta Yok");
                    borrowBtn.setEnabled(false);
                    borrowBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
                }
 
                borrowBtn.addClickListener(e -> {
                    if (userId == null) {
                        Notification.show("Oturum hatası!");
                        return;
                    }
                    if (borrowDAO.isItemBorrowedByUser(userId, item.getId())) {
                        Notification.show("Bu kitabı zaten ödünç aldınız!");
                        return;
                    }
                    boolean success = borrowDAO.borrowItem(userId, item.getId());
                    if (success) {
                        Notification.show(item.getTitle() + " başarıyla ödünç alındı!", 3000, Notification.Position.TOP_CENTER);
                        UI.getCurrent().navigate("katalog");
                    } else {
                        Notification.show("Ödünç alma işlemi başarısız!");
                    }
                });
 
                return borrowBtn;
            }).setHeader("İşlemler");
        }
 
        return grid;
    }
 
    private TextField buildSearchField(Grid<LibraryItem> grid) {
        TextField searchField = new TextField("Katalogda Ara...");
        searchField.setPlaceholder("Kitap adı veya yazar...");
        searchField.setWidthFull();
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.addValueChangeListener(event -> {
            String filtre = event.getValue().toLowerCase();
            grid.setItems(items.stream()
                .filter(item ->
                    item.getTitle().toLowerCase().contains(filtre) ||
                    item.getAuthor().toLowerCase().contains(filtre))
                .toList());
        });
        return searchField;
    }
 
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Object role = VaadinSession.getCurrent().getAttribute("role");
        if (role == null) {
            event.rerouteTo(LoginView.class);
            Notification.show("Lütfen önce giriş yapın!", 3000, Notification.Position.MIDDLE);
        }
    }
}
 
