package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpManage;
import com.bgsoftware.superiorskyblock.utils.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class WarpManageRenameButton extends SuperiorMenuButton<MenuWarpManage> {

    private WarpManageRenameButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                   String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpManage superiorMenu,
                              InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();

        Locale.WARP_RENAME.send(player);


        superiorMenu.closePage();

        PlayerChat.listen(player, message -> {
            IslandWarp islandWarp = superiorMenu.getIslandWarp();

            if (!message.equalsIgnoreCase("-cancel")) {
                String newName = IslandUtils.getWarpName(message);

                if (islandWarp.getIsland().getWarp(newName) != null) {
                    Locale.WARP_RENAME_ALREADY_EXIST.send(player);
                    return true;
                }

                if (!IslandUtils.isWarpNameLengthValid(newName)) {
                    Locale.WARP_NAME_TOO_LONG.send(player);
                    return true;
                }

                islandWarp.getIsland().renameWarp(islandWarp, newName);

                Locale.WARP_RENAME_SUCCESS.send(player, newName);

                if (MenuWarpManage.successUpdateSound != null)
                    MenuWarpManage.successUpdateSound.playSound(player);
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
