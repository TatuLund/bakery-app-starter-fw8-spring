package com.vaadin.starter.bakery.ui;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.vaadin.annotations.Push;
import com.vaadin.navigator.View;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ServiceException;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinServletResponse;
import com.vaadin.server.VaadinServletService;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.internal.Conventions;
import com.vaadin.spring.server.SpringUIProvider;
import com.vaadin.spring.server.SpringVaadinServlet;
import com.vaadin.spring.server.SpringVaadinServletService;
import com.vaadin.starter.bakery.app.Application;
import com.vaadin.starter.bakery.backend.data.Role;
import com.vaadin.testbench.uiunittest.AbstractUIUnitTest;
import com.vaadin.testbench.uiunittest.mocks.MockDeploymentConfiguration;
import com.vaadin.testbench.uiunittest.mocks.MockHttpSession;
import com.vaadin.testbench.uiunittest.mocks.MockServletConfig;
import com.vaadin.testbench.uiunittest.mocks.MockServletContext;
import com.vaadin.testbench.uiunittest.mocks.MockServletRequest;
import com.vaadin.testbench.uiunittest.mocks.MockServletResponse;
import com.vaadin.testbench.uiunittest.mocks.MockVaadinSession;
import com.vaadin.ui.UI;

@SuppressWarnings({ "java:S3011", "java:S4274" })
public abstract class AbstractUITest extends AbstractUIUnitTest {

    private static final String UI_CAN_T_BE_NULL = "UI can't be null";
    private static final AtomicInteger MOCK_ID = new AtomicInteger(1);

    private ConfigurableWebApplicationContext applicationContext;
    private VaadinServletRequest vaadinRequest;
    private VaadinServletResponse vaadinResponse;
    private MockHttpSession session;
    private SpringMockServletContext servletContext;
    private MockSpringVaadinService service;

    @Override
    public UI mockVaadin() throws ServiceException {
        configureSecurityContext();

        MockVaadinSession vaadinSession = getVaadinSession();
        SpringUIProvider uiProvider = new SpringUIProvider(vaadinSession);
        vaadinSession.addUIProvider(uiProvider);

        vaadinRequest = getVaadinRequest();
        MockServletResponse response = new MockServletResponse();
        vaadinResponse = new VaadinServletResponse(response, getService());
        getService().setCurrentInstances(vaadinRequest, vaadinResponse);

        Class<? extends UI> uiClass = uiProvider
                .getUIClass(new UIClassSelectionEvent(vaadinRequest));
        if (uiClass == null) {
            uiClass = getUiClass();
        }

        int uiId = MOCK_ID.getAndIncrement();
        UI ui = uiProvider.createInstance(
                new UICreateEvent(vaadinRequest, uiClass, uiId));
        initializeUi(ui, vaadinRequest, uiId);
        return ui;
    }

    @Override
    public void mockVaadin(UI ui) throws ServiceException {
        Objects.requireNonNull(ui, UI_CAN_T_BE_NULL);
        configureSecurityContext();

        vaadinRequest = getVaadinRequest();
        MockServletResponse response = new MockServletResponse();
        vaadinResponse = new VaadinServletResponse(response, getService());
        getService().setCurrentInstances(vaadinRequest, vaadinResponse);

        initializeUi(ui, vaadinRequest, MOCK_ID.getAndIncrement());
    }

    @Override
    public void tearDown() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.detach();
            ui.close();
        }

        VaadinSession vaadinSession = VaadinSession.getCurrent();
        if (vaadinSession != null) {
            vaadinSession.close();
        }

        VaadinService currentService = VaadinService.getCurrent();
        if (currentService != null) {
            currentService.setCurrentInstances(null, null);
        }

        VaadinService.setCurrent(null);
        VaadinSession.setCurrent(null);
        UI.setCurrent(null);
        SecurityContextHolder.clearContext();

        if (applicationContext != null) {
            applicationContext.close();
        }

        applicationContext = null;
        vaadinRequest = null;
        vaadinResponse = null;
        service = null;
        servletContext = null;
        session = null;
    }

    protected <T extends View> T navigate(Class<T> viewClass) {
        SpringView springView = viewClass.getAnnotation(SpringView.class);
        if (springView == null) {
            throw new IllegalArgumentException(
                    "The target class must be a @SpringView");
        }

        String viewName = Conventions.deriveMappingForView(viewClass,
                springView);
        return navigate(viewName, viewClass);
    }

    protected ConfigurableApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            SpringMockServletContext currentServletContext = getServletContext();
            AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
            context.setServletContext(currentServletContext);
            context.register(getConfigurationClasses());
            context.refresh();
            runStartupRunners(context);
            currentServletContext.setAttribute(
                    WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                    context);
            applicationContext = context;
        }
        return applicationContext;
    }

    protected Class<?>[] getConfigurationClasses() {
        return new Class<?>[] { Application.class };
    }

    protected Class<? extends UI> getUiClass() {
        return AppUI.class;
    }

    protected void configureSecurityContext() {
        authenticateAsAdmin();
    }

    protected void authenticate(String username, String... authorities) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        var grantedAuthorities = Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        User principal = new User(username, "n/a", grantedAuthorities);
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, principal.getPassword(),
                grantedAuthorities));
        SecurityContextHolder.setContext(context);
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

    protected MockHttpSession getSession() {
        if (session == null) {
            session = new MockHttpSession(getServletContext());
        }
        return session;
    }

    protected MockVaadinSession getVaadinSession() throws ServiceException {
        if (VaadinSession.getCurrent() == null) {
            MockVaadinSession vaadinSession = new MockVaadinSession(
                    getService(), getSession());
            vaadinSession.lock();
            VaadinSession.setCurrent(vaadinSession);
            return vaadinSession;
        }
        return (MockVaadinSession) VaadinSession.getCurrent();
    }

    protected VaadinServletRequest getVaadinRequest() throws ServiceException {
        MockServletRequest request = new MockServletRequest(getSession());
        request.setAttribute(
                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                getApplicationContext());
        vaadinRequest = new VaadinServletRequest(request, getService());
        return vaadinRequest;
    }

    protected MockSpringVaadinService getService() throws ServiceException {
        if (service == null) {
            service = new MockSpringVaadinService(
                    (ConfigurableWebApplicationContext) getApplicationContext(),
                    getServletContext());
            VaadinService.setCurrent(service);
        }
        return service;
    }

    private SpringMockServletContext getServletContext() {
        if (servletContext == null) {
            servletContext = new SpringMockServletContext();
        }
        return servletContext;
    }

    private void initializeUi(UI ui, VaadinRequest request, int uiId) {
        MockVaadinSession vaadinSession = (MockVaadinSession) VaadinSession
                .getCurrent();
        ui.setSession(vaadinSession);
        setUiToSession(vaadinSession, ui, uiId);

        if (ui.getClass().isAnnotationPresent(Push.class)) {
            Push push = ui.getClass().getAnnotation(Push.class);
            ui.getPushConfiguration().setPushMode(push.value());
            ui.getPushConfiguration().setTransport(push.transport());
        }

        ui.getPage().init(request);
        invokeInit(ui, request);
    }

    private void invokeInit(UI ui, VaadinRequest request) {
        Class<?> clazz = ui.getClass();
        try {
            Method initMethod = clazz.getDeclaredMethod("init",
                    VaadinRequest.class);
            initMethod.setAccessible(true);
            initMethod.invoke(ui, request);
        } catch (NoSuchMethodException | SecurityException
                | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new RuntimeException("Failed to initialize UI", e);
        }
    }

    private void runStartupRunners(
            AnnotationConfigWebApplicationContext context) {
        try {
            var applicationArguments = new DefaultApplicationArguments(
                    new String[0]);

            var applicationRunners = context
                    .getBeansOfType(ApplicationRunner.class)
                    .values()
                    .stream()
                    .collect(Collectors.toList());
            AnnotationAwareOrderComparator.sort(applicationRunners);
            for (ApplicationRunner runner : applicationRunners) {
                runner.run(applicationArguments);
            }

            var commandLineRunners = context
                    .getBeansOfType(CommandLineRunner.class)
                    .values()
                    .stream()
                    .collect(Collectors.toList());
            AnnotationAwareOrderComparator.sort(commandLineRunners);
            for (CommandLineRunner runner : commandLineRunners) {
                runner.run(applicationArguments.getSourceArgs());
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to run Spring Boot startup runners", e);
        }
    }

    private void setUiToSession(MockVaadinSession vaadinSession, UI ui,
            int uiId) {
        UI.setCurrent(ui);
        Class<?> clazz = ui.getClass();
        while (!clazz.equals(UI.class)) {
            clazz = clazz.getSuperclass();
        }

        try {
            Field uiIdField = clazz.getDeclaredField("uiId");
            uiIdField.setAccessible(true);
            uiIdField.set(ui, uiId);
        } catch (NoSuchFieldException | SecurityException
                | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set uiId field", e);
        }

        vaadinSession.addUI(ui);
    }

    private static final class SpringMockServletContext
            extends MockServletContext {

        private final Map<String, Object> attributes = new ConcurrentHashMap<>();

        @Override
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return Collections.enumeration(attributes.keySet());
        }

        @Override
        public void setAttribute(String name, Object object) {
            attributes.put(name, object);
        }

        @Override
        public void removeAttribute(String name) {
            attributes.remove(name);
        }

        @Override
        public String getServerInfo() {
            return "MockServletContext/1.0";
        }
    }

    private static final class SpringMockServletConfig
            extends MockServletConfig {

        private final ServletContext servletContext;

        private SpringMockServletConfig(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        @Override
        public ServletContext getServletContext() {
            return servletContext;
        }
    }

    private static final class MockSpringVaadinService
            extends SpringVaadinServletService {

        private MockSpringVaadinService(
                ConfigurableWebApplicationContext applicationContext,
                SpringMockServletContext servletContext)
                throws ServiceException {
            this(new MockSpringVaadinServlet(),
                    new MockDeploymentConfiguration(), applicationContext,
                    servletContext);
        }

        private MockSpringVaadinService(MockSpringVaadinServlet servlet,
                DeploymentConfiguration deploymentConfiguration,
                ConfigurableWebApplicationContext applicationContext,
                SpringMockServletContext servletContext)
                throws ServiceException {
            super(servlet, deploymentConfiguration, null);

            try {
                servlet.setServletService(this);
                servletContext.setAttribute(
                        WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                        applicationContext);
                servlet.init(new SpringMockServletConfig(servletContext));
                init();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected List<RequestHandler> createRequestHandlers()
                throws ServiceException {
            return Collections.emptyList();
        }

        @Override
        protected boolean isAtmosphereAvailable() {
            return true;
        }
    }

    private static final class MockSpringVaadinServlet
            extends SpringVaadinServlet {

        private SpringVaadinServletService service;

        @Override
        protected VaadinServletService createServletService(
                DeploymentConfiguration deploymentConfiguration)
                throws ServiceException {
            return service;
        }

        private void setServletService(SpringVaadinServletService service) {
            this.service = service;
        }
    }
}