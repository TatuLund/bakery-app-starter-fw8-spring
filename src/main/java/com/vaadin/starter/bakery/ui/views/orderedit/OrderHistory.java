package com.vaadin.starter.bakery.ui.views.orderedit;

import java.util.Locale;
import java.util.StringJoiner;

import javax.annotation.PostConstruct;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.annotation.PrototypeScope;
import org.vaadin.spring.events.EventBus.ViewEventBus;

import com.vaadin.data.BeanValidationBinder;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.starter.bakery.app.security.SecurityUtils;
import com.vaadin.starter.bakery.backend.data.entity.HistoryItem;
import com.vaadin.starter.bakery.backend.data.entity.Order;
import com.vaadin.starter.bakery.backend.service.OrderService;
import com.vaadin.starter.bakery.backend.service.UserService;
import com.vaadin.starter.bakery.ui.components.AttributeExtension;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.AriaAttributes;
import com.vaadin.starter.bakery.ui.utils.DateTimeFormatter;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickShortcut;
import com.vaadin.ui.Button;
import com.vaadin.ui.Composite;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Encapsulates the order history part of the order edit view.
 * <p>
 * Created as a single class because the logic is so simple that using a pattern
 * like MVP would add much overhead for little gain. If more complexity is added
 * to the class, you should consider splitting out a presenter.
 */
@SpringComponent
@PrototypeScope
@SuppressWarnings({ "java:S2160", "java:S110" })
@NullMarked
public class OrderHistory extends Composite {

	protected CssLayout items;
	protected Button commitNewComment;

	private Panel panel;

	private HistoryComment historyComment;

	private final DateTimeFormatter dateTimeFormatter;

	private transient ViewEventBus eventBus;

	@Nullable
	private Order order;

	private transient OrderService orderService;

	private transient UserService userService;

	@Autowired
	@SuppressWarnings("java:S2637")
	public OrderHistory(DateTimeFormatter dateTimeFormatter,
			ViewEventBus eventBus, OrderService orderService,
			UserService userService) {
		this.dateTimeFormatter = dateTimeFormatter;
		this.eventBus = eventBus;
		this.orderService = orderService;
		this.userService = userService;
		panel = new Panel();
		setCompositionRoot(panel);
	}

	@PostConstruct
	public void setup() {
		setupHistoryLayout();
		// Uses binder to get bean validation for the message
		BeanValidationBinder<HistoryItem> binder = new BeanValidationBinder<>(
				HistoryItem.class);
		binder.setRequiredConfigurator(null); // Don't show a *
		binder.bind(historyComment.getNewCommentInput(), "message");
		commitNewComment.addClickListener(clicked -> {
			if (binder.isValid()) {
				addNewComment(historyComment.getNewCommentInput().getValue());
			} else {
				historyComment.getNewCommentInput().focus();
			}
		});

		// We don't want a global shortcut for enter, scope it to the panel
		panel.addAction(
				new ClickShortcut(commitNewComment, KeyCode.ENTER));

		historyComment.getNewCommentInput()
				.addFocusListener(focused -> announceOrderHistory());
	}

	private void setupHistoryLayout() {
		setCaption("History");
		setStyleName(ValoTheme.PANEL_WELL);
		VerticalLayout historyLayout = new VerticalLayout();
		historyLayout.setStyleName("history");
		historyLayout.setMargin(true);
		items = new CssLayout();
		items.setId("items");
		items.setWidth("100%");
		historyLayout.addComponent(items);
		historyLayout.setComponentAlignment(items, Alignment.TOP_LEFT);
		historyComment = new HistoryComment();
		historyLayout.addComponent(historyComment);
		historyLayout.setComponentAlignment(historyComment, Alignment.TOP_LEFT);
		panel.setContent(historyLayout);
	}

	private void addNewComment(String comment) {
		orderService.addHistoryItem(order, comment,
				SecurityUtils.getCurrentUser(userService));
		eventBus.publish(this, new OrderUpdatedEvent());
	}

	/**
	 * Sets the order for which the history should be shown. The history is read
	 * from the order bean, so if you want to update the history, just update
	 * the order bean and call this method again to refresh the view.
	 *
	 * @param order
	 *            the order for which the history should be shown
	 */
	public void setOrder(Order order) {
		this.order = order;
		historyComment.getNewCommentInput().setValue("");
		items.removeAllComponents();
		order.getHistory().forEach(historyItem -> {
			var historyItemLabel = new Label(formatMessage(historyItem));
			historyItemLabel.addStyleName(ValoTheme.LABEL_SMALL);
			historyItemLabel.setCaption(String.format("%s by %s",
					formatTimestamp(historyItem),
					historyItem.getCreatedBy().getName()));
			historyItemLabel.setWidth("100%");
			items.addComponent(historyItemLabel);
		});
	}

	private String formatTimestamp(HistoryItem historyItem) {
		return dateTimeFormatter.format(historyItem.getTimestamp(), Locale.US);
	}

	private void announceOrderHistory() {
		if (order == null || order.getHistory() == null
				|| order.getHistory().isEmpty()) {
			Notification.show("Order history is empty.",
					Type.ASSISTIVE_NOTIFICATION);
			return;
		}

		Notification.show(buildHistoryAnnouncement(),
				Type.ASSISTIVE_NOTIFICATION);
	}

	private String buildHistoryAnnouncement() {
		StringJoiner joiner = new StringJoiner(". ", "Order history. ", ".");
		order.getHistory().forEach(
				historyItem -> joiner.add(String.format("%s by %s: %s",
						formatTimestamp(historyItem),
						historyItem.getCreatedBy().getName(),
						formatMessage(historyItem))));
		return joiner.toString();
	}

	private String formatMessage(HistoryItem historyItem) {
		return historyItem.getMessage();
	}

	private class HistoryComment extends Composite {

		private final TextField newCommentInput;

		HistoryComment() {
			HorizontalLayout commentLayout = new HorizontalLayout();
			commentLayout.setSpacing(false);
			commentLayout.setWidth("100%");
			commentLayout.setMargin(false);
			setCompositionRoot(commentLayout);

			newCommentInput = new TextField();
			newCommentInput.setId("newCommentInput");
			newCommentInput.setPlaceholder("Message");
			newCommentInput.setWidth("100%");
			AttributeExtension.of(newCommentInput).setAttribute(
					AriaAttributes.LABEL, "New comment");
			commentLayout.addComponent(newCommentInput);
			commentLayout.setComponentAlignment(newCommentInput,
					Alignment.TOP_LEFT);
			commentLayout.setExpandRatio(newCommentInput, 1.0F);

			commitNewComment = new Button();
			commitNewComment.setIcon(VaadinIcons.ENTER_ARROW);
			commitNewComment.setStyleName(ValoTheme.BUTTON_QUIET);
			commitNewComment.setId("commitNewComment");
			commitNewComment.setCaption("");
			commentLayout.addComponent(commitNewComment);
			commentLayout.setComponentAlignment(commitNewComment,
					Alignment.TOP_LEFT);
		}

		private TextField getNewCommentInput() {
			return newCommentInput;
		}
	}
}
