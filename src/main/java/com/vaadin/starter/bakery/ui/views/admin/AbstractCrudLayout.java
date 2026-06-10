package com.vaadin.starter.bakery.ui.views.admin;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.starter.bakery.ui.components.AttributeExtension;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.AriaAttributes;
import com.vaadin.starter.bakery.ui.components.Form;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@NullMarked
@SuppressWarnings({ "java:S2160", "java:S110" })
public abstract class AbstractCrudLayout extends VerticalLayout {

    @Nullable
    protected TextField search;
    @Nullable
    protected Button add;
    @Nullable
    protected Form form;
    @Nullable
    protected Button update;
    @Nullable
    protected Button cancel;
    @Nullable
    protected Button delete;

    protected AbstractCrudLayout() {
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
        initializeFormButtons(form);
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

    protected abstract CssLayout createListParent();

    protected abstract Form createForm();

    private void initializeFormButtons(Form form) {
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
    }

    public TextField getSearch() {
        return search;
    }

    public void setSearch(TextField search) {
        this.search = search;
    }

    public Button getAdd() {
        return add;
    }

    public void setAdd(Button add) {
        this.add = add;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public Button getUpdate() {
        return update;
    }

    public void setUpdate(Button update) {
        this.update = update;
    }

    public Button getCancel() {
        return cancel;
    }

    public void setCancel(Button cancel) {
        this.cancel = cancel;
    }

    public Button getDelete() {
        return delete;
    }

    public void setDelete(Button delete) {
        this.delete = delete;
    }
}

