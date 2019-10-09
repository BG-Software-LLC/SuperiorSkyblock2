package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public final class ConfirmDisbandMenu extends SuperiorMenu {

    private static ConfirmDisbandMenu instance;
    private static Inventory inventory = null;
    private static int confirmSlot, cancelSlot;

    private SuperiorPlayer superiorPlayer;

    private ConfirmDisbandMenu(){
        super("confirmPage");
        instance = this;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        Island island = superiorPlayer.getIsland();

        if(confirmSlot == e.getRawSlot()){
            IslandDisbandEvent islandDisbandEvent = new IslandDisbandEvent(superiorPlayer, island);
            Bukkit.getPluginManager().callEvent(islandDisbandEvent);

            if(!islandDisbandEvent.isCancelled()) {
                if(!Locale.DISBAND_ANNOUNCEMENT.isEmpty())
                    island.sendMessage(Locale.DISBAND_ANNOUNCEMENT.getMessage(superiorPlayer.getName()));

                Locale.DISBANDED_ISLAND.send(superiorPlayer);

                superiorPlayer.setDisbands(superiorPlayer.getDisbands() - 1);
                island.disbandIsland();
            }
        }
        else if(e.getRawSlot() != cancelSlot)
            return;

        superiorPlayer.asPlayer().closeInventory();
        super.onClick(e);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void open(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu) {
        this.superiorPlayer = superiorPlayer;
        super.open(superiorPlayer, previousMenu);
    }

    public static void init(){
        ConfirmDisbandMenu confirmDisbandMenu = new ConfirmDisbandMenu();

        File file = new File(plugin.getDataFolder(), "guis/confirm-disband.yml");

        if(!file.exists())
            FileUtils.saveResource("guis/confirm-disband.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtils.loadGUI(confirmDisbandMenu, cfg.getConfigurationSection("disband-gui"), InventoryType.HOPPER, "&l      Confirm Disband");

        ItemStack confirmItem = FileUtils.getItemStack(cfg.getConfigurationSection("disband-gui.confirm"));
        confirmSlot = cfg.getInt("disband-gui.confirm.slot", 1);
        confirmDisbandMenu.addSound(confirmSlot, FileUtils.getSound(cfg.getConfigurationSection("disband-gui.confirm.sound")));
        confirmDisbandMenu.addCommands(confirmSlot, cfg.getStringList("disband-gui.confirm.commands"));
        inventory.setItem(confirmSlot, confirmItem);

        ItemStack cancelItem = FileUtils.getItemStack(cfg.getConfigurationSection("disband-gui.cancel"));
        cancelSlot = cfg.getInt("disband-gui.cancel.slot", 3);
        confirmDisbandMenu.addSound(cancelSlot, FileUtils.getSound(cfg.getConfigurationSection("disband-gui.cancel.sound")));
        confirmDisbandMenu.addCommands(cancelSlot, cfg.getStringList("disband-gui.cancel.commands"));
        inventory.setItem(cancelSlot, cancelItem);
    }

    public static SuperiorMenu getMenu(){
        return instance;
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        instance.open(superiorPlayer, previousMenu);
    }

}
