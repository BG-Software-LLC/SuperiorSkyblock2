package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarps;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class WarpPagedObjectButton extends AbstractPagedMenuButton<MenuWarps.View, IslandWarp> {

    private WarpPagedObjectButton(MenuTemplateButton<MenuWarps.View> templateButton, MenuWarps.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

        if (menuView.hasManagePerms() && clickEvent.getClick().isRightClick()) {
            menuView.setPreviousMove(false);
            plugin.getMenus().openWarpManage(clickedPlayer, MenuViewWrapper.fromView(menuView), pagedObject);
        } else {
            Menus.MENU_WARPS.simulateClick(clickedPlayer, menuView.getWarpCategory().getIsland(), pagedObject);
            BukkitExecutor.sync(() -> menuView.setPreviousMove(false), 1L);
        }
    }

    @Override
    public ItemStack modifyViewItem(ItemStack buttonItem) {
        SuperiorPlayer superiorPlayer = menuView.getInventoryViewer();

        ItemStack icon = pagedObject.getIcon(superiorPlayer);
        ItemBuilder itemBuilder = new ItemBuilder(icon == null ? buttonItem : icon);

        if (menuView.hasManagePerms() && !Menus.MENU_WARPS.getEditLore().isEmpty())
            itemBuilder.appendLore(Menus.MENU_WARPS.getEditLore());

        return itemBuilder.replaceAll("{0}", pagedObject.getName())
                .replaceAll("{1}", Formatters.LOCATION_FORMATTER.format(pagedObject.getLocation()))
                .replaceAll("{2}", pagedObject.hasPrivateFlag() ?
                        ensureNotNull(Message.ISLAND_WARP_PRIVATE.getMessage(superiorPlayer.getUserLocale())) :
                        ensureNotNull(Message.ISLAND_WARP_PUBLIC.getMessage(superiorPlayer.getUserLocale())))
                .build(superiorPlayer);
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuWarps.View, IslandWarp> {

        @Override
        public PagedMenuTemplateButton<MenuWarps.View, IslandWarp> build() {
            return new PagedMenuTemplateButtonImpl<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getButtonIndex(), WarpPagedObjectButton.class,
                    WarpPagedObjectButton::new);
        }

    }

    private static String ensureNotNull(String check) {
        return check == null ? "" : check;
    }

}
