package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MenuWarps extends SuperiorMenu {

    private static int previousSlot, currentSlot, nextSlot;
    private static List<Integer> slots;

    private Island island;
    private int currentPage = 1;

    private MenuWarps(SuperiorPlayer superiorPlayer, Island island){
        super("menuWarps", superiorPlayer);
        this.island = island;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        if(e.getRawSlot() == previousSlot || e.getRawSlot() == nextSlot || e.getRawSlot() == currentSlot){
            if(e.getRawSlot() == currentSlot)
                return;

            boolean nextPage = slots.size() * currentPage < island.getAllWarps().size();

            if((!nextPage && e.getRawSlot() == nextSlot) || (currentPage == 1 && e.getRawSlot() == previousSlot))
                return;

            currentPage = e.getRawSlot() == nextSlot ? currentPage + 1 : currentPage - 1;

            previousMove = false;
            open(previousMenu);
        }

        else{
            if(e.getCurrentItem() == null)
                return;

            List<String> warps = new ArrayList<>(island.getAllWarps());
            warps.sort(String::compareTo);

            int indexOf = slots.indexOf(e.getRawSlot());

            if(indexOf >= warps.size() || indexOf == -1)
                return;

            String warpName = warps.get(indexOf);
            clickWarp(warpName, superiorPlayer);
        }
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = super.getInventory();

        List<String> warps = island.getAllWarps().stream()
                .filter(warp -> island.equals(superiorPlayer.getIsland()) || !island.isWarpPrivate(warp))
                .sorted(String::compareTo)
                .collect(Collectors.toList());

        for(int i = 0; i < slots.size(); i++){
            int warpsIndex = i + (slots.size() * (currentPage - 1));

            if(warpsIndex < warps.size()) {
                String warpName = warps.get(warpsIndex);
                inventory.setItem(slots.get(i), new ItemBuilder(inventory.getItem(slots.get(i)))
                        .replaceAll("{0}", warpName)
                        .replaceAll("{1}", SBlockPosition.of(island.getWarpLocation(warpName)).toString())
                        .replaceAll("{2}", island.isWarpPrivate(warpName) ?
                                ensureNotNull(Locale.ISLAND_WARP_PRIVATE.getMessage()) : ensureNotNull(Locale.ISLAND_WARP_PUBLIC.getMessage()))
                        .build(superiorPlayer));
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
                .replaceAll("{0}", (warps.size() > currentPage * slots.size() ? "&a" : "&c")).build(superiorPlayer));

        return inventory;
    }

    private void clickWarp(String warpName, SuperiorPlayer superiorPlayer){
        Location location = island.getWarpLocation(warpName);
        if(location != null) {
            this.previousMenu = null;
            superiorPlayer.asPlayer().closeInventory();
            island.warpPlayer(superiorPlayer, warpName);
        }
    }

    public static void init(){
        MenuWarps menuWarps = new MenuWarps(null, null);

        File file = new File(plugin.getDataFolder(), "menus/warps.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/warps.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuWarps, cfg);

        previousSlot = charSlots.getOrDefault(cfg.getString("previous-page", "%").charAt(0), Collections.singletonList(-1)).get(0);
        currentSlot = charSlots.getOrDefault(cfg.getString("current-page", "*").charAt(0), Collections.singletonList(-1)).get(0);
        nextSlot = charSlots.getOrDefault(cfg.getString("next-page", "^").charAt(0), Collections.singletonList(-1)).get(0);

        slots = charSlots.getOrDefault(cfg.getString("slots", "@").charAt(0), Collections.singletonList(-1));
        slots.sort(Integer::compareTo);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        MenuWarps menuWarps = new MenuWarps(superiorPlayer, island);
        if(plugin.getSettings().skipOneItemMenus && hasOnlyOneItem(island, superiorPlayer)){
            menuWarps.clickWarp(getOnlyOneItem(island, superiorPlayer), superiorPlayer);
        }
        else {
            menuWarps.open(previousMenu);
        }
    }

    public static void refreshMenus(){
        refreshMenus(MenuWarps.class);
    }

    private String ensureNotNull(String check){
        return check == null ? "" : check;
    }

    private static boolean hasOnlyOneItem(Island island, SuperiorPlayer superiorPlayer){
        return island.getAllWarps().stream()
                .filter(warp -> island.equals(superiorPlayer.getIsland()) || !island.isWarpPrivate(warp))
                .count() == 1;
    }

    private static String getOnlyOneItem(Island island, SuperiorPlayer superiorPlayer){
        return island.getAllWarps().stream()
                .filter(warp -> island.equals(superiorPlayer.getIsland()) || !island.isWarpPrivate(warp))
                .findFirst().orElse(null);
    }

}
