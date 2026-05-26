package com.vaadin.starter.bakery.ui.views;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.By;
import com.vaadin.testbench.elements.AbstractComponentElement;

public class MenuElement extends AbstractComponentElement {

	public void logout() {
		WebElement menuLink = getMenuLink("Log out");
		menuLink.click();
	}

	public WebElement getMenuLink(String caption) {
		try {
			// ../.. is because WebDriver refuses to click on a covered element
			return findElement(
					By.xpath("//span[text()='" + caption + "']/../.."));
		} catch (NoSuchElementException e) {
			return null;
		}
	}

}
