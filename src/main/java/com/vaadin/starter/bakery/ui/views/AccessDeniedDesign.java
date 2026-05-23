
package com.vaadin.starter.bakery.ui.views;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("java:S110")
public class AccessDeniedDesign extends VerticalLayout {

    public final void init() {
        setWidth("100%");
        setHeight("100%");
        setMargin(new MarginInfo(true, true, true, true));
        Label accessDeniedLabel = new Label();
        accessDeniedLabel.setContentMode(ContentMode.TEXT);
        accessDeniedLabel.setValue("Access denied");
        addComponent(accessDeniedLabel);
        setComponentAlignment(accessDeniedLabel, Alignment.TOP_LEFT);
    }

}
