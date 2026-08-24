package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpManage;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.island.warp.WarpIcons;
import org.bukkit.inventory.ItemStack;


public class WarpManageIconButton extends AbstractMenuViewButton<MenuWarpManage.View> {

    private WarpManageIconButton(AbstractMenuTemplateButton<MenuWarpManage.View> templateButton, MenuWarpManage.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public ItemStack createViewItem() {
        IslandWarp islandWarp = menuView.getIslandWarp();

        ItemBuilder newItemBuilder = islandWarp.getRawIcon() == null ?
                WarpIcons.DEFAULT_WARP_ICON.getBuilder() : new ItemBuilder(islandWarp.getRawIcon());
        ItemBuilder itemBuilder = getButtonTemplateItem().getBuilder();

        if (itemBuilder != null) {
            if (itemBuilder.hasDisplayName()) {
                newItemBuilder.withName(itemBuilder.getDisplayName());
            }
            if (itemBuilder.hasLore()) {
                newItemBuilder.appendLore(itemBuilder.getLore());
            }
        }

        return newItemBuilder.build(menuView.getInventoryViewer());
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuWarpManage.View> context) {
        IslandWarp islandWarp = menuView.getIslandWarp();
        menuView.setPreviousMove(false);
        plugin.getMenus().openWarpIconEdit(menuView.getInventoryViewer(), MenuViewWrapper.fromView(menuView), islandWarp);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuWarpManage.View> {

        @Override
        public MenuTemplateButton<MenuWarpManage.View> build() {
            return new MenuTemplateButtonImpl<>(this, WarpManageIconButton.class,
                    WarpManageIconButton::new);
        }

    }

}
