package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpCategoryIconEdit;
import com.bgsoftware.superiorskyblock.utils.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class WarpCategoryIconRenameButton extends SuperiorMenuButton<MenuWarpCategoryIconEdit> {

    private WarpCategoryIconRenameButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                         String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpCategoryIconEdit superiorMenu,
                              InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();

        Locale.WARP_CATEGORY_ICON_NEW_NAME.send(player);

        superiorMenu.closePage();

        PlayerChat.listen(player, message -> {
            if (!message.equalsIgnoreCase("-cancel")) {
                superiorMenu.getItemBuilder().withName(message);
            }

            PlayerChat.remove(player);

            superiorMenu.open(superiorMenu.getPreviousMenu());

            return true;
        });
    }

    public static class Builder extends AbstractBuilder<Builder, WarpCategoryIconRenameButton, MenuWarpCategoryIconEdit> {

        @Override
        public WarpCategoryIconRenameButton build() {
            return new WarpCategoryIconRenameButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
