package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpCategories;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.warps.utils.WarpsUtils;
import org.bukkit.inventory.ItemStack;

public class WarpCategoryPagedObjectButton extends AbstractPagedMenuButton<MenuWarpCategories.View, WarpCategory> {

    private WarpCategoryPagedObjectButton(MenuTemplateButton<MenuWarpCategories.View> templateButton, MenuWarpCategories.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public ItemStack modifyViewItem(ItemBuilder itemBuilder) {
        if (pagedObject == null) {
            return TemplateItem.AIR.build();
        }

        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();

        long availableWarps = WarpsUtils.getAvailableWarps(pagedObject, inventoryViewer).size();
        if (availableWarps == 0) {
            return null;
        }

        ItemStack icon = pagedObject.getIcon(inventoryViewer);
        ItemBuilder newItemBuilder = icon == null ? itemBuilder : new ItemBuilder(icon);

        if (BuiltinModules.WARPS.getConfiguration().isMenusWarpCategoryManageEnabled()
                && menuView.hasManagePerms() && !Menus.MENU_WARP_CATEGORIES.getEditLore().isEmpty()) {
            newItemBuilder.appendLore(Menus.MENU_WARP_CATEGORIES.getEditLore());
        }

        return newItemBuilder.replaceAll("{0}", pagedObject.getName())
                .replaceAll("{1}", availableWarps + "").build(inventoryViewer);
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuWarpCategories.View> context) {
        menuView.setPreviousMove(false);

        if (BuiltinModules.WARPS.getConfiguration().isMenusWarpCategoryManageEnabled()
                && menuView.hasManagePerms() && context.getClickType().isRightClick()) {
            plugin.getMenus().openWarpCategoryManage(menuView.getInventoryViewer(), MenuViewWrapper.fromView(menuView), pagedObject);
        } else {
            plugin.getMenus().openWarps(menuView.getInventoryViewer(), MenuViewWrapper.fromView(menuView), pagedObject);
        }
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuWarpCategories.View, WarpCategory> {

        @Override
        public PagedMenuTemplateButton<MenuWarpCategories.View, WarpCategory> build() {
            return new PagedMenuTemplateButtonImpl<>(this, WarpCategoryPagedObjectButton.class,
                    WarpCategoryPagedObjectButton::new);
        }

    }

}
