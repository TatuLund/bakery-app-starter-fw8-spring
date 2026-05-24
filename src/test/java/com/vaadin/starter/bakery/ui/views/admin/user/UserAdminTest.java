package com.vaadin.starter.bakery.ui.views.admin.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vaadin.server.ServiceException;
import com.vaadin.starter.bakery.backend.data.Role;
import com.vaadin.starter.bakery.backend.data.entity.User;
import com.vaadin.starter.bakery.backend.service.UserService;
import com.vaadin.starter.bakery.ui.AbstractUITest;
import com.vaadin.starter.bakery.ui.navigation.NavigationManager;
import com.vaadin.starter.bakery.ui.views.admin.AbstractCrudView;
import com.vaadin.starter.bakery.ui.views.admin.RoleSelect;
import com.vaadin.starter.bakery.ui.views.storefront.StorefrontView;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class UserAdminTest extends AbstractUITest {

    private static final String MODIFY_LOCKED_USER_NOT_PERMITTED = "User has been locked and cannot be modified or deleted";
    private static final String BAKER_EMAIL = "baker@vaadin.com";
    private static final String ADMIN_EMAIL = "admin@vaadin.com";

    private UserService userService;
    private NavigationManager navigationManager;
    private PasswordEncoder passwordEncoder;
    private UserAdminView view;

    @Before
    public void setUp() throws ServiceException {
        authenticateAsAdmin();
        mockVaadin();

        userService = getApplicationContext().getBean(UserService.class);
        navigationManager = getApplicationContext()
                .getBean(NavigationManager.class);
        passwordEncoder = getApplicationContext()
                .getBean(PasswordEncoder.class);
        view = navigate(UserAdminView.class);
    }

    @After
    public void cleanUp() {
        resetPassword(BAKER_EMAIL, "baker");
        tearDown();
    }

    @Test
    public void initialState_displaysLoadedGridAndDisabledForm() {
        assertTrue(test(grid()).size() > 0);
        assertTrue(grid().isAccessibleNavigation());
        assertEquals(0, grid().getSelectedItems().size());
        assertFalse(form().isEnabled());
        assertFalse(updateButton().isEnabled());
        assertFalse(cancelButton().isEnabled());
        assertEquals("", emailField().getValue());
        assertEquals("", nameField().getValue());
        assertEquals("", passwordField().getValue());
        assertNull(roleField().getValue());
        assertEquals(AbstractCrudView.CAPTION_UPDATE,
                updateButton().getCaption());
        assertEquals(AbstractCrudView.CAPTION_DISCARD,
                cancelButton().getCaption());
        assertEquals(viewId(), UI.getCurrent().getNavigator().getState());
    }

    @Test
    public void createUpdateDeleteUser_persistsChanges() {
        String email = uniqueEmail("browserless-user");
        String name = "John Doe";
        String updatedName = name + " updated";

        try {
            test(addButton()).click();

            assertTrue(form().isEnabled());
            assertFalse(deleteButton().isEnabled());
            assertEquals(AbstractCrudView.CAPTION_ADD,
                    updateButton().getCaption());
            assertEquals(AbstractCrudView.CAPTION_CANCEL,
                    cancelButton().getCaption());
            assertEquals(viewId() + "/new",
                    UI.getCurrent().getNavigator().getState());

            test(emailField()).setValue(email);
            test(nameField()).setValue(name);
            test(passwordField()).setValue("secret1");
            roleField().setValue(Role.ADMIN);

            assertTrue(updateButton().isEnabled());
            test(updateButton()).click();

            User created = findUser(email);
            assertNotNull(created);
            assertEquals(1, grid().getSelectedItems().size());
            assertEquals(String.valueOf(created.getId()), currentParameter());
            assertTrue(deleteButton().isEnabled());
            assertEquals(email, emailField().getValue());
            assertEquals(name, nameField().getValue());
            assertEquals(Role.ADMIN, roleField().getValue());
            assertEquals("", passwordField().getValue());

            test(nameField()).setValue(updatedName);
            assertTrue(updateButton().isEnabled());
            test(updateButton()).click();

            User updated = findUser(email);
            assertNotNull(updated);
            assertEquals(created.getId(), updated.getId());
            assertEquals(updatedName, updated.getName());
            assertEquals(updatedName, nameField().getValue());
            assertEquals(String.valueOf(updated.getId()), currentParameter());

            test(deleteButton()).click();

            assertNull(findUser(email));
            assertEquals(0, grid().getSelectedItems().size());
            assertFalse(form().isEnabled());
            assertEquals(viewId(), UI.getCurrent().getNavigator().getState());
        } finally {
            deleteUserIfExists(email);
        }
    }

    @Test
    public void createUserButCancel_doesNotPersistChanges() {
        String email = uniqueEmail("cancel-user");

        test(addButton()).click();

        test(emailField()).setValue(email);
        test(nameField()).setValue("Cancel User");
        test(passwordField()).setValue("secret1");
        roleField().setValue(Role.BAKER);

        assertTrue(updateButton().isEnabled());
        assertTrue(cancelButton().isEnabled());

        test(cancelButton()).click();

        assertNull(findUser(email));
        assertFalse(form().isEnabled());
        assertEquals(0, grid().getSelectedItems().size());
        assertEquals(viewId(), UI.getCurrent().getNavigator().getState());
    }

    @Test
    public void filterGrid_showsOnlyMatchingUsers() {
        String email = uniqueEmail("filter-user");
        User user = createUser(email, "Filter User", "secret1", Role.BARISTA);

        try {
            test(searchField()).setValue(email);

            assertEquals(1, test(grid()).size());
            assertEquals(user.getEmail(), test(grid()).cell(0, 0));

            test(searchField()).setValue("missing-user");
            assertEquals(0, test(grid()).size());
        } finally {
            userService.delete(user.getId());
        }
    }

    @Test
    public void sortGrid_ordersByEmailAndNameAscendingAndDescending() {
        String prefix = "sort-user-" + UUID.randomUUID();
        User first = createUser(prefix + "-charlie@example.com", "Charlie",
                "secret1",
                Role.BAKER);
        User second = createUser(prefix + "-alpha@example.com", "Alpha",
                "secret1",
                Role.BAKER);
        User third = createUser(prefix + "-bravo@example.com", "Bravo",
                "secret1",
                Role.BAKER);

        try {
            test(searchField()).setValue(prefix);
            assertEquals(3, test(grid()).size());

            test(grid()).toggleColumnSorting(0);
            assertUsersSortedByEmail(true);

            test(grid()).toggleColumnSorting(0);
            assertUsersSortedByEmail(false);

            view = navigate(UserAdminView.class);
            test(searchField()).setValue(prefix);
            assertEquals(3, test(grid()).size());

            test(grid()).toggleColumnSorting(1);
            assertUsersSortedByName(true);

            test(grid()).toggleColumnSorting(1);
            assertUsersSortedByName(false);
        } finally {
            userService.delete(first.getId());
            userService.delete(second.getId());
            userService.delete(third.getId());
        }
    }

    @Test
    public void navigateWithParameter_loadsExistingUserIntoFormAndAllowsSaving() {
        String email = uniqueEmail("parameter-user");
        User user = createUser(email, "Parameter User", "secret1", Role.BAKER);
        String updatedName = "Parameter User Updated";

        try {
            view = navigate(viewId() + "/" + user.getId(), UserAdminView.class);

            assertEquals(email, emailField().getValue());
            assertEquals("Parameter User", nameField().getValue());
            assertEquals("", passwordField().getValue());
            assertEquals(Role.BAKER, roleField().getValue());
            assertTrue(deleteButton().isEnabled());
            assertEquals(String.valueOf(user.getId()), currentParameter());

            test(nameField()).setValue(updatedName);
            assertTrue(updateButton().isEnabled());
            test(updateButton()).click();

            User updated = findExistingUser(email);
            assertEquals(user.getId(), updated.getId());
            assertEquals(updatedName, updated.getName());
            assertEquals(updatedName, nameField().getValue());
        } finally {
            userService.delete(user.getId());
        }
    }

    @Test
    public void updatePassword_rejectsShortValueAndPersistsValidValue() {
        User baker = findExistingUser(BAKER_EMAIL);
        view = navigate(viewId() + "/" + baker.getId(), UserAdminView.class);

        assertEquals("", passwordField().getValue());

        test(passwordField()).setValue("foo");

        assertFalse(updateButton().isEnabled());
        assertNotNull(passwordField().getComponentError());
        assertTrue(passwordEncoder.matches("baker",
                findExistingUser(BAKER_EMAIL).getPasswordHash()));

        test(passwordField()).setValue("foobar");
        assertTrue(updateButton().isEnabled());
        test(updateButton()).click();

        assertFalse(updateButton().isEnabled());
        assertTrue(passwordEncoder.matches("foobar",
                findExistingUser(BAKER_EMAIL).getPasswordHash()));

        view = navigate(viewId() + "/" + baker.getId(), UserAdminView.class);
        assertEquals("", passwordField().getValue());

        test(passwordField()).setValue("baker");
        test(updateButton()).click();

        assertTrue(passwordEncoder.matches("baker",
                findExistingUser(BAKER_EMAIL).getPasswordHash()));
    }

    @Test
    public void passwordRequiredForNewUser_preventsSaveWithoutPassword() {
        String email = uniqueEmail("missing-password");

        test(addButton()).click();

        assertTrue(passwordField().isRequiredIndicatorVisible());
        test(emailField()).setValue(email);
        test(nameField()).setValue("Missing Password User");
        roleField().setValue(Role.ADMIN);

        assertTrue(updateButton().isEnabled());
        test(updateButton()).click();

        assertNotNull(passwordField().getComponentError());
        assertNull(findUser(email));
    }

    @Test
    public void tryToUpdateLockedEntity_showsNotificationAndLeavesBackendUnchanged() {
        User admin = findExistingUser(ADMIN_EMAIL);
        String originalName = admin.getName();

        view = navigate(viewId() + "/" + admin.getId(), UserAdminView.class);

        test(nameField()).setValue(originalName + "-updated");
        assertTrue(updateButton().isEnabled());
        test(updateButton()).click();

        assertEquals(MODIFY_LOCKED_USER_NOT_PERMITTED,
                lastNotification().getCaption());
        assertEquals(originalName, findExistingUser(ADMIN_EMAIL).getName());
    }

    @Test
    public void tryToDeleteLockedEntity_showsNotificationAndKeepsUser() {
        User admin = findExistingUser(ADMIN_EMAIL);
        view = navigate(viewId() + "/" + admin.getId(), UserAdminView.class);

        assertTrue(deleteButton().isEnabled());
        test(deleteButton()).click();

        assertEquals(MODIFY_LOCKED_USER_NOT_PERMITTED,
                lastNotification().getCaption());
        assertNotNull(findUser(ADMIN_EMAIL));
    }

    @Test
    public void unsavedChanges_showConfirmationBeforeLeavingOrSwitchingSelection() {
        String prefix = "confirm-user-" + UUID.randomUUID();
        User first = createUser(prefix + "-one@example.com", "Confirm One",
                "secret1",
                Role.ADMIN);
        User second = createUser(prefix + "-two@example.com", "Confirm Two",
                "secret1",
                Role.BAKER);

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
            userService.delete(first.getId());
            userService.delete(second.getId());
        }
    }

    @Test
    public void confirmationDialogCanDiscardChangesAndNavigateAway() {
        String email = uniqueEmail("discard-user");
        User user = createUser(email, "Discard User", "secret1", Role.BARISTA);

        try {
            test(searchField()).setValue(email);
            test(grid()).click(0, 0);
            test(nameField()).setValue("Discard User Dirty");

            test(storefrontButton()).click();
            assertNotNull(discardChangesButton());
            test(discardChangesButton()).click();

            assertTrue(UI.getCurrent().getNavigator()
                    .getCurrentView() instanceof StorefrontView);
            assertEquals(navigationManager.getViewId(StorefrontView.class),
                    UI.getCurrent().getNavigator().getState());
        } finally {
            userService.delete(user.getId());
        }
    }

    @SuppressWarnings("unchecked")
    private Grid<User> grid() {
        return $(view.getViewComponent(), Grid.class).id("list");
    }

    private VerticalLayout form() {
        return $(view.getViewComponent(), VerticalLayout.class).id("form");
    }

    private TextField searchField() {
        return $(view.getViewComponent(), TextField.class).id("search");
    }

    private TextField emailField() {
        return $(view.getViewComponent(), TextField.class).id("email");
    }

    private TextField nameField() {
        return $(view.getViewComponent(), TextField.class).id("name");
    }

    private TextField passwordField() {
        return $(view.getViewComponent(), TextField.class).id("password");
    }

    private RoleSelect roleField() {
        return $(view.getViewComponent(), RoleSelect.class).id("role");
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

    private Notification lastNotification() {
        return $(Notification.class).last();
    }

    private String viewId() {
        return navigationManager.getViewId(UserAdminView.class);
    }

    private String currentParameter() {
        String state = UI.getCurrent().getNavigator().getState();
        int separator = state.indexOf('/');
        return separator < 0 ? "" : state.substring(separator + 1);
    }

    private Long currentSelectionId() {
        return grid().getSelectedItems().stream().findFirst()
                .map(User::getId)
                .orElse(null);
    }

    private void assertUsersSortedByEmail(boolean ascending) {
        for (int row = 1; row < test(grid()).size(); row++) {
            int comparison = test(grid()).item(row - 1).getEmail()
                    .compareToIgnoreCase(test(grid()).item(row).getEmail());
            assertTrue(ascending ? comparison <= 0 : comparison >= 0);
        }
    }

    private void assertUsersSortedByName(boolean ascending) {
        for (int row = 1; row < test(grid()).size(); row++) {
            int comparison = test(grid()).item(row - 1).getName()
                    .compareToIgnoreCase(test(grid()).item(row).getName());
            assertTrue(ascending ? comparison <= 0 : comparison >= 0);
        }
    }

    private User createUser(String email, String name, String rawPassword,
            String role) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userService.save(user);
    }

    private User findUser(String email) {
        return userService
                .findAnyMatching(Optional.of(email), PageRequest.of(0, 20))
                .getContent().stream()
                .filter(user -> email.equals(user.getEmail()))
                .findFirst()
                .orElse(null);
    }

    private User findExistingUser(String email) {
        User user = findUser(email);
        assertNotNull(user);
        return user;
    }

    private void deleteUserIfExists(String email) {
        User user = findUser(email);
        if (user != null) {
            userService.delete(user.getId());
        }
    }

    private void resetPassword(String email, String rawPassword) {
        User user = findUser(email);
        if (user == null || user.isLocked()) {
            return;
        }
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userService.save(user);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@example.com";
    }
}