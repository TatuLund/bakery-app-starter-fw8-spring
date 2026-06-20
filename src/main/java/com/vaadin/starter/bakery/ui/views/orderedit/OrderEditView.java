package com.vaadin.starter.bakery.ui.views.orderedit;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.PropertyId;
import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.BindingValidationStatus;
import com.vaadin.data.HasValue;
import com.vaadin.data.ValueContext;
import com.vaadin.data.validator.DateRangeValidator;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.event.ShortcutListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewBeforeLeaveEvent;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.starter.bakery.backend.data.OrderState;
import com.vaadin.starter.bakery.backend.data.entity.Order;
import com.vaadin.starter.bakery.backend.data.entity.OrderItem;
import com.vaadin.starter.bakery.ui.components.AttributeExtension;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.AriaAttributes;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.AriaRoles;
import com.vaadin.starter.bakery.ui.components.ConfirmPopup;
import com.vaadin.starter.bakery.ui.navigation.NavigationManager;
import com.vaadin.starter.bakery.ui.utils.DollarPriceConverter;
import com.vaadin.starter.bakery.ui.views.dashboard.DashboardView;
import com.vaadin.starter.bakery.ui.views.storefront.StorefrontView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SpringView(name = "order")
@SuppressWarnings({ "java:S2160", "java:S110", "java:S6813" })
public class OrderEditView extends VerticalLayout implements View {

	public enum Mode {
		EDIT, REPORT, CONFIRMATION;
	}

	protected HorizontalLayout reportHeader;
	protected Label orderId;
	protected Label stateLabel;
	protected OrderStateSelect state;
	protected DateField dueDate;
	protected ComboBox<LocalTime> dueTime;

	@Autowired
	protected PickupLocationComboBox pickupLocation;
	@PropertyId("customer.fullName")
	protected TextField fullName;
	@PropertyId("customer.phoneNumber")
	protected TextField phone;
	@PropertyId("customer.details")
	protected TextField details;
	protected CssLayout productInfoContainer;
	protected Button addItems;
	protected Label total;

	@Autowired
	private OrderHistory history;

	protected Button editDiscard;
	protected Button ok;
	private final OrderEditPresenter presenter;
	private final DollarPriceConverter priceConverter;
	private BeanValidationBinder<Order> binder;
	private Mode mode;
	private boolean hasChanges;
	private transient BeanFactory beanFactory;
	private View oldView;
	private Registration shortcutRegistration;
	private NavigationManager navigationManager;

	@Autowired
	public OrderEditView(OrderEditPresenter presenter, BeanFactory beanFactory,
			DollarPriceConverter priceConverter,
			NavigationManager navigationManager) {
		this.presenter = presenter;
		this.beanFactory = beanFactory;
		this.priceConverter = priceConverter;
		this.navigationManager = navigationManager;
		registerEscapeShortcut();
		setSizeFull();
	}

	private void registerEscapeShortcut() {
		shortcutRegistration = addShortcutListener(new EscapeListener());
	}

	@PostConstruct
	public void setup() {
		initLayout();
		presenter.init(this);

		// We're limiting dueTime to even hours between 07:00 and 17:00
		dueTime.setItems(
				IntStream.range(7, 17).mapToObj(h -> LocalTime.of(h, 0)));

		// Binder takes care of binding Vaadin fields defined as Java member
		// fields in this class to properties in the Order bean
		binder = new BeanValidationBinder<>(Order.class, true);

		// Almost all fields are required, so we don't want to display
		// indicators
		binder.setRequiredConfigurator(null);

		binder.forField(dueDate)
				.withValidator(
						new DateRangeValidator("Due date cannot be in the past",
								LocalDate.now(), null))
				.bind("dueDate");

		binder.bind(fullName, "customer.fullName");
		binder.bind(phone, "customer.phoneNumber");
		binder.bind(details, "customer.details");

		// Bindings are done in the order the fields appear on the screen as we
		// report validation errors for the first invalid field and it is most
		// intuitive for the user that we start from the top if there are
		// multiple errors.
		binder.bindInstanceFields(this);

		// Track changes manually as we use setBean and nested binders
		binder.addValueChangeListener(e -> hasChanges = true);

		addItems.addClickListener(e -> addEmptyOrderItem());
		editDiscard.addClickListener(e -> presenter.editBackCancelPressed());
		ok.addClickListener(e -> presenter.okPressed());
	}

	private void initLayout() {
		setSpacing(false);
		setMargin(new MarginInfo(false, true, true, true));

		CssLayout orderForm = createOrderForm();

		addComponent(orderForm);
		setComponentAlignment(orderForm, Alignment.TOP_LEFT);
	}

	private CssLayout createOrderForm() {
		CssLayout orderForm = new CssLayout();
		AttributeExtension.of(orderForm).setAttribute(AriaAttributes.ROLE,
				AriaRoles.FORM);
		orderForm.setStyleName("order-form responsive");
		orderForm.setResponsive(true);
		orderForm.setWidth("100%");

		reportHeader = new HorizontalLayout();
		reportHeader.setId("reportHeader");
		reportHeader.setWidth("100%");
		reportHeader.setMargin(false);

		orderId = new Label();
		orderId.setStyleName(
				ValoTheme.LABEL_H4 + " " + ValoTheme.LABEL_COLORED);
		orderId.setId("orderId");
		orderId.setContentMode(ContentMode.TEXT);
		orderId.setValue("#182");
		reportHeader.addComponent(orderId);
		reportHeader.setComponentAlignment(orderId, Alignment.TOP_LEFT);

		stateLabel = new Label();
		stateLabel.setStyleName(
				ValoTheme.LABEL_H4 + " " + ValoTheme.LABEL_COLORED);
		stateLabel.setId("stateLabel");
		stateLabel.setWidth("100%");
		stateLabel.setContentMode(ContentMode.TEXT);
		stateLabel.setValue("New order");
		reportHeader.addComponent(stateLabel);
		reportHeader.setComponentAlignment(stateLabel, Alignment.TOP_LEFT);
		reportHeader.setExpandRatio(stateLabel, 1.0F);

		state = new OrderStateSelect();
		state.setId("state");
		reportHeader.addComponent(state);
		reportHeader.setComponentAlignment(state, Alignment.MIDDLE_RIGHT);
		orderForm.addComponent(reportHeader);

		Label dueLabel = new Label();
		dueLabel.setStyleName(ValoTheme.LABEL_H4 + " header");
		dueLabel.setWidth("100%");
		dueLabel.setContentMode(ContentMode.TEXT);
		dueLabel.setValue("Due");
		dueLabel.setId("dueLabel");
		orderForm.addComponent(dueLabel);

		HorizontalLayout dateTimeWrapper = new HorizontalLayout();
		dateTimeWrapper.setStyleName("half");
		dateTimeWrapper.setWidth("100%");
		dateTimeWrapper.setMargin(false);

		dueDate = new DateField();
		dueDate.setLenient(true);
		dueDate.setId("dueDate");
		dueDate.setPlaceholder("Date");
		dueDate.setWidth("180px");
		AttributeExtension.of(dueDate).setAttribute(AriaAttributes.DESCRIBEDBY,
				"dueLabel");
		AttributeExtension.of(dueDate).setAttribute(AriaAttributes.LABEL,
				"Date");
		dateTimeWrapper.addComponent(dueDate);
		dateTimeWrapper.setComponentAlignment(dueDate, Alignment.TOP_LEFT);

		dueTime = new ComboBox<>();
		dueTime.setEmptySelectionAllowed(false);
		dueTime.setId("dueTime");
		dueTime.setTextInputAllowed(false);
		dueTime.setWidth("6em");
		AttributeExtension.of(dueTime).setAttribute(AriaAttributes.DESCRIBEDBY,
				"dueLabel");
		AttributeExtension.of(dueTime).setAttribute(AriaAttributes.LABEL,
				"Time");
		dateTimeWrapper.addComponent(dueTime);
		dateTimeWrapper.setComponentAlignment(dueTime, Alignment.TOP_LEFT);
		dateTimeWrapper.setExpandRatio(dueTime, 1.0F);
		orderForm.addComponent(dateTimeWrapper);

		orderForm.addComponent(createOrderInfoLayout());

		Label customerLabel = new Label();
		customerLabel.setStyleName("header");
		customerLabel.setWidth("100%");
		customerLabel.setContentMode(ContentMode.TEXT);
		customerLabel.setValue("Customer");
		customerLabel.setId("customerLabel");
		orderForm.addComponent(customerLabel);

		fullName = new TextField();
		fullName.setStyleName("half");
		fullName.setId("fullName");
		fullName.setPlaceholder("Firstname Lastname");
		fullName.setWidth("100%");
		AttributeExtension.of(fullName).setAttribute(AriaAttributes.DESCRIBEDBY,
				"customerLabel");
		AttributeExtension.of(fullName).setAttribute(AriaAttributes.LABEL,
				"Full name");
		orderForm.addComponent(fullName);

		phone = new TextField();
		phone.setStyleName("half");
		phone.setId("phone");
		phone.setPlaceholder("Phone number");
		phone.setWidth("100%");
		AttributeExtension.of(phone).setAttribute(AriaAttributes.DESCRIBEDBY,
				"customerLabel");
		AttributeExtension.of(phone).setAttribute(AriaAttributes.LABEL,
				"Phone number");
		orderForm.addComponent(phone);

		details = new TextField();
		details.setId("details");
		details.setPlaceholder("Additional details");
		details.setWidth("100%");
		AttributeExtension.of(details).setAttribute(AriaAttributes.DESCRIBEDBY,
				"customerLabel");
		AttributeExtension.of(details).setAttribute(AriaAttributes.LABEL,
				"Additional details");
		orderForm.addComponent(details);

		Label detailsLabel = new Label();
		detailsLabel.setStyleName("header");
		detailsLabel.setWidth("100%");
		detailsLabel.setContentMode(ContentMode.TEXT);
		detailsLabel.setValue("Products");
		orderForm.addComponent(detailsLabel);

		productInfoContainer = new CssLayout();
		productInfoContainer.setStyleName("product-container");
		productInfoContainer.setId("productInfoContainer");
		productInfoContainer.setWidth("100%");
		orderForm.addComponent(productInfoContainer);

		addItems = new Button();
		addItems.setIcon(VaadinIcons.PLUS_CIRCLE_O);
		addItems.setStyleName("add-items " + ValoTheme.BUTTON_LINK);
		addItems.setId("addItems");
		addItems.setWidth("100%");
		addItems.setHeight("100px");
		addItems.setCaptionAsHtml(true);
		addItems.setCaption("Add item");
		orderForm.addComponent(addItems);

		total = new Label();
		total.setStyleName(
				"total " + ValoTheme.LABEL_HUGE + " " + ValoTheme.LABEL_BOLD);
		total.setId("total");
		total.setWidth("100%");
		total.setContentMode(ContentMode.TEXT);
		total.setValue("0.00");
		AttributeExtension.of(total).setAttribute(AriaAttributes.LABEL,
				"Total price");
		AttributeExtension.of(total).setAttribute(AriaAttributes.LIVE,
				"polite");
		AttributeExtension.of(total).setAttribute("tabindex", "0");
		orderForm.addComponent(total);

		history.setId("history");
		history.setWidth("100%");
		orderForm.addComponent(history);

		orderForm.addComponent(createButtonsWrapper());

		return orderForm;
	}

	private HorizontalLayout createOrderInfoLayout() {
		HorizontalLayout orderInfoLayout = new HorizontalLayout();
		orderInfoLayout.setStyleName("half");
		orderInfoLayout.setWidth("100%");
		orderInfoLayout.setMargin(false);

		Label atLabel = new Label();
		atLabel.setStyleName("large");
		atLabel.setContentMode(ContentMode.TEXT);
		atLabel.setValue("@");
		orderInfoLayout.addComponent(atLabel);
		orderInfoLayout.setComponentAlignment(atLabel, Alignment.MIDDLE_LEFT);

		pickupLocation.setId("pickupLocation");
		pickupLocation.setWidth("100%");
		orderInfoLayout.addComponent(pickupLocation);
		orderInfoLayout.setComponentAlignment(pickupLocation,
				Alignment.MIDDLE_LEFT);
		orderInfoLayout.setExpandRatio(pickupLocation, 1.0F);

		return orderInfoLayout;
	}

	private CssLayout createButtonsWrapper() {
		CssLayout buttonsWrapper = new CssLayout();
		buttonsWrapper.setStyleName("buttons");
		buttonsWrapper.setWidth("100%");

		editDiscard = new Button();
		editDiscard.setIcon(VaadinIcons.CLOSE);
		editDiscard.setStyleName("edit-discard");
		editDiscard.setId("edit-discard");
		editDiscard.setCaptionAsHtml(true);
		editDiscard.setCaption("Cancel");
		editDiscard.addBlurListener(e -> {
			if (mode != Mode.EDIT) {
				dueDate.focus();
			}
		});
		buttonsWrapper.addComponent(editDiscard);

		ok = new Button();
		ok.setIcon(VaadinIcons.ANGLE_RIGHT);
		ok.setStyleName(ValoTheme.BUTTON_PRIMARY + " "
				+ ValoTheme.BUTTON_ICON_ALIGN_RIGHT);
		ok.setId("ok");
		ok.setCaption("Place order");
		buttonsWrapper.addComponent(ok);

		return buttonsWrapper;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// Save where we came from so that we can navigate back if the user
		// cancels
		oldView = event.getOldView();
		String parametersString = event.getParameters();
		if ("".equals(parametersString)) {
			presenter.enterView(null);
		} else {
			presenter.enterView(Long.valueOf(parametersString));
		}
	}

	public void setOrder(Order order) {
		stateLabel.setValue(order.getState().getDisplayName());
		binder.setBean(order);
		productInfoContainer.removeAllComponents();

		reportHeader.setVisible(order.getId() != null);
		if (order.getId() == null) {
			addEmptyOrderItem();
			dueDate.focus();
		} else {
			orderId.setValue("#" + order.getId());
			for (OrderItem item : order.getItems()) {
				ProductInfo productInfo = createProductInfo(item);
				productInfo.setReportMode(mode != Mode.EDIT);
				productInfoContainer.addComponent(productInfo);
			}
			history.setOrder(order);
		}
		hasChanges = false;
	}

	private void addEmptyOrderItem() {
		OrderItem orderItem = new OrderItem();
		ProductInfo productInfo = createProductInfo(orderItem);
		productInfoContainer.addComponent(productInfo);
		productInfo.focus();
		getOrder().getItems().add(orderItem);
	}

	protected void removeOrderItem(OrderItem orderItem) {
		getOrder().getItems().remove(orderItem);

		for (Component c : productInfoContainer) {
			if (c instanceof ProductInfo productInfo
					&& productInfo.getItem() == orderItem) {
				productInfoContainer.removeComponent(c);
				break;
			}
		}
	}

	/**
	 * Create a ProductInfo instance using Spring so that it is injected and can
	 * in turn inject a ProductComboBox and its data provider.
	 *
	 * @param orderItem
	 *            the item to edit
	 *
	 * @return a new product info instance
	 */
	private ProductInfo createProductInfo(OrderItem orderItem) {
		ProductInfo productInfo = beanFactory.getBean(ProductInfo.class);
		productInfo.setItem(orderItem);
		return productInfo;
	}

	protected Order getOrder() {
		return binder.getBean();
	}

	protected void setSum(int sum) {
		total.setValue(priceConverter.convertToPresentation(sum,
				new ValueContext(Locale.US)));
	}

	public void showNotFound() {
		removeAllComponents();
		var label = new Label("Order not found");
		label.setStyleName(ValoTheme.LABEL_FAILURE);
		label.setId("notFoundLabel");
		AttributeExtension.of(label).setAttribute(AriaAttributes.LIVE,
				"polite");
		addComponent(label);
		setComponentAlignment(label, Alignment.MIDDLE_CENTER);
	}

	public void setMode(Mode mode) {
		// Allow to style different modes separately
		if (this.mode != null) {
			removeStyleName(this.mode.name().toLowerCase());
		}
		addStyleName(mode.name().toLowerCase());

		this.mode = mode;
		binder.setReadOnly(mode != Mode.EDIT);
		for (Component c : productInfoContainer) {
			if (c instanceof ProductInfo productInfo) {
				productInfo.setReportMode(mode != Mode.EDIT);
			}
		}
		addItems.setVisible(mode == Mode.EDIT);
		history.setVisible(mode == Mode.REPORT);
		state.setVisible(mode == Mode.EDIT);

		switch (mode) {
		case REPORT -> {
			editDiscard.removeClickShortcut();
			editDiscard.focus();
			editDiscard.setCaption("Edit");
			editDiscard.setIcon(VaadinIcons.EDIT);
			editDiscard
					.setEnabled(getOrder().getState() != OrderState.DELIVERED);
			Optional<OrderState> nextState = presenter
					.getNextHappyPathState(getOrder().getState());
			ok.removeClickShortcut();
			ok.setCaption("Mark as "
					+ nextState.map(OrderState::getDisplayName).orElse("?"));
			ok.setVisible(nextState.isPresent());
		}
		case CONFIRMATION -> {
			editDiscard.setCaption("Back");
			editDiscard.setIcon(VaadinIcons.ANGLE_LEFT);
			editDiscard.setEnabled(true);
			ok.setCaption("Place order");
			ok.setVisible(true);
		}
		case EDIT -> {
			editDiscard.setCaption("Discard");
			editDiscard.setIcon(VaadinIcons.CLOSE);
			editDiscard.setEnabled(true);
			editDiscard.setClickShortcut(KeyCode.Z, ModifierKey.CTRL);
			ok.setClickShortcut(KeyCode.S, ModifierKey.CTRL);
			if (getOrder() != null && !getOrder().isNew()) {
				ok.setCaption("Save");
			} else {
				ok.setCaption("Review order");
			}
			ok.setVisible(true);
			state.focus();
		}
		default -> throw new IllegalArgumentException("Unknown mode " + mode);
		}
	}

	public Mode getMode() {
		return mode;
	}

	public Stream<HasValue<?>> validate() {
		Stream<HasValue<?>> errorFields = binder.validate()
				.getFieldValidationErrors().stream()
				.map(BindingValidationStatus::getField);

		for (Component c : productInfoContainer) {
			if (c instanceof ProductInfo productInfo
					&& !productInfo.isEmpty()) {
				errorFields = Stream.concat(errorFields,
						productInfo.validate());
			}
		}
		return errorFields;
	}

	@Override
	public void beforeLeave(ViewBeforeLeaveEvent event) {
		if (!containsUnsavedChanges()) {
			event.navigate();
		} else {
			ConfirmPopup confirmPopup = beanFactory.getBean(ConfirmPopup.class);
			confirmPopup.showLeaveViewConfirmDialog(this, event::navigate);
		}
	}

	public void onProductInfoChanged() {
		hasChanges = true;
	}

	public boolean containsUnsavedChanges() {
		return hasChanges;
	}

	@Override
	public void detach() {
		super.detach();
		if (shortcutRegistration != null) {
			shortcutRegistration.remove();
			shortcutRegistration = null;
		}
	}

	public boolean focusFirstErrorField() {
		Optional<HasValue<?>> firstErrorField = validate().findFirst();
		firstErrorField.ifPresent(field -> ((Focusable) field).focus());
		return firstErrorField.isPresent();
	}

	public void updateViewParameter(String parameter) {
		navigationManager.updateViewParameter(parameter);
	}

	public void navigateToStorefront() {
		navigationManager.navigateTo(StorefrontView.class);
	}

	public void showUnexpectedError() {
		Notification.show(
				"An unexpected error occurred while saving. Please refresh and try again.",
				Type.ERROR_MESSAGE);
	}

	public void showValidationError(String message) {
		Notification.show("Please check the contents of the fields: "
				+ message, Type.ERROR_MESSAGE);
	}

	public void showOptimisticLockingError() {
		Notification.show(
				"Somebody else might have updated the data. Please refresh and try again.",
				Type.ERROR_MESSAGE);
	}

	class EscapeListener extends ShortcutListener {
		EscapeListener() {
			super("Cancel", KeyCode.ESCAPE, new int[0]);
		}

		@Override
		public void handleAction(Object sender, Object target) {
			if (oldView == null) {
				return;
			}
			if (oldView instanceof DashboardView) {
				navigationManager.navigateTo("dashboard");
			}
			if (oldView instanceof StorefrontView) {
				navigationManager.navigateTo("storefront");
			}
		}
	}
}
