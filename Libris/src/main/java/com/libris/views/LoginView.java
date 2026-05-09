package com.libris.views;

import com.libris.UserDAO;
import com.libris.Member;
import com.libris.LibraryManager;
import com.libris.User;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinSession;

// login sayfası

@Route("login")
@PageTitle("Libris - Login")
public class LoginView extends VerticalLayout {

    private LibraryManager manager = new LibraryManager();

    // 🔥 forgot link artık class level
    private Anchor forgotLink = new Anchor("forgot-password", "Hesabımı Unuttum?");

    public LoginView() {

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeightFull();

        add(new H1("Libris Kütüphane Sistemi"));

        TextField username = new TextField("Kullanıcı Adı");
        PasswordField password = new PasswordField("Şifre");
        Button loginButton = new Button("Giriş Yap");

        // başlangıçta gizli
        forgotLink.setVisible(false);

        loginButton.addClickListener(click -> {

            String user = username.getValue();
            String pass = password.getValue();

            UserDAO dao = new UserDAO();

            boolean ok = dao.loginUser(user, pass);

            if (ok) {

                Member member = dao.getMemberByUsername(user);

                if (member != null) {

                    VaadinSession.getCurrent().setAttribute("username", member.getUsername());
                    VaadinSession.getCurrent().setAttribute("role", member.getRole());

                    if ("ADMIN".equals(member.getRole())) {
                        getUI().ifPresent(ui -> ui.navigate("admin"));
                    } else {
                        getUI().ifPresent(ui -> ui.navigate("katalog"));
                    }

                } else {
                    Notification.show("User found but cannot load data!");
                }

            } else {
                Notification.show("Hatalı kullanıcı adı veya şifre!");
                forgotLink.setVisible(true); // 🔥 burada göster
            }
        });

        // UI
        add(username, password, loginButton, forgotLink);
    }

    // ----------------------------------------------------
    // KAYDOL + FORGOT PASSWORD LINKLERİ (istersen sonra ekleriz)
    // ----------------------------------------------------
}