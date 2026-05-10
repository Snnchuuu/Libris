package com.libris.views;
 
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.dependency.StyleSheet;

@StyleSheet("styles/styles.css")
 
/**
 * AdminView
 * This view acts as a redirect for admin users.
 * Since LibraryCatalogView already handles role-based UI,
 * admins are forwarded directly to the catalog.
 */
@Route("admin")
public class AdminView extends VerticalLayout implements BeforeEnterObserver {
 
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Check if user is logged in and is an admin
        Object role = VaadinSession.getCurrent().getAttribute("role");
 
        if (role == null) {
            // Not logged in, send to login
            event.forwardTo("login");
        } else {
            // Admin is logged in, forward to catalog
            // LibraryCatalogView will show admin buttons automatically
            event.forwardTo("katalog");
        }
    }
}
 