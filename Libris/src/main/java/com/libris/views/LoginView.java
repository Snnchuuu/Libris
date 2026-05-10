package com.libris.views;
 
import com.libris.UserDAO;
import com.libris.Member;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinSession;
 
/**
 * LoginView
 * Handles user login with email and password.
 * Shows Libris logo and routes to catalog after successful login.
 */
@Route("login")
@PageTitle("Libris - Giriş")
public class LoginView extends VerticalLayout {
 
    private Anchor forgotLink = new Anchor("forgot-password", "Hesabımı Unuttum?");
 
    public LoginView() {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeightFull();
 
        // Logo
        Image logo = new Image("images/logo.png", "Libris Logo");
        logo.setHeight("180px");
        logo.getStyle().set("margin-bottom", "8px");
 
        // Title
        H1 title = new H1("Libris Kütüphane Sistemi");
        title.getStyle()
            .set("font-size", "1.8rem")
            .set("margin-top", "0")
            .set("margin-bottom", "24px")
            .set("color", "#4A3010");
 
        // Input fields
        TextField emailField = new TextField("E-posta");
        emailField.setWidth("320px");
 
        PasswordField passwordField = new PasswordField("Şifre");
        passwordField.setWidth("320px");
 
        Button loginButton = new Button("Giriş Yap");
        loginButton.setWidth("320px");
        loginButton.getStyle()
            .set("margin-top", "8px")
            .set("height", "42px");
        loginButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
 
        // Register link
        Anchor registerLink = new Anchor("register", "Hesabın yok mu? Kayıt ol");
        registerLink.getStyle().set("margin-top", "8px").set("color", "#7A5520");
 
        forgotLink.setVisible(false);
        forgotLink.getStyle().set("color", "#7A5520");
 
        loginButton.addClickListener(click -> {
            String email = emailField.getValue();
            String pass  = passwordField.getValue();
 
            UserDAO dao = new UserDAO();
            boolean ok  = dao.loginUser(email, pass);
 
            if (ok) {
                Member member = dao.getMemberByEmail(email);
 
                if (member != null) {
                    VaadinSession.getCurrent().setAttribute("username", member.getUsername());
                    VaadinSession.getCurrent().setAttribute("role", member.getRole());
                    VaadinSession.getCurrent().setAttribute("userId", member.getId());
                    getUI().ifPresent(ui -> ui.navigate("katalog"));
                } else {
                    Notification.show("Kullanıcı verisi yüklenemedi!");
                }
            } else {
                Notification.show("Hatalı e-posta veya şifre!");
                forgotLink.setVisible(true);
            }
        });
 
        add(logo, title, emailField, passwordField, loginButton, forgotLink, registerLink);
    }
}