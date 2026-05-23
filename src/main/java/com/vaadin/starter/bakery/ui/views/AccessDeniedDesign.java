
package com.vaadin.starter.bakery.ui.views;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class AccessDeniedDesign extends VerticalLayout {

    public final void init() {
        this.setWidth("100%");
        this.setHeight("100%");
        this.setMargin(new MarginInfo(true, true, true, true));
        Label accessDeniedLabel = new Label();
        accessDeniedLabel.setContentMode(ContentMode.TEXT);
        accessDeniedLabel.setValue("Access denied");
        this.addComponent(accessDeniedLabel);
        this.setComponentAlignment(accessDeniedLabel, Alignment.TOP_LEFT);
    }

}
