
package com.vaadin.starter.bakery.ui.views.admin.user;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.starter.bakery.backend.data.entity.User;
import com.vaadin.starter.bakery.ui.components.AttributeExtension;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.AriaAttributes;
import com.vaadin.starter.bakery.ui.components.Form;
import com.vaadin.starter.bakery.ui.views.admin.RoleSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings({ "java:S2160", "java:S110" })
public class UserAdminViewDesign extends VerticalLayout {

    protected TextField search;
    protected Button add;
    protected Grid<User> list;
    protected Form form;
    protected TextField email;
    protected TextField name;
    protected TextField password;
    protected RoleSelect role;
    protected Button update;
    protected Button cancel;
    protected Button delete;

    public final void init() {
        setStyleName("crud-template");
        setSpacing(false);
        setResponsive(true);
        setWidth("100%");
        setHeight("100%");
        setMargin(false);
        HorizontalLayout topBar = createTopBar();
        addComponent(topBar);
        setComponentAlignment(topBar, Alignment.TOP_LEFT);
        CssLayout contentWrapper = new CssLayout();
        contentWrapper.setStyleName("content");
        contentWrapper.setWidth("100%");
        contentWrapper.setHeight("100%");
        CssLayout listWrapper = createList();
        form = createForm();
        contentWrapper.addComponents(listWrapper, form);
        addComponent(contentWrapper);
        setComponentAlignment(contentWrapper, Alignment.TOP_LEFT);
        setExpandRatio(contentWrapper, 1.0F);
    }

    private CssLayout createList() {
        CssLayout listWrapper = new CssLayout();
        listWrapper.setStyleName("list");
        listWrapper.setWidth("100%");
        listWrapper.setHeight("100%");
        list = new Grid<>(User.class);
        listWrapper.addComponent(list);
        list.setWidth("100%");
        list.setHeight("100%");
        list.setId("list");
        list.setAccessibleNavigation(true);
        return listWrapper;
    }

    private HorizontalLayout createTopBar() {
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setStyleName("top-bar");
        topBar.setSpacing(false);
        topBar.setWidth("100%");
        topBar.setHeight("50px");
        topBar.setMargin(false);

        HorizontalLayout tools = new HorizontalLayout();
        tools.setSpacing(false);
        tools.setWidth("100%");
        tools.setMargin(false);

        this.search = new TextField();
        search.setIcon(VaadinIcons.SEARCH);
        search.setStyleName(ValoTheme.TEXTFIELD_SMALL + " "
                + ValoTheme.TEXTFIELD_INLINE_ICON + " search");
        search.setPlaceholder("Search");
        search.setWidth("100%");
        search.setId("search");
        AttributeExtension.of(search).setAttribute(AriaAttributes.LABEL,
                "Search users");
        tools.addComponent(search);
        tools.setComponentAlignment(search, Alignment.TOP_LEFT);

        topBar.addComponent(tools);
        topBar.setComponentAlignment(tools, Alignment.MIDDLE_LEFT);
        topBar.setExpandRatio(tools, 1.0F);

        add = new Button();
        add.setIcon(VaadinIcons.PLUS);
        add.setStyleName(ValoTheme.BUTTON_BORDERLESS);
        add.setCaption("Add new");
        add.setId("add");
        topBar.addComponent(add);
        topBar.setComponentAlignment(add, Alignment.MIDDLE_RIGHT);

        return topBar;
    }

    private Form createForm() {
        Form form = new Form();

        email = new TextField();
        email.setCaption("Email (login)");
        email.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        email.setWidth("100%");
        email.setHeight("31px");
        email.setId("email");
        form.addField(email);

        name = new TextField();
        name.setCaption("Name");
        name.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        name.setWidth("100%");
        name.setId("name");
        form.addField(name);

        password = new PasswordField();
        password.setCaption("Password");
        password.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        password.setWidth("100%");
        password.setId("password");
        form.addField(password);

        role = new RoleSelect();
        role.setStyleName(ValoTheme.COMBOBOX_SMALL);
        role.setWidth("100%");
        role.setId("role");
        form.addField(role);

        update = new Button();
        update.setStyleName(
                ValoTheme.BUTTON_SMALL + " " + ValoTheme.BUTTON_PRIMARY);
        update.setCaption("Update");
        update.setId("update");
        form.setUpdateButton(update);

        cancel = new Button();
        cancel.setStyleName(ValoTheme.BUTTON_SMALL);
        cancel.setCaption("Cancel");
        cancel.setId("cancel");
        form.setCancelButton(cancel);

        delete = new Button();
        delete.setStyleName(
                ValoTheme.BUTTON_SMALL + " " + ValoTheme.BUTTON_DANGER);
        delete.setCaption("Delete");
        delete.setId("delete");
        form.setDeleteButton(delete);

        return form;
    }

}
