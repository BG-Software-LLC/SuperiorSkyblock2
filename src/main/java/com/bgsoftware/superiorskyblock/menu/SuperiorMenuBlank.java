package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import java.util.Arrays;

public final class SuperiorMenuBlank extends SuperiorMenu {

    private SuperiorMenuBlank(SuperiorPlayer superiorPlayer){
        super("menuBlank", superiorPlayer);
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent e) {

    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        open(previousMenu);
    }

    public static void init(){
        SuperiorMenuBlank superiorMenuBlank = new SuperiorMenuBlank(null);

        superiorMenuBlank.resetData();

        superiorMenuBlank.setTitle("" + ChatColor.RED + ChatColor.BOLD + "ERROR");
        superiorMenuBlank.setInventoryType(InventoryType.CHEST);
        superiorMenuBlank.setRowsSize(3);
        superiorMenuBlank.setBackButton(-1);

        superiorMenuBlank.addFillItem(13, new ItemBuilder(Material.BEDROCK).withName("&cUnloaded Menu")
        .withLore(Arrays.asList("&7There was an issue with loading the menu.", "&7Contact administrator to fix the issue.")));

        try {
            superiorMenuBlank.addSound(13, new SoundWrapper(Sound.valueOf("BLOCK_ANVIL_PLACE"), 0.2f, 0.2f));
        }catch(Throwable ignored){}

        superiorMenuBlank.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu){
        new SuperiorMenuBlank(superiorPlayer).open(previousMenu);
    }

}
