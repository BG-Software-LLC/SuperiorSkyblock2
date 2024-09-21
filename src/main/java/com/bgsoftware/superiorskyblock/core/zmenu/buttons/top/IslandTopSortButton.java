package com.bgsoftware.superiorskyblock.core.zmenu.buttons.top;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.core.zmenu.PlayerCache;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.SuperiorButton;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class IslandTopSortButton extends SuperiorButton {

    private final SortingType sortingType;

    public IslandTopSortButton(SuperiorSkyblockPlugin plugin, SortingType sortingType) {
        super(plugin);
        this.sortingType = sortingType;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        super.onClick(player, event, inventory, slot, placeholders);
        PlayerCache playerCache = getCache(player);
        if (playerCache.getSortingType() != this.sortingType) {
            menuManager.openInventory(player, "top-islands", cache -> cache.setSortingType(this.sortingType));
        }
    }
}
