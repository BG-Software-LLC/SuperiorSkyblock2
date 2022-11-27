package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpCategoryManage;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class WarpCategoryManageRenameButton extends AbstractMenuViewButton<MenuWarpCategoryManage.View> {

    private WarpCategoryManageRenameButton(AbstractMenuTemplateButton<MenuWarpCategoryManage.View> templateButton, MenuWarpCategoryManage.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();

        Message.WARP_CATEGORY_RENAME.send(player);

        menuView.closeView();

        PlayerChat.listen(player, newName -> {
            WarpCategory warpCategory = menuView.getWarpCategory();

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

                    GameSoundImpl.playSound(player, Menus.MENU_WARP_CATEGORY_MANAGE.getSuccessUpdateSound());
                }
            }

            PlayerChat.remove(player);

            menuView.refreshView();

            return true;
        });
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuWarpCategoryManage.View> {

        @Override
        public MenuTemplateButton<MenuWarpCategoryManage.View> build() {
            return new MenuTemplateButtonImpl<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, WarpCategoryManageRenameButton.class, WarpCategoryManageRenameButton::new);
        }

    }

}
