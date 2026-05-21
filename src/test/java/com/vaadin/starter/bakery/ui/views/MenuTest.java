package com.vaadin.starter.bakery.ui.views;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;

import com.vaadin.server.ServiceException;
import com.vaadin.starter.bakery.ui.AbstractUITest;
import com.vaadin.starter.bakery.ui.MainView;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;

public class MenuTest extends AbstractUITest {

    private Runnable authenticator = this::authenticateAsAdmin;

    @After
    public void cleanUp() {
        tearDown();
    }

    @Override
    protected void configureSecurityContext() {
        authenticator.run();
    }

    @Test
    public void adminSeesAdminMenus() throws ServiceException {
        bootAsAdmin();

        assertTrue(usersButton().isVisible());
        assertTrue(productsButton().isVisible());
        assertTrue(logoutButton().isVisible());
    }

    @Test
    public void baristaDoesNotSeeAdminMenus() throws ServiceException {
        bootAsBarista();

        assertFalse(usersButton().isVisible());
        assertFalse(productsButton().isVisible());
        assertTrue(logoutButton().isVisible());
    }

    private void bootAsAdmin() throws ServiceException {
        authenticator = this::authenticateAsAdmin;
        mockVaadin();
        assertNotNull(mainView());
    }

    private void bootAsBarista() throws ServiceException {
        authenticator = this::authenticateAsBarista;
        mockVaadin();
        assertNotNull(mainView());
    }

    private MainView mainView() {
        return (MainView) UI.getCurrent().getContent();
    }

    private Button usersButton() {
        return $(mainView(), Button.class).id("users");
    }

    private Button productsButton() {
        return $(mainView(), Button.class).id("products");
    }

    private Button logoutButton() {
        return $(mainView(), Button.class).id("logout");
    }
}