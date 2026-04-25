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
import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.server.VaadinSession;

// katalog sayfası

@Route("katalog")
public class LibraryCatalogView extends VerticalLayout implements BeforeEnterObserver{

    // SQL gelene kadar test verileri, sql buraya bağlanacak
    // Listeyi static yaparak bellekte kalıcı olmasını sağladık (Ekleme/Silme artık çalışır)
    private static List<LibraryItem> items = new ArrayList<>();

    // Statik blok: Liste boşsa başlangıç verilerini bir kez doldurur
    static {
        if (items.isEmpty()) {
            items.add(new Book(1, "Java Programming", "H.M. Deitel", 2024, 5, "Available", "123-456", 800, "Education"));
            items.add(new Book(2, "Araba Sevdası", "Recaizade Mahmut Ekrem", 1875, 3, "Available", "456-789", 276, "Novel"));
            items.add(new EBook(3, "Digital Trends", "Tech Team", 2025, 1, "Available", "PDF", 15.5));
            items.add(new AudioBook(4, "Sapiens", "Yuval Noah Harari", 2014, 2, "Available", 900, "John Smith"));
        }
    }

    public LibraryCatalogView() {
        // Sayfa düzenini ayarla (Tam ekran kaplasın)
        setSizeFull();

        // Oturum bilgilerini alıyoruz
        String role = (String) VaadinSession.getCurrent().getAttribute("role");
        String activeUser = (String) VaadinSession.getCurrent().getAttribute("username");
        boolean isAdmin = "ADMIN".equals(role);

        // Grid (Tablo) - false diyerek otomatik sütun oluşturmayı kapatıyoruz (Hata almamak için en iyisi)
        Grid<LibraryItem> grid = new Grid<>(LibraryItem.class, false);
        
        // Sütunları manuel ekliyoruz (Sınıftaki metot isimlerinle tam uyumlu)
        grid.addColumn(LibraryItem::getTitle).setHeader("Kitap Adı");
        grid.addColumn(LibraryItem::getAuthor).setHeader("Yazar");
        grid.addColumn(LibraryItem::getPublicationYear).setHeader("Yayın Yılı");
        grid.addColumn(LibraryItem::getStatus).setHeader("Durum");
        grid.addColumn(item -> item.getClass().getSimpleName()).setHeader("Tür");
        
        grid.setItems(items);
        grid.setSizeFull();

        // Kullanıcıya özel selamlama paneli
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        
        H3 welcomeText = new H3("Kütüphane Sistemi - Hoş geldin, " + (activeUser != null ? activeUser : "Misafir"));
        
        // çıkış butonu
        Button logoutBtn = new Button("Güvenli Çıkış", VaadinIcon.SIGN_OUT.create());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        logoutBtn.addClickListener(e -> {
            VaadinSession.getCurrent().getSession().invalidate(); // Session'ı temizle
            UI.getCurrent().navigate("login"); // Login'e dön
        });
        
        header.add(welcomeText);
        header.expand(welcomeText); // Metni genişleterek butonu sağa yaslar
        header.add(logoutBtn);
        
        add(header);

        // --- ADMIN TOOLBAR ---
        // Sadece admin girişi yapıldıysa "Ekle/Sil" butonlarını gösteriyoruz
        if (isAdmin) {
            Button addBtn = new Button("Yeni Kitap Ekle", VaadinIcon.PLUS.create());
            addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
            
            Button deleteBtn = new Button("Seçileni Sil", VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

            HorizontalLayout adminActions = new HorizontalLayout(addBtn, deleteBtn);
            add(adminActions);

            // Silme işlevi
            deleteBtn.addClickListener(e -> {
                grid.asSingleSelect().getOptionalValue().ifPresent(selectedItem -> {
                    items.remove(selectedItem);
                    grid.getDataProvider().refreshAll();
                    Notification.show(selectedItem.getTitle() + " sistemden kaldırıldı.");
                });
                if (grid.asSingleSelect().isEmpty()) {
                    Notification.show("Lütfen silmek istediğiniz kitabı tablodan seçin!");
                }
            });

            // Ekleme işlevi
            addBtn.addClickListener(e -> {
                Dialog dialog = new Dialog();
                dialog.setHeaderTitle("Yeni Kitap Ekle");

                TextField titleIn = new TextField("Kitap Adı");
                TextField authIn = new TextField("Yazar");
                TextField yearIn = new TextField("Yayın Yılı");
                TextField isbnIn = new TextField("ISBN");

                dialog.add(new VerticalLayout(titleIn, authIn, yearIn, isbnIn));

                Button saveBtn = new Button("Kaydet", saveEvt -> {
                    try {
                        int year = Integer.parseInt(yearIn.getValue());
                        // Yeni bir Book nesnesi oluşturuyoruz (ID'yi şimdilik random verelim)
                        Book newBook = new Book((int)(Math.random()*1000), titleIn.getValue(), 
                                               authIn.getValue(), year, 1, "Available", 
                                               isbnIn.getValue(), 100, "General");
                        items.add(newBook);
                        grid.getDataProvider().refreshAll();
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

        // Arama Kutusu
        TextField searchField = new TextField("Katalogda Ara...");
        searchField.setPlaceholder("Kitap adı veya yazar...");
        searchField.setWidthFull();
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());

        // arama kutusunun işlevselliği burada
        searchField.addValueChangeListener(event -> {
            // Yazılan metni al ve küçük harfe çevir (büyük/küçük harf duyarlılığı olmasın diye)
            String filtre = event.getValue().toLowerCase();

            // Tablodaki listeyi filtrele
            grid.setItems(items.stream()
                .filter(item -> 
                    // Başlıkta veya Yazar isminde filtre metni geçiyor mu?
                    item.getTitle().toLowerCase().contains(filtre) || 
                    item.getAuthor().toLowerCase().contains(filtre)
                )
                .toList());
        });

        //ödünç alma butonları
        if (!isAdmin) {
            grid.addComponentColumn(item -> {
                Button borrowBtn = new Button("Ödünç Al");

                // Sinanın eklediği mantığı korudum
                // Dijital ürünler ödünç alınamaz
                // Ürün zaten ödünç alınmışsa veya dijitalse butonu kapat
                if (item instanceof EBook || item instanceof AudioBook) {
                    borrowBtn.setText("Dijital Ürün");
                    borrowBtn.setEnabled(false);
                } else if ("Borrowed".equals(item.getStatus())) {
                    borrowBtn.setText("Ödünç Verildi");
                    borrowBtn.setEnabled(false);
                    borrowBtn.addThemeVariants(ButtonVariant.LUMO_ERROR); // Kırmızı buton
                }

                // Tıklama dinleyici
                borrowBtn.addClickListener(e -> {
                    // Stok ve Durum Kontrolü
                    if (item.getCopyCount() > 0 && !"Borrowed".equals(item.getStatus())) {
                        
                        // Veriyi Güncelle (Simülasyon)
                        item.setCopyCount(item.getCopyCount() - 1);
                        item.setStatus("Borrowed"); // Durumu değiştir
                        
                        Notification.show(item.getTitle() + " başarıyla ödünç alındı!", 3000, Notification.Position.TOP_CENTER);
                        
                        // Tabloyu Anlık Yenile
                        grid.getDataProvider().refreshItem(item); 
                    }
                });

                return borrowBtn;
            }).setHeader("İşlemler");
        }
        
        // Oluşturduğumuz bileşenleri ekrana ekliyoruz
        add(searchField, grid); 
        
        
    }
    
    // login yapılmadan erişilemesin diye
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Session'da "role" bilgisi yoksa (giriş yapılmamışsa)
        Object role = VaadinSession.getCurrent().getAttribute("role");
        
        if (role == null) {
            // Kullanıcıyı login sayfasına postala
            event.rerouteTo(LoginView.class);
            Notification.show("Lütfen önce giriş yapın!", 3000, Notification.Position.MIDDLE);
        }
    }
}