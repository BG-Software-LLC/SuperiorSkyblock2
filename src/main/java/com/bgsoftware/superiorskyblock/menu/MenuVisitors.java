package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.commands.CommandUtils;
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

public final class MenuVisitors extends PagedSuperiorMenu<SuperiorPlayer> {

    private static List<Integer> uniqueVisitorsSlot;

    private Island island;

    private MenuVisitors(SuperiorPlayer superiorPlayer, Island island){
        super("menuVisitors", superiorPlayer, true);
        this.island = island;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, SuperiorPlayer targetPlayer) {
        if(uniqueVisitorsSlot.contains(event.getRawSlot())){
            previousMove = false;
            MenuUniqueVisitors.openInventory(superiorPlayer, this, island);
        }
        else if(targetPlayer != null){
            if (event.getClick().name().contains("RIGHT")) {
                CommandUtils.dispatchSubCommand(superiorPlayer.asPlayer(), "invite " + targetPlayer.getName());
            } else if (event.getClick().name().contains("LEFT")) {
                CommandUtils.dispatchSubCommand(superiorPlayer.asPlayer(), "expel " + targetPlayer.getName());
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

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            cfg.save(file);
        }

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuVisitors, "visitors.yml", cfg);

        uniqueVisitorsSlot = charSlots.get(cfg.getString("unique-visitors", " ").charAt(0), Collections.singletonList(-1));

        menuVisitors.setPreviousSlot(charSlots.get(cfg.getString("previous-page", " ").charAt(0), Collections.singletonList(-1)));
        menuVisitors.setCurrentSlot(charSlots.get(cfg.getString("current-page", " ").charAt(0), Collections.singletonList(-1)));
        menuVisitors.setNextSlot(charSlots.get(cfg.getString("next-page", " ").charAt(0), Collections.singletonList(-1)));
        menuVisitors.setSlots(charSlots.get(cfg.getString("slots", " ").charAt(0), Collections.singletonList(-1)));

        charSlots.delete();

        menuVisitors.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuVisitors(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(){
        refreshMenus(MenuVisitors.class);
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("visitors-panel.title"));

        int size = cfg.getInt("visitors-panel.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("visitors-panel.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("visitors-panel.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = itemChars[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("visitors-panel"),
                cfg.getConfigurationSection("visitors-panel.visitor-item"), newMenu, patternChars,
                slotsChar, itemChars[charCounter++], itemChars[charCounter++], itemChars[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

}
