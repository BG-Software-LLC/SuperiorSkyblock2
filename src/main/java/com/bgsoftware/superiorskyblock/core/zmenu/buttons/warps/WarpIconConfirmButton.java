package com.bgsoftware.superiorskyblock.core.zmenu.buttons.warps;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.SuperiorButton;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class WarpIconConfirmButton extends SuperiorButton {
    public WarpIconConfirmButton(Plugin plugin) {
        super((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        super.onClick(player, event, inventory, slot, placeholders);

        IslandWarp islandWarp = getCache(player).getIslandWarp();
        SuperiorPlayer superiorPlayer = getSuperiorPlayer(player);

        EventResult<ItemStack> eventResult = plugin.getEventsBus().callIslandChangeWarpIconEvent(superiorPlayer, islandWarp.getIsland(), islandWarp, getCache(player).getEditableBuilder().build());

        if (eventResult.isCancelled())
            return;

        player.closeInventory();

        Message.WARP_ICON_UPDATED.send(superiorPlayer);
        islandWarp.setIcon(eventResult.getResult());
    }
}
