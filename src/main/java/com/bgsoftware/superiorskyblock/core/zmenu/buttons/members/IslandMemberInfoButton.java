package com.bgsoftware.superiorskyblock.core.zmenu.buttons.members;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemSkulls;
import com.bgsoftware.superiorskyblock.core.zmenu.PlayerCache;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.SuperiorButton;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class IslandMemberInfoButton extends SuperiorButton {
    public IslandMemberInfoButton(Plugin plugin) {
        super((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public void onInventoryOpen(Player player, InventoryDefault inventory, Placeholders placeholders) {
        super.onInventoryOpen(player, inventory, placeholders);
        PlayerCache playerCache = getCache(player);
        placeholders.register("name", playerCache.getTargetPlayer().getName());
    }

    @Override
    public ItemStack getCustomItemStack(Player player) {
        PlayerCache playerCache = getCache(player);

        SuperiorPlayer currentPlayer = playerCache.getTargetPlayer();
        Placeholders placeholders = new Placeholders();
        placeholders.register("name", currentPlayer.getName());
        placeholders.register("role", currentPlayer.getPlayerRole().getDisplayName());

        ItemStack itemStack = getItemStack().build(player, false, placeholders);
        return ItemSkulls.getPlayerHead(itemStack, currentPlayer.getTextureValue());
    }
}
