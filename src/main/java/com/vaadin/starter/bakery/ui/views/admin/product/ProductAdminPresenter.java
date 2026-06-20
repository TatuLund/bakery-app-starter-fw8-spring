package com.vaadin.starter.bakery.ui.views.admin.product;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.starter.bakery.backend.data.entity.Product;
import com.vaadin.starter.bakery.backend.service.ProductService;
import com.vaadin.starter.bakery.ui.views.admin.AbstractCrudPresenter;

@NullMarked
@SpringComponent
@ViewScope
public class ProductAdminPresenter extends
		AbstractCrudPresenter<Product, ProductService, ProductAdminView> {

	@Autowired
	public ProductAdminPresenter(ProductService service) {
		super(service, Product.class);
	}
}
