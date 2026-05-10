package com.libris.views;
 
import com.libris.UserDAO;
import com.libris.Member;
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
 
/**
 * LoginView
 * Handles user login with email and password.
 * Checks credentials against the database,
 * stores session info, and routes to catalog.
 */
@Route("login")
@PageTitle("Libris - Login")
public class LoginView extends VerticalLayout {
 
    // Forgot password link (hidden until login fails)
    private Anchor forgotLink = new Anchor("forgot-password", "Hesabımı Unuttum?");
 
    public LoginView() {
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeightFull();
 
        add(new H1("Libris Kütüphane Sistemi"));
 
        // Email field 
        TextField emailField = new TextField("E-posta");
        PasswordField passwordField = new PasswordField("Şifre");
        Button loginButton = new Button("Giriş Yap");
 
        // Hide forgot link initially
        forgotLink.setVisible(false);
 
        loginButton.addClickListener(click -> {
            String email = emailField.getValue();
            String pass  = passwordField.getValue();
 
            UserDAO dao = new UserDAO();
 
            // Check credentials using email and password
            boolean ok = dao.loginUser(email, pass);
 
            if (ok) {
                // Fetch full user object from database using email
                Member member = dao.getMemberByEmail(email);
 
                if (member != null) {
                    // Store session attributes for use across views
                    VaadinSession.getCurrent().setAttribute("username", member.getUsername());
                    VaadinSession.getCurrent().setAttribute("role", member.getRole());
                    VaadinSession.getCurrent().setAttribute("userId", member.getId());
 
                    // Both admin and member go to catalog
                    // LibraryCatalogView handles admin/member UI differences
                    getUI().ifPresent(ui -> ui.navigate("katalog"));
 
                } else {
                    Notification.show("Kullanıcı verisi yüklenemedi!");
                }
 
            } else {
                // Login failed — show error and forgot link
                Notification.show("Hatalı e-posta veya şifre!");
                forgotLink.setVisible(true);
            }
        });
 
        add(emailField, passwordField, loginButton, forgotLink);
    }
}