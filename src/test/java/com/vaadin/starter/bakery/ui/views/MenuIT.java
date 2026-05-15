package com.vaadin.starter.bakery.ui.views;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.starter.bakery.AbstractIT;

public class MenuIT extends AbstractIT {

	@Test
	public void adminSeesAdminMenus() {
		loginAsAdmin();
		MenuElement menu = $(MenuElement.class).first();
		Assert.assertNotNull(menu.getMenuLink("Users"));
		Assert.assertNotNull(menu.getMenuLink("Products"));
		Assert.assertNotNull(menu.getMenuLink("Log out"));
	}

	@Test
	public void baristaDoesNotSeeAdminMenus() {
		loginAsBarista();
		MenuElement menu = $(MenuElement.class).first();
		Assert.assertNull(menu.getMenuLink("Users"));
		Assert.assertNull(menu.getMenuLink("Products"));
		Assert.assertNotNull(menu.getMenuLink("Log out"));
	}
}
