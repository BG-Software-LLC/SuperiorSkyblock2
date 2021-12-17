package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpManage;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class WarpManagePrivateButton extends SuperiorMenuButton<MenuWarpManage> {

    private WarpManagePrivateButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                    String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpManage superiorMenu,
                              InventoryClickEvent clickEvent) {
        IslandWarp islandWarp = superiorMenu.getIslandWarp();
        islandWarp.setPrivateFlag(!islandWarp.hasPrivateFlag());
        if (islandWarp.hasPrivateFlag())
            Locale.WARP_PRIVATE_UPDATE.send(clickEvent.getWhoClicked());
        else
            Locale.WARP_PUBLIC_UPDATE.send(clickEvent.getWhoClicked());
    }

    public static class Builder extends AbstractBuilder<Builder, WarpManagePrivateButton, MenuWarpManage> {

        @Override
        public WarpManagePrivateButton build() {
            return new WarpManagePrivateButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
