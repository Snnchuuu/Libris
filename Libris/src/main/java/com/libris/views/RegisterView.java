package com.libris.views;

import com.libris.LibraryManager;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;

/* Info:
 * This class represents the user registration page.
 * Users can create a new account using username, email and password.
 */

@Route("register") // localhost:8080/register
@PageTitle("Uye Ol")
public class RegisterView extends VerticalLayout {
	private static final long serialVersionUID = 1L;


    private LibraryManager manager = new LibraryManager();

    public RegisterView() {

        // Page layout settings
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeightFull();

        // Title
        add(new H1("Create New Libris Account"));

        // Fields
        TextField username = new TextField("Username");
        TextField name = new TextField("Full Name"); // ✅ EKLENDİ
        TextField email = new TextField("Email");
        PasswordField password = new PasswordField("Password");

        Button registerButton = new Button("Register");

        registerButton.addClickListener(e -> {

            String u = username.getValue();
            String n = name.getValue(); // ✅ EKLENDİ
            String em = email.getValue();
            String pw = password.getValue();

            // Basic validation
            if (u.isEmpty() || n.isEmpty() || em.isEmpty() || pw.isEmpty()) {
                Notification.show("Please fill all fields!");
                return;
            }

            // ⚠️ BURASI ÖNEMLİ: artık name de gönderilmeli
            boolean success = manager.registerUser(u, n, em, pw);

            if (success) {
                Notification.show("Account created successfully!");
                getUI().ifPresent(ui -> ui.navigate("login"));
            } else {
                Notification.show("Username or email already exists!");
            }
        });

        // Girişe geri dönüş butonu — zaten hesabı olanlar için
        Button backToLoginBtn = new Button("Girişe Dön", VaadinIcon.ARROW_LEFT.create());
        backToLoginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backToLoginBtn.addClickListener(e ->
            UI.getCurrent().navigate("login")
        );

        add(username, name, email, password, registerButton, backToLoginBtn);
    }
}