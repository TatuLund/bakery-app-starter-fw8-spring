package com.vaadin.starter.bakery.ui.views.orderedit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.springframework.data.domain.PageRequest;

import com.vaadin.data.ValueContext;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.server.ServiceException;
import com.vaadin.starter.bakery.backend.data.OrderState;
import com.vaadin.starter.bakery.backend.data.entity.Customer;
import com.vaadin.starter.bakery.backend.data.entity.Order;
import com.vaadin.starter.bakery.backend.data.entity.OrderItem;
import com.vaadin.starter.bakery.backend.data.entity.PickupLocation;
import com.vaadin.starter.bakery.backend.data.entity.Product;
import com.vaadin.starter.bakery.backend.data.entity.User;
import com.vaadin.starter.bakery.backend.service.OrderService;
import com.vaadin.starter.bakery.backend.service.PickupLocationService;
import com.vaadin.starter.bakery.backend.service.ProductService;
import com.vaadin.starter.bakery.backend.service.UserService;
import com.vaadin.starter.bakery.ui.AbstractUITest;
import com.vaadin.starter.bakery.ui.navigation.NavigationManager;
import com.vaadin.starter.bakery.ui.utils.DollarPriceConverter;
import com.vaadin.starter.bakery.ui.views.storefront.StorefrontView;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

public abstract class AbstractOrderEditTest extends AbstractUITest {

    protected static class ExpectedProductLine {
        Product product;
        String productName;
        int quantity;
        String comment;
        int price;

        ExpectedProductLine(Product product, int quantity, String comment) {
            this.product = product;
            this.productName = product.getName();
            this.quantity = quantity;
            this.comment = comment;
            this.price = product.getPrice();
        }
    }

    protected static class ExpectedOrder {
        LocalDate dueDate;
        LocalTime dueTime;
        PickupLocation pickupLocation;
        String pickupLocationName;
        String fullName;
        String phone;
        String details;
        List<ExpectedProductLine> products = new ArrayList<>();
        String total;
        OrderState state = OrderState.NEW;
    }

    protected static class OrderFixture {
        final Order order;
        final ExpectedOrder expected;

        OrderFixture(Order order, ExpectedOrder expected) {
            this.order = order;
            this.expected = expected;
        }
    }

    protected static class HistoryEntry {
        final String date;
        final String message;
        final String author;

        HistoryEntry(String date, String message, String author) {
            this.date = date;
            this.message = message;
            this.author = author;
        }
    }

    protected OrderService orderService;
    protected ProductService productService;
    protected PickupLocationService pickupLocationService;
    protected UserService userService;
    protected DollarPriceConverter priceConverter;
    protected NavigationManager navigationManager;
    protected OrderEditView view;

    @Before
    public void setUp() throws ServiceException {
        mockVaadin();

        orderService = getApplicationContext().getBean(OrderService.class);
        productService = getApplicationContext().getBean(ProductService.class);
        pickupLocationService = getApplicationContext()
                .getBean(PickupLocationService.class);
        userService = getApplicationContext().getBean(UserService.class);
        priceConverter = getApplicationContext()
                .getBean(DollarPriceConverter.class);
        navigationManager = getApplicationContext()
                .getBean(NavigationManager.class);
        navigate(StorefrontView.class);
    }

    @Override
    protected void configureSecurityContext() {
        authenticateAsBarista();
    }

    @After
    public void cleanUp() {
        tearDown();
    }

    protected OrderEditView openNewOrder() {
        navigate(StorefrontView.class);
        test(newOrderButton()).click();
        return currentOrderEditView();
    }

    protected OrderEditView openOrder(long orderId) {
        view = navigate(orderViewId() + "/" + orderId, OrderEditView.class);
        return view;
    }

    protected OrderEditView currentOrderEditView() {
        View currentView = UI.getCurrent().getNavigator().getCurrentView();
        assertTrue(currentView instanceof OrderEditView);
        view = (OrderEditView) currentView;
        return view;
    }

    protected ExpectedProductLine line(Product product, int quantity,
            String comment) {
        return new ExpectedProductLine(product, quantity, comment);
    }

    protected ExpectedOrder expectedOrder(LocalDate dueDate, LocalTime dueTime,
            PickupLocation pickupLocation, String fullName, String phone,
            String details, ExpectedProductLine... lines) {
        ExpectedOrder expected = new ExpectedOrder();
        expected.dueDate = dueDate;
        expected.dueTime = dueTime;
        expected.pickupLocation = pickupLocation;
        expected.pickupLocationName = pickupLocation.getName();
        expected.fullName = fullName;
        expected.phone = phone;
        expected.details = details;
        for (ExpectedProductLine line : lines) {
            expected.products.add(copyLine(line));
        }
        expected.total = totalFor(expected.products);
        return expected;
    }

    protected ExpectedOrder copyOrder(ExpectedOrder source) {
        ExpectedOrder copy = new ExpectedOrder();
        copy.dueDate = source.dueDate;
        copy.dueTime = source.dueTime;
        copy.pickupLocation = source.pickupLocation;
        copy.pickupLocationName = source.pickupLocationName;
        copy.fullName = source.fullName;
        copy.phone = source.phone;
        copy.details = source.details;
        copy.state = source.state;
        for (ExpectedProductLine line : source.products) {
            copy.products.add(copyLine(line));
        }
        copy.total = source.total;
        return copy;
    }

    protected OrderFixture persist(ExpectedOrder expected) {
        Order order = new Order();
        order.setDueDate(expected.dueDate);
        order.setDueTime(expected.dueTime);
        order.setPickupLocation(expected.pickupLocation);

        Customer customer = new Customer();
        customer.setFullName(expected.fullName);
        customer.setPhoneNumber(expected.phone);
        customer.setDetails(expected.details);
        order.setCustomer(customer);

        List<OrderItem> items = new ArrayList<>();
        for (ExpectedProductLine line : expected.products) {
            OrderItem item = new OrderItem();
            item.setProduct(line.product);
            item.setQuantity(line.quantity);
            item.setComment(line.comment);
            items.add(item);
        }
        order.setItems(items);
        order.setState(OrderState.NEW);

        return new OrderFixture(orderService.saveOrder(order, currentUser()),
                copyOrder(expected));
    }

    protected void fillOrderForm(ExpectedOrder expected) {
        dueDateField().setValue(expected.dueDate);
        dueTimeField().setValue(expected.dueTime);
        pickupLocationField().setValue(expected.pickupLocation);
        test(fullNameField()).setValue(expected.fullName);
        test(phoneField()).setValue(expected.phone);
        test(detailsField()).setValue(expected.details);

        for (int index = 0; index < expected.products.size(); index++) {
            if (index > 0) {
                test(addItemsButton()).click();
            }
            setProductLine(index, expected.products.get(index));
        }
    }

    protected void setProductLine(int index, ExpectedProductLine line) {
        ProductInfo productInfo = productInfo(index);
        productField(productInfo).setValue(line.product);
        test(quantityField(productInfo))
                .setValue(String.valueOf(line.quantity));
        test(commentField(productInfo)).setValue(line.comment);
    }

    protected ExpectedOrder snapshotOrder() {
        ExpectedOrder order = new ExpectedOrder();
        order.dueDate = dueDateField().getValue();
        order.dueTime = dueTimeField().getValue();
        order.pickupLocation = pickupLocationField().getValue();
        order.pickupLocationName = order.pickupLocation.getName();
        order.fullName = fullNameField().getValue();
        order.phone = phoneField().getValue();
        order.details = detailsField().getValue();
        order.state = currentState();
        for (int index = 0; index < numberOfProducts(); index++) {
            order.products.add(snapshotProductLine(productInfo(index)));
        }
        order.total = totalLabel().getValue();
        return order;
    }

    protected void assertOrder(ExpectedOrder expected) {
        ExpectedOrder actual = snapshotOrder();

        assertEquals(expected.dueDate, actual.dueDate);
        assertEquals(expected.dueTime, actual.dueTime);
        assertEquals(expected.pickupLocationName, actual.pickupLocationName);
        assertEquals(expected.fullName, actual.fullName);
        assertEquals(expected.phone, actual.phone);
        assertEquals(expected.details, actual.details);
        assertEquals(expected.state, actual.state);
        assertEquals(expected.products.size(), actual.products.size());

        for (int index = 0; index < expected.products.size(); index++) {
            ExpectedProductLine expectedLine = expected.products.get(index);
            ExpectedProductLine actualLine = actual.products.get(index);
            assertEquals(expectedLine.productName, actualLine.productName);
            assertEquals(expectedLine.quantity, actualLine.quantity);
            assertEquals(expectedLine.comment, actualLine.comment);
            assertEquals(expectedLine.price, actualLine.price);
        }

        assertEquals(expected.total, actual.total);
    }

    protected void assertWithinLastFiveMinutes(String date) {
        LocalDateTime commentTime = LocalDateTime.parse(
                date.replace('\u202f', ' '),
                DateTimeFormatter.ofPattern("M/d/uu, h:mm a", Locale.US));
        assertTrue(commentTime.until(LocalDateTime.now(),
                ChronoUnit.MINUTES) <= 5);
    }

    protected void assertConfirmationDialogBlocksLeaving() {
        test(storefrontButton()).click();
        assertTrue(UI.getCurrent().getNavigator()
                .getCurrentView() instanceof OrderEditView);
        test(confirmCancelButton()).click();

        test(logoutButton()).click();
        assertTrue(UI.getCurrent().getNavigator()
                .getCurrentView() instanceof OrderEditView);
        test(confirmCancelButton()).click();
    }

    protected void assertConfirmationDialogBlocksLeavingEsc() {
        test($(OrderEditView.class).first()).shortcut(KeyCode.ESCAPE);
        assertTrue(UI.getCurrent().getNavigator()
                .getCurrentView() instanceof OrderEditView);
        test(confirmCancelButton()).click();
    }

    protected void assertEnabledWithCaption(Button button, String caption) {
        assertTrue(button.isEnabled());
        assertEquals(caption, button.getCaption());
    }

    protected List<Product> anyProducts(int count) {
        List<Product> products = productService
                .findAnyMatching(Optional.empty(),
                        PageRequest.of(0, count + 10))
                .getContent();
        assertTrue(products.size() >= count);
        return products.subList(0, count);
    }

    protected PickupLocation defaultPickupLocation() {
        return pickupLocationService.getDefault();
    }

    protected PickupLocation alternatePickupLocation(PickupLocation current) {
        return pickupLocationService.findAnyMatching(Optional.empty(),
                PageRequest.of(0, 10)).getContent().stream()
                .filter(location -> !location.getId().equals(current.getId()))
                .findFirst()
                .orElse(current);
    }

    protected Product alternateProduct(Product current) {
        return productService
                .findAnyMatching(Optional.empty(), PageRequest.of(0, 10))
                .getContent().stream()
                .filter(product -> !product.getId().equals(current.getId()))
                .findFirst()
                .orElse(current);
    }

    protected List<HistoryEntry> historyEntries() {
        List<HistoryEntry> entries = new ArrayList<>();
        for (Component component : historyItems()) {
            if (!(component instanceof Label)) {
                continue;
            }
            Label label = (Label) component;
            String caption = label.getCaption();
            int separator = caption.indexOf(" by ");
            entries.add(new HistoryEntry(caption.substring(0, separator),
                    label.getValue(), caption.substring(separator + 4)));
        }
        return entries;
    }

    protected void addHistoryComment(String message) {
        test(newCommentInput()).setValue(message);
        test(commitNewCommentButton()).click();
    }

    protected User currentUser() {
        return userService.findByEmail("barista@vaadin.com");
    }

    protected User bakerUser() {
        return userService.findByEmail("baker@vaadin.com");
    }

    protected Long currentOrderId() {
        String text = orderIdLabel().getValue();
        return Long.valueOf(text.substring(1));
    }

    protected String orderViewId() {
        return navigationManager.getViewId(OrderEditView.class);
    }

    protected String currentStatePath() {
        return UI.getCurrent().getNavigator().getState();
    }

    protected HorizontalLayout reportHeader() {
        return $(view, HorizontalLayout.class).id("reportHeader");
    }

    protected Label orderIdLabel() {
        return $(view, Label.class).id("orderId");
    }

    protected Label stateLabel() {
        return $(view, Label.class).id("stateLabel");
    }

    protected OrderStateSelect stateField() {
        return $(view, OrderStateSelect.class).id("state");
    }

    protected DateField dueDateField() {
        return $(view, DateField.class).id("dueDate");
    }

    @SuppressWarnings("unchecked")
    protected ComboBox<LocalTime> dueTimeField() {
        return $(view, ComboBox.class).id("dueTime");
    }

    protected PickupLocationComboBox pickupLocationField() {
        return $(view, PickupLocationComboBox.class).id("pickupLocation");
    }

    protected TextField fullNameField() {
        return $(view, TextField.class).id("fullName");
    }

    protected TextField phoneField() {
        return $(view, TextField.class).id("phone");
    }

    protected TextField detailsField() {
        return $(view, TextField.class).id("details");
    }

    protected CssLayout productInfoContainer() {
        return $(view, CssLayout.class).id("productInfoContainer");
    }

    protected ProductInfo productInfo(int index) {
        return $(productInfoContainer(), ProductInfo.class).get(index);
    }

    protected int numberOfProducts() {
        return productInfoContainer().getComponentCount();
    }

    protected Button addItemsButton() {
        return $(view, Button.class).id("addItems");
    }

    protected Label totalLabel() {
        return $(view, Label.class).id("total");
    }

    protected OrderHistory history() {
        return $(view, OrderHistory.class).id("history");
    }

    protected CssLayout historyItems() {
        return $(history(), CssLayout.class).id("items");
    }

    protected TextField newCommentInput() {
        return $(history(), TextField.class).id("newCommentInput");
    }

    protected Button commitNewCommentButton() {
        return $(history(), Button.class).id("commitNewComment");
    }

    protected Button editDiscardButton() {
        return $(view, Button.class).id("edit-discard");
    }

    protected Button okButton() {
        return $(view, Button.class).id("ok");
    }

    protected Button newOrderButton() {
        return $(Button.class).id("newOrder");
    }

    protected Button storefrontButton() {
        return $(Button.class).id("storefront");
    }

    protected Button logoutButton() {
        return $(Button.class).id("logout");
    }

    protected Button confirmCancelButton() {
        return $(Button.class).id("confirmdialog-cancel-button");
    }

    protected Button confirmOkButton() {
        return $(Button.class).id("confirmdialog-ok-button");
    }

    protected Notification lastNotification() {
        return $(Notification.class).last();
    }

    private ExpectedProductLine snapshotProductLine(ProductInfo row) {
        Product product = productField(row).getValue();
        ExpectedProductLine line = new ExpectedProductLine(product,
                Integer.parseInt(quantityField(row).getValue()),
                commentValue(row));
        line.price = parseMoney(priceLabel(row).getValue());
        return line;
    }

    private ExpectedProductLine copyLine(ExpectedProductLine line) {
        ExpectedProductLine copy = new ExpectedProductLine(line.product,
                line.quantity, line.comment);
        copy.price = line.price;
        return copy;
    }

    private String totalFor(List<ExpectedProductLine> lines) {
        int total = 0;
        for (ExpectedProductLine line : lines) {
            total += line.quantity * line.price;
        }
        return priceConverter.convertToPresentation(total,
                new ValueContext(Locale.US));
    }

    private OrderState currentState() {
        if (view.getMode() == OrderEditView.Mode.EDIT) {
            return stateField().getValue();
        }
        if (view.getMode() == OrderEditView.Mode.REPORT) {
            return OrderState.forDisplayName(stateLabel().getValue());
        }
        return OrderState.NEW;
    }

    private ProductComboBox productField(ProductInfo row) {
        return $(row, ProductComboBox.class).id("product");
    }

    private TextField quantityField(ProductInfo row) {
        return $(row, TextField.class).id("quantity");
    }

    private Label priceLabel(ProductInfo row) {
        return $(row, Label.class).id("price");
    }

    private TextArea commentField(ProductInfo row) {
        return $(row, TextArea.class).id("comment");
    }

    private String commentValue(ProductInfo row) {
        if (view.getMode() == OrderEditView.Mode.EDIT) {
            return commentField(row).getValue();
        }
        return $(row, Label.class).id("comment").getValue();
    }

    private int parseMoney(String value) {
        return priceConverter.convertToModel(value, new ValueContext(Locale.US))
                .getOrThrow(IllegalStateException::new);
    }
}