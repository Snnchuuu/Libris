package com.libris.views;

import com.libris.*;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;


import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.server.VaadinSession;

import java.time.LocalDateTime;

// Katalog sayfası — admin ve üye aynı rotaya gelir,
// içerik role göre dinamik olarak şekillenir.

@Route("katalog")
public class LibraryCatalogView extends VerticalLayout
        implements BeforeEnterObserver, HasDynamicTitle {

    private static final long serialVersionUID = 1L;

    // Veritabanı işlemleri için DAO nesneleri
    private final BookDAO         dao            = new BookDAO();
    private final BorrowDAO       borrowDao      = new BorrowDAO();
    private final UserDAO         userDao        = new UserDAO();
    private final ReviewDAO       reviewDao      = new ReviewDAO();
    private final ReservationDAO  reservationDao = new ReservationDAO();
    private final WishlistDAO     wishlistDao    = new WishlistDAO();

    // Grid'i field olarak tutuyoruz; birden fazla metot erişecek
    private final Grid<LibraryItem> grid = new Grid<>(LibraryItem.class, false);

    // Bu üç field hem constructor'da hem override metodlarda kullanılıyor
    private boolean isAdmin       = false;
    private int     userId        = -1;
    private Member  currentMember = null;

    // Header'daki bakiye göstergesi ve "Cezayı Öde" butonu — iade sonrası güncellenir
    private final Span balanceLabel = new Span();
    private Button     payFineBtn   = null;

    // BorrowDAO'da kitap ödünç alma süresi şu an 3 dakika (test modu).
    // Burada da aynısını kullanıyoruz ki onay e-postasındaki vade tarihi tutarlı olsun.
    private static final int LOAN_PERIOD_MINUTES = 3;
    
   

    public LibraryCatalogView() {
        setSizeFull();

        // --- OTURUM BİLGİLERİ ---
        // Login ekranında VaadinSession'a atanan değerleri okuyoruz
        String role       = (String) VaadinSession.getCurrent().getAttribute("role");
        String activeUser = (String) VaadinSession.getCurrent().getAttribute("username");
        isAdmin           = "ADMIN".equals(role);

        // Üye ise DB'den user_id'yi çek — ödünç kontrol sorgularında lazım
        // Admin için bu adımı atlıyoruz (admin ödünç almıyor)
        if (!isAdmin && activeUser != null) {
            userId        = borrowDao.getUserIdByUsername(activeUser);
            currentMember = userDao.getMemberByUsername(activeUser);
        }

        // ============================================================
        // HEADER — Üst bilgi çubuğu
        // Solda hoş geldin mesajı, sağda çıkış butonu
        // ============================================================
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);

        // Role göre farklı karşılama mesajı
        String welcomeMsg = isAdmin
                ? "Admin Paneli — Hoş geldin, " + activeUser
                : "Kütüphane Kataloğu — Hoş geldin, " + (activeUser != null ? activeUser : "Misafir");

        H3 welcomeText = new H3(welcomeMsg);

        // Çıkış butonu: session'ı temizler ve login'e yönlendirir
        Button logoutBtn = new Button("Güvenli Çıkış", VaadinIcon.SIGN_OUT.create());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        logoutBtn.addClickListener(e -> {
            VaadinSession.getCurrent().getSession().invalidate();
            UI.getCurrent().navigate("login");
        });

        header.add(welcomeText);
        header.expand(welcomeText); // welcomeText genişler → buton sağa yaslanır
        
        add(header);
        if (!isAdmin) {
            // Ceza bakiyesi göstergesi — anlık DB değeri
            double balance = borrowDao.getBalance(userId);
            balanceLabel.setText("Bakiye: " + PenaltyService.formatAmount(balance));
            balanceLabel.getStyle()
                .set("padding", "4px 10px")
                .set("border-radius", "12px")
                .set("font-weight", "600")
                .set("background", balance > 0 ? "#ffebee" : "#e8f5e9")
                .set("color",      balance > 0 ? "#b71c1c" : "#2e7d32");
            header.add(balanceLabel);

            // Cezası varsa "Öde" butonu
            Button payFineBtn = new Button("Cezayı Öde", VaadinIcon.MONEY.create());
            payFineBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            payFineBtn.setVisible(balance > 0);
            payFineBtn.addClickListener(e -> openPayFineDialog());
            header.add(payFineBtn);
            this.payFineBtn = payFineBtn;

            Button myBorrowsBtn = new Button("Ödünç Aldıklarım", VaadinIcon.LIST.create());
            myBorrowsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            myBorrowsBtn.addClickListener(e -> UI.getCurrent().navigate("my-borrows"));
            header.add(myBorrowsBtn);

            Button myReservationsBtn = new Button("Rezervasyonlarım", VaadinIcon.CLOCK.create());
            myReservationsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            myReservationsBtn.addClickListener(e -> UI.getCurrent().navigate("my-reservations"));
            header.add(myReservationsBtn);

            Button myWishlistBtn = new Button("İstek Listem", VaadinIcon.HEART.create());
            myWishlistBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            myWishlistBtn.addClickListener(e -> UI.getCurrent().navigate("my-wishlist"));
            header.add(myWishlistBtn);
        }
        header.add(logoutBtn);

        // ============================================================
        // GRID — Tüm kullanıcıların gördüğü ortak sütunlar
        // false parametresi: otomatik sütun oluşturmayı kapatır
        // ============================================================
        grid.addColumn(LibraryItem::getTitle).setHeader("Kitap Adı").setSortable(true);
        grid.addColumn(LibraryItem::getAuthor).setHeader("Yazar").setSortable(true);
        grid.addColumn(LibraryItem::getPublicationYear).setHeader("Yayın Yılı").setSortable(true);
        grid.addColumn(item -> {

            // Dijital ürünler
            if (item instanceof EBook || item instanceof AudioBook) {
                return "Digital Access";
            }

            // Admin için sadece stok mantığı
            if (isAdmin) {
                return item.getCopyCount() > 0
                        ? "Available"
                        : "Non Available";
            }

            // Kullanıcı bu kitabı kendisi aldıysa — "Borrowed" — stok kontrolünden ÖNCE!
            // (Aksi halde son kopyayı alan kişiye "Non Available" gösterilirdi.)
            if (borrowDao.isAlreadyBorrowed(userId, item.getId())) {
                return "Borrowed";
            }

            // Tüm kopyalar başka kullanıcılarda
            if ("Borrowed".equals(item.getStatus())) {
                return "Non Available";
            }

            // Normal durum
            return "Available";

        }).setHeader("Durum");

        // Tür sütunu: sınıf adı yerine Türkçe gösterim
        grid.addColumn(item -> {
            if (item instanceof Book)       return "Kitap";
            if (item instanceof EBook)      return "E-Kitap";
            if (item instanceof AudioBook)  return "Sesli Kitap";
            if (item instanceof Periodical) return "Süreli Yayın";
            return "Bilinmiyor";
        }).setHeader("Tür");

        // Stok sütunu: EBook ve AudioBook dijital olduğundan stok kavramı yok
        grid.addColumn(item -> {
            if (item instanceof EBook || item instanceof AudioBook) return "—";
            return String.valueOf(item.getCopyCount());
        }).setHeader("Stok");

        // Ortalama puan kolonu — yıldız + yorum sayısı
        grid.addColumn(item -> {
            double avg = reviewDao.getAverageRating(item.getId());
            int count  = reviewDao.getReviewsByItem(item.getId()).size();
            if (count == 0) return "—";
            return String.format("⭐ %.1f (%d)", avg, count);
        }).setHeader("Puan").setAutoWidth(true);

        // Yorumlar butonu — herkes (admin & üye) görür
        grid.addComponentColumn(item -> {
            Button reviewsBtn = new Button("Yorumlar", VaadinIcon.COMMENTS.create());
            reviewsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            reviewsBtn.addClickListener(e -> openReviewsDialog(item));
            return reviewsBtn;
        }).setHeader("Yorumlar").setAutoWidth(true).setFlexGrow(0);

        grid.setSizeFull();

        // İlk veriyi DB'den çek
        refreshGrid();

        // ============================================================
        // ROLE'E GÖRE ÖZEL BÖLÜMLER
        // ============================================================
        if (isAdmin) {
            buildAdminToolbar(); // Admin: ekleme/silme/stok güncelleme
        }

        // ============================================================
        // ARAMA KUTUSU — Her iki rol de kullanabilir
        // Kullanıcı yazdıkça DB'de LIKE sorgusu çalışır
        // ============================================================
        TextField searchField = new TextField();
        searchField.setPlaceholder("Kitap adı veya yazar...");
        searchField.setLabel("Katalogda Ara...");
        searchField.setWidthFull();
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());

        searchField.addValueChangeListener(event -> {
            String query = event.getValue().trim();
            if (query.isEmpty()) {
                refreshGrid(); // Kutu boşsa tüm listeyi geri getir
            } else {
                grid.setItems(dao.searchItems(query)); // DB'de ara
            }
        });

        if (!isAdmin) {
            buildBorrowColumn(); // Üye: ödünç alma sütunu
        }

        add(searchField, grid);
    }

    // ================================================================
    // refreshGrid() — DB'den tüm öğeleri çekip tabloyu günceller.
    // Ekleme, silme, ödünç alma sonrası çağrılır.
    // ================================================================
    private void refreshGrid() {
        grid.setItems(dao.getAllItems());
    }

    // ================================================================
    // buildAdminToolbar() — Admin'e özel ekleme/silme butonları
    // ve stok güncelleme sütunu. Sadece isAdmin=true ise çağrılır.
    // ================================================================
    private void buildAdminToolbar() {

        // 4 ayrı tür için ayrı "Ekle" butonu — her birinin dialog'u farklı
        Button addBookBtn   = new Button("Kitap Ekle",        VaadinIcon.PLUS.create());
        Button addEbookBtn  = new Button("E-Kitap Ekle",      VaadinIcon.PLUS.create());
        Button addAudioBtn  = new Button("Sesli Kitap Ekle",  VaadinIcon.PLUS.create());
        Button addPeriodBtn = new Button("Süreli Yayın Ekle", VaadinIcon.PLUS.create());
        Button deleteBtn    = new Button("Seçileni Sil",      VaadinIcon.TRASH.create());

        addBookBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,  ButtonVariant.LUMO_SUCCESS);
        addEbookBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        addAudioBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        addPeriodBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout adminToolbar = new HorizontalLayout(
                addBookBtn, addEbookBtn, addAudioBtn, addPeriodBtn, deleteBtn);
        add(adminToolbar);

        // --- SİL ---
        // Tabloda seçili satır varsa DB'den sil ve grid'i yenile
        deleteBtn.addClickListener(e -> {
            grid.asSingleSelect().getOptionalValue().ifPresent(selected -> {
                boolean ok = dao.deleteItem(selected.getId());
                if (ok) {
                    refreshGrid();
                    Notification.show(selected.getTitle() + " sistemden kaldırıldı.");
                } else {
                    Notification.show("Silme başarısız! Veritabanı hatası.");
                }
            });
            if (grid.asSingleSelect().isEmpty()) {
                Notification.show("Lütfen silmek istediğiniz öğeyi tablodan seçin!");
            }
        });

        // --- KİTAP EKLE ---
        addBookBtn.addClickListener(e -> {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Yeni Kitap Ekle");

            TextField titleIn = new TextField("Kitap Adı");
            TextField authIn  = new TextField("Yazar");
            TextField yearIn  = new TextField("Yayın Yılı");
            TextField isbnIn  = new TextField("ISBN");
            TextField pageIn  = new TextField("Sayfa Sayısı");
            TextField genreIn = new TextField("Tür (Genre)");
            TextField stockIn = new TextField("Stok Adedi");

            dialog.add(new VerticalLayout(titleIn, authIn, yearIn, isbnIn, pageIn, genreIn, stockIn));

            Button saveBtn = new Button("Kaydet", ev -> {
                try {
                    int year  = Integer.parseInt(yearIn.getValue());
                    int pages = Integer.parseInt(pageIn.getValue());
                    int stock = Integer.parseInt(stockIn.getValue());

                    // ID=0 veriyoruz; MySQL AUTO_INCREMENT gerçek ID'yi atar
                    Book book = new Book(0, titleIn.getValue(), authIn.getValue(),
                            year, stock, "Available",
                            isbnIn.getValue(), pages, genreIn.getValue());

                    if (dao.addBook(book)) {
                        refreshGrid();
                        dialog.close();
                        Notification.show("Kitap başarıyla eklendi!");
                    } else {
                        Notification.show("Hata: Veritabanına eklenemedi!");
                    }
                } catch (NumberFormatException ex) {
                    // Kullanıcı rakam alanına harf girdiyse
                    Notification.show("Hata: Yıl, sayfa ve stok rakam olmalıdır!");
                }
            });
            saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            dialog.getFooter().add(new Button("İptal", i -> dialog.close()), saveBtn);
            dialog.open();
        });

        // --- E-KİTAP EKLE ---
        // Stok alanı YOK — dijital ürünlerin fiziksel stoğu olmaz
        addEbookBtn.addClickListener(e -> {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Yeni E-Kitap Ekle");

            TextField titleIn  = new TextField("Kitap Adı");
            TextField authIn   = new TextField("Yazar");
            TextField yearIn   = new TextField("Yayın Yılı");
            TextField formatIn = new TextField("Dosya Formatı (PDF, EPUB...)");
            TextField sizeIn   = new TextField("Dosya Boyutu (MB)");

            dialog.add(new VerticalLayout(titleIn, authIn, yearIn, formatIn, sizeIn));

            Button saveBtn = new Button("Kaydet", ev -> {
                try {
                    int    year = Integer.parseInt(yearIn.getValue());
                    double size = Double.parseDouble(sizeIn.getValue());

                    // copyCount=1 sabit: dijital ürün sınırsız ama DB null kabul etmiyor
                    EBook ebook = new EBook(0, titleIn.getValue(), authIn.getValue(),
                            year, 1, "Available", formatIn.getValue(), size);

                    if (dao.addEBook(ebook)) {
                        refreshGrid();
                        dialog.close();
                        Notification.show("E-Kitap başarıyla eklendi!");
                    } else {
                        Notification.show("Hata: Veritabanına eklenemedi!");
                    }
                } catch (NumberFormatException ex) {
                    Notification.show("Hata: Yıl rakam, boyut ondalıklı sayı olmalıdır!");
                }
            });
            saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            dialog.getFooter().add(new Button("İptal", i -> dialog.close()), saveBtn);
            dialog.open();
        });

        // --- SESLİ KİTAP EKLE ---
        // Stok alanı YOK — dijital ürün
        addAudioBtn.addClickListener(e -> {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Yeni Sesli Kitap Ekle");

            TextField titleIn    = new TextField("Kitap Adı");
            TextField authIn     = new TextField("Yazar");
            TextField yearIn     = new TextField("Yayın Yılı");
            TextField durationIn = new TextField("Süre (dakika)");
            TextField narratorIn = new TextField("Seslendirmen");

            dialog.add(new VerticalLayout(titleIn, authIn, yearIn, durationIn, narratorIn));

            Button saveBtn = new Button("Kaydet", ev -> {
                try {
                    int year     = Integer.parseInt(yearIn.getValue());
                    int duration = Integer.parseInt(durationIn.getValue());

                    AudioBook audio = new AudioBook(0, titleIn.getValue(), authIn.getValue(),
                            year, 1, "Available", duration, narratorIn.getValue());

                    if (dao.addAudioBook(audio)) {
                        refreshGrid();
                        dialog.close();
                        Notification.show("Sesli kitap başarıyla eklendi!");
                    } else {
                        Notification.show("Hata: Veritabanına eklenemedi!");
                    }
                } catch (NumberFormatException ex) {
                    Notification.show("Hata: Yıl ve süre rakam olmalıdır!");
                }
            });
            saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            dialog.getFooter().add(new Button("İptal", i -> dialog.close()), saveBtn);
            dialog.open();
        });

        // --- SÜRELİ YAYIN EKLE ---
        addPeriodBtn.addClickListener(e -> {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Yeni Süreli Yayın Ekle");

            TextField titleIn  = new TextField("Yayın Adı");
            TextField authIn   = new TextField("Yazar / Kurum");
            TextField yearIn   = new TextField("Yayın Yılı");
            TextField issueIn  = new TextField("Sayı Numarası");
            TextField periodIn = new TextField("Periyot (Weekly, Monthly...)");
            TextField stockIn  = new TextField("Stok Adedi");

            dialog.add(new VerticalLayout(titleIn, authIn, yearIn, issueIn, periodIn, stockIn));

            Button saveBtn = new Button("Kaydet", ev -> {
                try {
                    int year  = Integer.parseInt(yearIn.getValue());
                    int issue = Integer.parseInt(issueIn.getValue());
                    int stock = Integer.parseInt(stockIn.getValue());

                    Periodical p = new Periodical(0, titleIn.getValue(), authIn.getValue(),
                            year, stock, "Available", issue, periodIn.getValue());

                    if (dao.addPeriodical(p)) {
                        refreshGrid();
                        dialog.close();
                        Notification.show("Süreli yayın başarıyla eklendi!");
                    } else {
                        Notification.show("Hata: Veritabanına eklenemedi!");
                    }
                } catch (NumberFormatException ex) {
                    Notification.show("Hata: Yıl, sayı ve stok rakam olmalıdır!");
                }
            });
            saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            dialog.getFooter().add(new Button("İptal", i -> dialog.close()), saveBtn);
            dialog.open();
        });

        // --- STOK GÜNCELLE sütunu ---
        // EBook ve AudioBook için pasif gösterim; Book ve Periodical için aktif
        grid.addComponentColumn(item -> {

            // Dijital ürünlerde stok güncelleme anlamsız
            if (item instanceof EBook || item instanceof AudioBook) {
                Button noStockBtn = new Button("Dijital Ürün");
                noStockBtn.setEnabled(false);
                return noStockBtn;
            }

            Button editStockBtn = new Button("Stoğu Güncelle", VaadinIcon.EDIT.create());
            editStockBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);

            editStockBtn.addClickListener(ev -> {
                Dialog stockDialog = new Dialog();
                stockDialog.setHeaderTitle("Stok Güncelle: " + item.getTitle());

                TextField newStockField = new TextField("Yeni Stok Adedi");
                newStockField.setValue(String.valueOf(item.getCopyCount()));
                stockDialog.add(newStockField);

                Button confirmBtn = new Button("Güncelle", ce -> {
                    try {
                        int newTotal = Integer.parseInt(newStockField.getValue());
                        int oldTotal = item.getCopyCount();

                        // updateTotalStock: hem copy_count hem available_copies güncellenir
                        // Mevcut ödünç sayısı korunur — sadece toplam stok değişir
                        boolean ok = dao.updateTotalStock(item.getId(), newTotal);
                        if (ok) {
                            item.setCopyCount(newTotal);
                            item.setStatus(newTotal > 0 ? "Available" : "Borrowed");
                            grid.getDataProvider().refreshItem(item);
                            stockDialog.close();

                            // Stok arttıysa, bekleyen rezervasyon sahiplerine bildir
                            int notified = 0;
                            if (newTotal > oldTotal && newTotal > 0) {
                                notified = ReservationNotifier.notifyAllPending(
                                    item.getId(), item.getTitle());
                            }
                            if (notified > 0) {
                                Notification.show(
                                    "Stok güncellendi! " + notified
                                    + " rezervasyon sahibine e-posta gönderildi.");
                            } else {
                                Notification.show("Stok güncellendi!");
                            }
                        } else {
                            Notification.show("Hata: Stok güncellenemedi!");
                        }
                    } catch (NumberFormatException ex) {
                        Notification.show("Hata: Lütfen geçerli bir sayı girin!");
                    }
                });
                confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                stockDialog.getFooter().add(new Button("İptal", i -> stockDialog.close()), confirmBtn);
                stockDialog.open();
            });

            return editStockBtn;

        }).setHeader("Admin İşlemleri");
    }

    // ================================================================
    // buildBorrowColumn() — Üye görünümüne ödünç alma sütunu ekler.
    // Sadece isAdmin=false ise çağrılır.
    // ================================================================
    private void buildBorrowColumn() {
        grid.addComponentColumn(item -> {

            // Durum 1: Dijital ürün — tıklanabilir buton, bilgi dialog'u açar
            if (item instanceof EBook || item instanceof AudioBook) {
                Button digitalBtn = new Button("🔓 Dijital Erişim");
                digitalBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                digitalBtn.addClickListener(e -> {
                    Dialog infoDialog = new Dialog();

                    // EBook ve AudioBook için farklı içerik göster
                    if (item instanceof EBook) {
                        EBook ebook = (EBook) item;
                        infoDialog.setHeaderTitle("📖 " + ebook.getTitle());
                        infoDialog.add(new VerticalLayout(
                            new Span("Yazar: "   + ebook.getAuthor()),
                            new Span("Format: "  + ebook.getFileFormat()),
                            new Span("Boyut: "   + ebook.getFileSize() + " MB"),
                            new Span("Bu e-kitap dijital olarak serbestçe erişilebilir.")
                        ));
                    } else if (item instanceof AudioBook) {
                        AudioBook audio = (AudioBook) item;
                        infoDialog.setHeaderTitle("🎧 " + audio.getTitle());
                        infoDialog.add(new VerticalLayout(
                            new Span("Yazar: "       + audio.getAuthor()),
                            new Span("Seslendirmen: " + audio.getNarrator()),
                            new Span("Süre: "        + audio.getDuration() + " dakika"),
                            new Span("Bu sesli kitap dijital olarak serbestçe erişilebilir.")
                        ));
                    }

                    infoDialog.getFooter().add(new Button("Kapat", ev -> infoDialog.close()));
                    infoDialog.open();
                });

                return digitalBtn;
            }

         // Durum 2: Kullanıcı kitabı zaten aldıysa "İade Et" — stok kontrolünden ÖNCE!
         //          (Yoksa son kopyayı alan kişi stok 0'a indiği için iade edemez.)
            if (borrowDao.isAlreadyBorrowed(userId, item.getId())) {

                Button returnBtn = new Button("İade Et");
                returnBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

                returnBtn.addClickListener(e -> {

                    // İade + ceza hesaplama tek transaction'da
                    BorrowDAO.ReturnResult result = borrowDao.returnAndPenalize(userId, item.getId());

                    if (result.success) {

                        // Stoğu 1 arttır
                        boolean stockOk = dao.updateStock(item.getId(), +1);

                        if (stockOk) {

                            String msg;
                            if (result.fineAmount > 0) {
                                msg = "✅ " + item.getTitle()
                                    + " iade edildi. Gecikme: " + result.unitsLate + " "
                                    + PenaltyService.UNIT_LABEL
                                    + " — Ceza: " + PenaltyService.formatAmount(result.fineAmount);
                            } else {
                                msg = "✅ " + item.getTitle() + " başarıyla iade edildi!";
                            }
                            Notification.show(msg, 4000, Notification.Position.TOP_CENTER);

                            // İade onay e-postası — ceza bilgisiyle
                            sendReturnEmailQuiet(item.getTitle(), result.fineAmount, result.unitsLate);

                            // Header'daki bakiyeyi tazele
                            updateBalanceUI(result.newBalance);

                            // Bekleyen rezervasyon sahiplerine bildir
                            int notified = ReservationNotifier.notifyAllPending(
                                item.getId(), item.getTitle());
                            if (notified > 0) {
                                Notification.show(
                                    "📬 " + notified + " kişi rezervasyon e-postası ile bilgilendirildi.",
                                    3000, Notification.Position.BOTTOM_END);
                            }

                            refreshGrid();

                            // Değerlendirme isteği
                            openReviewDialog(item.getId(), item.getTitle());

                        } else {
                            Notification.show("Hata: Stok güncellenemedi!");
                        }

                    } else {
                        Notification.show("Hata: İade işlemi başarısız!");
                    }
                });

                return returnBtn;
            }

            // Durum 3: Bu kullanıcı almamış AMA stok bitmiş (status='Borrowed') → Rezerve Et
            if ("Borrowed".equals(item.getStatus())) {
                Button reserveBtn = new Button("Rezerve Et", VaadinIcon.CLOCK.create());
                reserveBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
                reserveBtn.addClickListener(e -> {
                    boolean ok = reservationDao.createReservation(userId, item.getId());
                    if (ok) {
                        Notification.show(
                            "✅ Rezervasyon oluşturuldu: " + item.getTitle(),
                            3000, Notification.Position.TOP_CENTER);
                    } else {
                        Notification.show("Rezervasyon oluşturulamadı.",
                            3000, Notification.Position.MIDDLE);
                    }
                });
                return reserveBtn;
            }

            // Durum 4: Normal — ödünç alınabilir
            Button borrowBtn = new Button("Ödünç Al");
            borrowBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

            borrowBtn.addClickListener(e -> {
                if (item.getCopyCount() <= 0) {
                    Notification.show("Üzgünüz, stok tükendi!");
                    refreshGrid();
                    return;
                }
                if (borrowDao.isAlreadyBorrowed(userId, item.getId())) {
                    Notification.show("Bu kitabı zaten ödünç almışsınız!");
                    return;
                }

                boolean borrowOk = borrowDao.borrowItem(userId, item.getId());
                if (borrowOk) {
                    boolean stockOk = dao.updateStock(item.getId(), -1);
                    if (stockOk) {
                        Notification.show(
                            "✅ " + item.getTitle() + " başarıyla ödünç alındı! "
                            + "İade tarihi: " + LOAN_PERIOD_MINUTES + " dakika sonra.",
                            4000, Notification.Position.TOP_CENTER
                        );

                        // Ödünç alma onay e-postası — sessiz başarısızlık
                        sendBorrowEmailQuiet(
                            item.getTitle(),
                            LocalDateTime.now().plusMinutes(LOAN_PERIOD_MINUTES)
                        );

                        // Bu kullanıcının bu kitap için PENDING rezervasyonu varsa
                        // otomatik FULFILLED yap → "Bekleyen" listeden düşsün.
                        reservationDao.fulfillByUserAndItem(userId, item.getId());

                        refreshGrid(); // DB'den güncel veriyi çek — stok da güncellenmiş olacak
                    } else {
                        Notification.show("Hata: Stok güncellenemedi, lütfen tekrar deneyin.");
                    }
                } else {
                    Notification.show("Hata: Ödünç alma kaydı oluşturulamadı.");
                }
            });

            return borrowBtn;

        }).setHeader("İşlemler");

        // Wishlist (kalp) sütunu — sadece üye görünümünde, fiziksel/digital fark etmez.
        grid.addComponentColumn(item -> {
            boolean inList = wishlistDao.isInWishlist(userId, item.getId());
            Button heart = new Button(VaadinIcon.HEART.create());
            heart.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
            heart.getElement().setProperty("title",
                inList ? "İstek listesinden çıkar" : "İstek listesine ekle");
            // dolu/kırmızı görünüm: zaten listede ise
            if (inList) {
                heart.getStyle().set("color", "#e91e63");
            }
            heart.addClickListener(e -> {
                boolean nowIn;
                if (inList) {
                    wishlistDao.remove(userId, item.getId());
                    nowIn = false;
                    Notification.show("İstek listesinden çıkarıldı: " + item.getTitle(),
                        2000, Notification.Position.BOTTOM_END);
                } else {
                    wishlistDao.add(userId, item.getId());
                    nowIn = true;
                    Notification.show("İstek listesine eklendi: " + item.getTitle(),
                        2000, Notification.Position.BOTTOM_END);
                }
                // Kalp ikonunu yenilemek için satırı refresh et
                grid.getDataProvider().refreshItem(item);
            });
            return heart;
        }).setHeader("İstek").setWidth("80px").setFlexGrow(0);
    }

    // ================================================================
    //  YARDIMCI METOTLAR — e-posta ve değerlendirme diyaloğu
    // ================================================================

    /** Ödünç alma onay e-postası — başarısız olursa sessizce loglar. */
    private void sendBorrowEmailQuiet(String title, LocalDateTime dueDate) {
        if (currentMember == null || currentMember.getEmail() == null) return;
        try {
            EmailService.sendBorrowConfirmation(
                currentMember.getEmail(),
                currentMember.getName(),
                title,
                dueDate
            );
        } catch (Exception ex) {
            System.err.println("[Catalog] Borrow email failed: " + ex.getMessage());
        }
    }

    /** İade onay e-postası — başarısız olursa sessizce loglar. (Geriye uyumluluk için no-fine versiyon) */
    private void sendReturnEmailQuiet(String title) {
        sendReturnEmailQuiet(title, 0.0, 0L);
    }

    /** İade onay e-postası — ceza bilgisiyle. */
    private void sendReturnEmailQuiet(String title, double fineAmount, long unitsLate) {
        if (currentMember == null || currentMember.getEmail() == null) return;
        try {
            EmailService.sendReturnConfirmation(
                currentMember.getEmail(),
                currentMember.getName(),
                title,
                LocalDateTime.now(),
                fineAmount,
                unitsLate
            );
        } catch (Exception ex) {
            System.err.println("[Catalog] Return email failed: " + ex.getMessage());
        }
    }

    /** Header'daki bakiye etiketini ve Öde butonunu yeni değere göre günceller. */
    private void updateBalanceUI(double newBalance) {
        balanceLabel.setText("Bakiye: " + PenaltyService.formatAmount(newBalance));
        balanceLabel.getStyle()
            .set("background", newBalance > 0 ? "#ffebee" : "#e8f5e9")
            .set("color",      newBalance > 0 ? "#b71c1c" : "#2e7d32");
        if (payFineBtn != null) payFineBtn.setVisible(newBalance > 0);
    }

    /** "Cezayı Öde" diyaloğu — bakiyeyi 0'a sıfırlar (ödeme simülasyonu). */
    private void openPayFineDialog() {
        double balance = borrowDao.getBalance(userId);
        if (balance <= 0) {
            Notification.show("Cezanız yok.", 2000, Notification.Position.TOP_CENTER);
            updateBalanceUI(0);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Ceza Ödemesi");
        dialog.add(new VerticalLayout(
            new Span("Toplam ceza tutarı: " + PenaltyService.formatAmount(balance)),
            new Span("Ödemek istediğinizden emin misiniz? (Bu bir simülasyondur)")
        ));

        Button cancel = new Button("İptal", e -> dialog.close());
        Button pay    = new Button("Öde", e -> {
            boolean ok = borrowDao.clearBalance(userId);
            if (ok) {
                Notification.show("Ödeme alındı. Bakiyeniz sıfırlandı.",
                    3000, Notification.Position.TOP_CENTER);
                updateBalanceUI(0);
            } else {
                Notification.show("Ödeme başarısız.", 3000, Notification.Position.MIDDLE);
            }
            dialog.close();
        });
        pay.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancel, pay);
        dialog.open();
    }

    /**
     * Bir kitabın tüm yorumlarını listeleyen diyaloğu açar.
     * Üst kısımda ortalama puan, altında yorumların listesi.
     */
    private void openReviewsDialog(LibraryItem item) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Yorumlar — " + item.getTitle());
        dialog.setWidth("520px");

        double avg = reviewDao.getAverageRating(item.getId());
        java.util.List<String> reviews = reviewDao.getReviewsByItem(item.getId());

        VerticalLayout body = new VerticalLayout();
        body.setPadding(false);
        body.setSpacing(true);

        // Üstte özet
        if (reviews.isEmpty()) {
            Span empty = new Span("Henüz yorum yapılmamış. İlk yorumu sen yaz!");
            empty.getStyle().set("color", "#666").set("font-style", "italic");
            body.add(empty);
        } else {
            Span summary = new Span(String.format("Ortalama: ⭐ %.1f / 5  (%d yorum)",
                avg, reviews.size()));
            summary.getStyle().set("font-weight", "600").set("font-size", "1.1em");
            body.add(summary);

            // Her yorum: "username | 5/5 | comment | yyyy-MM-dd"
            for (String entry : reviews) {
                String[] parts = entry.split("\\s*\\|\\s*", 4);
                if (parts.length < 4) {
                    body.add(new Span(entry));
                    continue;
                }
                String user    = parts[0];
                String rating  = parts[1];
                String comment = parts[2];
                String date    = parts[3];

                VerticalLayout card = new VerticalLayout();
                card.setPadding(false);
                card.setSpacing(false);
                card.getStyle()
                    .set("border", "1px solid #e0e0e0")
                    .set("border-radius", "6px")
                    .set("padding", "10px")
                    .set("background", "#fafafa");

                Span head = new Span("⭐ " + rating + "  •  " + user + "  •  " + date);
                head.getStyle().set("font-size", "0.9em").set("color", "#444");

                Span text = new Span(comment == null || comment.isBlank() ? "(yorum yok)" : comment);
                text.getStyle().set("margin-top", "4px");

                card.add(head, text);
                body.add(card);
            }
        }

        // Üye ise altta "Bu kitabı değerlendir" butonu
        if (!isAdmin && currentMember != null) {
            Button writeBtn = new Button("Yorum Yaz", VaadinIcon.STAR.create());
            writeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            writeBtn.addClickListener(e -> {
                dialog.close();
                openReviewDialog(item.getId(), item.getTitle());
            });
            body.add(writeBtn);
        }

        dialog.add(body);
        dialog.getFooter().add(new Button("Kapat", e -> dialog.close()));
        dialog.open();
    }

    /** Kitap değerlendirme diyaloğu — iade sonrası açılır. */
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
                    refreshGrid(); // Puan kolonu yenilensin
                } else {
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

    // ================================================================
    // getPageTitle() — HasDynamicTitle'dan geliyor.
    // Tarayıcı sekmesi başlığını runtime'da belirler.
    // @PageTitle bunu yapamaz; o sadece sabit string alır.
    // ================================================================
    @Override
    public String getPageTitle() {
        return isAdmin ? "Admin Paneli" : "Katalog";
    }

    // ================================================================
    // beforeEnter() — Giriş yapılmadan sayfaya erişimi engeller.
    // Vaadin routing öncesinde bu metodu otomatik çağırır.
    // ================================================================
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Object role = VaadinSession.getCurrent().getAttribute("role");
        if (role == null) {
            event.rerouteTo(LoginView.class);
            Notification.show("Lütfen önce giriş yapın!", 3000, Notification.Position.MIDDLE);
        }
    }
}