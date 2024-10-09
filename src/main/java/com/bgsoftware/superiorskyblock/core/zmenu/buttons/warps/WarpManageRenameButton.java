package com.bgsoftware.superiorskyblock.core.zmenu.buttons.warps;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.SuperiorButton;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;

public class WarpManageRenameButton extends SuperiorButton {
    public WarpManageRenameButton(Plugin plugin) {
        super((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        super.onClick(player, event, inventory, slot, placeholders);

        IslandWarp islandWarp = getCache(player).getIslandWarp();
        Message.WARP_RENAME.send(player);
        player.closeInventory();

        PlayerChat.listen(player, newName -> {

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

                    GameSoundImpl.playSound(player, Menus.MENU_WARP_MANAGE.getSuccessUpdateSound());
                }
            }

            PlayerChat.remove(player);

            return true;
        });
    }
}
