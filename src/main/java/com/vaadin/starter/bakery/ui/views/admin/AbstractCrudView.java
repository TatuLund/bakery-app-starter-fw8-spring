package com.vaadin.starter.bakery.ui.views.admin;

import java.util.List;

import javax.annotation.PostConstruct;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.annotation.Secured;
import org.vaadin.artur.spring.dataprovider.FilterablePageableDataProvider;

import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.BindingValidationStatus;
import com.vaadin.data.HasValue;
import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.StatusChangeEvent;
import com.vaadin.data.ValidationException;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewBeforeLeaveEvent;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.starter.bakery.app.HasLogger;
import com.vaadin.starter.bakery.backend.data.Role;
import com.vaadin.starter.bakery.backend.data.entity.AbstractEntity;
import com.vaadin.starter.bakery.backend.service.UserFriendlyDataException;
import com.vaadin.starter.bakery.ui.components.ConfirmPopup;
import com.vaadin.starter.bakery.ui.navigation.NavigationManager;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Focusable;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.components.grid.SingleSelectionModel;

/**
 * Base class for a CRUD (Create, read, update, delete) view.
 * <p>
 * The view has three states it can be in and the user can navigate between the
 * states with the controls present:
 * <ol>
 * <li>Initial state
 * <ul>
 * <li>Form is disabled
 * <li>Nothing is selected in grid
 * </ul>
 * <li>Adding an entity
 * <ul>
 * <li>Form is enabled
 * <li>"Delete" has no function
 * <li>"Discard" moves to the "Initial state"
 * <li>"Save" creates the entity and moves to the "Updating an entity" state
 * </ul>
 * <li>Updating an entity
 * <ul>
 * <li>Entity highlighted in Grid
 * <li>Form is enabled
 * <li>"Delete" deletes the entity from the database
 * <li>"Discard" resets the form contents to what is in the database
 * <li>"Save" updates the entity and keeps the form open
 * <li>"Save" and "Discard" are only enabled when changes have been made
 * </ol>
 *
 * @param <T>
 *            the type of entity which can be edited in the view
 */
@NullMarked
@Secured(Role.ADMIN)
public abstract class AbstractCrudView<T extends AbstractEntity>
		implements View, HasLogger {

	public static final String CAPTION_DISCARD = "Discard";
	public static final String CAPTION_CANCEL = "Cancel";
	public static final String CAPTION_UPDATE = "Update";
	public static final String CAPTION_ADD = "Add";

	@Nullable
	private BeanValidationBinder<T> binder;

	private final Class<T> entityType;

	private FilterablePageableDataProvider<T, Object> dataProvider;

	private final transient BeanFactory beanFactory;

	private NavigationManager navigationManager;
	private boolean hasValidationErrors;

	protected AbstractCrudView(NavigationManager navigationManager,
			Class<T> entityType,
			FilterablePageableDataProvider<T, Object> dataProvider,
			BeanFactory beanFactory) {
		this.navigationManager = navigationManager;
		this.entityType = entityType;
		this.dataProvider = dataProvider;
		this.beanFactory = beanFactory;
	}

	@PostConstruct
	private void initLogic() {
		createBinder();

		setDataProvider(dataProvider);
		bindFormFields(getBinder());
		showInitialState();

		getGrid().addSelectionListener(e -> {
			if (!e.isUserOriginated()) {
				return;
			}

			if (e.getFirstSelectedItem().isPresent()) {
				getPresenter().editRequest(e.getFirstSelectedItem().get());
			} else {
				throw new IllegalStateException(
						"This should never happen as deselection is not allowed");
			}
		});

		// Force user to choose save or cancel in form once enabled
		((SingleSelectionModel<T>) getGrid().getSelectionModel())
				.setDeselectAllowed(false);

		// Button logic
		getUpdate().addClickListener(event -> updateClicked());
		getCancel().addClickListener(event -> cancelClicked());
		getDelete().addClickListener(event -> deleteClicked());
		getAdd().addClickListener(event -> addNewClicked());

		// Search functionality
		getSearch().addValueChangeListener(
				event -> filterGrid(event.getValue()));

	}

	protected void filterGrid(String filter) {
		dataProvider.setFilter(filter);
	}

	protected void createBinder() {
		binder = new BeanValidationBinder<>(entityType);
		binder.addStatusChangeListener(this::onFormStatusChange);
		binder.addValueChangeListener(this::onFormValueChange);
		binder.setChangeDetectionEnabled(true);
	}

	protected BeanValidationBinder<T> getBinder() {
		return binder;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		if (!event.getParameters().isEmpty()) {
			getPresenter().editRequest(event.getParameters());
		}
	}

	@Override
	public void beforeLeave(ViewBeforeLeaveEvent event) {
		runWithConfirmation(event::navigate, () -> {
			// Nothing special needs to be done if user aborts the navigation
		});
	}

	public void showInitialState() {
		getBinder().readBean(null);
		getForm().setEnabled(false);
		getGrid().deselectAll();
		getUpdate().setCaption(CAPTION_UPDATE);
		getCancel().setCaption(CAPTION_DISCARD);
		getCancel().setDisableOnClick(true);
		getCancel().setEnabled(false);
	}

	public void editItem(T editItem, boolean isNew) {
		getBinder().readBean(editItem);

		if (isNew) {
			getGrid().deselectAll();
			getUpdate().setCaption(CAPTION_ADD);
			getCancel().setCaption(CAPTION_CANCEL);
			getFirstFormField().focus();
		} else {
			getUpdate().setCaption(CAPTION_UPDATE);
			getCancel().setCaption(CAPTION_DISCARD);
		}

		getForm().setEnabled(true);
		getDelete().setEnabled(!isNew);

	}

	public void deleteClicked() {
		try {
			getPresenter().deleteEntity();
		} catch (UserFriendlyDataException e) {
			Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
			getLogger().debug("Unable to delete entity of type "
					+ getPresenter().getEditItemType(), e);
			return;
		} catch (DataIntegrityViolationException e) {
			Notification.show(
					"The given entity cannot be deleted as there are references to it in the database",
					Type.ERROR_MESSAGE);
			getLogger().error("Unable to delete entity of type "
					+ getPresenter().getEditItemType(), e);
			return;
		}
		dataProvider.refreshAll();
		revertToInitialState();
	}

	public void onFormStatusChange(StatusChangeEvent event) {
		boolean hasChanges = event.getBinder().hasChanges();
		hasValidationErrors = event.hasValidationErrors();
		setUpdateEnabled(hasChanges && !hasValidationErrors);
	}

	public void onFormValueChange(ValueChangeEvent<?> event) {
		boolean hasChanges = getBinder().hasChanges();
		setCancelEnabled(hasChanges);
		setUpdateEnabled(hasChanges && !hasValidationErrors);
	}

	public void revertSelection(@Nullable T editItem) {
		Grid<T> grid = getGrid();
		if (editItem == null) {
			grid.deselectAll();
		} else {
			grid.select(editItem);
		}
	}

	/**
	 * Runs the given command if the form contains no unsaved changes or if the
	 * user clicks ok in the confirmation dialog telling about unsaved changes.
	 *
	 * @param onConfirmation
	 *            the command to run if there are not changes or user pushes
	 *            {@literal confirm}
	 * @param onCancel
	 *            the command to run if there are changes and the user pushes
	 *            {@literal cancel}
	 * @return <code>true</code> if the {@literal confirm} command was run
	 *         immediately, <code>false</code> otherwise
	 */
	public void runWithConfirmation(Runnable onConfirmation,
			Runnable onCancel) {
		if (hasUnsavedChanges()) {
			ConfirmPopup confirmPopup = beanFactory.getBean(ConfirmPopup.class);
			confirmPopup.showLeaveViewConfirmDialog(this, onConfirmation,
					onCancel);
		} else {
			onConfirmation.run();
		}
	}

	private boolean hasUnsavedChanges() {
		return getBinder().hasChanges();
	}

	public void updateClicked() {
		try {
			// The validate() call is needed only to ensure that the error
			// indicator is properly shown for the field in case of an error
			getBinder().validate();
			getBinder().writeBean(getPresenter().getEntity());
		} catch (ValidationException e) {
			// Commit failed because of validation errors
			List<BindingValidationStatus<?>> fieldErrors = e
					.getFieldValidationErrors();
			if (!fieldErrors.isEmpty()) {
				// Field level error
				HasValue<?> firstErrorField = fieldErrors.get(0).getField();
				focusField(firstErrorField);
			} else {
				// Bean validation error
				ValidationResult firstError = e.getBeanValidationErrors()
						.get(0);
				Notification.show(firstError.getErrorMessage(),
						Type.ERROR_MESSAGE);
			}
			return;
		}

		boolean isNew = getPresenter().isNewEntity();
		T entity;
		try {
			entity = getPresenter().saveEntity();
		} catch (OptimisticLockingFailureException e) {
			// Somebody else probably edited the data at the same time
			Notification.show(
					"Somebody else might have updated the data. Please refresh and try again.",
					Type.ERROR_MESSAGE);
			getLogger().debug(
					"Optimistic locking error while saving entity of type "
							+ entityType.getName(),
					e);
			return;
		} catch (UserFriendlyDataException e) {
			Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
			getLogger().debug("Unable to update entity of type "
					+ entityType.getName(), e);
			return;
		} catch (Exception e) {
			// Something went wrong, no idea what
			Notification.show(
					"A problem occured while saving the data. Please check the fields.",
					Type.ERROR_MESSAGE);
			getLogger().error("Unable to save entity of type "
					+ entityType.getName(), e);
			return;
		}

		if (isNew) {
			// Move to the "Updating an entity" state
			dataProvider.refreshAll();
			getPresenter().editRequest(entity);
			revertSelection(entity);
		} else {
			// Stay in the "Updating an entity" state
			dataProvider.refreshItem(entity);
			getPresenter().editRequest(entity);
		}
	}

	public void setDataProvider(DataProvider<T, Object> dataProvider) {
		getGrid().setDataProvider(dataProvider);
	}

	public void setUpdateEnabled(boolean enabled) {
		getUpdate().setEnabled(enabled);
	}

	public void setCancelEnabled(boolean enabled) {
		getCancel().setEnabled(enabled);
	}

	public void focusField(HasValue<?> field) {
		if (field instanceof Focusable focusable) {
			focusable.focus();
		} else {
			getLogger().warn("Unable to focus field of type {}",
					field.getClass().getName());
		}
	}

	public boolean selectEntity(T entity) {
		try {
			getGrid().select(entity);
			return true;
		} catch (Exception e) {
			Notification.show("Unknown entity id", Type.ERROR_MESSAGE);
			return false;
		}
	}

	public void cancelClicked() {
		if (getPresenter().isNewEntity()) {
			revertToInitialState();
		} else {
			getPresenter().editCurrent();
		}
	}

	private void revertToInitialState() {
		getPresenter().resetEditItem();
		showInitialState();
		navigationManager.updateViewParameter("");
	}

	public void addNewClicked() {
		runWithConfirmation(() -> getPresenter().editNewEntity(), () -> {
		});
	}

	public void updateViewParameter(String parameter) {
		navigationManager.updateViewParameter(parameter);
	}

	@SuppressWarnings("java:S1452")
	protected abstract AbstractCrudPresenter<T, ?, ? extends AbstractCrudView<T>> getPresenter();

	protected abstract Grid<T> getGrid();

	protected abstract void setGrid(Grid<T> grid);

	protected abstract Component getForm();

	protected abstract Button getAdd();

	protected abstract Button getCancel();

	protected abstract Button getDelete();

	protected abstract Button getUpdate();

	protected abstract TextField getSearch();

	protected abstract Focusable getFirstFormField();

	public abstract void bindFormFields(BeanValidationBinder<T> binder);
}
