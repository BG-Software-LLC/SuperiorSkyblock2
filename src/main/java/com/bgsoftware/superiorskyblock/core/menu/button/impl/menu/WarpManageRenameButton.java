package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpManage;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class WarpManageRenameButton extends SuperiorMenuButton<MenuWarpManage> {

    private WarpManageRenameButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                   String requiredPermission, GameSound lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpManage superiorMenu,
                              InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();

        Message.WARP_RENAME.send(player);

        superiorMenu.closePage();

        PlayerChat.listen(player, newName -> {
            IslandWarp islandWarp = superiorMenu.getIslandWarp();

            if (!newName.equalsIgnoreCase("-cancel")) {
                if (islandWarp.getIsland().getWarp(newName) != null) {
                    Message.WARP_RENAME_ALREADY_EXIST.send(player);
                    return true;
                }

                if (!IslandUtils.isWarpNameLengthValid(newName)) {
                    Message.WARP_NAME_TOO_LONG.send(player);
                    return true;
                }

                EventResult<String> eventResult = plugin.getEventsBus().callIslandRenameWarpEvent(
                        islandWarp.getIsland(), plugin.getPlayers().getSuperiorPlayer(player), islandWarp, newName);

                if (!eventResult.isCancelled()) {
                    islandWarp.getIsland().renameWarp(islandWarp, eventResult.getResult());

                    Message.WARP_RENAME_SUCCESS.send(player, eventResult.getResult());

                    if (MenuWarpManage.successUpdateSound != null)
                        MenuWarpManage.successUpdateSound.playSound(player);
                }
            }

            PlayerChat.remove(player);

            superiorMenu.open(superiorMenu.getPreviousMenu());

            return true;
        });
    }

    public static class Builder extends AbstractBuilder<Builder, WarpManageRenameButton, MenuWarpManage> {

        @Override
        public WarpManageRenameButton build() {
            return new WarpManageRenameButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
