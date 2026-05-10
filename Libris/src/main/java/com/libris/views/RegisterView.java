package com.libris.views;

import com.libris.LibraryManager;
import com.libris.UserDAO;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;

import java.util.regex.Pattern;

/**
 * RegisterView — Yeni üye kayıt sayfası.
 *
 * Validasyonlar:
 *   - Tüm alanlar dolu olmalı
 *   - Kullanıcı adı en az 3 karakter, sadece harf/rakam/_
 *   - Ad en az 2 karakter
 *   - Email RFC formatına uygun olmalı (Vaadin EmailField + ek regex)
 *   - Şifre en az 6 karakter
 *   - Kullanıcı adı ve email DB'de daha önce alınmamış olmalı
 */
@StyleSheet("styles/styles.css")
@Route("register")
@PageTitle("Üye Ol | Libris")
public class RegisterView extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    // Email regex — basit ama yeterli (RFC 5322 simplified)
    private static final Pattern EMAIL_REGEX = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // Kullanıcı adı: harf/rakam/alt çizgi, 3-30 karakter
    private static final Pattern USERNAME_REGEX = Pattern.compile("^[A-Za-z0-9_]{3,30}$");

    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MIN_NAME_LENGTH = 2;

    private final LibraryManager manager = new LibraryManager();
    private final UserDAO        userDao = new UserDAO();

    public RegisterView() {

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeightFull();

        H1 title = new H1("Yeni Libris Hesabı");
        title.getStyle().set("margin-bottom", "16px");

        // Form alanları
        TextField username = new TextField("Kullanıcı Adı");
        username.setHelperText("3-30 karakter; harf, rakam ve alt çizgi");
        username.setMinLength(3);
        username.setMaxLength(30);
        username.setWidth("320px");

        TextField name = new TextField("Ad Soyad");
        name.setMinLength(MIN_NAME_LENGTH);
        name.setWidth("320px");

        EmailField email = new EmailField("E-posta");
        email.setErrorMessage("Geçerli bir e-posta adresi girin");
        email.setWidth("320px");

        PasswordField password = new PasswordField("Şifre");
        password.setHelperText("En az " + MIN_PASSWORD_LENGTH + " karakter");
        password.setMinLength(MIN_PASSWORD_LENGTH);
        password.setWidth("320px");

        Button registerButton = new Button("Kayıt Ol");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidth("320px");

        registerButton.addClickListener(e -> {

            String u  = username.getValue() == null ? "" : username.getValue().trim();
            String n  = name.getValue() == null ? "" : name.getValue().trim();
            String em = email.getValue() == null ? "" : email.getValue().trim().toLowerCase();
            String pw = password.getValue() == null ? "" : password.getValue();

            // 1) Boş alan kontrolü
            if (u.isEmpty() || n.isEmpty() || em.isEmpty() || pw.isEmpty()) {
                showError("Lütfen tüm alanları doldurun.");
                return;
            }

            // 2) Kullanıcı adı format kontrolü
            if (!USERNAME_REGEX.matcher(u).matches()) {
                username.setInvalid(true);
                username.setErrorMessage("3-30 karakter; sadece harf, rakam ve _");
                showError("Geçersiz kullanıcı adı.");
                return;
            } else {
                username.setInvalid(false);
            }

            // 3) Ad uzunluk
            if (n.length() < MIN_NAME_LENGTH) {
                name.setInvalid(true);
                name.setErrorMessage("Ad en az " + MIN_NAME_LENGTH + " karakter olmalı");
                showError("Lütfen geçerli bir ad girin.");
                return;
            } else {
                name.setInvalid(false);
            }

            // 4) Email format kontrolü (Vaadin'inkine ek olarak kendi regex'imizle)
            if (!EMAIL_REGEX.matcher(em).matches()) {
                email.setInvalid(true);
                email.setErrorMessage("Geçerli bir e-posta adresi girin (örn. ornek@mail.com)");
                showError("E-posta adresi geçerli değil.");
                return;
            } else {
                email.setInvalid(false);
            }

            // 5) Şifre uzunluk
            if (pw.length() < MIN_PASSWORD_LENGTH) {
                password.setInvalid(true);
                password.setErrorMessage("En az " + MIN_PASSWORD_LENGTH + " karakter");
                showError("Şifre en az " + MIN_PASSWORD_LENGTH + " karakter olmalı.");
                return;
            } else {
                password.setInvalid(false);
            }

            // 6) Kullanıcı adı zaten alınmış mı
            if (userDao.getMemberByUsername(u) != null) {
                username.setInvalid(true);
                username.setErrorMessage("Bu kullanıcı adı zaten alınmış");
                showError("Bu kullanıcı adı zaten kullanılıyor — başka bir tane seç.");
                return;
            }

            // 7) E-posta zaten alınmış mı
            if (userDao.getMemberByEmail(em) != null) {
                email.setInvalid(true);
                email.setErrorMessage("Bu e-posta zaten kayıtlı");
                showError("Bu e-posta adresi zaten kayıtlı — giriş yapmayı dene.");
                return;
            }

            // 8) Hepsi OK — kayıt et
            boolean success = manager.registerUser(u, n, em, pw);

            if (success) {
                Notification.show(
                    "✅ Hesabın oluşturuldu! Giriş ekranına yönlendiriliyorsun.",
                    3000, Notification.Position.TOP_CENTER);
                getUI().ifPresent(ui -> ui.navigate("login"));
            } else {
                showError("Kayıt başarısız oldu. Lütfen daha sonra tekrar deneyin.");
            }
        });

        // Girişe dön butonu
        Button backToLoginBtn = new Button("Girişe Dön", VaadinIcon.ARROW_LEFT.create());
        backToLoginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backToLoginBtn.setWidth("320px");
        backToLoginBtn.addClickListener(e -> UI.getCurrent().navigate("login"));

        add(title, username, name, email, password, registerButton, backToLoginBtn);
    }

    /** Kırmızı/uyarı tipi notification gösterir. */
    private void showError(String msg) {
        Notification n = Notification.show(msg, 3500, Notification.Position.TOP_CENTER);
        n.addThemeVariants(com.vaadin.flow.component.notification.NotificationVariant.LUMO_ERROR);
    }
}
