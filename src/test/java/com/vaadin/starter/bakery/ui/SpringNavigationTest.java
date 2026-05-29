package com.vaadin.starter.bakery.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.ServiceException;
import com.vaadin.shared.Position;
import com.vaadin.starter.bakery.ui.components.OrdersGrid;
import com.vaadin.starter.bakery.ui.views.AccessDeniedView;
import com.vaadin.starter.bakery.ui.views.dashboard.DashboardView;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

public class SpringNavigationTest extends AbstractUITest {

    private Runnable authenticator = this::authenticateAsAdmin;

    @Before
    public void setUp() throws ServiceException {
        mockVaadin();
    }

    @After
    public void cleanUp() {
        tearDown();
    }

    @Override
    protected void configureSecurityContext() {
        authenticator.run();
    }

    @Test
    public void navigateToSpringView_resolvesAutowiredDependencies() {
        DashboardView view = navigate(DashboardView.class);

        assertNotNull(view);
        assertSame(view, UI.getCurrent().getNavigator().getCurrentView());
        assertNotNull($(OrdersGrid.class).id("dueGrid"));
    }

    @Test
    public void baristaNavigatingToUserAdmin_seesAccessDeniedAssistively()
            throws ServiceException {
        tearDown();
        authenticator = this::authenticateAsBarista;
        mockVaadin();

        AccessDeniedView view = navigate("user-admin", AccessDeniedView.class);

        assertNotNull(view);
        assertSame(view, UI.getCurrent().getNavigator().getCurrentView());
        assertNotNull($(view, Label.class).id("accessDeniedLabel"));
        assertNotNull(lastNotification());
        assertEquals(Position.ASSISTIVE, lastNotification().getPosition());
        assertEquals(
                "Access denied. You do not have permission to view this page.",
                lastNotification().getCaption());
    }

    private Notification lastNotification() {
        return $(Notification.class).last();
    }
}