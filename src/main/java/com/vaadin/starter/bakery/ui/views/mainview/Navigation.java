package com.vaadin.starter.bakery.ui.views.mainview;

import org.jspecify.annotations.NullMarked;

import com.vaadin.starter.bakery.ui.components.AttributeExtension.HasAttributes;
import com.vaadin.ui.Button;
import com.vaadin.ui.Composite;
import com.vaadin.ui.CssLayout;

@NullMarked
@SuppressWarnings("java:S2160")
class Navigation extends Composite implements HasAttributes<Navigation> {

    private final CssLayout items = new CssLayout();

    Navigation() {
        setCompositionRoot(items);
        items.setStyleName("navigation");
        items.setId("menu");
        items.setWidth("100%");
    }

    public void addMenuButton(Button button) {
        items.addComponent(button);
        button.addClickListener(e -> {
            clearSelected();
            if (button instanceof MenuButton menuButton) {
                menuButton.setSelected(true);
            }
        });
    }

    void clearSelected() {
        var iter = items.iterator();
        while (iter.hasNext()) {
            var component = iter.next();
            if (component instanceof MenuButton menuButton) {
                menuButton.setSelected(false);
            }
        }
    }
}
