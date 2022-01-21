package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuVisitors;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class OpenUniqueVisitorsButton extends SuperiorMenuButton<MenuVisitors> {

    private OpenUniqueVisitorsButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                     String requiredPermission, SoundWrapper lackPermissionSound) {
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
