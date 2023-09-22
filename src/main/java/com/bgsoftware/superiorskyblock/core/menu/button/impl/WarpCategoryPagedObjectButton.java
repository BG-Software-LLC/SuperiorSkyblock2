package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
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
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class WarpCategoryPagedObjectButton extends AbstractPagedMenuButton<MenuWarpCategories.View, WarpCategory> {

    private WarpCategoryPagedObjectButton(MenuTemplateButton<MenuWarpCategories.View> templateButton, MenuWarpCategories.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public ItemStack modifyViewItem(ItemStack buttonItem) {
        if (pagedObject == null) {
            return TemplateItem.AIR.build();
        }

        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        Island island = menuView.getIsland();

        boolean isMember = island.isMember(inventoryViewer);
        long accessAmount = pagedObject.getWarps().stream().filter(
                islandWarp -> isMember || !islandWarp.hasPrivateFlag()
        ).count();

        if (accessAmount == 0)
            return null;

        if (!menuView.hasManagePerms() || Menus.MENU_WARP_CATEGORIES.getEditLore().isEmpty()) {
            return pagedObject.getIcon(island.getOwner());
        } else {
            return new ItemBuilder(pagedObject.getIcon(null))
                    .appendLore(Menus.MENU_WARP_CATEGORIES.getEditLore())
                    .build(island.getOwner());
        }
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        menuView.setPreviousMove(false);

        if (menuView.hasManagePerms() && clickEvent.getClick().isRightClick()) {
            plugin.getMenus().openWarpCategoryManage(menuView.getInventoryViewer(), MenuViewWrapper.fromView(menuView), pagedObject);
        } else {
            Menus.MENU_WARPS.openMenu(menuView.getInventoryViewer(), menuView, pagedObject);
        }
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuWarpCategories.View, WarpCategory> {

        @Override
        public PagedMenuTemplateButton<MenuWarpCategories.View, WarpCategory> build() {
            return new PagedMenuTemplateButtonImpl<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getButtonIndex(), WarpCategoryPagedObjectButton.class,
                    WarpCategoryPagedObjectButton::new);
        }

    }

}
