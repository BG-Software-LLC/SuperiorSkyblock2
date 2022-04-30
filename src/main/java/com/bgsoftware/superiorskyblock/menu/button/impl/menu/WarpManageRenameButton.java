package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpManage;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.events.EventResult;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class WarpManageRenameButton extends SuperiorMenuButton<MenuWarpManage> {

    private WarpManageRenameButton(TemplateItem buttonItem, SoundWrapper clickSound, List<String> commands,
                                   String requiredPermission, SoundWrapper lackPermissionSound) {
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
