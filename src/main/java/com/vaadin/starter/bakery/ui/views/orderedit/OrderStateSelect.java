package com.vaadin.starter.bakery.ui.views.orderedit;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.starter.bakery.app.HasLogger;
import com.vaadin.starter.bakery.backend.data.OrderState;
import com.vaadin.ui.ComboBox;

@SpringComponent
@ViewScope
@SuppressWarnings("java:S110")
public class OrderStateSelect extends ComboBox<OrderState> implements HasLogger {

	public OrderStateSelect() {
		setEmptySelectionAllowed(false);
		setTextInputAllowed(false);
		setItems(OrderState.values());
		setItemCaptionGenerator(OrderState::getDisplayName);
	}

}
