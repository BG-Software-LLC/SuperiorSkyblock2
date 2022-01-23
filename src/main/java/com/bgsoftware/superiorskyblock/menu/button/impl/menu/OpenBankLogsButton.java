package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuIslandBank;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class OpenBankLogsButton extends SuperiorMenuButton<MenuIslandBank> {

    private OpenBankLogsButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                               String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuIslandBank superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        superiorMenu.setPreviousMove(false);
        plugin.getMenus().openBankLogs(clickedPlayer, superiorMenu, superiorMenu.getTargetIsland());
    }

    public static class Builder extends AbstractBuilder<Builder, OpenBankLogsButton, MenuIslandBank> {

        @Override
        public OpenBankLogsButton build() {
            return new OpenBankLogsButton(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        }

    }

}
