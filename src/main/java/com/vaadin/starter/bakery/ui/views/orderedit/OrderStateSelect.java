package com.vaadin.starter.bakery.ui.views.orderedit;

import org.jspecify.annotations.NullMarked;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.starter.bakery.app.HasLogger;
import com.vaadin.starter.bakery.backend.data.OrderState;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.HasAttributes;
import com.vaadin.ui.ComboBox;

@SpringComponent
@ViewScope
@SuppressWarnings("java:S110")
@NullMarked
public class OrderStateSelect extends ComboBox<OrderState>
		implements HasLogger, HasAttributes<OrderStateSelect> {

	public OrderStateSelect() {
		setEmptySelectionAllowed(false);
		setTextInputAllowed(false);
		setItems(OrderState.values());
		setItemCaptionGenerator(OrderState::getDisplayName);
		setAriaLabel("Order state");
	}
}
