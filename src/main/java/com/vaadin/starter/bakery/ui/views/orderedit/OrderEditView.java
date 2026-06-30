package com.vaadin.starter.bakery.ui.views.orderedit;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
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
import com.vaadin.starter.bakery.ui.components.AttributeExtension.HasAttributes;
import com.vaadin.starter.bakery.ui.components.ConfirmPopup;
import com.vaadin.starter.bakery.ui.navigation.NavigationManager;
import com.vaadin.starter.bakery.ui.utils.DollarPriceConverter;
import com.vaadin.starter.bakery.ui.views.dashboard.DashboardView;
import com.vaadin.starter.bakery.ui.views.storefront.StorefrontView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Composite;
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
@NullMarked
public class OrderEditView extends VerticalLayout implements View {

	public enum Mode {
		EDIT, REPORT, CONFIRMATION;
	}

	protected OrderForm orderForm;
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
	@Nullable
	private Registration shortcutRegistration;
	private NavigationManager navigationManager;

	@Autowired
	@SuppressWarnings("java:S2637")
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
	@SuppressWarnings("java:S8688")
	public void setup() {
		initLayout();
		presenter.init(this);

		// We're limiting dueTime to even hours between 07:00 and 17:00
		dueTime.setItems(
				IntStream.range(7, 17).mapToObj(hour -> LocalTime.of(hour, 0)));

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

		// Bindings are done in the order the fields appear on the screen as we
		// report validation errors for the first invalid field and it is most
		// intuitive for the user that we start from the top if there are
		// multiple errors.
		binder.bindInstanceFields(this);

		// Track changes manually as we use setBean and nested binders
		binder.addValueChangeListener(valueChanged -> hasChanges = true);

		addItems.addClickListener(clicked -> orderForm.addEmptyOrderItem());
		editDiscard
				.addClickListener(clicked -> presenter.editBackCancelPressed());
		ok.addClickListener(clicked -> presenter.okPressed());
	}

	private void initLayout() {
		setSpacing(false);
		setMargin(new MarginInfo(false, true, true, true));

		orderForm = new OrderForm();

		addComponent(orderForm);
		setComponentAlignment(orderForm, Alignment.TOP_LEFT);
	}

	@Override
	public void enter(ViewChangeEvent viewChange) {
		// Save where we came from so that we can navigate back if the user
		// cancels
		oldView = viewChange.getOldView();
		String parametersString = viewChange.getParameters();
		if ("".equals(parametersString)) {
			presenter.enterView(null);
		} else {
			presenter.enterView(Long.valueOf(parametersString));
		}
	}

	/**
	 * Sets the order to be edited or viewed in this view.
	 *
	 * @param order
	 *            the order to be edited or viewed
	 */
	public void setOrder(Order order) {
		orderForm.setState(order.getState().getDisplayName());
		binder.setBean(order);

		orderForm.setHeaderVisible(order.getId() != null);
		orderForm.updateProductInfos(order);
		hasChanges = false;
	}

	public void removeOrderItem(OrderItem orderItem) {
		orderForm.removeOrderItem(orderItem);
	}

	@Nullable
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
		orderForm.updateProductInfoMode(mode);
		addItems.setVisible(mode == Mode.EDIT);
		history.setVisible(mode == Mode.REPORT);
		state.setVisible(mode == Mode.EDIT);

		switch (mode) {
		case REPORT -> reportState();
		case CONFIRMATION -> confirmationState();
		case EDIT -> editState();
		default -> throw new IllegalArgumentException("Unknown mode " + mode);
		}
	}

	private void editState() {
		editDiscard.setCaption("Discard");
		editDiscard.setIcon(VaadinIcons.CLOSE);
		editDiscard.setEnabled(true);
		editDiscard.setClickShortcut(KeyCode.Z, ModifierKey.CTRL);
		ok.setClickShortcut(KeyCode.S, ModifierKey.CTRL);
		var order = getOrder();
		if (order != null && !order.isNew()) {
			ok.setCaption("Save");
		} else {
			ok.setCaption("Review order");
		}
		ok.setVisible(true);
		state.focus();
	}

	private void confirmationState() {
		editDiscard.setCaption("Back");
		editDiscard.setIcon(VaadinIcons.ANGLE_LEFT);
		editDiscard.setEnabled(true);
		ok.setCaption("Place order");
		ok.setVisible(true);
	}

	@SuppressWarnings("null")
	private void reportState() {
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

	/**
	 * Returns the current mode of this view.
	 *
	 * @return the current mode, enum value of {@link Mode}
	 */
	public Mode getMode() {
		return mode;
	}

	@SuppressWarnings("null")
	public Stream<HasValue<?>> validate() {
		Stream<HasValue<?>> errorFields = binder.validate()
				.getFieldValidationErrors().stream()
				.map(BindingValidationStatus::getField);

		for (ProductInfo productInfo : orderForm.getProductInfos()) {
			errorFields = Stream.concat(errorFields,
					productInfo.validate());
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

	class OrderForm extends Composite implements HasAttributes<OrderForm> {

		private static final String DUE_LABEL_ID = "dueLabel";
		private static final String CUSTOMER_LABEL_ID = "customerLabel";
		private HorizontalLayout reportHeader;
		private Label stateLabel;
		private CssLayout productInfoContainer;
		private Label orderId;

		OrderForm() {
			CssLayout layout = new CssLayout();
			layout.setStyleName("order-form responsive");
			layout.setResponsive(true);
			layout.setWidth("100%");
			setCompositionRoot(layout);
			setAttribute(AriaAttributes.ROLE, AriaRoles.FORM);

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
			layout.addComponent(reportHeader);

			Label dueLabel = new Label();
			dueLabel.setStyleName(ValoTheme.LABEL_H4 + " header");
			dueLabel.setWidth("100%");
			dueLabel.setContentMode(ContentMode.TEXT);
			dueLabel.setValue("Due");
			dueLabel.setId(DUE_LABEL_ID);
			layout.addComponent(dueLabel);

			HorizontalLayout dateTimeWrapper = buildDateTimeComponent();
			layout.addComponent(dateTimeWrapper);

			layout.addComponent(createOrderInfoLayout());

			Label customerLabel = new Label();
			customerLabel.setStyleName("header");
			customerLabel.setWidth("100%");
			customerLabel.setContentMode(ContentMode.TEXT);
			customerLabel.setValue("Customer");
			customerLabel.setId(CUSTOMER_LABEL_ID);
			layout.addComponent(customerLabel);

			fullName = new TextField();
			fullName.setStyleName("half");
			fullName.setId("fullName");
			fullName.setPlaceholder("Firstname Lastname");
			fullName.setWidth("100%");
			AttributeExtension.of(fullName).setAttribute(
					AriaAttributes.DESCRIBEDBY, CUSTOMER_LABEL_ID);
			AttributeExtension.of(fullName).setAttribute(AriaAttributes.LABEL,
					"Full name");
			layout.addComponent(fullName);

			phone = new TextField();
			phone.setStyleName("half");
			phone.setId("phone");
			phone.setPlaceholder("Phone number");
			phone.setWidth("100%");
			AttributeExtension.of(phone).setAttribute(
					AriaAttributes.DESCRIBEDBY,
					CUSTOMER_LABEL_ID);
			AttributeExtension.of(phone).setAttribute(AriaAttributes.LABEL,
					"Phone number");
			layout.addComponent(phone);

			details = new TextField();
			details.setId("details");
			details.setPlaceholder("Additional details");
			details.setWidth("100%");
			AttributeExtension.of(details).setAttribute(
					AriaAttributes.DESCRIBEDBY,
					CUSTOMER_LABEL_ID);
			AttributeExtension.of(details).setAttribute(AriaAttributes.LABEL,
					"Additional details");
			layout.addComponent(details);

			Label detailsLabel = new Label();
			detailsLabel.setStyleName("header");
			detailsLabel.setWidth("100%");
			detailsLabel.setContentMode(ContentMode.TEXT);
			detailsLabel.setValue("Products");
			layout.addComponent(detailsLabel);

			productInfoContainer = new CssLayout();
			productInfoContainer.setStyleName("product-container");
			productInfoContainer.setId("productInfoContainer");
			productInfoContainer.setWidth("100%");
			layout.addComponent(productInfoContainer);

			addItems = new Button();
			addItems.setIcon(VaadinIcons.PLUS_CIRCLE_O);
			addItems.setStyleName("add-items " + ValoTheme.BUTTON_LINK);
			addItems.setId("addItems");
			addItems.setWidth("100%");
			addItems.setHeight("100px");
			addItems.setCaptionAsHtml(true);
			addItems.setCaption("Add item");
			layout.addComponent(addItems);

			total = new Label();
			total.setStyleName(
					"total " + ValoTheme.LABEL_HUGE + " "
							+ ValoTheme.LABEL_BOLD);
			total.setId("total");
			total.setWidth("100%");
			total.setContentMode(ContentMode.TEXT);
			total.setValue("0.00");
			AttributeExtension.of(total).setAttribute(AriaAttributes.LABEL,
					"Total price");
			AttributeExtension.of(total).setAttribute(AriaAttributes.LIVE,
					"polite");
			AttributeExtension.of(total).setAttribute("tabindex", "0");
			layout.addComponent(total);

			history.setId("history");
			history.setWidth("100%");
			layout.addComponent(history);

			layout.addComponent(createButtonsWrapper());
		}

		private HorizontalLayout buildDateTimeComponent() {
			HorizontalLayout dateTimeWrapper = new HorizontalLayout();
			dateTimeWrapper.setStyleName("half");
			dateTimeWrapper.setWidth("100%");
			dateTimeWrapper.setMargin(false);

			dueDate = new DateField();
			dueDate.setLenient(true);
			dueDate.setId("dueDate");
			dueDate.setPlaceholder("Date");
			dueDate.setWidth("180px");
			AttributeExtension.of(dueDate).setAttribute(
					AriaAttributes.DESCRIBEDBY,
					DUE_LABEL_ID);
			AttributeExtension.of(dueDate).setAttribute(AriaAttributes.LABEL,
					"Date");
			dateTimeWrapper.addComponent(dueDate);
			dateTimeWrapper.setComponentAlignment(dueDate, Alignment.TOP_LEFT);

			dueTime = new ComboBox<>();
			dueTime.setEmptySelectionAllowed(false);
			dueTime.setId("dueTime");
			dueTime.setTextInputAllowed(false);
			dueTime.setWidth("6em");
			AttributeExtension.of(dueTime).setAttribute(
					AriaAttributes.DESCRIBEDBY,
					DUE_LABEL_ID);
			AttributeExtension.of(dueTime).setAttribute(AriaAttributes.LABEL,
					"Time");
			dateTimeWrapper.addComponent(dueTime);
			dateTimeWrapper.setComponentAlignment(dueTime, Alignment.TOP_LEFT);
			dateTimeWrapper.setExpandRatio(dueTime, 1.0F);
			return dateTimeWrapper;
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
			orderInfoLayout.setComponentAlignment(atLabel,
					Alignment.MIDDLE_LEFT);

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
			editDiscard.addBlurListener(blurred -> {
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

		/**
		 * Sets the visibility of the report header.
		 *
		 * @param visible
		 *            true to make the report header visible, false to hide it
		 */
		public void setHeaderVisible(boolean visible) {
			reportHeader.setVisible(visible);
		}

		/**
		 * Sets the state label to the given value.
		 *
		 * @param state
		 *            the state to set
		 */
		public void setState(String state) {
			stateLabel.setValue(state);
		}

		/**
		 * Updates the product info components to match the items in the given
		 * order.
		 * 
		 * @param order
		 *            the order to update the product info components for
		 */
		public void updateProductInfos(Order order) {
			productInfoContainer.removeAllComponents();
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
		}

		/**
		 * Adds a new empty order item to the order and creates a corresponding
		 * ProductInfo component for it.
		 */
		public void addEmptyOrderItem() {
			OrderItem orderItem = new OrderItem();
			ProductInfo productInfo = createProductInfo(orderItem);
			productInfoContainer.addComponent(productInfo);
			productInfo.focus();
			var order = getOrder();
			if (order != null) {
				order.getItems().add(orderItem);
			}
		}

		protected void removeOrderItem(OrderItem orderItem) {
			var order = getOrder();
			if (order != null) {
				order.getItems().remove(orderItem);
			}

			for (Component c : productInfoContainer) {
				if (c instanceof ProductInfo productInfo
						&& productInfo.getItem() == orderItem) {
					productInfoContainer.removeComponent(c);
					break;
				}
			}
		}

		/**
		 * Create a ProductInfo instance using Spring so that it is injected and
		 * can in turn inject a ProductComboBox and its data provider.
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

		/**
		 * Updates the mode of all product info components.
		 * 
		 * @param mode
		 *            the mode to set
		 */
		public void updateProductInfoMode(Mode mode) {
			for (Component c : productInfoContainer) {
				if (c instanceof ProductInfo productInfo) {
					productInfo.setReportMode(mode != Mode.EDIT);
				}
			}
		}

		/**
		 * Returns a list of all product info components that are not empty.
		 *
		 * @return a list of all product info components that are not empty
		 */
		public List<ProductInfo> getProductInfos() {
			List<ProductInfo> productInfos = new ArrayList<>();
			for (Component c : productInfoContainer) {
				if (c instanceof ProductInfo productInfo
						&& !productInfo.isEmpty()) {
					productInfos.add(productInfo);
				}
			}
			return productInfos;
		}
	}
}
