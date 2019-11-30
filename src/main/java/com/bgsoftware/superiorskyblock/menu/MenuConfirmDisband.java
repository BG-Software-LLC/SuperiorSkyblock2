package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MenuConfirmDisband extends SuperiorMenu {

    private static int confirmSlot, cancelSlot;

    private MenuConfirmDisband(SuperiorPlayer superiorPlayer){
        super("menuConfirmDisband", superiorPlayer);
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent e) {
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

        previousMove = false;
        superiorPlayer.asPlayer().closeInventory();
    }

    public static void init(){
        MenuConfirmDisband menuConfirmDisband = new MenuConfirmDisband(null);

        File file = new File(plugin.getDataFolder(), "menus/confirm-disband.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/confirm-disband.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuConfirmDisband, cfg);

        confirmSlot = charSlots.getOrDefault(cfg.getString("confirm", "@").charAt(0), Collections.singletonList(-1)).get(0);
        cancelSlot = charSlots.getOrDefault(cfg.getString("cancel", "^").charAt(0), Collections.singletonList(-1)).get(0);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new MenuConfirmDisband(superiorPlayer).open(previousMenu);
    }

}
