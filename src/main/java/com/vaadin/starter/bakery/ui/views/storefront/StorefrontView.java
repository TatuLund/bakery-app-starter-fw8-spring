package com.vaadin.starter.bakery.ui.views.storefront;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.starter.bakery.backend.data.entity.Order;
import com.vaadin.starter.bakery.ui.navigation.NavigationManager;
import com.vaadin.starter.bakery.ui.components.AttributeExtension;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.AriaAttributes;
import com.vaadin.starter.bakery.ui.components.OrdersGrid;
import com.vaadin.starter.bakery.ui.views.orderedit.OrderEditView;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickShortcut;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * The storefront view showing upcoming orders.
 * <p>
 * Created as a single View class because the logic is so simple that using a
 * pattern like MVP would add much overhead for little gain. If more complexity
 * is added to the class, you should consider splitting out a presenter.
 */
@SpringView
@SuppressWarnings({ "java:S110", "java:S2160", "java:S6813" })
public class StorefrontView extends VerticalLayout implements View {

	private static final String PARAMETER_SEARCH = "search";

	private static final String PARAMETER_INCLUDE_PAST = "includePast";

	protected Panel searchPanel;

	protected TextField searchField;

	protected Button searchButton;

	protected CheckBox includePast;

	protected Button newOrder;

	@Autowired
	private OrdersGrid list;

	private final NavigationManager navigationManager;

	@Autowired
	public StorefrontView(NavigationManager navigationManager) {
		this.navigationManager = navigationManager;
	}

	/**
	 * This method is invoked once each time an instance of the view is created.
	 * <p>
	 * This typically happens whenever a user opens the URL for the view, or
	 * refreshes the browser as long as the view is set to {@link ViewScope}. If
	 * we set the view to {@link UIScope}, the instance will be kept in memory
	 * (in the session) as long as the UI exists in memory and the same view
	 * instance will be reused whenever the user enters the view.
	 * <p>
	 * Here we set up listeners and attach data providers and otherwise
	 * configure the components for the parts which only need to be done once.
	 */
	@PostConstruct
	public void setup() {
		initLayout();
		list.addSelectionListener(
				e -> selectedOrder(e.getFirstSelectedItem().get()));
		newOrder.addClickListener(e -> newOrder());
		searchButton.addClickListener(
				e -> search(searchField.getValue(), includePast.getValue()));

		// We don't want a global shortcut for enter, scope it to the panel
		searchPanel.addAction(
				new ClickShortcut(searchButton, KeyCode.ENTER, null));
	}

	private void initLayout() {
		setStyleName("storefront");
		setSpacing(false);
		setWidth("100%");
		setHeight("100%");
		setMargin(false);

		searchPanel = new Panel();
		searchPanel.setStyleName(ValoTheme.PANEL_BORDERLESS);

		HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setSpacing(false);
		toolbar.setWidth("100%");
		toolbar.setMargin(new MarginInfo(true, true, false, true));

		CssLayout filterLayout = createFilterLayout();

		toolbar.addComponent(filterLayout);
		toolbar.setComponentAlignment(filterLayout, Alignment.TOP_LEFT);
		toolbar.setExpandRatio(filterLayout, 1.0F);

		newOrder = new Button();
		newOrder.setIcon(VaadinIcons.PLUS);
		newOrder.setStyleName(ValoTheme.BUTTON_FRIENDLY);
		newOrder.setId("newOrder");
		newOrder.setCaption("New");
		toolbar.addComponent(newOrder);
		toolbar.setComponentAlignment(newOrder, Alignment.TOP_LEFT);

		searchPanel.setContent(toolbar);
		addComponent(searchPanel);
		setComponentAlignment(searchPanel, Alignment.TOP_LEFT);

		list.setId("list");
		list.setWidth("100%");
		list.setHeight("100%");
		addComponent(list);
		setComponentAlignment(list, Alignment.TOP_LEFT);
		setExpandRatio(list, 1.0F);
	}

	private CssLayout createFilterLayout() {
		CssLayout filterLayout = new CssLayout();
		filterLayout.setStyleName("list-filters");
		filterLayout.setWidth("100%");

		searchField = new TextField();
		searchField.setId("searchField");
		searchField.setPlaceholder("Search");
		AttributeExtension.of(searchField).setAttribute(AriaAttributes.LABEL,
				"Order search criteria");
		filterLayout.addComponent(searchField);

		searchButton = new Button();
		searchButton.setIcon(VaadinIcons.SEARCH);
		searchButton.setId("searchButton");
		searchButton.setCaption("");
		searchButton.setDescription("Search orders");
		filterLayout.addComponent(searchButton);

		includePast = new CheckBox();
		includePast.setCaption("Include past");
		includePast.setStyleName(ValoTheme.CHECKBOX_SMALL);
		filterLayout.addComponent(includePast);

		return filterLayout;
	}

	public void selectedOrder(Order order) {
		navigationManager.navigateTo(OrderEditView.class, order.getId());
	}

	public void newOrder() {
		navigationManager.navigateTo(OrderEditView.class);
	}

	public void search(String searchTerm, boolean includePast) {
		filterGrid(searchTerm, includePast);
		String parameters = PARAMETER_SEARCH + "=" + searchTerm;
		if (includePast) {
			parameters += "&" + PARAMETER_INCLUDE_PAST;
		}
		navigationManager.updateViewParameter(parameters);
	}

	/**
	 * This is called whenever the user enters the view, regardless of if the
	 * view instance was created right before this or if an old instance was
	 * reused.
	 * <p>
	 * Here we update the data shown in the view so the user sees the latest
	 * changes.
	 */
	@Override
	public void enter(ViewChangeEvent event) {
		Map<String, String> params = event.getParameterMap();
		String searchTerm = params.getOrDefault(PARAMETER_SEARCH, "");
		boolean includePast = params.containsKey(PARAMETER_INCLUDE_PAST);
		filterGrid(searchTerm, includePast);
	}

	public void filterGrid(String searchTerm, boolean includePast) {
		list.filterGrid(searchTerm, includePast);
		searchField.setValue(searchTerm);
		this.includePast.setValue(includePast);
	}
}
