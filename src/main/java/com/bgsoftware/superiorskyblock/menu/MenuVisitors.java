package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MenuVisitors extends PagedSuperiorMenu<SuperiorPlayer> {

    private static int uniqueVisitorsSlot;

    private Island island;

    private MenuVisitors(SuperiorPlayer superiorPlayer, Island island){
        super("menuVisitors", superiorPlayer, true);
        this.island = island;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, SuperiorPlayer targetPlayer) {
        if(event.getRawSlot() == uniqueVisitorsSlot){
            previousMove = false;
            MenuUniqueVisitors.openInventory(superiorPlayer, this, island);
        }
        else{
            if (event.getClick().name().contains("RIGHT")) {
                Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island invite " + targetPlayer.getName());
            } else if (event.getClick().name().contains("LEFT")) {
                Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island expel " + targetPlayer.getName());
            }
        }
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, SuperiorPlayer superiorPlayer) {
        Island island = superiorPlayer.getIsland();
        String islandOwner = island != null ? island.getOwner().getName() : "None";
        String islandName =  island != null ? island.getName().isEmpty() ? islandOwner : island.getName() : "None";
        return new ItemBuilder(clickedItem)
                .replaceAll("{0}", superiorPlayer.getName())
                .replaceAll("{1}", islandOwner)
                .replaceAll("{2}", islandName)
                .asSkullOf(superiorPlayer).build(super.superiorPlayer);
    }

    @Override
    protected List<SuperiorPlayer> requestObjects() {
        return island.getIslandVisitors();
    }

    public static void init(){
        MenuVisitors menuVisitors = new MenuVisitors(null, null);

        File file = new File(plugin.getDataFolder(), "menus/visitors.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/visitors.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuVisitors, "visitors.yml", cfg);

        uniqueVisitorsSlot = charSlots.getOrDefault(cfg.getString("unique-visitors", "~").charAt(0), Collections.singletonList(-1)).get(0);

        menuVisitors.setPreviousSlot(charSlots.getOrDefault(cfg.getString("previous-page", "%").charAt(0), Collections.singletonList(-1)).get(0));
        menuVisitors.setCurrentSlot(charSlots.getOrDefault(cfg.getString("current-page", "*").charAt(0), Collections.singletonList(-1)).get(0));
        menuVisitors.setNextSlot(charSlots.getOrDefault(cfg.getString("next-page", "^").charAt(0), Collections.singletonList(-1)).get(0));
        menuVisitors.setSlots(charSlots.getOrDefault(cfg.getString("slots", "@").charAt(0), Collections.singletonList(-1)));
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuVisitors(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(){
        refreshMenus(MenuVisitors.class);
    }

}
