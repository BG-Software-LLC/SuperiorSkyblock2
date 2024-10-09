package com.bgsoftware.superiorskyblock.core.zmenu.buttons.warps;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.SuperiorButton;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;

public class WarpIconNameButton extends SuperiorButton {
    public WarpIconNameButton(Plugin plugin) {
        super((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public void onInventoryOpen(Player player, InventoryDefault inventory, Placeholders placeholders) {
        super.onInventoryOpen(player, inventory, placeholders);

        IslandWarp islandWarp = getCache(player).getIslandWarp();
        placeholders.register("name", islandWarp.getName() == null ? "" : islandWarp.getName());
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        super.onClick(player, event, inventory, slot, placeholders);

        Message.WARP_ICON_NEW_NAME.send(player);
        player.closeInventory();

        PlayerChat.listen(player, message -> {
            if (!message.equalsIgnoreCase("-cancel")) {
                getCache(player).getEditableBuilder().withName(message);
            }

            PlayerChat.remove(player);

            Bukkit.getScheduler().runTask(plugin, () -> menuManager.openInventory(player, "warp-icon-edit"));

            return true;
        });
    }
}
