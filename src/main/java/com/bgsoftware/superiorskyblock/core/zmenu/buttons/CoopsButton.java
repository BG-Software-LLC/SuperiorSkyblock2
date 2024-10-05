package com.bgsoftware.superiorskyblock.core.zmenu.buttons;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemSkulls;
import fr.maxlego08.menu.api.button.PaginateButton;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.maxlego08.menu.zcore.utils.inventory.Pagination;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class CoopsButton extends SuperiorButton implements PaginateButton {

    public CoopsButton(Plugin plugin) {
        super((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public void onInventoryOpen(Player player, InventoryDefault inventory, Placeholders placeholders) {
        super.onInventoryOpen(player, inventory, placeholders);
        Island island = getCache(player).getIsland();
        placeholders.register("coop", String.valueOf(island.getCoopPlayers().size()));
        placeholders.register("max-coop", String.valueOf(island.getCoopLimit()));
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
        List<SuperiorPlayer> bannedPlayers = pagination.paginate(getCache(player).getIsland().getCoopPlayers(), this.slots.size(), inventory.getPage());

        for (int i = 0; i != Math.min(bannedPlayers.size(), this.slots.size()); i++) {
            int slot = slots.get(i);
            SuperiorPlayer coopPlayer = bannedPlayers.get(i);
            Placeholders placeholders = new Placeholders();
            placeholders.register("player", coopPlayer.getName());
            placeholders.register("role", coopPlayer.getPlayerRole().toString());

            inventory.addItem(slot, ItemSkulls.getPlayerHead(getItemStack().build(player, false, placeholders), coopPlayer.getTextureValue())).setClick(event -> {
                plugin.getCommands().dispatchSubCommand(player, "uncoop", coopPlayer.getName());
                menuManager.openInventory(player, "coops");
            });
        }
    }

    @Override
    public int getPaginationSize(Player player) {
        return getCache(player).getIsland().getCoopPlayers().size();
    }
}
