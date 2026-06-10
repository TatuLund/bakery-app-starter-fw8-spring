package com.vaadin.starter.bakery.ui.views.mainview;

import org.jspecify.annotations.NullMarked;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.AriaRoles;
import com.vaadin.starter.bakery.ui.components.AttributeExtension.HasAttributes;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

@NullMarked
@SuppressWarnings("java:S2160")
class MenuButton extends Button implements HasAttributes<MenuButton> {

    private final String caption;

    MenuButton(String caption, VaadinIcons icon) {
        this.caption = caption;
        setCaption(caption);
        setIcon(icon);
        setStyleName(ValoTheme.BUTTON_BORDERLESS);
        setWidth("100%");
        setHeight("80px");
        setRole(AriaRoles.LINK);
    }

    public void setSelected(boolean selected) {
        if (selected) {
            addStyleName(ValoTheme.MENU_SELECTED);
            setAriaLabel(String.format("%s %s", caption,
                    "current page"));
        } else {
            removeStyleName(ValoTheme.MENU_SELECTED);
            setAriaLabel(caption);
        }
    }
}
