package com.vaadin.starter.bakery.ui.views.admin;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;

import com.vaadin.navigator.ViewBeforeLeaveEvent;
import com.vaadin.starter.bakery.app.HasLogger;
import com.vaadin.starter.bakery.backend.data.entity.AbstractEntity;
import com.vaadin.starter.bakery.backend.service.CrudService;
import com.vaadin.starter.bakery.backend.service.UserFriendlyDataException;

@NullMarked
public abstract class AbstractCrudPresenter<T extends AbstractEntity, S extends CrudService<T>, V extends AbstractCrudView<T>>
		implements HasLogger, Serializable {

	@Nullable
	private V view;

	private final transient S service;

	// The model for the view. Not extracted to a class to reduce clutter. If
	// the model becomes more complex, it could be encapsulated in a separate
	// class.
	@Nullable
	private T editItem;

	private final Class<T> entityType;

	protected AbstractCrudPresenter(S service, Class<T> entityType) {
		this.service = service;
		this.entityType = entityType;
	}

	public void beforeLeavingView(ViewBeforeLeaveEvent event) {
	}

	protected S getService() {
		return service;
	}

	protected T loadEntity(long id) {
		return service.load(id);
	}

	protected T getEntity() {
		return editItem;
	}

	private T createEntity() {
		try {
			return entityType.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new UnsupportedOperationException(
					"Entity of type " + entityType.getName()
							+ " is missing a public no-args constructor",
					e);
		}
	}

	protected void deleteEntity(T entity) {
		if (entity.isNew()) {
			throw new IllegalArgumentException(
					"Cannot delete an entity which is not in the database");
		} else {
			service.delete(entity.getId());
		}
	}

	/**
	 * Initializes the presenter. This method must be called before using the
	 * presenter. The presenter is designed to be used with a single view, so it
	 * is not possible to change the view after initialization. The presenter
	 * will throw an exception if any method is called before initialization.
	 *
	 * @param view
	 *            the view to initialize the presenter with, not null
	 */
	public void init(V view) {
		Objects.requireNonNull(view, "View must not be null");
		this.view = view;
	}

	protected V getView() {
		assert view != null : "View is not initialized yet";
		return view;
	}

	public void editRequest(String parameters) {
		long id;
		try {
			id = Long.parseLong(parameters);
		} catch (NumberFormatException e) {
			id = -1;
		}

		if (id == -1) {
			editItem(createEntity());
		} else {
			selectAndEditEntity(loadEntity(id));
		}
	}

	private void selectAndEditEntity(T entity) {
		if (getView().selectEntity(entity)) {
			editRequest(entity);
		}
	}

	/**
	 * Switches the view to the "Editing an existing entity" state. The view
	 * will show the given entity for editing. The view will also update the URL
	 * to include the ID of the entity being edited, so that the view can be
	 * bookmarked in the "Editing an existing entity" state. The view will show
	 * an error notification if the given entity is null, which should not be
	 * possible in normal usage.
	 * 
	 * @param entity
	 *            the entity to edit, not null
	 */
	public void editRequest(T entity) {
		getView().runWithConfirmation(() -> {
			// Fetch a fresh item so we have the latest changes (less optimistic
			// locking problems)
			T freshEntity = loadEntity(entity.getId());
			editItem(freshEntity);
		}, () ->
		// Revert selection in grid
		getView().revertSelection(editItem));
	}

	/**
	 * Switches the view to the "Editing an existing entity" state. The view
	 * will show the given entity for editing. The view will also update the URL
	 * to include the ID of the entity being edited, so that the view can be
	 * bookmarked in the "Editing an existing entity" state. The view will show
	 * an error notification if the given entity is null, which should not be
	 * possible in normal usage.
	 */
	public void editCurrent() {
		editItem(editItem);
	}

	@SuppressWarnings("java:S2583")
	protected void editItem(T item) {
		if (item == null) {
			throw new IllegalArgumentException(
					"The entity to edit cannot be null");
		}
		this.editItem = item;

		boolean isNew = item.isNew();
		if (isNew) {
			getView().updateViewParameter("new");
		} else {
			getView().updateViewParameter(String.valueOf(item.getId()));
		}

		getView().editItem(editItem, isNew);
	}

	/**
	 * Switches the view to the "Editing a new entity" state. The entity is not
	 * saved to the database until the user clicks the "Save" button.
	 */
	public void editNewEntity() {
		T entity = createEntity();
		editItem(entity);
	}

	/**
	 * @return true if the view is currently in the "Editing a new entity"
	 *         state, false otherwise
	 */
	public boolean isNewEntity() {
		return editItem.isNew();
	}

	private String getEditItemType() {
		return editItem.getClass().getName();
	}

	/**
	 * Saves the entity being edited. If the entity is new, it will be added to
	 * the database. If the entity already exists, it will be updated in the
	 * database. After a successful save, the view will switch to the "Editing
	 * an existing entity" state. If the save fails, the view will remain in the
	 * same state and show an error notification. The view will also show an
	 * error notification if the entity being edited is null, which should not
	 * be possible in normal usage.
	 */
	public void saveEntity() {
		boolean isNew = isNewEntity();
		T entity;
		try {
			entity = service.save(editItem);
		} catch (OptimisticLockingFailureException e) {
			// Somebody else probably edited the data at the same time
			getView().viewConcurrentEditError();
			getLogger().debug(
					"Optimistic locking error while saving entity of type {} {}",
					entityType.getName(), e);
			return;
		} catch (UserFriendlyDataException e) {
			getView().viewErrorNotification(e.getMessage());
			getLogger().debug("Unable to update entity of type {} {}",
					entityType.getName(), e);
			return;
		} catch (Exception e) {
			// Something went wrong, no idea what
			getView().showGenericSaveError();
			getLogger().error("Unable to save entity of type {} {}",
					entityType.getName(), e);
			return;
		}

		if (isNew) {
			// Move to the "Updating an entity" state
			getView().refreshGrid();
			editRequest(entity);
			getView().revertSelection(entity);
		} else {
			// Stay in the "Updating an entity" state
			getView().refreshItem(entity);
			editRequest(entity);
		}
	}

	/**
	 * Deletes the entity being edited. After a successful delete, the view will
	 * switch to the "Editing an existing entity" state with no entity selected.
	 * If the delete fails, the view will remain in the same state and show an
	 * error notification. The view will also show an error notification if the
	 * entity being edited is null, which should not be possible in normal
	 * usage.
	 */
	public void deleteEntity() {
		try {
			deleteEntity(editItem);
		} catch (UserFriendlyDataException e) {
			getView().viewErrorNotification(e.getMessage());
			getLogger().debug("Unable to delete entity of type "
					+ getEditItemType(), e);
		} catch (DataIntegrityViolationException e) {
			getView().viewErrorNotification(
					"The given entity cannot be deleted as there are references to it in the database");
			getLogger().error("Unable to delete entity of type "
					+ getEditItemType(), e);
		}
	}

	/**
	 * Resets the entity being edited. After calling this method, there will be
	 * no entity selected for editing.
	 */
	public void resetEditItem() {
		editItem = null;
	}
}
