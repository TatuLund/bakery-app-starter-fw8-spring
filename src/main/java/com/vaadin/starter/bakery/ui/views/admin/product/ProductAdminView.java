package com.vaadin.starter.bakery.ui.views.admin.product;

import javax.annotation.PostConstruct;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.ValueContext;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.starter.bakery.backend.data.entity.Product;
import com.vaadin.starter.bakery.ui.navigation.NavigationManager;
import com.vaadin.starter.bakery.ui.utils.DollarPriceConverter;
import com.vaadin.starter.bakery.ui.views.admin.AbstractCrudView;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Focusable;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;

@NullMarked
@SpringView
public class ProductAdminView extends AbstractCrudView<Product> {

	private final ProductAdminPresenter presenter;

	private final ProductAdminLayout userAdminLayout;

	private final DollarPriceConverter priceToStringConverter;

	private static final String PRICE_PROPERTY = "price";

	@Autowired
	public ProductAdminView(ProductAdminPresenter presenter,
			ProductAdminDataProvider productAdminDataProvider,
			DollarPriceConverter priceToStringConverter,
			NavigationManager navigationManager, BeanFactory beanFactory) {
		super(navigationManager, Product.class,
				productAdminDataProvider, beanFactory);
		this.presenter = presenter;
		this.priceToStringConverter = priceToStringConverter;
		userAdminLayout = new ProductAdminLayout();
	}

	@PostConstruct
	private void init() {
		presenter.init(this);
		// Show two columns: "name" and "price".
		getGrid().setColumns("name", PRICE_PROPERTY);
		// The price column is configured automatically based on the bean. As
		// we want a custom converter, we remove the column and configure it
		// manually.
		getGrid().removeColumn(PRICE_PROPERTY);
		getGrid()
				.addColumn(product -> priceToStringConverter
						.convertToPresentation(product.getPrice(),
								new ValueContext(getGrid())))
				.setSortProperty(PRICE_PROPERTY).setCaption("Price");
	}

	@Override
	public void bindFormFields(BeanValidationBinder<Product> binder) {
		binder.forField(getViewComponent().price)
				.withConverter(priceToStringConverter).bind(PRICE_PROPERTY);
		binder.bindInstanceFields(getViewComponent());
	}

	@Override
	public ProductAdminLayout getViewComponent() {
		return userAdminLayout;
	}

	@Override
	protected ProductAdminPresenter getPresenter() {
		return presenter;
	}

	@Override
	protected Grid<Product> getGrid() {
		return getViewComponent().list;
	}

	@Override
	protected void setGrid(Grid<Product> grid) {
		getViewComponent().list = grid;
	}

	@Override
	protected Component getForm() {
		return getViewComponent().getForm();
	}

	@Override
	protected Button getAdd() {
		return getViewComponent().getAdd();
	}

	@Override
	protected Button getCancel() {
		return getViewComponent().getCancel();
	}

	@Override
	protected Button getDelete() {
		return getViewComponent().getDelete();
	}

	@Override
	protected Button getUpdate() {
		return getViewComponent().getUpdate();
	}

	@Override
	protected TextField getSearch() {
		return getViewComponent().getSearch();
	}

	@Override
	protected Focusable getFirstFormField() {
		return getViewComponent().name;
	}
}