package com.vaadin.starter.bakery.ui.components;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Composite;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

public class Form extends Composite {

	private final VerticalLayout root;
	private final CssLayout editWrapper;
	private final HorizontalLayout buttonsWrapper;
	private Button updateButton;
	private Button cancelButton;
	private Button deleteButton;

	public Form() {
		root = new VerticalLayout();
		root.setSpacing(false);
		root.setMargin(false);
		root.setSizeFull();

		editWrapper = new CssLayout();
		editWrapper.setStyleName("edit");
		editWrapper.setWidth("100%");
		editWrapper.setHeight("100%");
		root.addComponent(editWrapper);
		root.setComponentAlignment(editWrapper, Alignment.TOP_LEFT);
		root.setExpandRatio(editWrapper, 1.0F);

		buttonsWrapper = new HorizontalLayout();
		buttonsWrapper.setStyleName("buttons border-top");
		buttonsWrapper.setSpacing(false);
		buttonsWrapper.setWidth("100%");
		buttonsWrapper.setHeight("50px");
		buttonsWrapper.setMargin(false);
		root.addComponent(buttonsWrapper);
		root.setComponentAlignment(buttonsWrapper, Alignment.TOP_LEFT);

		setCompositionRoot(root);
		setEnabled(false);
		setStyleName("inspect");
		setId("form");
		setWidth("100%");
		setHeight("100%");
	}

	public void addField(Component field) {
		CssLayout fieldWrapper = new CssLayout();
		fieldWrapper.setStyleName("section half");
		fieldWrapper.setWidth("100%");
		fieldWrapper.addComponent(field);
		editWrapper.addComponent(fieldWrapper);
	}

	public void setDeleteButton(Button deleteButton) {
		this.deleteButton = deleteButton;
		refreshButtons();
	}

	public void setCancelButton(Button cancelButton) {
		this.cancelButton = cancelButton;
		refreshButtons();
	}

	public void setUpdateButton(Button updateButton) {
		this.updateButton = updateButton;
		refreshButtons();
	}

	private void refreshButtons() {
		buttonsWrapper.removeAllComponents();

		addButton(updateButton, Alignment.MIDDLE_LEFT);
		addButton(cancelButton, Alignment.MIDDLE_CENTER);
		addButton(deleteButton, Alignment.MIDDLE_RIGHT);

		if (deleteButton != null) {
			buttonsWrapper.setExpandRatio(deleteButton, 1.0F);
		}
	}

	private void addButton(Button button, Alignment alignment) {
		if (button == null) {
			return;
		}

		buttonsWrapper.addComponent(button);
		buttonsWrapper.setComponentAlignment(button, alignment);
	}
}