package com.bgsoftware.superiorskyblock.core.zmenu.buttons;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemSkulls;
import com.bgsoftware.superiorskyblock.core.zmenu.PlayerCache;
import fr.maxlego08.menu.api.utils.Placeholders;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class TargetShowButton extends SuperiorButton {
    public TargetShowButton(Plugin plugin) {
        super((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public ItemStack getCustomItemStack(Player player) {
        PlayerCache playerCache = getCache(player);
        SuperiorPlayer targetPlayer = playerCache.getTargetPlayer();
        Placeholders placeholders = new Placeholders();
        placeholders.register("player-name", targetPlayer.getName());

        ItemStack itemStack = this.getItemStack().build(player, false, placeholders);
        return ItemSkulls.getPlayerHead(itemStack, targetPlayer.getTextureValue());
    }
}
