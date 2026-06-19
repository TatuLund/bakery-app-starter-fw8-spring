package com.vaadin.starter.bakery.ui.views;

import org.vaadin.spring.annotation.PrototypeScope;

import com.vaadin.spring.annotation.SpringComponent;

@SpringComponent
@PrototypeScope
@SuppressWarnings("java:S110")
public class ErrorView extends AbstractErrorView {

    public ErrorView() {
        errorLabel.setValue("View not found");
    }
    
}
