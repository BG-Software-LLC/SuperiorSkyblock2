package com.bgsoftware.superiorskyblock.core.zmenu.buttons;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemSkulls;
import fr.maxlego08.menu.api.button.PaginateButton;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.maxlego08.menu.zcore.utils.inventory.Pagination;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class VisitorsButton extends SuperiorButton implements PaginateButton {

    public VisitorsButton(Plugin plugin) {
        super((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public void onInventoryOpen(Player player, InventoryDefault inventory, Placeholders placeholders) {
        super.onInventoryOpen(player, inventory, placeholders);
        placeholders.register("visitor", String.valueOf(getCache(player).getIsland().getUniqueVisitorsWithTimes().size()));
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

        Pagination<SuperiorPlayer> pagination = new Pagination<>();
        List<SuperiorPlayer> uniqueVisitorInfos = pagination.paginate(requestObjects(player), this.slots.size(), inventory.getPage());

        for (int i = 0; i != Math.min(uniqueVisitorInfos.size(), this.slots.size()); i++) {

            int slot = slots.get(i);
            SuperiorPlayer visitor = uniqueVisitorInfos.get(i);

            Island island = visitor.getIsland();
            String islandOwner = island != null ? island.getOwner().getName() : "None";
            String islandName = island != null ? island.getName().isEmpty() ? islandOwner : island.getName() : "None";

            Placeholders placeholders = new Placeholders();
            placeholders.register("player", visitor.getName());
            placeholders.register("island-owner", islandOwner);
            placeholders.register("island-name", islandName);

            inventory.addItem(slot, ItemSkulls.getPlayerHead(getItemStack().build(player, false, placeholders), visitor.getTextureValue()));
        }
    }

    @Override
    public int getPaginationSize(Player player) {
        return requestObjects(player).size();
    }

    protected List<SuperiorPlayer> requestObjects(Player player) {
        return getCache(player).getIsland().getIslandVisitors(false);
    }
}
