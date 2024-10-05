package com.bgsoftware.superiorskyblock.core.zmenu.buttons;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.zmenu.PlayerCache;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collections;

public class RateButton extends SuperiorButton {

    private final Rating rating;

    public RateButton(SuperiorSkyblockPlugin plugin, Rating rating) {
        super(plugin);
        this.rating = rating;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {

        PlayerCache cache = getCache(player);
        SuperiorPlayer inventoryViewer = getSuperiorPlayer(player);
        Island island = cache.getIsland();

        if (rating == Rating.UNKNOWN) {
            if (!plugin.getEventsBus().callIslandRemoveRatingEvent(inventoryViewer, inventoryViewer, island))
                return;

            island.removeRating(inventoryViewer);
        } else {
            if (!plugin.getEventsBus().callIslandRateEvent(inventoryViewer, inventoryViewer, island, rating))
                return;

            island.setRating(inventoryViewer, rating);
        }

        Message.RATE_SUCCESS.send(inventoryViewer, rating.getValue());

        IslandUtils.sendMessage(island, Message.RATE_ANNOUNCEMENT, Collections.emptyList(), inventoryViewer.getName(), rating.getValue());

        super.onClick(player, event, inventory, slot, placeholders);
    }
}
