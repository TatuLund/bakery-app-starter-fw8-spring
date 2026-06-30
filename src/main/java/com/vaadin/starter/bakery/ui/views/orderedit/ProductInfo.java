package com.vaadin.starter.bakery.ui.views.orderedit;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.annotation.PrototypeScope;
import org.vaadin.spring.events.EventBus.ViewEventBus;

import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.BindingValidationStatus;
import com.vaadin.data.HasValue;
import com.vaadin.data.ValueContext;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.event.selection.SingleSelectionEvent;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.starter.bakery.backend.data.entity.OrderItem;
import com.vaadin.starter.bakery.backend.data.entity.Product;
import com.vaadin.starter.bakery.ui.components.AttributeExtension;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.AriaAttributes;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.AriaRoles;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.HasAttributes;
import com.vaadin.starter.bakery.ui.utils.DollarPriceConverter;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Composite;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Notification.Type;

@SpringComponent
@PrototypeScope
@SuppressWarnings({ "java:S110", "java:S2160", "java:S6813" })
@NullMarked
public class ProductInfo extends Composite
		implements HasAttributes<ProductInfo> {

	protected Button delete;

	@Autowired
	private ProductComboBox product;

	protected TextField quantity;

	protected Label price;

	protected TextArea comment;

	private CssLayout layout;

	private final DollarPriceConverter priceFormatter;

	private final transient ViewEventBus viewEventBus;

	private BeanValidationBinder<OrderItem> binder = new BeanValidationBinder<>(
			OrderItem.class);

	// Use Label instead of TextArea in "report mode" for a better presentation
	private Label readOnlyComment = new Label();

	private boolean reportMode = false;

	@Autowired
	@SuppressWarnings("java:S2637")
	public ProductInfo(DollarPriceConverter priceFormatter,
			ViewEventBus viewEventBus) {
		setRole(AriaRoles.GROUP);
		setAriaLabel("Product information");
		this.priceFormatter = priceFormatter;
		this.viewEventBus = viewEventBus;
		layout = new CssLayout();
		setCompositionRoot(layout);
	}

	@PostConstruct
	public void setup() {
		initLayout();
		binder.setRequiredConfigurator(null);
		binder.forField(quantity)
				.withConverter(new StringToIntegerConverter(-1,
						"Please enter a number"))
				.bind("quantity");
		binder.bindInstanceFields(this);
		quantity.addValueChangeListener(
				valueChanged -> announceLinePrice(valueChanged.getValue()));
		binder.addValueChangeListener(valueChanged -> fireProductInfoChanged());

		product.addSelectionListener(this::onProductSelected);

		readOnlyComment.setWidth("100%");
		readOnlyComment.setId(comment.getId());
		readOnlyComment.setStyleName(comment.getStyleName());

		delete.addClickListener(e -> fireOrderItemDeleted());
	}

	@SuppressWarnings("null")
	private void onProductSelected(
			SingleSelectionEvent<Product> selectionEvent) {
		Optional<Product> selectedProduct = selectionEvent
				.getFirstSelectedItem();
		int productPrice = selectedProduct.map(Product::getPrice).orElse(0);
		updatePrice(productPrice);
	}

	private void initLayout() {
		setStyleName("product-row responsive");
		setResponsive(true);
		setWidth("100%");

		HorizontalLayout productComboBoxWrapper = new HorizontalLayout();
		productComboBoxWrapper.setStyleName("long");
		productComboBoxWrapper.setSpacing(false);
		productComboBoxWrapper.setWidth("100%");
		productComboBoxWrapper.setMargin(false);

		delete = new Button();
		delete.setIcon(VaadinIcons.TRASH);
		delete.setStyleName("delete");
		delete.setCaption("");
		delete.setId("delete");
		delete.setDescription("Delete product entry");
		productComboBoxWrapper.addComponent(delete);
		productComboBoxWrapper.setComponentAlignment(delete,
				Alignment.MIDDLE_LEFT);

		product.setStyleName("product");
		product.setId("product");
		product.setWidth("100%");
		productComboBoxWrapper.addComponent(product);
		productComboBoxWrapper.setComponentAlignment(product,
				Alignment.MIDDLE_LEFT);
		productComboBoxWrapper.setExpandRatio(product, 1.0F);
		layout.addComponent(productComboBoxWrapper);

		HorizontalLayout quantityLayout = new HorizontalLayout();
		quantityLayout.setStyleName("short");
		quantityLayout.setSpacing(false);
		quantityLayout.setWidth("150px");
		quantityLayout.setHeight("37px");
		quantityLayout.setMargin(false);

		quantity = new TextField();
		quantity.setStyleName("quantity");
		quantity.setId("quantity");
		quantity.setWidth("4em");
		AttributeExtension.of(quantity).setAttribute(AriaAttributes.LABEL,
				"Quantity");
		quantityLayout.addComponent(quantity);
		quantityLayout.setComponentAlignment(quantity, Alignment.TOP_LEFT);

		Label times = new Label();
		times.setStyleName("times");
		times.setWidth("100%");
		times.setContentMode(ContentMode.TEXT);
		times.setValue("x");
		quantityLayout.addComponent(times);
		quantityLayout.setComponentAlignment(times, Alignment.MIDDLE_CENTER);
		quantityLayout.setExpandRatio(times, 1.0F);

		price = new Label();
		price.setStyleName("price");
		price.setId("price");
		price.setContentMode(ContentMode.TEXT);
		price.setValue("0.00");
		AttributeExtension.of(price).setAttribute("tabindex", "0");
		quantityLayout.addComponent(price);
		quantityLayout.setComponentAlignment(price, Alignment.MIDDLE_RIGHT);
		layout.addComponent(quantityLayout);

		comment = new TextArea();
		comment.setRows(2);
		comment.setStyleName("comment long");
		comment.setId("comment");
		comment.setPlaceholder("Details");
		comment.setWidth("100%");
		layout.addComponent(comment);
	}

	private void updatePrice(int productPrice) {
		price.setValue(priceFormatter.convertToPresentation(productPrice,
				new ValueContext(Locale.US)));
	}

	private void announceLinePrice(@Nullable String quantityValue) {
		if (getItem().getProduct() == null || quantityValue == null
				|| quantityValue.trim().isEmpty()) {
			return;
		}
		parseQuantity(quantityValue).ifPresent(value -> Notification.show(
				String.format(Locale.US, "Quantity %d, line price $%s", value,
						priceFormatter.convertToPresentation(getSum(),
								new ValueContext(Locale.US))),
				Type.ASSISTIVE_NOTIFICATION));
	}

	private Optional<Integer> parseQuantity(@Nullable String quantityValue) {
		if (quantityValue == null || quantityValue.trim().isEmpty()) {
			return Optional.empty();
		}

		try {
			return Optional.of(Integer.parseInt(quantityValue.trim()));
		} catch (NumberFormatException exception) {
			return Optional.empty();
		}
	}

	private void fireProductInfoChanged() {
		viewEventBus.publish(this, new ProductInfoChangeEvent());
	}

	private void fireOrderItemDeleted() {
		viewEventBus.publish(this, new OrderItemDeletedEvent(getItem()));
	}

	public int getSum() {
		OrderItem item = getItem();
		return item.getQuantity() * item.getProduct().getPrice();
	}

	public void setItem(OrderItem item) {
		binder.setBean(item);
	}

	public OrderItem getItem() {
		return binder.getBean();
	}

	public void setReportMode(boolean reportMode) {
		if (reportMode == this.reportMode) {
			return;
		}
		this.reportMode = reportMode;
		binder.setReadOnly(reportMode);
		delete.setVisible(!reportMode);

		// Swap the TextArea for a Label in report mode
		if (reportMode) {
			readOnlyComment.setVisible(!comment.isEmpty());
			readOnlyComment.setValue(comment.getValue());
			layout.replaceComponent(comment, readOnlyComment);
		} else {
			layout.replaceComponent(readOnlyComment, comment);
		}
	}

	/**
	 * Checks if no product has been selected. If no product is selected, the
	 * whole product info section is ignored when saving changes.
	 *
	 * @return <code>true</code> if no product is selected, <code>false</code>
	 *         otherwise
	 */
	public boolean isEmpty() {
		return product.isEmpty();
	}

	@SuppressWarnings("null")
	public Stream<HasValue<?>> validate() {
		return binder.validate().getFieldValidationErrors().stream()
				.map(BindingValidationStatus::getField);
	}

	@Override
	public void focus() {
		product.focus();
	}
}
