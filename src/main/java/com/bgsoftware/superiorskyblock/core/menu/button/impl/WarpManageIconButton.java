package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpManage;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.island.warp.WarpIcons;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WarpManageIconButton extends AbstractMenuViewButton<MenuWarpManage.View> {

    private WarpManageIconButton(AbstractMenuTemplateButton<MenuWarpManage.View> templateButton, MenuWarpManage.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public ItemStack createViewItem() {
        IslandWarp islandWarp = menuView.getIslandWarp();

        ItemBuilder itemBuilder = islandWarp.getRawIcon() == null ?
                WarpIcons.DEFAULT_WARP_ICON.getBuilder() : new ItemBuilder(islandWarp.getRawIcon());

        ItemStack buttonItem = super.createViewItem();

        if (buttonItem != null && buttonItem.hasItemMeta()) {
            ItemMeta itemMeta = buttonItem.getItemMeta();
            if (itemMeta.hasDisplayName())
                itemBuilder.withName(itemMeta.getDisplayName());

            if (itemMeta.hasLore())
                itemBuilder.appendLore(itemMeta.getLore());
        }

        return itemBuilder.build(menuView.getInventoryViewer());
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        IslandWarp islandWarp = menuView.getIslandWarp();
        menuView.setPreviousMove(false);
        plugin.getMenus().openWarpIconEdit(menuView.getInventoryViewer(), MenuViewWrapper.fromView(menuView), islandWarp);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuWarpManage.View> {

        @Override
        public MenuTemplateButton<MenuWarpManage.View> build() {
            return new MenuTemplateButtonImpl<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, WarpManageIconButton.class, WarpManageIconButton::new);
        }

    }

}
