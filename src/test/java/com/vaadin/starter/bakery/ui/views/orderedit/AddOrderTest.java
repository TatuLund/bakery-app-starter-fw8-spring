package com.vaadin.starter.bakery.ui.views.orderedit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import com.vaadin.data.ValueContext;
import com.vaadin.starter.bakery.backend.data.OrderState;
import com.vaadin.starter.bakery.backend.data.entity.Order;
import com.vaadin.starter.bakery.backend.data.entity.Product;
import com.vaadin.starter.bakery.ui.components.AttributeExtension;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.AriaAttributes;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;

public class AddOrderTest extends AbstractOrderEditTest {

    @Test
    public void emptyAddOrderView() {
        openNewOrder();

        assertFalse(reportHeader().isVisible());
        assertEquals(OrderEditView.Mode.EDIT, view.getMode());
        assertEnabledWithCaption(editCancelButton(), "Cancel");
        assertEnabledWithCaption(okButton(), "Review order");
        assertEnabledWithCaption(addItemsButton(), "Add item");
    }

    @Test
    public void addOrderViewExposesAccessibilityMetadata() {
        openNewOrder();

        assertEquals("dueLabel",
                attribute(dueDateField(), AriaAttributes.DESCRIBEDBY));
        assertEquals("Date", attribute(dueDateField(), AriaAttributes.LABEL));
        assertEquals("dueLabel",
                attribute(dueTimeField(), AriaAttributes.DESCRIBEDBY));
        assertEquals("Time", attribute(dueTimeField(), AriaAttributes.LABEL));
        assertEquals("customerLabel",
                attribute(fullNameField(), AriaAttributes.DESCRIBEDBY));
        assertEquals("Full name",
                attribute(fullNameField(), AriaAttributes.LABEL));
        assertEquals("Total price",
                attribute(totalLabel(), AriaAttributes.LABEL));
        assertEquals("polite", attribute(totalLabel(), AriaAttributes.LIVE));
        assertEquals("group", attribute(productInfo(0), AriaAttributes.ROLE));
        assertEquals("Product information", attribute(productInfo(0),
                AriaAttributes.LABEL));
        assertEquals("Quantity",
                attribute(quantityField(productInfo(0)), AriaAttributes.LABEL));
        assertEquals("Delete product entry",
                deleteButton(productInfo(0)).getDescription());
    }

    @Test
    public void changingQuantityAnnouncesLinePriceAssistively() {
        Product product = anyProducts(1).get(0);

        openNewOrder();
        productField(productInfo(0)).setValue(product);
        test(quantityField(productInfo(0))).setValue("3");

        assertNotNull(lastNotification());
        assertEquals(String.format("Quantity 3, line price $%s",
                priceConverter.convertToPresentation(product.getPrice() * 3,
                        new ValueContext(Locale.US))),
                lastNotification().getCaption());
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
        test($(productInfo(numberOfProducts() - 1), Button.class)
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
        assertEnabledWithCaption(editCancelButton(), "Edit");
        assertEnabledWithCaption(okButton(), "Mark as Confirmed");
        assertOrder(draft);

        Order persisted = orderService.findOrder(orderId);
        assertNotNull(persisted);

        openOrder(orderId);
        assertOrder(draft);
    }

    @Test
    public void pastDueDateIsNotAccepted() {
        ExpectedOrder draft = sampleDraftOrder();
        draft.dueDate = LocalDate.now().minusDays(1);

        openNewOrder();
        fillOrderForm(draft);

        test(okButton()).click();

        assertEquals(OrderEditView.Mode.EDIT, view.getMode());
        assertNotNull(dueDateField().getComponentError());
    }

    @Test
    public void changeStateForNewOrder() {
        ExpectedOrder draft = expectedOrder(LocalDate.of(2026, 12, 5),
                LocalTime.of(8, 0), defaultPickupLocation(), "fullname",
                "phone", "detailss",
                line(anyProducts(1).get(0), 12, "A comment"));

        openNewOrder();
        fillOrderForm(draft);

        test(okButton()).click();
        test(okButton()).click();

        assertEquals(OrderState.NEW,
                OrderState.forDisplayName(stateLabel().getValue()));

        test(editCancelButton()).click();
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
        List<Product> products = anyProducts(2);
        return expectedOrder(LocalDate.of(2026, 12, 5),
                LocalTime.of(8, 0),
                defaultPickupLocation(), "First Last", "Phone",
                "Details",
                line(products.get(0), 2, "Lactose free"),
                line(products.get(1), 1, ""));
    }

    private String attribute(AbstractComponent component, String name) {
        return AttributeExtension.of(component).getAttribute(name);
    }

    private Button deleteButton(ProductInfo row) {
        return $(row, Button.class).id("delete");
    }

    private TextField quantityField(ProductInfo row) {
        return $(row, TextField.class).id("quantity");
    }

    private ProductComboBox productField(ProductInfo row) {
        return $(row, ProductComboBox.class).id("product");
    }
}