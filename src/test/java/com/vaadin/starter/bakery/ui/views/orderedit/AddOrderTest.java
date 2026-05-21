package com.vaadin.starter.bakery.ui.views.orderedit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.Test;

import com.vaadin.starter.bakery.backend.data.OrderState;
import com.vaadin.starter.bakery.backend.data.entity.Order;

public class AddOrderTest extends AbstractOrderEditTest {

    @Test
    public void emptyAddOrderView() {
        openNewOrder();

        assertFalse(reportHeader().isVisible());
        assertEquals(OrderEditView.Mode.EDIT, view.getMode());
        assertEnabledWithCaption(cancelButton(), "Cancel");
        assertEnabledWithCaption(okButton(), "Review order");
        assertEnabledWithCaption(addItemsButton(), "Add item");
    }

    @Test
    public void addOrder() {
        ExpectedOrder draft = sampleDraftOrder();

        openNewOrder();
        fillOrderForm(draft);

        assertEquals(draft.total, totalLabel().getValue());

        test(addItemsButton()).click();
        test(addItemsButton()).click();
        setProductLine(numberOfProducts() - 1, draft.products.get(0));
        assertEquals(draft.products.size() + 2, numberOfProducts());
        test($(productInfo(numberOfProducts() - 1), com.vaadin.ui.Button.class)
                .id("delete")).click();
        assertEquals(draft.products.size() + 1, numberOfProducts());

        test(okButton()).click();

        assertEquals(OrderEditView.Mode.CONFIRMATION, view.getMode());
        assertEnabledWithCaption(okButton(), "Place order");
        assertOrder(draft);
        assertEquals(draft.products.size(), numberOfProducts());

        test(okButton()).click();

        Long orderId = currentOrderId();
        assertNotNull(orderId);
        assertTrue(orderIdLabel().getValue().matches("#\\d+"));
        assertEquals(orderViewId() + "/" + orderId, currentStatePath());
        assertEnabledWithCaption(cancelButton(), "Edit");
        assertEnabledWithCaption(okButton(), "Mark as Confirmed");
        assertOrder(draft);

        Order persisted = orderService.findOrder(orderId);
        assertNotNull(persisted);

        openOrder(orderId);
        assertOrder(draft);
    }

    @Test
    public void changeStateForNewOrder() {
        ExpectedOrder draft = expectedOrder(LocalDate.of(2026, 12, 5),
                LocalTime.of(8, 0), defaultPickupLocation(), "fullname",
                "phone", "detailss", line(anyProducts(1).get(0), 12,
                        "A comment"));

        openNewOrder();
        fillOrderForm(draft);

        test(okButton()).click();
        test(okButton()).click();

        assertEquals(OrderState.NEW,
                OrderState.forDisplayName(stateLabel().getValue()));

        test(cancelButton()).click();
        assertEquals(OrderEditView.Mode.EDIT, view.getMode());
        stateField().setValue(OrderState.CONFIRMED);
        test(okButton()).focus();
        test(okButton()).click();

        assertEquals(OrderState.CONFIRMED,
                OrderState.forDisplayName(stateLabel().getValue()));
    }

    @Test
    public void confirmDialogWhenAbandoningNewOrder() {
        openNewOrder();

        test(fullNameField()).setValue("Something");

        test(storefrontButton()).click();
        assertNotNull(currentOrderEditView());
        assertNotNull(confirmCancelButton());
        test(confirmCancelButton()).click();

        test(logoutButton()).click();
        assertNotNull(currentOrderEditView());
        assertNotNull(confirmCancelButton());
        test(confirmCancelButton()).click();
    }

    private ExpectedOrder sampleDraftOrder() {
        List<com.vaadin.starter.bakery.backend.data.entity.Product> products = anyProducts(
                2);
        return expectedOrder(LocalDate.of(2026, 12, 5), LocalTime.of(8, 0),
                defaultPickupLocation(), "First Last", "Phone", "Details",
                line(products.get(0), 2, "Lactose free"),
                line(products.get(1), 1, ""));
    }
}