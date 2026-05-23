package com.vaadin.starter.bakery.ui.views;

import org.vaadin.spring.annotation.PrototypeScope;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringComponent;

@SpringComponent
@PrototypeScope
@SuppressWarnings("java:S110")
public class AccessDeniedView extends AccessDeniedDesign implements View {

	@Override
	public void enter(ViewChangeEvent event) {
		// Nothing to do, just show the view
	}

}
