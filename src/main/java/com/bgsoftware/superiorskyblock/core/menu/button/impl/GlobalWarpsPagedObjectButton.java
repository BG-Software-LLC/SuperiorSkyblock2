package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuGlobalWarps;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GlobalWarpsPagedObjectButton extends AbstractPagedMenuButton<MenuGlobalWarps.View, Island> {

    private GlobalWarpsPagedObjectButton(MenuTemplateButton<MenuGlobalWarps.View> templateButton, MenuGlobalWarps.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        if (Menus.MENU_GLOBAL_WARPS.isVisitorWarps()) {
            menuView.setPreviousMove(false);
            plugin.getCommands().dispatchSubCommand(menuView.getInventoryViewer().asPlayer(),
                    "visit", pagedObject.getOwner().getName());
        } else {
            Menus.MENU_WARP_CATEGORIES.openMenu(menuView.getInventoryViewer(), menuView, pagedObject);
        }
    }

    @Override
    public ItemStack modifyViewItem(ItemStack buttonItem) {
        return new ItemBuilder(buttonItem)
                .asSkullOf(pagedObject.getOwner())
                .replaceAll("{0}", pagedObject.getOwner().getName())
                .replaceLoreWithLines("{1}", pagedObject.getDescription().split("\n"))
                .replaceAll("{2}", pagedObject.getIslandWarps().size() + "")
                .build(pagedObject.getOwner());
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuGlobalWarps.View, Island> {

        @Override
        public PagedMenuTemplateButton<MenuGlobalWarps.View, Island> build() {
            return new PagedMenuTemplateButtonImpl<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getButtonIndex(), GlobalWarpsPagedObjectButton.class,
                    GlobalWarpsPagedObjectButton::new);
        }

    }

}
