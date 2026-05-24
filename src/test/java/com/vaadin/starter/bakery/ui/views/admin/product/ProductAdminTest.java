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
import com.vaadin.starter.bakery.ui.views.storefront.StorefrontView;

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
        assertTrue(grid().isAccessibleNavigation());
        assertFalse(form().isEnabled());
        assertFalse(updateButton().isEnabled());
        assertFalse(cancelButton().isEnabled());
        assertEquals("", nameField().getValue());
        assertEquals("", priceField().getValue());
        assertEquals(AbstractCrudView.CAPTION_UPDATE,
                updateButton().getCaption());
        assertEquals(AbstractCrudView.CAPTION_DISCARD,
                cancelButton().getCaption());
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
        assertEquals(AbstractCrudView.CAPTION_CANCEL,
                cancelButton().getCaption());
        assertEquals(viewId() + "/new",
                UI.getCurrent().getNavigator().getState());

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
    public void createProductButCancel_doesNotPersistChanges() {
        String name = uniqueName("cancel-product");

        test(addButton()).click();

        test(nameField()).setValue(name);
        test(priceField()).setValue("$98.76");

        assertTrue(updateButton().isEnabled());
        assertTrue(cancelButton().isEnabled());

        test(cancelButton()).click();

        assertNull(findProduct(name));
        assertFalse(form().isEnabled());
        assertEquals(0, grid().getSelectedItems().size());
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
    public void sortGrid_ordersByNameAndPriceAscendingAndDescending() {
        String prefix = uniqueName("sort-product");
        Product first = createProduct(prefix + "-charlie", 300);
        Product second = createProduct(prefix + "-alpha", 100);
        Product third = createProduct(prefix + "-bravo", 200);

        try {
            test(searchField()).setValue(prefix);
            assertEquals(3, test(grid()).size());

            test(grid()).toggleColumnSorting(0);
            assertProductsSortedByName(true);

            test(grid()).toggleColumnSorting(0);
            assertProductsSortedByName(false);

            view = navigate(ProductAdminView.class);
            test(searchField()).setValue(prefix);
            assertEquals(3, test(grid()).size());

            test(grid()).toggleColumnSorting(1);
            assertProductsSortedByPrice(true);

            test(grid()).toggleColumnSorting(1);
            assertProductsSortedByPrice(false);
        } finally {
            productService.delete(first.getId());
            productService.delete(second.getId());
            productService.delete(third.getId());
        }
    }

    @Test
    public void navigateWithParameter_loadsExistingProductIntoFormAndAllowsSaving() {
        Product product = createProduct(uniqueName("parameter-product"), 4321);
        String updatedName = product.getName() + "-updated";

        try {
            view = navigate(viewId() + "/" + product.getId(),
                    ProductAdminView.class);

            assertEquals(product.getName(), nameField().getValue());
            assertEquals("$43.21", priceField().getValue());
            assertTrue(deleteButton().isEnabled());
            assertEquals(String.valueOf(product.getId()), currentParameter());

            test(nameField()).setValue(updatedName);
            assertTrue(updateButton().isEnabled());
            test(updateButton()).click();

            Product updated = findProduct(updatedName);
            assertNotNull(updated);
            assertEquals(product.getId(), updated.getId());
            assertEquals(updatedName, nameField().getValue());
        } finally {
            productService.delete(product.getId());
        }
    }

    @Test
    public void unsavedChanges_showConfirmationBeforeLeavingOrSwitchingSelection() {
        String prefix = uniqueName("confirm-product");
        Product first = createProduct(prefix + "-one", 100);
        Product second = createProduct(prefix + "-two", 200);

        try {
            test(searchField()).setValue(prefix);
            test(grid()).click(0, 0);

            Long selectedId = currentSelectionId();
            assertNotNull(selectedId);

            String dirtyName = nameField().getValue() + "-dirty";
            test(nameField()).setValue(dirtyName);
            assertTrue(updateButton().isEnabled());

            test(storefrontButton()).click();
            assertNotNull(confirmCancelButton());
            test(confirmCancelButton()).click();
            assertEquals(selectedId, currentSelectionId());
            assertEquals(dirtyName, nameField().getValue());

            test(logoutButton()).click();
            assertNotNull(confirmCancelButton());
            test(confirmCancelButton()).click();
            assertEquals(selectedId, currentSelectionId());
            assertEquals(dirtyName, nameField().getValue());

            test(addButton()).click();
            assertNotNull(confirmCancelButton());
            test(confirmCancelButton()).click();
            assertEquals(selectedId, currentSelectionId());
            assertEquals(dirtyName, nameField().getValue());

            test(grid()).click(0, 1);
            assertNotNull(confirmCancelButton());
            test(confirmCancelButton()).click();
            assertEquals(selectedId, currentSelectionId());
            assertEquals(dirtyName, nameField().getValue());
        } finally {
            if (form().isEnabled() && cancelButton().isEnabled()) {
                test(cancelButton()).click();
            }
            productService.delete(first.getId());
            productService.delete(second.getId());
        }
    }

    @Test
    public void confirmationDialogCanDiscardChangesAndNavigateAway() {
        Product product = createProduct(uniqueName("discard-product"), 555);

        try {
            test(searchField()).setValue(product.getName());
            test(grid()).click(0, 0);
            test(nameField()).setValue(product.getName() + "-dirty");

            test(storefrontButton()).click();
            assertNotNull(discardChangesButton());
            test(discardChangesButton()).click();

            assertTrue(UI.getCurrent().getNavigator()
                    .getCurrentView() instanceof StorefrontView);
            assertEquals(navigationManager.getViewId(StorefrontView.class),
                    UI.getCurrent().getNavigator().getState());
        } finally {
            productService.delete(product.getId());
        }
    }

    @SuppressWarnings("unchecked")
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

    private Button storefrontButton() {
        return $(Button.class).id("storefront");
    }

    private Button logoutButton() {
        return $(Button.class).id("logout");
    }

    private Button confirmCancelButton() {
        return $(Button.class).id("confirmdialog-cancel-button");
    }

    private Button discardChangesButton() {
        return $(Button.class).id("confirmdialog-ok-button");
    }

    private String viewId() {
        return navigationManager.getViewId(ProductAdminView.class);
    }

    private String currentParameter() {
        String state = UI.getCurrent().getNavigator().getState();
        int separator = state.indexOf('/');
        return separator < 0 ? "" : state.substring(separator + 1);
    }

    private Long currentSelectionId() {
        return grid().getSelectedItems().stream().findFirst()
                .map(Product::getId)
                .orElse(null);
    }

    private void assertProductsSortedByName(boolean ascending) {
        for (int row = 1; row < test(grid()).size(); row++) {
            int comparison = test(grid()).item(row - 1).getName()
                    .compareToIgnoreCase(test(grid()).item(row).getName());
            assertTrue(ascending ? comparison <= 0 : comparison >= 0);
        }
    }

    private void assertProductsSortedByPrice(boolean ascending) {
        for (int row = 1; row < test(grid()).size(); row++) {
            int comparison = Integer.compare(
                    test(grid()).item(row - 1).getPrice(),
                    test(grid()).item(row).getPrice());
            assertTrue(ascending ? comparison <= 0 : comparison >= 0);
        }
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