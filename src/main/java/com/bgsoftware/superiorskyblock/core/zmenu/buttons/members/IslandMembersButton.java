package com.bgsoftware.superiorskyblock.core.zmenu.buttons.members;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemSkulls;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.SuperiorButton;
import fr.maxlego08.menu.api.button.PaginateButton;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.maxlego08.menu.zcore.utils.inventory.Pagination;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

public class IslandMembersButton extends SuperiorButton implements PaginateButton {

    public IslandMembersButton(Plugin plugin) {
        super((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public boolean hasSpecialRender() {
        return true;
    }

    @Override
    public void onRender(Player player, InventoryDefault inventory) {
        SuperiorPlayer superiorPlayer = getSuperiorPlayer(player);
        List<SuperiorPlayer> members = new ArrayList<>(superiorPlayer.getIsland().getIslandMembers(true));
        members.sort(Comparator.comparingInt((ToIntFunction<SuperiorPlayer>) value -> value.getPlayerRole().getWeight()).reversed());

        Pagination<SuperiorPlayer> pagination = new Pagination<>();
        members = pagination.paginate(members, this.slots.size(), inventory.getPage());

        for (int i = 0; i != Math.min(members.size(), this.slots.size()); i++) {
            int slot = slots.get(i);
            SuperiorPlayer currentPlayer = members.get(i);

            Placeholders placeholders = new Placeholders();
            placeholders.register("name", currentPlayer.getName());
            placeholders.register("role", currentPlayer.getPlayerRole().getDisplayName());

            ItemStack itemStack = getItemStack().build(player, false, placeholders);
            itemStack = ItemSkulls.getPlayerHead(itemStack, currentPlayer.getTextureValue());

            inventory.addItem(slot, itemStack).setClick(event -> menuManager.openInventory(superiorPlayer, "member-manage", playerCache -> playerCache.setTargetPlayer(currentPlayer)));
        }
    }

    @Override
    public int getPaginationSize(Player player) {
        SuperiorPlayer superiorPlayer = getSuperiorPlayer(player);
        return superiorPlayer.getIsland().getIslandMembers(true).size();
    }
}
