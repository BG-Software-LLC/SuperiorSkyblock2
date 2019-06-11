package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
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

    private static ItemStack confirmItem, cancelItem;

    private SuperiorPlayer superiorPlayer;

    private ConfirmDisbandMenu(){
        super("confirmPage");
        instance = this;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        Island island = superiorPlayer.getIsland();

        if(confirmItem.equals(clickedItem)){
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
        else if(!cancelItem.equals(clickedItem))
            return;

        superiorPlayer.asPlayer().closeInventory();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu) {
        this.superiorPlayer = superiorPlayer;
        super.openInventory(superiorPlayer, previousMenu);
    }

    public static void init(){
        ConfirmDisbandMenu confirmDisbandMenu = new ConfirmDisbandMenu();

        File file = new File(plugin.getDataFolder(), "guis/confirm-disband.yml");

        if(!file.exists())
            FileUtil.saveResource("guis/confirm-disband.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        String title = cfg.getString("disband-gui.title", "&l      Confirm Disband");
        confirmDisbandMenu.openSound = getSound(cfg.getString("disband-gui.open-sound", ""));
        confirmDisbandMenu.closeSound = getSound(cfg.getString("disband-gui.close-sound", ""));

        inventory = Bukkit.createInventory(confirmDisbandMenu, InventoryType.HOPPER, ChatColor.translateAlternateColorCodes('&', title));

        confirmItem = FileUtil.getItemStack(cfg.getConfigurationSection("disband-gui.confirm"));
        int confirmSlot = cfg.getInt("disband-gui.confirm.slot", 1);
        inventory.setItem(confirmSlot, confirmItem);

        cancelItem = FileUtil.getItemStack(cfg.getConfigurationSection("disband-gui.cancel"));
        int cancelSlot = cfg.getInt("disband-gui.cancel.slot", 3);
        inventory.setItem(cancelSlot, cancelItem);
    }

    public static ConfirmDisbandMenu createInventory(){
        return instance;
    }

}
