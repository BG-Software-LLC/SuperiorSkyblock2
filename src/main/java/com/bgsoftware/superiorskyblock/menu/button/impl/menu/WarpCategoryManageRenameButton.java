package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpCategoryManage;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class WarpCategoryManageRenameButton extends SuperiorMenuButton<MenuWarpCategoryManage> {

    private WarpCategoryManageRenameButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                           String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpCategoryManage superiorMenu,
                              InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();

        Message.WARP_CATEGORY_RENAME.send(player);

        superiorMenu.closePage();

        PlayerChat.listen(player, message -> {
            WarpCategory warpCategory = superiorMenu.getWarpCategory();

            if (warpCategory.getIsland().getWarpCategory(warpCategory.getName()) != null) {
                if (!message.equalsIgnoreCase("-cancel")) {
                    String newName = IslandUtils.getWarpName(message);

                    if (warpCategory.getIsland().getWarpCategory(newName) != null) {
                        Message.WARP_CATEGORY_RENAME_ALREADY_EXIST.send(player);
                        return true;
                    }

                    if (!IslandUtils.isWarpNameLengthValid(newName)) {
                        Message.WARP_CATEGORY_NAME_TOO_LONG.send(player);
                        return true;
                    }

                    warpCategory.getIsland().renameCategory(warpCategory, newName);

                    Message.WARP_CATEGORY_RENAME_SUCCESS.send(player, newName);

                    if (MenuWarpCategoryManage.successUpdateSound != null)
                        MenuWarpCategoryManage.successUpdateSound.playSound(player);
                }
            }

            PlayerChat.remove(player);

            superiorMenu.open(superiorMenu.getPreviousMenu());

            return true;
        });
    }

    public static class Builder extends AbstractBuilder<Builder, WarpCategoryManageRenameButton, MenuWarpCategoryManage> {

        @Override
        public WarpCategoryManageRenameButton build() {
            return new WarpCategoryManageRenameButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
