package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MenuGlobalWarps extends SuperiorMenu {

    private static int previousSlot, currentSlot, nextSlot;
    private static List<Integer> slots;
    private static boolean visitorWarps;

    private int currentPage;

    private MenuGlobalWarps(SuperiorPlayer superiorPlayer, int currentPage){
        super("menuGlobalWarps", superiorPlayer);
        this.currentPage = currentPage;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        if(e.getRawSlot() == previousSlot || e.getRawSlot() == nextSlot || e.getRawSlot() == currentSlot){
            if(e.getRawSlot() == currentSlot)
                return;

            int islandsSize = (int) plugin.getGrid().getIslands().stream()
                    .filter(island -> !island.getAllWarps().isEmpty()).count();

            boolean nextPage = slots.size() * currentPage < islandsSize;

            if((!nextPage && e.getRawSlot() == nextSlot) || (currentPage == 1 && e.getRawSlot() == previousSlot))
                return;

            currentPage = e.getRawSlot() == nextSlot ? currentPage + 1 : currentPage - 1;

            previousMove = false;
            open(previousMenu);
        }

        else{
            if(e.getCurrentItem() == null)
                return;

            List<Island> islands = getFilteredIslands(superiorPlayer)
                    .sorted(SortingComparators.WORTH_COMPARATOR)
                    .collect(Collectors.toList());

            int indexOf = slots.indexOf(e.getRawSlot());

            if(indexOf >= islands.size() || indexOf == -1)
                return;

            Island island = islands.get(indexOf);

            if(visitorWarps){
                Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "is visit " + island.getOwner().getName());
            }
            else{
                MenuWarps.openInventory(superiorPlayer, this, island);
            }
        }
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = super.getInventory();

        List<Island> islands = getFilteredIslands(superiorPlayer)
                .sorted(SortingComparators.WORTH_COMPARATOR)
                .collect(Collectors.toList());

        for(int i = 0; i < slots.size(); i++){
            int islandIndex = i + (slots.size() * (currentPage - 1));
            if(islandIndex < islands.size()) {
                Island island = islands.get(islandIndex);
                inventory.setItem(slots.get(i), new ItemBuilder(inventory.getItem(slots.get(i))).asSkullOf(island.getOwner())
                        .replaceAll("{0}", island.getOwner().getName())
                        .replaceLoreWithLines("{1}", island.getDescription().split("\n"))
                        .replaceAll("{2}", island.getAllWarps().size() + "").build(superiorPlayer));
            }
            else{
                inventory.setItem(slots.get(i), new ItemStack(Material.AIR));
            }
        }

        inventory.setItem(previousSlot, new ItemBuilder(inventory.getItem(previousSlot))
                .replaceAll("{0}", (currentPage == 1 ? "&c" : "&a")).build(superiorPlayer));

        inventory.setItem(currentSlot, new ItemBuilder(inventory.getItem(currentSlot))
                .replaceAll("{0}", currentPage + "").build(superiorPlayer));

        inventory.setItem(nextSlot, new ItemBuilder(inventory.getItem(nextSlot))
                .replaceAll("{0}", (islands.size() > currentPage * slots.size() ? "&a" : "&c")).build(superiorPlayer));

        return inventory;
    }

    public static void init(){
        MenuGlobalWarps menuGlobalWarps = new MenuGlobalWarps(null, 1);

        File file = new File(plugin.getDataFolder(), "menus/global-warps.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/global-warps.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuGlobalWarps, "global-warps.yml", cfg);

        previousSlot = charSlots.getOrDefault(cfg.getString("previous-page", "%").charAt(0), Collections.singletonList(-1)).get(0);
        currentSlot = charSlots.getOrDefault(cfg.getString("current-page", "*").charAt(0), Collections.singletonList(-1)).get(0);
        nextSlot = charSlots.getOrDefault(cfg.getString("next-page", "^").charAt(0), Collections.singletonList(-1)).get(0);
        visitorWarps = cfg.getBoolean("visitor-warps", false);

        slots = charSlots.getOrDefault(cfg.getString("warps", "@").charAt(0), Collections.singletonList(-1));
        slots.sort(Integer::compareTo);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        openInventory(superiorPlayer, 1, previousMenu);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, int currentPage, SuperiorMenu previousMenu){
        new MenuGlobalWarps(superiorPlayer, currentPage).open(previousMenu);
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
