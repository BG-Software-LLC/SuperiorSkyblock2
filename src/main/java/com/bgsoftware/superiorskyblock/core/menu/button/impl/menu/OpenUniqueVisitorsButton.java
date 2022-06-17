package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuVisitors;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class OpenUniqueVisitorsButton extends SuperiorMenuButton<MenuVisitors> {

    private OpenUniqueVisitorsButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                     String requiredPermission, GameSound lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuVisitors superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        Island targetIsland = superiorMenu.getTargetIsland();
        superiorMenu.setPreviousMove(false);
        plugin.getMenus().openUniqueVisitors(clickedPlayer, superiorMenu, targetIsland);
    }

    public static class Builder extends AbstractBuilder<Builder, OpenUniqueVisitorsButton, MenuVisitors> {

        @Override
        public OpenUniqueVisitorsButton build() {
            return new OpenUniqueVisitorsButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
