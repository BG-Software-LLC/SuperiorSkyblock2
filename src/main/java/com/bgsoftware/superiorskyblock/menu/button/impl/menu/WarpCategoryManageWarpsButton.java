package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpCategoryManage;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class WarpCategoryManageWarpsButton extends SuperiorMenuButton<MenuWarpCategoryManage> {

    private WarpCategoryManageWarpsButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                          String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpCategoryManage superiorMenu,
                              InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        superiorMenu.setPreviousMove(false);
        plugin.getMenus().openWarps(clickedPlayer, superiorMenu, superiorMenu.getWarpCategory());
    }

    public static class Builder extends AbstractBuilder<Builder, WarpCategoryManageWarpsButton, MenuWarpCategoryManage> {

        @Override
        public WarpCategoryManageWarpsButton build() {
            return new WarpCategoryManageWarpsButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
