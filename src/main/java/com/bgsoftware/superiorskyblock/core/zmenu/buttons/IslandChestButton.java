package com.bgsoftware.superiorskyblock.core.zmenu.buttons;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import fr.maxlego08.menu.api.button.PaginateButton;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.maxlego08.menu.zcore.utils.inventory.Pagination;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class IslandChestButton extends SuperiorButton implements PaginateButton {

    public IslandChestButton(Plugin plugin) {
        super((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public boolean hasPermission() {
        return true;
    }

    @Override
    public boolean checkPermission(Player player, InventoryDefault inventory, Placeholders placeholders) {
        return requestObjects(player).size() != 0;
    }

    @Override
    public boolean hasSpecialRender() {
        return true;
    }

    @Override
    public void onRender(Player player, InventoryDefault inventory) {

        Pagination<IslandChest> pagination = new Pagination<>();
        List<IslandChest> islandChests = pagination.paginate(requestObjects(player), this.slots.size(), inventory.getPage());

        SuperiorPlayer superiorPlayer = getSuperiorPlayer(player);

        for (int i = 0; i != Math.min(islandChests.size(), this.slots.size()); i++) {

            int slot = slots.get(i);
            IslandChest islandChest = islandChests.get(i);
            Placeholders placeholders = new Placeholders();
            placeholders.register("index", String.valueOf(islandChest.getIndex()));
            placeholders.register("size", String.valueOf(islandChest.getRows()));

            inventory.addItem(slot, getItemStack().build(player, false, placeholders)).setClick(event -> islandChest.openChest(superiorPlayer));
        }
    }

    @Override
    public int getPaginationSize(Player player) {
        return requestObjects(player).size();
    }

    private List<IslandChest> requestObjects(Player player) {
        return new SequentialListBuilder<IslandChest>().build(Arrays.asList(getCache(player).getIsland().getChest()));
    }
}
