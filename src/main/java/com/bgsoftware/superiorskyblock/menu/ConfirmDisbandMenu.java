package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.utils.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public final class ConfirmDisbandMenu extends SuperiorMenu {

    private static Inventory inventory = null;

    private ConfirmDisbandMenu(){
        super("confirmPage");
    }

    @Override
    public void onClick(InventoryClickEvent e) {
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static void init(){
        inventory = Bukkit.createInventory(null, InventoryType.HOPPER, ChatColor.BOLD + "      Confirm Disband");
        inventory.setItem(1, new ItemBuilder(Materials.getGlass(DyeColor.LIME)).withName("&aConfirm").withLore("&8Are you sure?").build());
        inventory.setItem(3, new ItemBuilder(Materials.getGlass(DyeColor.RED)).withName("&4Cancel").build());
    }

    public static ConfirmDisbandMenu createInventory(){
        return new ConfirmDisbandMenu();
    }

}
