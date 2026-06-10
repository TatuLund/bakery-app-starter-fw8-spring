
package com.vaadin.starter.bakery.ui.views.admin.product;

import org.jspecify.annotations.NullMarked;

import com.vaadin.starter.bakery.backend.data.entity.Product;
import com.vaadin.starter.bakery.ui.components.Form;
import com.vaadin.starter.bakery.ui.views.admin.AbstractCrudLayout;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

@NullMarked
@SuppressWarnings({ "java:S2160", "java:S110" })
public class ProductAdminLayout extends AbstractCrudLayout {

    protected Grid<Product> list;
    protected TextField name;
    protected TextField price;

    @Override
    protected CssLayout createListParent() {
        CssLayout listParent = new CssLayout();
        listParent.setStyleName("list");
        listParent.setId("listParent");
        listParent.setWidth("100%");
        listParent.setHeight("100%");

        list = new Grid<>(Product.class);
        list.setWidth("100%");
        list.setHeight("100%");
        list.setId("list");
        list.setAccessibleNavigation(true);
        listParent.addComponent(list);

        return listParent;
    }

    @Override
    protected Form createForm() {
        Form form = new Form();

        name = new TextField();
        name.setCaption("Name");
        name.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        name.setWidth("100%");
        name.setId("name");
        form.addField(name);

        price = new TextField();
        price.setCaption("Price");
        price.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        price.setWidth("100%");
        price.setId("price");
        form.addField(price);

        return form;
    }
}
