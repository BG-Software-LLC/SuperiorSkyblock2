package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpCategoryIconEdit;
import com.bgsoftware.superiorskyblock.utils.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class WarpCategoryIconEditTypeButton extends SuperiorMenuButton<MenuWarpCategoryIconEdit> {

    private WarpCategoryIconEditTypeButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                           String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpCategoryIconEdit superiorMenu,
                              InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();

        Locale.WARP_CATEGORY_ICON_NEW_TYPE.send(player);

        superiorMenu.closePage();

        PlayerChat.listen(player, message -> {
            if (!message.equalsIgnoreCase("-cancel")) {
                String[] sections = message.split(":");
                Material material;

                try {
                    material = Material.valueOf(sections[0].toUpperCase());
                    if (material == Material.AIR)
                        throw new IllegalArgumentException();
                } catch (IllegalArgumentException ex) {
                    Locale.INVALID_MATERIAL.send(player, message);
                    return true;
                }

                String rawMessage = sections.length == 2 ? sections[1] : "0";

                short data;

                try {
                    data = Short.parseShort(rawMessage);
                    if (data < 0)
                        throw new IllegalArgumentException();
                } catch (IllegalArgumentException ex) {
                    Locale.INVALID_MATERIAL_DATA.send(player, rawMessage);
                    return true;
                }

                superiorMenu.getItemBuilder()
                        .withType(material).withDurablity(data);
            }

            PlayerChat.remove(player);

            superiorMenu.open(superiorMenu.getPreviousMenu());

            return true;
        });
    }

    public static class Builder extends AbstractBuilder<Builder, WarpCategoryIconEditTypeButton, MenuWarpCategoryIconEdit> {

        @Override
        public WarpCategoryIconEditTypeButton build() {
            return new WarpCategoryIconEditTypeButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
