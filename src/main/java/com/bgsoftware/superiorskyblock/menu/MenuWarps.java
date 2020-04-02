package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
            Executor.sync(() -> {
                previousMove = false;
                superiorPlayer.asPlayer().closeInventory();
                island.warpPlayer(superiorPlayer, warpName);
            }, 1L);
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

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            cfg.save(file);
        }

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuWarps, "warps.yml", cfg);

        menuWarps.setPreviousSlot(charSlots.get(cfg.getString("previous-page", " ").charAt(0), Collections.singletonList(-1)));
        menuWarps.setCurrentSlot(charSlots.get(cfg.getString("current-page", " ").charAt(0), Collections.singletonList(-1)));
        menuWarps.setNextSlot(charSlots.get(cfg.getString("next-page", " ").charAt(0), Collections.singletonList(-1)));
        menuWarps.setSlots(charSlots.get(cfg.getString("slots", " ").charAt(0), Collections.singletonList(-1)));

        charSlots.delete();

        menuWarps.markCompleted();
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

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/warps-gui.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("warps-gui.title"));

        int size = cfg.getInt("warps-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("warps-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("warps-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = itemChars[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("warps-gui"),
                cfg.getConfigurationSection("warps-gui.warp-item"),
                newMenu, patternChars,
                slotsChar, itemChars[charCounter++], itemChars[charCounter++], itemChars[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

}
