package com.vaadin.starter.bakery.ui;

import java.util.Map;

import org.springframework.core.env.MapPropertySource;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.vaadin.starter.bakery.app.Application;
import com.vaadin.starter.bakery.backend.data.Role;
import com.vaadin.testbench.uiunittest.SpringSecurityUIUnitTest;
import com.vaadin.ui.UI;

public abstract class AbstractUITest extends SpringSecurityUIUnitTest {

    protected static final String OPEN_IN_VIEW_PROPERTY = "spring.jpa.open-in-view";

    private static final String TEST_PROPERTY_SOURCE = "bakeryUiUnitTestProperties";

    protected Class<?>[] getConfigurationClasses() {
        return new Class<?>[] { Application.class };
    }

    @Override
    protected void configureContext(
            AnnotationConfigWebApplicationContext context) {
        super.configureContext(context);
        context.getEnvironment().getPropertySources().addFirst(
                new MapPropertySource(TEST_PROPERTY_SOURCE,
                        Map.of(OPEN_IN_VIEW_PROPERTY, "false")));
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