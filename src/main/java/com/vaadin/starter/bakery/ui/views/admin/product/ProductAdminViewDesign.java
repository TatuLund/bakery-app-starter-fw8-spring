
package com.vaadin.starter.bakery.ui.views.admin.product;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.starter.bakery.backend.data.entity.Product;
import com.vaadin.starter.bakery.ui.components.AttributeExtension;
import com.vaadin.starter.bakery.ui.components.Form;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.AriaAttributes;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings({ "java:S2160", "java:S110" })
public class ProductAdminViewDesign extends VerticalLayout {

    protected TextField search;
    protected Button add;
    protected Grid<Product> list;
    protected Form form;
    protected TextField name;
    protected TextField price;
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
        CssLayout listParent = createListParent();
        form = createForm();
        contentWrapper.addComponents(listParent, form);
        addComponent(contentWrapper);
        setComponentAlignment(contentWrapper, Alignment.TOP_LEFT);
        setExpandRatio(contentWrapper, 1.0F);
    }

    private HorizontalLayout createTopBar() {
        HorizontalLayout topBar = new HorizontalLayout();
        topBar.setStyleName("top-bar");
        topBar.setSpacing(false);
        topBar.setWidth("100%");
        topBar.setHeight("50px");
        topBar.setMargin(false);

        HorizontalLayout searchWrapper = new HorizontalLayout();
        searchWrapper.setSpacing(false);
        searchWrapper.setWidth("100%");
        searchWrapper.setMargin(false);

        search = new TextField();
        search.setIcon(VaadinIcons.SEARCH);
        search.setStyleName(ValoTheme.TEXTFIELD_SMALL + " "
                + ValoTheme.TEXTFIELD_INLINE_ICON + " search");
        search.setPlaceholder("Search");
        AttributeExtension.of(search).setAttribute(AriaAttributes.LABEL,
                "Search products");
        search.setWidth("100%");
        search.setId("search");
        searchWrapper.addComponent(search);
        searchWrapper.setComponentAlignment(search, Alignment.TOP_LEFT);

        topBar.addComponent(searchWrapper);
        topBar.setComponentAlignment(searchWrapper, Alignment.MIDDLE_LEFT);
        topBar.setExpandRatio(searchWrapper, 1.0F);

        add = new Button();
        add.setIcon(VaadinIcons.PLUS);
        add.setStyleName("borderless");
        add.setCaption("Add new");
        add.setId("add");
        topBar.addComponent(add);
        topBar.setComponentAlignment(add, Alignment.MIDDLE_RIGHT);

        return topBar;
    }

    private CssLayout createListParent() {
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
        listParent.setId("listParent");

        return listParent;
    }

    private Form createForm() {
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
