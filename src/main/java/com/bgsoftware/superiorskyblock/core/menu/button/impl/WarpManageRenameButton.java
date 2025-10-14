package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpManage;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandNames;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class WarpManageRenameButton extends AbstractMenuViewButton<MenuWarpManage.View> {

    private WarpManageRenameButton(AbstractMenuTemplateButton<MenuWarpManage.View> templateButton, MenuWarpManage.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        Message.WARP_RENAME.send(player);

        menuView.closeView();

        PlayerChat.listen(player, newName -> {
            IslandWarp islandWarp = menuView.getIslandWarp();
            Island island = islandWarp.getIsland();

            if (!newName.equalsIgnoreCase("-cancel")) {
                if (!IslandNames.isValidWarpName(superiorPlayer, island, newName, true))
                    return true;

                PluginEvent<PluginEventArgs.IslandRenameWarp> event = PluginEventsFactory.callIslandRenameWarpEvent(
                        islandWarp.getIsland(), player, islandWarp, newName);

                if (!event.isCancelled()) {
                    islandWarp.getIsland().renameWarp(islandWarp, event.getArgs().warpName);

                    Message.WARP_RENAME_SUCCESS.send(player, event.getArgs().warpName);

                    GameSoundImpl.playSound(player, Menus.MENU_WARP_MANAGE.getSuccessUpdateSound());
                }
            }

            PlayerChat.remove(player);

            menuView.refreshView();

            return true;
        });
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuWarpManage.View> {

        @Override
        public MenuTemplateButton<MenuWarpManage.View> build() {
            return new MenuTemplateButtonImpl<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, WarpManageRenameButton.class, WarpManageRenameButton::new);
        }

    }

}
