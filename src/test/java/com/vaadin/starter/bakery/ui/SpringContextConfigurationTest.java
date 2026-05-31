package com.vaadin.starter.bakery.ui;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.ServiceException;

public class SpringContextConfigurationTest extends AbstractUITest {

    @Before
    public void setUp() throws ServiceException {
        mockVaadin();
    }

    @After
    public void cleanUp() {
        tearDown();
    }

    @Test
    public void browserlessSpringContextDisablesOpenSessionInView() {
        assertEquals("false", getApplicationContext().getEnvironment()
                .getProperty(OPEN_IN_VIEW_PROPERTY));
    }
}