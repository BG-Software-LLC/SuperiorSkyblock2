package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MenuWarps extends PagedSuperiorMenu<String> {

    private Island island;

    private MenuWarps(SuperiorPlayer superiorPlayer, Island island){
        super("menuWarps", superiorPlayer);
        this.island = island;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent event, String warpName) {
        Location location = island.getWarpLocation(warpName);
        if(location != null) {
            this.previousMenu = null;
            superiorPlayer.asPlayer().closeInventory();
            island.warpPlayer(superiorPlayer, warpName);
        }
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, String warpName) {
        return new ItemBuilder(clickedItem)
                .replaceAll("{0}", warpName)
                .replaceAll("{1}", SBlockPosition.of(island.getWarpLocation(warpName)).toString())
                .replaceAll("{2}", island.isWarpPrivate(warpName) ?
                        ensureNotNull(Locale.ISLAND_WARP_PRIVATE.getMessage(superiorPlayer.getUserLocale())) :
                        ensureNotNull(Locale.ISLAND_WARP_PUBLIC.getMessage(superiorPlayer.getUserLocale())))
                .build(superiorPlayer);
    }

    @Override
    protected List<String> requestObjects() {
        return island.getAllWarps().stream()
                .filter(warp -> island.equals(superiorPlayer.getIsland()) || !island.isWarpPrivate(warp))
                .sorted(String::compareTo).collect(Collectors.toList());
    }

    public static void init(){
        MenuWarps menuWarps = new MenuWarps(null, null);

        File file = new File(plugin.getDataFolder(), "menus/warps.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/warps.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuWarps, "warps.yml", cfg);

        menuWarps.setPreviousSlot(charSlots.getOrDefault(cfg.getString("previous-page", "%").charAt(0), Collections.singletonList(-1)).get(0));
        menuWarps.setCurrentSlot(charSlots.getOrDefault(cfg.getString("current-page", "*").charAt(0), Collections.singletonList(-1)).get(0));
        menuWarps.setNextSlot(charSlots.getOrDefault(cfg.getString("next-page", "^").charAt(0), Collections.singletonList(-1)).get(0));
        menuWarps.setSlots(charSlots.getOrDefault(cfg.getString("slots", "@").charAt(0), Collections.singletonList(-1)));
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        MenuWarps menuWarps = new MenuWarps(superiorPlayer, island);
        if(plugin.getSettings().skipOneItemMenus && hasOnlyOneItem(island, superiorPlayer)){
            menuWarps.onPlayerClick(null, getOnlyOneItem(island, superiorPlayer));
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
