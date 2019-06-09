package com.bgsoftware.superiorskyblock.gui.menus;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.gui.buttons.Button;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.BiConsumer;

public class ConfirmMenu extends Menu {

    public ConfirmMenu(Player player, String title, BiConsumer<Player, ClickType> action) {
        super(player, title, 1);
        create(title);

        setButton(1, new Button(getConfirmItem(), action));
        setButton(3, new Button(getCancelItem(), (clicker, type) -> clicker.closeInventory()));

        open();
    }

    protected void create(String title) {
        inventory = Bukkit.createInventory(null, InventoryType.HOPPER, title);
        SuperiorSkyblockPlugin.getPlugin().getMenuHandler().getMenus().put(player.getUniqueId(), this);
    }

    private static ItemStack getConfirmItem() {
        ItemStack item = getWool("GREEN", 5);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Confirm");
        item.setItemMeta(meta);

        return item;
    }

    private static ItemStack getCancelItem() {
        ItemStack item = getWool("RED", 14);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Cancel");
        item.setItemMeta(meta);

        return item;
    }

    private static ItemStack getWool(String color, int data) {
        try {
            return new ItemStack(Material.valueOf(color + "_WOOL"));
        } catch (Exception ignored) {}

        return new ItemStack(Material.valueOf("WOOL"), 1, (short) data);
    }
}
