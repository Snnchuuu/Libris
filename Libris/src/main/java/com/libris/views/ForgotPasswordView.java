package com.libris.views;

import com.libris.UserDAO;
import com.libris.Member;
import com.libris.EmailService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;

/*
 * Info:
 * This class represents the "Forgot Password" page.
 * User enters email and system checks if account exists in database.
 * If found, login info is sent via EmailService.
 * NOTE: This is a student-level demo implementation.
 */

@Route("forgot-password")
@PageTitle("Hesap Kurtarma")
public class ForgotPasswordView extends VerticalLayout {
	private static final long serialVersionUID = 1L;


    public ForgotPasswordView() {

        // ----------------------------------------------------
        // UI DESIGN (centered layout)
        // ----------------------------------------------------
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeightFull();

        add(new H1("Hesap Kurtarma"));

        // ----------------------------------------------------
        // INPUT FIELD
        // ----------------------------------------------------
        TextField emailField = new TextField("E-posta adresi");

        // Send button
        Button sendButton = new Button("Gönder");

        // Register link (only shown if account not found)
        Anchor registerLink = new Anchor("register", "Hesap Oluştur");
        registerLink.setVisible(false);

        // ----------------------------------------------------
        // BUTTON ACTION
        // ----------------------------------------------------
        sendButton.addClickListener(e -> {

            String email = emailField.getValue();

            // Database access object
            UserDAO dao = new UserDAO();

            // Try to find user by email
            Member user = dao.getMemberByEmail(email);

            if (user != null) {

                /*
                 * If user exists:
                 * send login credentials via email service
                 */
                EmailService.sendLoginInfo(
                        email,
                        user.getUsername(),
                        user.getPassword()
                );

                // console debug (for development)
                System.out.println("EMAIL SENT TO: " + email);

                // success message
                Notification.show("Bilgiler e-posta adresine gönderildi!");

                // hide register link
                registerLink.setVisible(false);

            } else {

                /*
                 * If no user found with that email
                 */
                Notification.show("Bu e-posta ile kayıtlı hesap bulunamadı!");

                // show register option
                registerLink.setVisible(true);
            }
        });

        // ----------------------------------------------------
        // ADD COMPONENTS TO PAGE
        // ----------------------------------------------------
        add(emailField, sendButton, registerLink);
    }
}