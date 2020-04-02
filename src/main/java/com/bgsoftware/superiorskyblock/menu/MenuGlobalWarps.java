package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.commands.CommandUtils;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MenuGlobalWarps extends PagedSuperiorMenu<Island> {

    public static boolean visitorWarps;

    private MenuGlobalWarps(SuperiorPlayer superiorPlayer){
        super("menuGlobalWarps", superiorPlayer);
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, Island island) {
        if(visitorWarps){
            previousMove = false;
            CommandUtils.dispatchSubCommand(superiorPlayer.asPlayer(), "visit " + island.getOwner().getName());
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

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            cfg.save(file);
        }

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuGlobalWarps, "global-warps.yml", cfg);

        visitorWarps = cfg.getBoolean("visitor-warps", false);

        menuGlobalWarps.setPreviousSlot(charSlots.get(cfg.getString("previous-page", " ").charAt(0), Collections.singletonList(-1)));
        menuGlobalWarps.setCurrentSlot(charSlots.get(cfg.getString("current-page", " ").charAt(0), Collections.singletonList(-1)));
        menuGlobalWarps.setNextSlot(charSlots.get(cfg.getString("next-page", " ").charAt(0), Collections.singletonList(-1)));
        menuGlobalWarps.setSlots(charSlots.get(cfg.getString("warps", " ").charAt(0), Collections.singletonList(-1)));

        charSlots.delete();

        menuGlobalWarps.markCompleted();
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

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/warps-gui.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("global-gui.title"));

        int size = cfg.getInt("global-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("global-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("global-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = itemChars[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("global-gui"),
                cfg.getConfigurationSection("global-gui.warp-item"),
                newMenu, patternChars,
                slotsChar, itemChars[charCounter++], itemChars[charCounter++], itemChars[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("visitor-warps", cfg.getConfigurationSection("global-gui.visitor-warps"));
        newMenu.set("warps", newMenu.getString("slots"));
        newMenu.set("slots", null);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

}