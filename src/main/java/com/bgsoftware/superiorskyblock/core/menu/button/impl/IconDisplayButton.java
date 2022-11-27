package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractIconProviderMenu;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class IconDisplayButton<E> extends AbstractMenuViewButton<AbstractIconProviderMenu.View<E>> {

    private IconDisplayButton(AbstractMenuTemplateButton<AbstractIconProviderMenu.View<E>> templateButton,
                              AbstractIconProviderMenu.View<E> menuView) {
        super(templateButton, menuView);
    }

    @Override
    public ItemStack createViewItem() {
        TemplateItem iconTemplate = menuView.getIconTemplate();
        return iconTemplate == null ? null : iconTemplate.build();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        // Dummy button
    }

    public static class Builder<E> extends AbstractMenuTemplateButton.AbstractBuilder<AbstractIconProviderMenu.View<E>> {

        @Override
        public MenuTemplateButton<AbstractIconProviderMenu.View<E>> build() {
            return new MenuTemplateButtonImpl<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, IconDisplayButton.class, IconDisplayButton::new);
        }

    }

}
