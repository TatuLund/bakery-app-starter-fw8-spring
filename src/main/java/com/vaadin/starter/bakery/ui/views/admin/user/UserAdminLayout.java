
package com.vaadin.starter.bakery.ui.views.admin.user;

import org.jspecify.annotations.NullMarked;

import com.vaadin.starter.bakery.backend.data.entity.User;
import com.vaadin.starter.bakery.ui.components.Form;
import com.vaadin.starter.bakery.ui.views.admin.AbstractCrudLayout;
import com.vaadin.starter.bakery.ui.views.admin.RoleSelect;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

@NullMarked
@SuppressWarnings({ "java:S2160", "java:S110" })
public class UserAdminLayout extends AbstractCrudLayout {

    protected Grid<User> list;
    protected TextField email;
    protected TextField name;
    protected TextField password;
    protected RoleSelect role;

    @Override
    protected CssLayout createListParent() {
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

    @Override
    protected Form createForm() {
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

        return form;
    }
}
