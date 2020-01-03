package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MenuGlobalWarps extends PagedSuperiorMenu<Island> {

    private static boolean visitorWarps;

    private MenuGlobalWarps(SuperiorPlayer superiorPlayer){
        super("menuGlobalWarps", superiorPlayer);
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, Island island) {
        if(visitorWarps){
            Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "is visit " + island.getOwner().getName());
        }
        else{
            MenuWarps.openInventory(superiorPlayer, this, island);
        }
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, Island island) {
        return new ItemBuilder(clickedItem).asSkullOf(island.getOwner())
                .replaceAll("{0}", island.getOwner().getName())
                .replaceLoreWithLines("{1}", island.getDescription().split("\n"))
                .replaceAll("{2}", island.getAllWarps().size() + "").build(superiorPlayer);
    }

    @Override
    protected List<Island> requestObjects() {
        return getFilteredIslands(superiorPlayer)
                .sorted(SortingComparators.WORTH_COMPARATOR)
                .collect(Collectors.toList());
    }

    public static void init(){
        MenuGlobalWarps menuGlobalWarps = new MenuGlobalWarps(null);

        File file = new File(plugin.getDataFolder(), "menus/global-warps.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/global-warps.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuGlobalWarps, "global-warps.yml", cfg);

        visitorWarps = cfg.getBoolean("visitor-warps", false);

        menuGlobalWarps.setPreviousSlot(charSlots.getOrDefault(cfg.getString("previous-page", "%").charAt(0), Collections.singletonList(-1)).get(0));
        menuGlobalWarps.setCurrentSlot(charSlots.getOrDefault(cfg.getString("current-page", "*").charAt(0), Collections.singletonList(-1)).get(0));
        menuGlobalWarps.setNextSlot(charSlots.getOrDefault(cfg.getString("next-page", "^").charAt(0), Collections.singletonList(-1)).get(0));
        menuGlobalWarps.setSlots(charSlots.getOrDefault(cfg.getString("warps", "@").charAt(0), Collections.singletonList(-1)));
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new MenuGlobalWarps(superiorPlayer).open(previousMenu);
    }

    public static void refreshMenus(){
        SuperiorMenu.refreshMenus(MenuGlobalWarps.class);
    }

    private static Stream<Island> getFilteredIslands(SuperiorPlayer superiorPlayer){
        return plugin.getGrid().getIslands().stream()
                .filter(island -> {
                    if(visitorWarps)
                        return island.getVisitorsLocation() != null;
                    else if(island.equals(superiorPlayer.getIsland()))
                        return !island.getAllWarps().isEmpty();
                    else
                        return island.getAllWarps().stream().anyMatch(warp -> !island.isWarpPrivate(warp));
                });
    }

}