package com.libris.views;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

// bu sınıfın tek amacı kök dizine girildiğinde yönlendirme yapmaktır

@Route("") // Boş string kök dizin (http://localhost:8080) demektir
public class MainView extends VerticalLayout implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Object role = VaadinSession.getCurrent().getAttribute("role");

        if (role != null) {
            // Eğer giriş yapılmışsa doğrudan kataloğa git
            event.forwardTo("katalog");
        } else {
            // Giriş yapılmamışsa login sayfasına git
            event.forwardTo("login");
        }
    }
}