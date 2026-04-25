package com.libris.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

// login sayfası

@Route("login") // Tarayıcıda localhost:8080/login yazınca bu ekran açılır
public class LoginView extends VerticalLayout {

    public LoginView() {
        // Ekranın ortalanması için ayarlar
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeightFull();

        add(new H1("Libris Kütüphane Sistemi"));

        TextField username = new TextField("Kullanıcı Adı");
        PasswordField password = new PasswordField("Şifre");
        Button loginButton = new Button("Giriş Yap");

        loginButton.addClickListener(click -> {
            String user = username.getValue();
            String pass = password.getValue();

            // giriş bilgileri burda, database eklendiğinden oradan kontrol edilecek
            if ("admin".equals(user) && "123".equals(pass)) {
                // Session'a ADMIN rolünü kaydet
                VaadinSession.getCurrent().setAttribute("role", "ADMIN");
                VaadinSession.getCurrent().setAttribute("username", "Yönetici");
                getUI().ifPresent(ui -> ui.navigate("katalog"));
            } else if ("user".equals(user) && "123".equals(pass)) {
                // Session'a USER rolünü kaydet
                VaadinSession.getCurrent().setAttribute("role", "USER");
                VaadinSession.getCurrent().setAttribute("username", "Abdulkadir Ustaoğlu");
                getUI().ifPresent(ui -> ui.navigate("katalog"));
            } else {
                Notification.show("Hatalı kullanıcı adı veya şifre!");
            }
        });

        add(username, password, loginButton);
    }
}