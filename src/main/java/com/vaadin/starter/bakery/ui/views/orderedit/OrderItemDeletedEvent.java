package com.vaadin.starter.bakery.ui.views.orderedit;

import java.io.Serializable;

import com.vaadin.starter.bakery.backend.data.entity.OrderItem;

public class OrderItemDeletedEvent implements Serializable {

	private OrderItem orderItem;

	public OrderItemDeletedEvent(OrderItem orderItem) {
		this.orderItem = orderItem;
	}

	public OrderItem getOrderItem() {
		return orderItem;
	}
}
