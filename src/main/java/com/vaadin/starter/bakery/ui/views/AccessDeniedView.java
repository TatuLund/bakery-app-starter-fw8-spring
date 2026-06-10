package com.vaadin.starter.bakery.ui.views;

import org.jspecify.annotations.NullMarked;
import org.vaadin.spring.annotation.PrototypeScope;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;

@NullMarked
@SpringComponent
@PrototypeScope
@SuppressWarnings("java:S110")
public class AccessDeniedView extends VerticalLayout implements View {

	public AccessDeniedView() {
		setWidth("100%");
		setHeight("100%");
		setMargin(true);
		Label accessDeniedLabel = new Label();
		accessDeniedLabel.setId("accessDeniedLabel");
		accessDeniedLabel.setContentMode(ContentMode.TEXT);
		accessDeniedLabel.setValue("Access deniesd");
		addComponent(accessDeniedLabel);
		setComponentAlignment(accessDeniedLabel, Alignment.TOP_LEFT);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// Assistive notification for screen readers when navigating to this
		// view
		Notification.show(
				"Access denied. You do not have permission to view this page.",
				Notification.Type.ASSISTIVE_NOTIFICATION);
	}

}
