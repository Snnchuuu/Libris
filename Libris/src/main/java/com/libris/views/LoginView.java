package com.libris.views;

import com.libris.UserDAO;
import com.libris.Member;
import com.libris.LibraryManager;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinSession;

// Giriş sayfası — kullanıcı adı ve şifre ile oturum açılır.
// Hatalı girişte "Hesabımı Unuttum" linki belirir.
// Altında kayıt sayfasına yönlendiren link bulunur.

@Route("login")
@PageTitle("Libris - Login")
public class LoginView extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    // Hatalı girişte gösterilecek link — başta gizli
    private Anchor forgotLink = new Anchor("forgot-password", "Hesabımı Unuttum?");

    public LoginView() {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeightFull();

        add(new H1("Libris Kütüphane Sistemi"));

        TextField username     = new TextField("Kullanıcı Adı");
        PasswordField password = new PasswordField("Şifre");
        Button loginButton     = new Button("Giriş Yap");

        // Hatalı giriş yapılana kadar gizli kalır
        forgotLink.setVisible(false);

        loginButton.addClickListener(click -> {
            String user = username.getValue();
            String pass = password.getValue();

            UserDAO dao = new UserDAO();
            boolean ok  = dao.loginUser(user, pass);

            if (ok) {
                Member member = dao.getMemberByUsername(user);

                if (member != null) {
                    // Oturum bilgilerini session'a kaydet
                    VaadinSession.getCurrent().setAttribute("username", member.getUsername());
                    VaadinSession.getCurrent().setAttribute("role",     member.getRole());

                    // Admin ve üye aynı sayfaya gidiyor;
                    // içerik katalog sayfasında role göre ayrışıyor
                    getUI().ifPresent(ui -> ui.navigate("katalog"));
                } else {
                    Notification.show("Kullanıcı bulundu fakat veriler yüklenemedi!");
                }
            } else {
                // Hatalı girişte şifre hatırlatma linkini göster
                Notification.show("Hatalı kullanıcı adı veya şifre!");
                forgotLink.setVisible(true);
            }
        });

        // "Hesap oluştur" linki — RegisterView'a yönlendirir
        // HorizontalLayout ile "Hesabın yok mu?" yazısı ve link yan yana durur
        HorizontalLayout registerRow = new HorizontalLayout();
        registerRow.setAlignItems(Alignment.CENTER);

        Anchor registerLink = new Anchor("register", "Kayıt Ol");
        registerRow.add(registerLink);

        // Bileşenleri sırayla ekle
        add(username, password, loginButton, forgotLink, registerRow);
    }
}