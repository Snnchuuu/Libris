package com.libris.views;

import com.libris.LibraryManager;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/* Info:
 * This class represents the user registration page.
 * Users can create a new account using username, email and password.
 */

@Route("register") // localhost:8080/register
public class RegisterView extends VerticalLayout {

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

        add(username, name, email, password, registerButton);
    }
}