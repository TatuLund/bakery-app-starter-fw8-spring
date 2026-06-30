package com.vaadin.starter.bakery.ui.views.mainview;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.navigator.ViewLeaveAction;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringViewDisplay;
import com.vaadin.spring.access.SecuredViewAccessControl;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.starter.bakery.ui.components.AttributeExtension;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.AriaAttributes;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.AriaRoles;
import com.vaadin.starter.bakery.ui.navigation.NavigationManager;
import com.vaadin.starter.bakery.ui.views.admin.product.ProductAdminView;
import com.vaadin.starter.bakery.ui.views.admin.user.UserAdminView;
import com.vaadin.starter.bakery.ui.views.dashboard.DashboardView;
import com.vaadin.starter.bakery.ui.views.storefront.StorefrontView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * The main view containing the menu and the content area where actual views are
 * shown.
 * <p>
 * Created as a single View class because the logic is so simple that using a
 * pattern like MVP would add much overhead for little gain. If more complexity
 * is added to the class, you should consider splitting out a presenter.
 */
@NullMarked
@SpringViewDisplay
@UIScope
@SuppressWarnings({ "java:S2160", "java:S110" })
public class MainView extends HorizontalLayout implements ViewDisplay {

	protected Label activeViewName;
	protected Button menuButton;
	protected Navigation menu;
	protected Button storefront;
	protected Button dashboard;
	protected Button users;
	protected Button products;
	protected Button logout;
	protected VerticalLayout content;

	private final Map<Class<? extends View>, Button> navigationButtons = new HashMap<>();
	private final NavigationManager navigationManager;
	private final SecuredViewAccessControl viewAccessControl;

	@Autowired
	@SuppressWarnings("java:S2637")
	public MainView(NavigationManager navigationManager,
			SecuredViewAccessControl viewAccessControl) {
		this.navigationManager = navigationManager;
		this.viewAccessControl = viewAccessControl;
	}

	@PostConstruct
	public void setup() {
		setupAppLayout();
		attachNavigation(storefront, StorefrontView.class);
		attachNavigation(dashboard, DashboardView.class);
		attachNavigation(users, UserAdminView.class);
		attachNavigation(products, ProductAdminView.class);

		logout.addClickListener(clicked -> logout());
	}

	private void setupAppLayout() {
		setStyleName("app-shell");
		setSpacing(false);
		setResponsive(true);
		setWidth("100%");
		setHeight("100%");
		setMargin(false);
		CssLayout navigationContainer = new CssLayout();
		navigationContainer.setStyleName("navigation-bar-container");
		navigationContainer.setWidth("200px");
		navigationContainer.setHeight("100%");
		CssLayout navigationBar = createNavigationBar();
		navigationContainer.addComponent(navigationBar);
		addComponent(navigationContainer);
		setComponentAlignment(navigationContainer, Alignment.TOP_LEFT);
		content = new VerticalLayout();
		content.setStyleName("content-container " + ValoTheme.SCROLLABLE);
		content.setWidth("100%");
		content.setHeight("100%");
		content.setMargin(false);
		content.setId("content");
		AttributeExtension.of(content).setAttribute(AriaAttributes.ROLE,
				AriaRoles.MAIN);
		addComponent(content);
		setComponentAlignment(content, Alignment.TOP_LEFT);
		setExpandRatio(content, 1.0F);
	}

	private CssLayout createNavigationBar() {
		CssLayout navigationBar = new CssLayout();
		navigationBar.setStyleName("navigation-bar");
		navigationBar.setWidth("100%");
		navigationBar.setHeight("100%");

		Label icon = new Label();
		icon.setStyleName("icon");

		Label logo = new Label();
		logo.setStyleName("logo");
		logo.setWidth("100%");
		logo.setContentMode(ContentMode.TEXT);
		logo.setValue("Bakery");
		navigationBar.addComponents(icon, logo);

		activeViewName = new Label();
		activeViewName.setStyleName("activeViewName");
		activeViewName.setContentMode(ContentMode.TEXT);
		activeViewName.setValue("Active view");
		navigationBar.addComponent(activeViewName);

		menuButton = new Button();
		menuButton.setIcon(VaadinIcons.MENU);
		menuButton.setStyleName("menu " + ValoTheme.BUTTON_BORDERLESS);
		menuButton.setCaption("Menu");
		menuButton.setId("menuButton");
		navigationBar.addComponent(menuButton);

		menu = new Navigation();

		storefront = new MenuButton("Storefront", VaadinIcons.CART);
		storefront.setId("storefront");
		menu.addMenuButton(storefront);

		dashboard = new MenuButton("Dashboard",
				VaadinIcons.LINE_BAR_CHART);
		dashboard.setId("dashboard");
		menu.addMenuButton(dashboard);

		users = new MenuButton("Users", VaadinIcons.USERS);
		users.setId("users");
		menu.addMenuButton(users);

		products = new MenuButton("Products", VaadinIcons.STOCK);
		products.setId("products");
		menu.addMenuButton(products);

		logout = new MenuButton("Log out", VaadinIcons.SIGN_OUT);
		logout.setId("logout");
		menu.addMenuButton(logout);
		menu.setId("menu");

		navigationBar.addComponent(menu);
		return navigationBar;
	}

	/**
	 * Makes clicking the given button navigate to the given view if the user
	 * has access to the view.
	 * <p>
	 * If the user does not have access to the view, hides the button.
	 *
	 * @param navigationButton
	 *            the button to use for navigatio
	 * @param targetView
	 *            the view to navigate to when the user clicks the button
	 */
	private void attachNavigation(Button navigationButton,
			Class<? extends View> targetView) {
		boolean hasAccessToView = viewAccessControl.isAccessGranted(targetView);
		navigationButton.setVisible(hasAccessToView);

		if (hasAccessToView) {
			navigationButtons.put(targetView, navigationButton);
			navigationButton.addClickListener(
					clicked -> navigationManager.navigateTo(targetView));
		}
	}

	@Override
	public void showView(View view) {
		content.removeAllComponents();
		content.addComponent(view.getViewComponent());

		navigationButtons.forEach((viewClass, button) -> button
				.setStyleName("selected", viewClass == view.getClass()));

		Button menuItem = navigationButtons.get(view.getClass());
		String viewName = "";
		if (menuItem != null) {
			viewName = menuItem.getCaption();
		}
		activeViewName.setValue(viewName);
	}

	/**
	 * Logs the user out after ensuring the currently open view has no unsaved
	 * changes.
	 */
	public void logout() {
		ViewLeaveAction doLogout = () -> {
			UI ui = getUI();
			ui.getSession().getSession().invalidate();
			ui.getPage().reload();
		};

		navigationManager.runAfterLeaveConfirmation(doLogout);
	}
}
