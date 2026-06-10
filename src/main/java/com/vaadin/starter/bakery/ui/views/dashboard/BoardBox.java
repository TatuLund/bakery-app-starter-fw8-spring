package com.vaadin.starter.bakery.ui.views.dashboard;

import org.jspecify.annotations.NullMarked;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

@SuppressWarnings({ "java:S110", "java:S2160" })
@NullMarked
public class BoardBox extends CssLayout {

	private CssLayout mainDiv = new CssLayout();

	public BoardBox(Component component) {
		addStyleName("board-box-wrapper");
		setSizeFull();
		addComponent(mainDiv);
		mainDiv.addStyleName("board-box");
		mainDiv.setSizeFull();
		CssLayout inner = new CssLayout();
		inner.setSizeFull();
		inner.addStyleName("board-box-inner");
		inner.addComponent(component);
		mainDiv.addComponent(inner);
	}

	public BoardBox(Component component, String styleName) {
		this(component);
		mainDiv.addStyleName(styleName);
	}

	public void setNeedsAttention(boolean needsAttention) {
		mainDiv.setStyleName("board-box-needs-attention", needsAttention);
	}
}
