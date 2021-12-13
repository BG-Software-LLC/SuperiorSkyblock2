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

public final class WarpCategoryIconEditLoreButton extends SuperiorMenuButton<MenuWarpCategoryIconEdit> {

    private WarpCategoryIconEditLoreButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                           String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpCategoryIconEdit superiorMenu,
                              InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();

        Locale.WARP_CATEGORY_ICON_NEW_LORE.send(player);

        PlayerChat.listen(player, message -> {
            if (!message.equalsIgnoreCase("-cancel")) {
                superiorMenu.getItemBuilder().withLore(message.split("\\\\n"));
            }

            PlayerChat.remove(player);

            superiorMenu.open(superiorMenu.getPreviousMenu());

            return true;
        });
    }

    public static class Builder extends AbstractBuilder<Builder, WarpCategoryIconEditLoreButton, MenuWarpCategoryIconEdit> {

        @Override
        public WarpCategoryIconEditLoreButton build() {
            return new WarpCategoryIconEditLoreButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
