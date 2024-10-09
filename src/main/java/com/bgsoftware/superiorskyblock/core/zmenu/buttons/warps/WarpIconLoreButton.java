package com.bgsoftware.superiorskyblock.core.zmenu.buttons.warps;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.SuperiorButton;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;

public class WarpIconLoreButton extends SuperiorButton {
    public WarpIconLoreButton(Plugin plugin) {
        super((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        super.onClick(player, event, inventory, slot, placeholders);

        Message.WARP_ICON_NEW_LORE.send(player);
        player.closeInventory();

        PlayerChat.listen(player, message -> {
            if (!message.equalsIgnoreCase("-cancel")) {
                getCache(player).getEditableBuilder().withLore(message.split("\\\\n"));
            }

            PlayerChat.remove(player);
            Bukkit.getScheduler().runTask(plugin, () -> menuManager.openInventory(player, "warp-icon-edit"));

            return true;
        });
    }
}
