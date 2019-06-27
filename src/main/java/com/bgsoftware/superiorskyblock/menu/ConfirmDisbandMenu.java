package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.UUID;

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
                for(UUID uuid : island.getMembers()){
                    if(Bukkit.getOfflinePlayer(uuid).isOnline()){
                        Locale.DISBAND_ANNOUNCEMENT.send(Bukkit.getPlayer(uuid), superiorPlayer.getName());
                    }
                }

                Locale.DISBANDED_ISLAND.send(superiorPlayer);

                superiorPlayer.setDisbands(superiorPlayer.getDisbands() - 1);
                island.disbandIsland();
            }
        }
        else if(e.getRawSlot() != cancelSlot)
            return;

        superiorPlayer.asPlayer().closeInventory();
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
            FileUtil.saveResource("guis/confirm-disband.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtil.loadGUI(confirmDisbandMenu, cfg.getConfigurationSection("disband-gui"), InventoryType.HOPPER, "&l      Confirm Disband");

        ItemStack confirmItem = FileUtil.getItemStack(cfg.getConfigurationSection("disband-gui.confirm"));
        confirmSlot = cfg.getInt("disband-gui.confirm.slot", 1);
        inventory.setItem(confirmSlot, confirmItem);

        ItemStack cancelItem = FileUtil.getItemStack(cfg.getConfigurationSection("disband-gui.cancel"));
        cancelSlot = cfg.getInt("disband-gui.cancel.slot", 3);
        inventory.setItem(cancelSlot, cancelItem);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        instance.open(superiorPlayer, previousMenu);
    }

}
