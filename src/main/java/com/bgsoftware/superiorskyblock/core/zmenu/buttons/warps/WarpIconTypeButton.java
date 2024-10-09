package com.bgsoftware.superiorskyblock.core.zmenu.buttons.warps;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.SuperiorButton;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;

import java.util.Locale;

public class WarpIconTypeButton extends SuperiorButton {
    public WarpIconTypeButton(Plugin plugin) {
        super((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        super.onClick(player, event, inventory, slot, placeholders);

        Message.WARP_ICON_NEW_TYPE.send(player);
        player.closeInventory();

        PlayerChat.listen(player, message -> {
            if (!message.equalsIgnoreCase("-cancel")) {
                Material material;

                try {
                    material = Material.valueOf(message.toUpperCase(Locale.ENGLISH));
                    if (material == Material.AIR)
                        throw new IllegalArgumentException();
                } catch (IllegalArgumentException ex) {
                    Message.INVALID_MATERIAL.send(player, message);
                    return true;
                }

                getCache(player).getEditableBuilder().withType(material);
            }

            PlayerChat.remove(player);

            Bukkit.getScheduler().runTask(plugin, () -> menuManager.openInventory(player, "warp-icon-edit"));

            return true;
        });
    }
}
