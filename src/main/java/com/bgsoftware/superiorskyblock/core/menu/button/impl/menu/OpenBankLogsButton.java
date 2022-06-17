package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandBank;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class OpenBankLogsButton extends SuperiorMenuButton<MenuIslandBank> {

    private OpenBankLogsButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                               String requiredPermission, GameSound lackPermissionSound) {
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
