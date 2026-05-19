package com.vaadin.starter.bakery.ui.views.admin.product;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;

import com.vaadin.server.ServiceException;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.starter.bakery.backend.data.entity.Product;
import com.vaadin.starter.bakery.backend.service.ProductService;
import com.vaadin.starter.bakery.ui.AbstractUITest;
import com.vaadin.starter.bakery.ui.navigation.NavigationManager;
import com.vaadin.starter.bakery.ui.views.admin.AbstractCrudView;

public class ProductAdminTest extends AbstractUITest {

    private ProductService productService;
    private NavigationManager navigationManager;
    private ProductAdminView view;

    @Before
    public void setUp() throws ServiceException {
        authenticateAsAdmin();
        mockVaadin();

        productService = getApplicationContext().getBean(ProductService.class);
        navigationManager = getApplicationContext()
                .getBean(NavigationManager.class);
        view = navigate(ProductAdminView.class);
    }

    @After
    public void cleanUp() {
        tearDown();
    }

    @Test
    public void initialState_displaysLoadedGridAndDisabledForm() {
        assertTrue(test(grid()).size() > 0);
        assertFalse(form().isEnabled());
        assertFalse(updateButton().isEnabled());
        assertFalse(cancelButton().isEnabled());
        assertEquals("", nameField().getValue());
        assertEquals("", priceField().getValue());
        assertEquals(AbstractCrudView.CAPTION_UPDATE, updateButton().getCaption());
        assertEquals(AbstractCrudView.CAPTION_DISCARD, cancelButton().getCaption());
        assertEquals(viewId(), UI.getCurrent().getNavigator().getState());
    }

    @Test
    public void createUpdateDeleteProduct_persistsChanges() {
        String name = uniqueName("browserless-product");
        String updatedName = name + "-updated";

        test(addButton()).click();

        assertTrue(form().isEnabled());
        assertFalse(deleteButton().isEnabled());
        assertEquals(AbstractCrudView.CAPTION_ADD, updateButton().getCaption());
        assertEquals(AbstractCrudView.CAPTION_CANCEL, cancelButton().getCaption());
        assertEquals(viewId() + "/new", UI.getCurrent().getNavigator().getState());

        test(nameField()).setValue(name);
        test(priceField()).setValue("$12.34");
        assertTrue(updateButton().isEnabled());
        test(updateButton()).click();

        Product created = findProduct(name);
        assertNotNull(created);
        assertTrue(deleteButton().isEnabled());
        assertEquals(String.valueOf(created.getId()), currentParameter());

        test(nameField()).setValue(updatedName);
        assertTrue(updateButton().isEnabled());
        test(updateButton()).click();

        Product updated = findProduct(updatedName);
        assertNotNull(updated);
        assertEquals(created.getId(), updated.getId());
        assertEquals(updatedName, nameField().getValue());
        assertEquals(String.valueOf(updated.getId()), currentParameter());

        test(deleteButton()).click();

        assertNull(findProduct(updatedName));
        assertFalse(form().isEnabled());
        assertEquals(viewId(), UI.getCurrent().getNavigator().getState());
    }

    @Test
    public void filterGrid_showsOnlyMatchingProducts() {
        Product product = createProduct(uniqueName("filter-product"), 1234);

        try {
            test(searchField()).setValue(product.getName());

            assertEquals(1, test(grid()).size());
            assertEquals(product.getName(), test(grid()).cell(0, 0));

            test(searchField()).setValue("missing-product");
            assertEquals(0, test(grid()).size());
        } finally {
            productService.delete(product.getId());
        }
    }

    @Test
    public void navigateWithParameter_loadsExistingProductIntoForm() {
        Product product = createProduct(uniqueName("parameter-product"), 4321);

        try {
            view = navigate(viewId() + "/" + product.getId(), ProductAdminView.class);

            assertEquals(product.getName(), nameField().getValue());
            assertEquals("$43.21", priceField().getValue());
            assertTrue(deleteButton().isEnabled());
            assertEquals(String.valueOf(product.getId()), currentParameter());
        } finally {
            productService.delete(product.getId());
        }
    }

    private Grid<Product> grid() {
        return $(view.getViewComponent(), Grid.class).id("list");
    }

    private VerticalLayout form() {
        return $(view.getViewComponent(), VerticalLayout.class).id("form");
    }

    private TextField searchField() {
        return $(view.getViewComponent(), TextField.class).id("search");
    }

    private TextField nameField() {
        return $(view.getViewComponent(), TextField.class).id("name");
    }

    private TextField priceField() {
        return $(view.getViewComponent(), TextField.class).id("price");
    }

    private Button addButton() {
        return $(view.getViewComponent(), Button.class).id("add");
    }

    private Button updateButton() {
        return $(view.getViewComponent(), Button.class).id("update");
    }

    private Button cancelButton() {
        return $(view.getViewComponent(), Button.class).id("cancel");
    }

    private Button deleteButton() {
        return $(view.getViewComponent(), Button.class).id("delete");
    }

    private String viewId() {
        return navigationManager.getViewId(ProductAdminView.class);
    }

    private String currentParameter() {
        String state = UI.getCurrent().getNavigator().getState();
        int separator = state.indexOf('/');
        return separator < 0 ? "" : state.substring(separator + 1);
    }

    private Product createProduct(String name, int price) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        return productService.save(product);
    }

    private Product findProduct(String exactName) {
        List<Product> matches = productService
                .findAnyMatching(Optional.of(exactName), PageRequest.of(0, 20))
                .getContent();
        return matches.stream()
                .filter(product -> exactName.equals(product.getName()))
                .findFirst()
                .orElse(null);
    }

    private String uniqueName(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}