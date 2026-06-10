package com.vaadin.starter.bakery.ui.views.admin.user;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.starter.bakery.backend.data.entity.User;
import com.vaadin.starter.bakery.backend.service.UserService;
import com.vaadin.starter.bakery.ui.navigation.NavigationManager;
import com.vaadin.starter.bakery.ui.views.admin.AbstractCrudPresenter;

@NullMarked
@SpringComponent
@ViewScope
public class UserAdminPresenter
		extends AbstractCrudPresenter<User, UserService, UserAdminView> {

	@Autowired
	public UserAdminPresenter(UserAdminDataProvider userAdminDataProvider,
			NavigationManager navigationManager,
			UserService service, BeanFactory beanFactory) {
		super(navigationManager, service, User.class, userAdminDataProvider,
				beanFactory);
	}

	public String encodePassword(String value) {
		return getService().encodePassword(value);
	}

	@Override
	protected void editItem(User item) {
		super.editItem(item);
		getView().setPasswordRequired(item.isNew());
	}
}