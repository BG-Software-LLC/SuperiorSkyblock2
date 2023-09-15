package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.BaseMenuView;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BorderColorToggleButton extends AbstractMenuViewButton<BaseMenuView> {

    private BorderColorToggleButton(AbstractMenuTemplateButton<BaseMenuView> templateButton, BaseMenuView menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public ItemStack createViewItem() {
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        TemplateItem buttonItem = inventoryViewer.hasWorldBorderEnabled() ? getTemplate().enabledItem : getTemplate().disabledItem;
        return buttonItem.build(inventoryViewer);
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        plugin.getCommands().dispatchSubCommand(clickEvent.getWhoClicked(), "toggle", "border");
        BukkitExecutor.sync(menuView::closeView, 1L);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<BaseMenuView> {

        private TemplateItem disabledItem;

        public Builder setEnabledItem(TemplateItem enabledItem) {
            this.buttonItem = enabledItem;
            return this;
        }

        public Builder setDisabledItem(TemplateItem disabledItem) {
            this.disabledItem = disabledItem;
            return this;
        }

        @Override
        public MenuTemplateButton<BaseMenuView> build() {
            return new Template(clickSound, commands, requiredPermission, lackPermissionSound, buttonItem, disabledItem);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<BaseMenuView> {

        private final TemplateItem enabledItem;
        private final TemplateItem disabledItem;

        Template(@Nullable GameSound clickSound, @Nullable List<String> commands, @Nullable String requiredPermission,
                 @Nullable GameSound lackPermissionSound, @Nullable TemplateItem enabledItem,
                 @Nullable TemplateItem disabledItem) {
            super(null, clickSound, commands, requiredPermission, lackPermissionSound,
                    BorderColorToggleButton.class, BorderColorToggleButton::new);
            this.enabledItem = enabledItem == null ? TemplateItem.AIR : enabledItem;
            this.disabledItem = disabledItem == null ? TemplateItem.AIR : disabledItem;
        }

    }

}
