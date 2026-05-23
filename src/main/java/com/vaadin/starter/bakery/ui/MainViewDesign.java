
package com.vaadin.starter.bakery.ui;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings({ "java:S2160", "java:S110" })
public class MainViewDesign extends HorizontalLayout {

    protected Label activeViewName;
    protected Button menuButton;
    protected CssLayout menu;
    protected Button storefront;
    protected Button dashboard;
    protected Button users;
    protected Button products;
    protected Button logout;
    protected VerticalLayout content;

    public final void init() {
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
        content.setStyleName("content-container v-scrollable");
        content.setWidth("100%");
        content.setHeight("100%");
        content.setMargin(false);
        content.setId("content");
        addComponent(content);
        setComponentAlignment(content, Alignment.TOP_LEFT);
        setExpandRatio(content, 1.0F);
    }

    private CssLayout createNavigationBar() {
        CssLayout navigationBar = new CssLayout();
        navigationBar.setStyleName("navigation-bar");
        navigationBar.setWidth("100%");
        navigationBar.setHeight("100%");

        Label logo = new Label();
        logo.setStyleName("logo");
        logo.setWidth("100%");
        logo.setContentMode(ContentMode.TEXT);
        logo.setValue("###Bakery###");
        navigationBar.addComponent(logo);

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

        menu = new CssLayout();
        menu.setStyleName("navigation");
        menu.setId("menu");
        menu.setWidth("100%");

        storefront = new Button();
        storefront.setIcon(VaadinIcons.CART);
        storefront.setCaption("Storefront");
        storefront.setId("storefront");
        addMenuButton(storefront);

        dashboard = new Button();
        dashboard.setIcon(VaadinIcons.LINE_BAR_CHART);
        dashboard.setCaption("Dashboard");
        dashboard.setId("dashboard");
        addMenuButton(dashboard);

        users = new Button();
        users.setIcon(VaadinIcons.USERS);
        users.setCaption("Users");
        users.setId("users");
        addMenuButton(users);

        products = new Button();
        products.setIcon(VaadinIcons.STOCK);
        products.setCaption("Products");
        products.setId("products");
        addMenuButton(products);

        logout = new Button();
        logout.setIcon(VaadinIcons.SIGN_OUT);
        logout.setCaption("Log out");
        logout.setId("logout");
        addMenuButton(logout);
        menu.setId("menu");

        navigationBar.addComponent(menu);
        return navigationBar;
    }

    private void addMenuButton(Button button) {
        button.setStyleName(ValoTheme.BUTTON_BORDERLESS);
        button.setWidth("100%");
        button.setHeight("80px");
        menu.addComponent(button);
    }

}
