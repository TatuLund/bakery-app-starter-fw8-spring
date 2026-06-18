package com.vaadin.starter.bakery.ui.views.orderedit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

import org.junit.Test;

import com.vaadin.data.ValueContext;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.starter.bakery.backend.data.OrderState;
import com.vaadin.starter.bakery.backend.data.entity.Order;
import com.vaadin.starter.bakery.backend.data.entity.Product;
import com.vaadin.starter.bakery.ui.views.storefront.StorefrontView;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;

public class UpdateOrderTest extends AbstractOrderEditTest {

    private static final String CONCURRENT_UPDATE_MESSAGE = "Somebody else might have updated the data. Please refresh and try again.";

    @Test
    public void addHistoryComment() {
        OrderFixture fixture = persist(sampleExistingOrder());
        openOrder(fixture.order.getId());

        List<HistoryEntry> initialEntries = historyEntries();
        int initialSize = initialEntries.size();

        addHistoryComment("foo");

        List<HistoryEntry> updatedEntries = historyEntries();
        assertEquals(initialSize + 1, updatedEntries.size());
        HistoryEntry lastEntry = updatedEntries.get(initialSize);
        assertEquals("foo", lastEntry.message);
        assertEquals("Malin", lastEntry.author);
        assertWithinLastFiveMinutes(lastEntry.date);

        openOrder(fixture.order.getId());
        updatedEntries = historyEntries();
        assertEquals(initialSize + 1, updatedEntries.size());
        lastEntry = updatedEntries.get(initialSize);
        assertEquals("foo", lastEntry.message);
        assertEquals("Malin", lastEntry.author);
        assertWithinLastFiveMinutes(lastEntry.date);
    }

    @Test
    public void focusingHistoryCommentAnnouncesOrderHistoryAssistively() {
        OrderFixture fixture = persist(sampleExistingOrder());
        openOrder(fixture.order.getId());

        List<HistoryEntry> entries = historyEntries();

        test(newCommentInput()).focus();

        assertNotNull(lastNotification());
        assertEquals(expectedHistoryAnnouncement(entries),
                lastNotification().getCaption());
    }

    @Test
    public void updateOrderInfo() {
        OrderFixture fixture = persist(sampleExistingOrder());
        openOrder(fixture.order.getId());

        test(editCancelButton()).click();
        assertEnabledWithCaption(editCancelButton(), "Cancel");
        assertEnabledWithCaption(okButton(), "Save");

        ExpectedOrder updated = copyOrder(fixture.expected);
        updated.dueDate = updated.dueDate.plusDays(1);
        updated.dueTime = updated.dueTime.plusHours(1);
        updated.state = OrderState.CONFIRMED;
        updated.fullName = updated.fullName + "-updated";
        updated.phone = updated.phone + "-updated";
        updated.details = updated.details + "-updated";
        updated.pickupLocation = alternatePickupLocation(
                updated.pickupLocation);
        updated.pickupLocationName = updated.pickupLocation.getName();
        for (ExpectedProductLine line : updated.products) {
            line.quantity = line.quantity + 1;
            line.comment = line.comment + "-updated";
        }
        updated.total = priceConverter.convertToPresentation(
                updated.products.stream()
                        .mapToInt(line -> line.quantity * line.price).sum(),
                new ValueContext(Locale.US));

        dueDateField().setValue(updated.dueDate);
        dueTimeField().setValue(updated.dueTime);
        stateField().setValue(updated.state);
        test(fullNameField()).setValue(updated.fullName);
        test(phoneField()).setValue(updated.phone);
        test(detailsField()).setValue(updated.details);
        pickupLocationField().setValue(updated.pickupLocation);
        for (int index = 0; index < updated.products.size(); index++) {
            setProductLine(index, updated.products.get(index));
        }

        test(okButton()).click();

        assertEnabledWithCaption(editCancelButton(), "Edit");
        assertOrder(updated);
    }

    @Test
    public void deliveredOrderDisablesCancelButton() {
        OrderFixture fixture = persist(sampleExistingOrder());
        Order deliveredOrder = orderService.findOrder(fixture.order.getId());
        deliveredOrder.setState(OrderState.DELIVERED);
        orderService.saveOrder(deliveredOrder, bakerUser());

        openOrder(fixture.order.getId());

        assertFalse(editCancelButton().isEnabled());
        assertEquals("Edit", editCancelButton().getCaption());
    }

    @Test
    public void updateButCancel() {
        // GIVEN: An existing order is opened for editing
        OrderFixture fixture = persist(sampleExistingOrder());
        openOrder(fixture.order.getId());

        // Click cancel/edit to enter edit mode
        test(editCancelButton()).click();

        test(fullNameField()).setValue(fixture.expected.fullName + "-updated");
        test(phoneField()).setValue(fixture.expected.phone + "-updated");
        test(detailsField()).setValue(fixture.expected.details + "-updated");
        setProductLine(0, line(fixture.expected.products.get(0).product,
                fixture.expected.products.get(0).quantity + 1,
                fixture.expected.products.get(0).comment + "-updated"));
    
        // WHEN: Clicking cancel
        test(editCancelButton()).click();

        // THEN: We edits are discarded and form is not editable
        assertOrder(fixture.expected);

        assertFalse(test(fullNameField()).isInteractable());
        assertFalse(test(phoneField()).isInteractable());
        assertFalse(test(detailsField()).isInteractable());
        assertFalse(test(fullNameField()).isInteractable());
        assertFalse(test(pickupLocationField()).isInteractable());
    }

    @Test
    public void updateButCancelByEsc() {
        // GIVEN: An existing order is opened for editing
        OrderFixture fixture = persist(sampleExistingOrder());
        openOrder(fixture.order.getId());

        // Click cancel/edit to enter edit mode
        test(editCancelButton()).click();

        test(fullNameField()).setValue(fixture.expected.fullName + "-updated");
        test(phoneField()).setValue(fixture.expected.phone + "-updated");
        test(detailsField()).setValue(fixture.expected.details + "-updated");
        setProductLine(0, line(fixture.expected.products.get(0).product,
                fixture.expected.products.get(0).quantity + 1,
                fixture.expected.products.get(0).comment + "-updated"));
    
        // WHEN: Pressing escape to cancel
        test($(OrderEditView.class).first()).shortcut(KeyCode.ESCAPE);

        // THEN: We are back to StorefrontView
        assertNotNull($(StorefrontView.class).first());
    }

    @Test
    public void emptyProductRowsDoNotPreventSave() {
        OrderFixture fixture = persist(sampleExistingOrder());
        openOrder(fixture.order.getId());

        test(editCancelButton()).click();
        int originalCount = numberOfProducts();
        test(addItemsButton()).click();
        test(addItemsButton()).click();
        test(addItemsButton()).click();
        assertEquals(originalCount + 3, numberOfProducts());

        test(okButton()).click();

        assertEnabledWithCaption(editCancelButton(), "Edit");
        assertEquals(originalCount, numberOfProducts());
    }

    @Test
    public void confirmDialogAfterCustomerChanges() {
        OrderFixture fixture = persist(sampleExistingOrder());
        openOrder(fixture.order.getId());

        test(editCancelButton()).click();
        test(fullNameField()).setValue(fixture.expected.fullName + "foo");

        assertConfirmationDialogBlocksLeaving();
    }

    @Test
    public void confirmDialogAfterCustomerChanges_whenPressingEsc() {
        OrderFixture fixture = persist(sampleExistingOrder());
        openOrder(fixture.order.getId());

        test(editCancelButton()).click();
        test(fullNameField()).setValue(fixture.expected.fullName + "foo");

        assertConfirmationDialogBlocksLeavingEsc();
    }

    @Test
    public void confirmDialogAfterProductChanges() {
        OrderFixture fixture = persist(sampleExistingOrder());
        openOrder(fixture.order.getId());

        test(editCancelButton()).click();
        test($(productInfo(0), TextField.class).id("quantity"))
                .setValue(String.valueOf(
                        fixture.expected.products.get(0).quantity + 1));

        assertConfirmationDialogBlocksLeaving();
    }

    @Test
    public void confirmDialogAfterProductAdd() {
        OrderFixture fixture = persist(sampleExistingOrder());
        openOrder(fixture.order.getId());

        test(editCancelButton()).click();
        test(addItemsButton()).click();
        Product alternateProduct = alternateProduct(
                fixture.expected.products.get(0).product);
        setProductLine(numberOfProducts() - 1, line(alternateProduct, 1, ""));

        assertConfirmationDialogBlocksLeaving();
    }

    @Test
    public void confirmDialogAfterProductDelete() {
        OrderFixture fixture = persist(sampleExistingOrder());
        openOrder(fixture.order.getId());

        test(editCancelButton()).click();
        test($(productInfo(0), Button.class).id("delete")).click();

        assertConfirmationDialogBlocksLeaving();
    }

    @Test
    public void concurrentEditing() {
        OrderFixture fixture = persist(sampleExistingOrder());
        openOrder(fixture.order.getId());

        test(editCancelButton()).click();
        test(fullNameField())
                .setValue(fixture.expected.fullName + "-edited-by-user-1");

        Order updatedByOtherUser = orderService
                .findOrder(fixture.order.getId());
        updatedByOtherUser.getCustomer().setFullName(
                updatedByOtherUser.getCustomer().getFullName()
                        + "-edited-by-user-2");
        orderService.saveOrder(updatedByOtherUser, bakerUser());

        test(okButton()).click();

        assertEnabledWithCaption(editCancelButton(), "Cancel");
        assertNotNull(lastNotification());
        assertEquals(CONCURRENT_UPDATE_MESSAGE,
                lastNotification().getCaption());
    }

    private ExpectedOrder sampleExistingOrder() {
        List<Product> products = anyProducts(2);
        return expectedOrder(LocalDate.of(2026, 11, 20), LocalTime.of(8, 0),
                defaultPickupLocation(), "First Last", "Phone", "Details",
                line(products.get(0), 2, "Comment 1"),
                line(products.get(1), 1, "Comment 2"));
    }

    private String expectedHistoryAnnouncement(List<HistoryEntry> entries) {
        StringJoiner joiner = new StringJoiner(". ", "Order history. ", ".");
        entries.forEach(entry -> joiner.add(
                entry.date + " by " + entry.author + ": " + entry.message));
        return joiner.toString();
    }
}