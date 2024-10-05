package com.bgsoftware.superiorskyblock.core.zmenu.buttons;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemSkulls;
import com.bgsoftware.superiorskyblock.island.top.SortingComparators;
import fr.maxlego08.menu.api.button.PaginateButton;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.maxlego08.menu.zcore.utils.inventory.Pagination;
import org.bukkit.entity.Player;

import java.util.List;

public class GlobalWarpsButton extends SuperiorButton implements PaginateButton {

    private final boolean visitorWarps;

    public GlobalWarpsButton(SuperiorSkyblockPlugin plugin, boolean visitorWarps) {
        super(plugin);
        this.visitorWarps = visitorWarps;
    }

    @Override
    public boolean hasPermission() {
        return true;
    }

    @Override
    public boolean checkPermission(Player player, InventoryDefault inventory, Placeholders placeholders) {
        return getPaginationSize(player) != 0;
    }

    @Override
    public boolean hasSpecialRender() {
        return true;
    }

    @Override
    public void onRender(Player player, InventoryDefault inventory) {

        Pagination<Island> pagination = new Pagination<>();
        List<Island> islands = pagination.paginate(requestObjects(player), this.slots.size(), inventory.getPage());

        for (int i = 0; i != Math.min(islands.size(), this.slots.size()); i++) {

            int slot = slots.get(i);
            Island island = islands.get(i);

            Placeholders placeholders = new Placeholders();
            placeholders.register("player", island.getOwner().getName());
            placeholders.register("description", island.getDescription());
            placeholders.register("warps", String.valueOf(island.getIslandWarps().size()));

            inventory.addItem(slot, ItemSkulls.getPlayerHead(getItemStack().build(player, false, placeholders), island.getOwner().getTextureValue())).setClick(event -> {

                if (this.visitorWarps) {
                    plugin.getCommands().dispatchSubCommand(player, "visit", island.getOwner().getName());
                } else {
                    plugin.getZMenumanager().openInventory(player, "warp-categories", cache -> cache.setIsland(island));
                }
            });
        }
    }

    @Override
    public int getPaginationSize(Player player) {
        return requestObjects(player).size();
    }

    protected List<Island> requestObjects(Player player) {
        return new SequentialListBuilder<Island>().sorted(SortingComparators.WORTH_COMPARATOR).filter(island -> {
            if (this.visitorWarps) return island.getVisitorsLocation((Dimension) null /* unused */) != null;
            else if (island.equals(getSuperiorPlayer(player).getIsland())) return !island.getIslandWarps().isEmpty();
            else return island.getIslandWarps().values().stream().anyMatch(islandWarp -> !islandWarp.hasPrivateFlag());
        }).build(plugin.getGrid().getIslands());
    }
}
