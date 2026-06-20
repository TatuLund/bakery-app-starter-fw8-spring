package com.vaadin.starter.bakery.ui.views.admin;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import com.vaadin.navigator.ViewBeforeLeaveEvent;
import com.vaadin.starter.bakery.app.HasLogger;
import com.vaadin.starter.bakery.backend.data.entity.AbstractEntity;
import com.vaadin.starter.bakery.backend.service.CrudService;

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

	public void editRequest(T entity) {
		getView().runWithConfirmation(() -> {
			// Fetch a fresh item so we have the latest changes (less optimistic
			// locking problems)
			T freshEntity = loadEntity(entity.getId());
			editItem(freshEntity);
		}, () -> {
			// Revert selection in grid
			getView().revertSelection(editItem);
		});
	}

	public void editCurrent() {
		editItem(editItem);
	}

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

	public void editNewEntity() {
		T entity = createEntity();
		editItem(entity);
	}

	public boolean isNewEntity() {
		return editItem.isNew();
	}

	public String getEditItemType() {
		return editItem.getClass().getName();
	}

	public T saveEntity() {
		return service.save(editItem);
	}

	public void deleteEntity() {
		deleteEntity(editItem);
	}

	public void resetEditItem() {
		editItem = null;
	}
}
