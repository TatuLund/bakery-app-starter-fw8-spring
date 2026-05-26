package com.vaadin.starter.bakery.ui.views.dashboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.ServiceException;
import com.vaadin.starter.bakery.ui.AbstractUITest;
import com.vaadin.starter.bakery.ui.components.AttributeExtension;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.AriaAttributes;
import com.vaadin.starter.bakery.ui.components.CustomChart;
import com.vaadin.starter.bakery.ui.components.OrdersGrid;
import com.vaadin.ui.AbstractComponent;

public class DashboardViewTest extends AbstractUITest {

    private DashboardView view;

    @Before
    public void setUp() throws ServiceException {
        mockVaadin();
        view = navigate(DashboardView.class);
    }

    @After
    public void cleanUp() {
        tearDown();
    }

    @Test
    public void dashboardExposesAccessibleSummariesForChartsAndGrid() {
        String currentMonth = LocalDate.now().getMonth()
                .getDisplayName(TextStyle.FULL, Locale.US);

        assertTrue(dueGrid().isAccessibleNavigation());
        assertTrue(attribute(deliveriesThisMonthGraph(), AriaAttributes.LABEL)
                .startsWith("Deliveries in " + currentMonth + ": Deliveries:"));
        assertTrue(attribute(deliveriesThisYearGraph(), AriaAttributes.LABEL)
                .startsWith("Deliveries in " + Year.now().getValue()
                        + ": Deliveries:"));
        assertTrue(attribute(yearlySalesGraph(), AriaAttributes.LABEL)
                .startsWith("Sales last years"));
        assertTrue(attribute(monthlyProductSplit(), AriaAttributes.LABEL)
                .startsWith("Products delivered in " + currentMonth));
        assertEquals("dueGrid", dueGrid().getId());
    }

    private OrdersGrid dueGrid() {
        return $(view, OrdersGrid.class).id("dueGrid");
    }

    private CustomChart deliveriesThisMonthGraph() {
        return $(view, CustomChart.class).id("deliveriesThisMonth");
    }

    private CustomChart deliveriesThisYearGraph() {
        return $(view, CustomChart.class).id("deliveriesThisYear");
    }

    private CustomChart yearlySalesGraph() {
        return $(view, CustomChart.class).id("yearlySales");
    }

    private CustomChart monthlyProductSplit() {
        return $(view, CustomChart.class).id("monthlyProductSplit");
    }

    private String attribute(AbstractComponent component, String name) {
        return AttributeExtension.of(component).getAttribute(name);
    }
}