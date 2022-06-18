package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpCategoryManage;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class WarpCategoryManageWarpsButton extends SuperiorMenuButton<MenuWarpCategoryManage> {

    private WarpCategoryManageWarpsButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                          String requiredPermission, GameSound lackPermissionSound) {
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
