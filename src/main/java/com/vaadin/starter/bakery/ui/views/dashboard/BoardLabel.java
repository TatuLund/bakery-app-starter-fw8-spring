package com.vaadin.starter.bakery.ui.views.dashboard;

import org.jspecify.annotations.NullMarked;
import org.springframework.lang.Nullable;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;

@NullMarked
@SuppressWarnings("java:S2160")
public class BoardLabel extends Label {

	@Nullable
	private String header;
	@Nullable
	private String content;

	public BoardLabel(String header, String content) {
		super("", ContentMode.HTML);
		addStyleName("board-box-label");
		setSizeFull();
		setHeader(header);
		setContent(content);
	}

	public BoardLabel(String header, String content, String styleName) {
		this(header, content);
		addStyleName(styleName);
	}

	private void setHeader(String header) {
		this.header = header;
		updateValue();
	}

	public void setContent(String content) {
		this.content = content;
		updateValue();
	}

	private void updateValue() {
		setValue("<h1>" + content + "</h1>" //
				+ "<h4>" + header + "</h4>");
	}

}
