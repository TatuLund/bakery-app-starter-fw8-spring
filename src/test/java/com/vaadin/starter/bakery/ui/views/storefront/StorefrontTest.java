package com.vaadin.starter.bakery.ui.views.storefront;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.ServiceException;
import com.vaadin.starter.bakery.backend.data.entity.Order;
import com.vaadin.starter.bakery.ui.AbstractUITest;
import com.vaadin.starter.bakery.ui.components.OrdersGrid;
import com.vaadin.starter.bakery.ui.navigation.NavigationManager;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

public class StorefrontTest extends AbstractUITest {

    private NavigationManager navigationManager;
    private StorefrontView view;

    @Before
    public void setUp() throws ServiceException {
        mockVaadin();

        navigationManager = getApplicationContext()
                .getBean(NavigationManager.class);
        view = navigate(StorefrontView.class);
    }

    @After
    public void cleanUp() {
        tearDown();
    }

    @Override
    protected void configureSecurityContext() {
        authenticateAsBarista();
    }

    @Test
    public void gridContainsData() {
        assertTrue(
                "With the generated data, there should be at least twenty rows in the grid",
                test(list()).size() > 20);
        assertTrue(list().isAccessibleNavigation());

        Order firstOrder = test(list()).item(0);
        assertEquals(
                "With the generated data, there should be at least one order due today",
                LocalDate.now(), firstOrder.getDueDate());
        assertTrue("The customer part should contain data",
                firstOrder.getCustomer().getFullName().length() > 10);
        assertFalse("The order should contain products",
                firstOrder.getItems().isEmpty());
    }

    @Test
    public void filterUsingUrl() {
        view = navigate(viewId() + "/search=kerry", StorefrontView.class);

        assertEquals("kerry", searchField().getValue());
        assertEquals(viewId() + "/search=kerry", currentState());
        assertTrue(test(list()).size() > 0);
        assertTrue(test(list()).size() < 100);
        assertAllVisibleCustomersContain("kerry");
    }

    @Test
    public void filterUsingSearchField() {
        test(searchField()).setValue("pickett");
        test(searchButton()).click();

        int rowCount = test(list()).size();
        assertEquals("pickett", searchField().getValue());
        assertEquals(viewId() + "/search=pickett", currentState());
        assertTrue(
                "The row count of storefront list was expected to have more than zero rows. Rows in list: "
                        + rowCount,
                rowCount > 0);
        assertTrue(
                "The row count of storefront list was expected to be less than 100. Rows in list: "
                        + rowCount,
                rowCount < 100);
        assertAllVisibleCustomersContain("pickett");
    }

    @Test
    public void searchControlsExposeAccessibilityMetadata() {
        assertEquals("Search orders", searchButton().getDescription());
        assertTrue(list().isAccessibleNavigation());
    }

    private void assertAllVisibleCustomersContain(String filter) {
        for (int row = 0; row < test(list()).size(); row++) {
            assertTrue(test(list()).item(row).getCustomer().getFullName()
                    .toLowerCase().contains(filter));
        }
    }

    private OrdersGrid list() {
        return $(view, OrdersGrid.class).get(0);
    }

    private TextField searchField() {
        return $(view, TextField.class).get(0);
    }

    private Button searchButton() {
        return $(view, Button.class).get(0);
    }

    @SuppressWarnings("unused")
    private CheckBox includePast() {
        return $(view, CheckBox.class).get(0);
    }

    private String currentState() {
        return UI.getCurrent().getNavigator().getState();
    }

    private String viewId() {
        return navigationManager.getViewId(StorefrontView.class);
    }
}