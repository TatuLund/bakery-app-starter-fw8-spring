package com.vaadin.starter.bakery.ui.views;

import org.jspecify.annotations.NullMarked;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@NullMarked
@SuppressWarnings({ "java:S110", "java:S2160" })
public abstract class AbstractErrorView extends VerticalLayout implements View {

	Label errorLabel;

	protected AbstractErrorView() {
		setSizeFull();
		setMargin(true);
		errorLabel = new Label();
		errorLabel.setId("errorLabel");
		errorLabel.setContentMode(ContentMode.TEXT);
		errorLabel.addStyleName(ValoTheme.LABEL_FAILURE);
		addComponent(errorLabel);
		setComponentAlignment(errorLabel, Alignment.MIDDLE_CENTER);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// Assistive notification for screen readers when navigating to this
		// view
		Notification.show(errorLabel.getValue(),
				Notification.Type.ASSISTIVE_NOTIFICATION);
	}

}
