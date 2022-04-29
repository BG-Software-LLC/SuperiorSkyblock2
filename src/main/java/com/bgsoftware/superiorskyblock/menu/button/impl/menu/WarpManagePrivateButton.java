package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpManage;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class WarpManagePrivateButton extends SuperiorMenuButton<MenuWarpManage> {

    private WarpManagePrivateButton(TemplateItem buttonItem, SoundWrapper clickSound, List<String> commands,
                                    String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpManage superiorMenu,
                              InventoryClickEvent clickEvent) {
        IslandWarp islandWarp = superiorMenu.getIslandWarp();

        boolean openToPublic = islandWarp.hasPrivateFlag();

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

        if (openToPublic ? !plugin.getEventsBus().callIslandOpenWarpEvent(islandWarp.getIsland(), superiorPlayer, islandWarp) :
                !plugin.getEventsBus().callIslandCloseWarpEvent(islandWarp.getIsland(), superiorPlayer, islandWarp))
            return;

        islandWarp.setPrivateFlag(!openToPublic);

        if (openToPublic)
            Message.WARP_PUBLIC_UPDATE.send(superiorPlayer);
        else
            Message.WARP_PRIVATE_UPDATE.send(superiorPlayer);
    }

    public static class Builder extends AbstractBuilder<Builder, WarpManagePrivateButton, MenuWarpManage> {

        @Override
        public WarpManagePrivateButton build() {
            return new WarpManagePrivateButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
