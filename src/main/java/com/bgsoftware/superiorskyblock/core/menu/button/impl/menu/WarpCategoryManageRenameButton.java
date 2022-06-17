package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpCategoryManage;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class WarpCategoryManageRenameButton extends SuperiorMenuButton<MenuWarpCategoryManage> {

    private WarpCategoryManageRenameButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                           String requiredPermission, GameSound lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpCategoryManage superiorMenu,
                              InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();

        Message.WARP_CATEGORY_RENAME.send(player);

        superiorMenu.closePage();

        PlayerChat.listen(player, newName -> {
            WarpCategory warpCategory = superiorMenu.getWarpCategory();

            if (warpCategory.getIsland().getWarpCategory(warpCategory.getName()) != null &&
                    !newName.equalsIgnoreCase("-cancel")) {
                if (warpCategory.getIsland().getWarpCategory(newName) != null) {
                    Message.WARP_CATEGORY_RENAME_ALREADY_EXIST.send(player);
                    return true;
                }

                if (!IslandUtils.isWarpNameLengthValid(newName)) {
                    Message.WARP_CATEGORY_NAME_TOO_LONG.send(player);
                    return true;
                }

                EventResult<String> eventResult = plugin.getEventsBus().callIslandRenameWarpCategoryEvent(
                        warpCategory.getIsland(), plugin.getPlayers().getSuperiorPlayer(player), warpCategory, newName);

                if (!eventResult.isCancelled()) {
                    warpCategory.getIsland().renameCategory(warpCategory, eventResult.getResult());

                    Message.WARP_CATEGORY_RENAME_SUCCESS.send(player, eventResult.getResult());

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
