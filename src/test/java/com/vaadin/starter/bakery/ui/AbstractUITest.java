package com.vaadin.starter.bakery.ui;

import com.vaadin.starter.bakery.app.Application;
import com.vaadin.starter.bakery.backend.data.Role;
import com.vaadin.testbench.uiunittest.SpringSecurityUIUnitTest;
import com.vaadin.ui.UI;

public abstract class AbstractUITest extends SpringSecurityUIUnitTest {

    protected Class<?>[] getConfigurationClasses() {
        return new Class<?>[] { Application.class };
    }

    @Override
    protected Class<? extends UI> getUiClass() {
        return AppUI.class;
    }

    @Override
    public DeploymentMode getDeploymentMode() {
        return DeploymentMode.PRODUCTION;
    }

    @Override
    protected void configureSecurityContext() {
        authenticateAsAdmin();
    }

    protected void authenticateAsAdmin() {
        authenticate("admin@vaadin.com", Role.ADMIN);
    }

    protected void authenticateAsBaker() {
        authenticate("baker@vaadin.com", Role.BAKER);
    }

    protected void authenticateAsBarista() {
        authenticate("barista@vaadin.com", Role.BARISTA);
    }
}