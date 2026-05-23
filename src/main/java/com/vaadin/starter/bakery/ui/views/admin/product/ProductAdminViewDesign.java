
package com.vaadin.starter.bakery.ui.views.admin.product;

import com.vaadin.server.FontAwesome;
import com.vaadin.starter.bakery.backend.data.entity.Product;
import com.vaadin.starter.bakery.ui.components.Form;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class ProductAdminViewDesign extends VerticalLayout {

    protected TextField search;
    protected Button add;
    protected CssLayout listParent;
    protected Grid<Product> list;
    protected Form form;
    protected TextField name;
    protected TextField price;
    protected Button update;
    protected Button cancel;
    protected Button delete;

    public final void init() {
        this.setStyleName("crud-template");
        this.setSpacing(false);
        this.setResponsive(true);
        this.setWidth("100%");
        this.setHeight("100%");
        this.setMargin(false);
        HorizontalLayout topBar = createTopBar();
        this.addComponent(topBar);
        this.setComponentAlignment(topBar, Alignment.TOP_LEFT);
        CssLayout contentWrapper = new CssLayout();
        contentWrapper.setStyleName("content");
        contentWrapper.setWidth("100%");
        contentWrapper.setHeight("100%");
        this.listParent = createListParent();
        this.form = createForm();
        contentWrapper.addComponents(listParent, form);
        this.addComponent(contentWrapper);
        this.setComponentAlignment(contentWrapper, Alignment.TOP_LEFT);
        this.setExpandRatio(contentWrapper, 1.0F);
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

        this.search = new TextField();
        search.setIcon(FontAwesome.SEARCH);
        search.setStyleName("small inline-icon search");
        search.setPlaceholder("Search");
        search.setWidth("100%");
        search.setId("search");
        searchWrapper.addComponent(search);
        searchWrapper.setComponentAlignment(search, Alignment.TOP_LEFT);

        topBar.addComponent(searchWrapper);
        topBar.setComponentAlignment(searchWrapper, Alignment.MIDDLE_LEFT);
        topBar.setExpandRatio(searchWrapper, 1.0F);

        this.add = new Button();
        add.setIcon(FontAwesome.PLUS);
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

        this.list = new Grid<>(Product.class);
        list.setWidth("100%");
        list.setHeight("100%");
        list.setId("list");
        listParent.addComponent(list);
        listParent.setId("listParent");

        return listParent;
    }

    private Form createForm() {
        Form form = new Form();

        this.name = new TextField();
        name.setCaption("Name");
        name.setStyleName("small");
        name.setWidth("100%");
        name.setId("name");
        form.addField(name);

        this.price = new TextField();
        price.setCaption("Price");
        price.setStyleName("small");
        price.setWidth("100%");
        price.setId("price");
        form.addField(price);

        this.update = new Button();
        update.setStyleName("small primary");
        update.setCaption("Update");
        update.setId("update");
        form.setUpdateButton(update);

        this.cancel = new Button();
        cancel.setStyleName("small");
        cancel.setCaption("Cancel");
        cancel.setId("cancel");
        form.setCancelButton(cancel);

        this.delete = new Button();
        delete.setStyleName("small danger");
        delete.setCaption("Delete");
        delete.setId("delete");
        form.setDeleteButton(delete);

        return form;
    }

}
