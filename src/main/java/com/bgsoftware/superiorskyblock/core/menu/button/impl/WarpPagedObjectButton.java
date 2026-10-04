package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.MenuActions;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarps;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.inventory.ItemStack;

public class WarpPagedObjectButton extends AbstractPagedMenuButton<MenuWarps.View, IslandWarp> {

    private WarpPagedObjectButton(MenuTemplateButton<MenuWarps.View> templateButton, MenuWarps.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuWarps.View> context) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(context.getPlayer());

        if (menuView.hasManagePerms() && context.getClickType().isRightClick()) {
            menuView.setPreviousMove(false);
            plugin.getMenus().openWarpManage(clickedPlayer, MenuViewWrapper.fromView(menuView), pagedObject);
        } else {
            MenuActions.simulateWarpsClick(clickedPlayer, menuView.getWarpCategory().getIsland(), pagedObject);
            BukkitExecutor.sync(() -> menuView.setPreviousMove(false), 1L);
        }
    }

    @Override
    public ItemStack modifyViewItem(ItemBuilder itemBuilder) {
        SuperiorPlayer superiorPlayer = menuView.getInventoryViewer();

        ItemStack icon = pagedObject.getIcon(superiorPlayer);
        ItemBuilder newItemBuilder = icon == null ? itemBuilder : new ItemBuilder(icon);

        if (menuView.hasManagePerms() && !Menus.MENU_WARPS.getEditLore().isEmpty())
            itemBuilder.appendLore(Menus.MENU_WARPS.getEditLore());

        try (ObjectsPools.Wrapper<LazyWorldLocation> wrapper = ObjectsPools.LAZY_LOCATION.obtain()) {
            return newItemBuilder.replaceAll("{0}", pagedObject.getName())
                    .replaceAll("{1}", Formatters.LOCATION_FORMATTER.format(pagedObject.getLocation(wrapper.getHandle())))
                    .replaceAll("{2}", pagedObject.hasPrivateFlag() ?
                            ensureNotNull(Message.ISLAND_WARP_PRIVATE.getMessage(superiorPlayer.getUserLocale())) :
                            ensureNotNull(Message.ISLAND_WARP_PUBLIC.getMessage(superiorPlayer.getUserLocale())))
                    .build(superiorPlayer);
        }
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuWarps.View, IslandWarp> {

        @Override
        public PagedMenuTemplateButton<MenuWarps.View, IslandWarp> build() {
            return new PagedMenuTemplateButtonImpl<>(this, WarpPagedObjectButton.class,
                    WarpPagedObjectButton::new);
        }

    }

    private static String ensureNotNull(String check) {
        return check == null ? "" : check;
    }

}
