package com.vaadin.starter.bakery.ui;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.ServiceException;
import com.vaadin.starter.bakery.ui.components.OrdersGrid;
import com.vaadin.starter.bakery.ui.views.dashboard.DashboardView;
import com.vaadin.ui.UI;

public class SpringNavigationTest extends AbstractUITest {

    @Before
    public void setUp() throws ServiceException {
        mockVaadin();
    }

    @After
    public void cleanUp() {
        tearDown();
    }

    @Test
    public void navigateToSpringView_resolvesAutowiredDependencies() {
        DashboardView view = navigate(DashboardView.class);

        assertNotNull(view);
        assertSame(view, UI.getCurrent().getNavigator().getCurrentView());
        assertNotNull($(OrdersGrid.class).id("dueGrid"));
    }
}