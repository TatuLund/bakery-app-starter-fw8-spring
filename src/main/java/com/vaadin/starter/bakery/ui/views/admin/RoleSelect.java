package com.vaadin.starter.bakery.ui.views.admin;

import com.vaadin.starter.bakery.backend.data.Role;
import com.vaadin.ui.ComboBox;

@SuppressWarnings("java:S110")
public class RoleSelect extends ComboBox<String> {

	public RoleSelect() {
		setCaption("Role");
		setEmptySelectionAllowed(false);
		setItems(Role.getAllRoles());
		setTextInputAllowed(false);
	}
}
