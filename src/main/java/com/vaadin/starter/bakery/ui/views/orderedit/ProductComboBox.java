package com.vaadin.starter.bakery.ui.views.orderedit;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.annotation.PrototypeScope;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.starter.bakery.backend.data.entity.Product;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.HasAttributes;
import com.vaadin.ui.ComboBox;

@SpringComponent
@PrototypeScope
@SuppressWarnings("java:S110")
public class ProductComboBox extends ComboBox<Product>
		implements HasAttributes<ProductComboBox> {

	@Autowired
	public ProductComboBox(ProductComboBoxDataProvider dataProvider) {
		setAriaLabel("Product");
		setWidth("100%");
		setEmptySelectionAllowed(false);
		setPlaceholder("Product");
		setItemCaptionGenerator(Product::getName);
		setDataProvider(dataProvider);
	}

}
