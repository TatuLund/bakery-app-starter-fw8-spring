package com.vaadin.starter.bakery.ui.views.orderedit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.annotation.PrototypeScope;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.starter.bakery.backend.data.entity.PickupLocation;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.HasAttributes;
import com.vaadin.ui.ComboBox;

@SpringComponent
@PrototypeScope
@SuppressWarnings({ "java:S110", "java:S2160" })
public class PickupLocationComboBox extends ComboBox<PickupLocation>
		implements HasAttributes<PickupLocationComboBox> {

	private final PickupLocationComboBoxDataProvider dataProvider;

	@Autowired
	public PickupLocationComboBox(
			PickupLocationComboBoxDataProvider dataProvider) {
		this.dataProvider = dataProvider;
		setAriaLabel("Pickup location");
		setEmptySelectionAllowed(false);
		setTextInputAllowed(false);
		setPlaceholder("Pickup location");
		setItemCaptionGenerator(PickupLocation::getName);
	}

	@PostConstruct
	private void initDataProvider() {
		setDataProvider(dataProvider);
	}

}
