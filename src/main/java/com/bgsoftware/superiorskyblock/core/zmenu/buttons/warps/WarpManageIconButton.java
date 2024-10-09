package com.bgsoftware.superiorskyblock.core.zmenu.buttons.warps;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.SuperiorButton;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class WarpManageIconButton extends SuperiorButton {
    public WarpManageIconButton(Plugin plugin) {
        super((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public void onInventoryOpen(Player player, InventoryDefault inventory, Placeholders placeholders) {
        super.onInventoryOpen(player, inventory, placeholders);

        IslandWarp islandWarp = getCache(player).getIslandWarp();
        placeholders.register("name", islandWarp.getName() == null ? "" : islandWarp.getName());
    }

    @Override
    public ItemStack getCustomItemStack(Player player) {
        IslandWarp islandWarp = getCache(player).getIslandWarp();

        Placeholders placeholders = new Placeholders();
        placeholders.register("material", islandWarp.getRawIcon() == null ? "STONE" : islandWarp.getRawIcon().getType().name());

        return getItemStack().build(player, false, placeholders);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        super.onClick(player, event, inventory, slot, placeholders);

        IslandWarp islandWarp = getCache(player).getIslandWarp();
        plugin.getMenus().openWarpIconEdit(getSuperiorPlayer(player), null, islandWarp);
    }
}
