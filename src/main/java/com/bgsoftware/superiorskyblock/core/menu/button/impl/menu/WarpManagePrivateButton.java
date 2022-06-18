package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpManage;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class WarpManagePrivateButton extends SuperiorMenuButton<MenuWarpManage> {

    private WarpManagePrivateButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                    String requiredPermission, GameSound lackPermissionSound) {
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
