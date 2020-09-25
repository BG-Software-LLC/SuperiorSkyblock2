package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MenuConfirmDisband extends SuperiorMenu {

    private static List<Integer> confirmSlot, cancelSlot;

    private MenuConfirmDisband(SuperiorPlayer superiorPlayer){
        super("menuConfirmDisband", superiorPlayer);
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent e) {
        Island island = superiorPlayer.getIsland();

        if(confirmSlot.contains(e.getRawSlot())){
            if(EventsCaller.callIslandDisbandEvent(superiorPlayer, island)){
                IslandUtils.sendMessage(island, Locale.DISBAND_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName());

                Locale.DISBANDED_ISLAND.send(superiorPlayer);

                superiorPlayer.setDisbands(superiorPlayer.getDisbands() - 1);

                previousMove = false;
                superiorPlayer.asPlayer().closeInventory();

                island.disbandIsland();
            }
        }
        else if(cancelSlot.contains(e.getRawSlot())) {
            previousMove = false;
            superiorPlayer.asPlayer().closeInventory();
        }
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu);
    }

    public static void init(){
        MenuConfirmDisband menuConfirmDisband = new MenuConfirmDisband(null);

        File file = new File(plugin.getDataFolder(), "menus/confirm-disband.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/confirm-disband.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            cfg.save(file);
        }

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuConfirmDisband, "confirm-disband.yml", cfg);

        confirmSlot = getSlots(cfg, "confirm", charSlots);
        cancelSlot = getSlots(cfg, "cancel", charSlots);

        charSlots.delete();

        menuConfirmDisband.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new MenuConfirmDisband(superiorPlayer).open(previousMenu);
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/confirm-disband.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("disband-gui.title"));
        newMenu.set("type", "HOPPER");

        char[] patternChars = new char[5];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("disband-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("disband-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char confirmChar = itemChars[charCounter++], cancelChar = itemChars[charCounter++];

        MenuConverter.convertItem(cfg.getConfigurationSection("disband-gui.confirm"), patternChars, confirmChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("disband-gui.cancel"), patternChars, cancelChar,
                itemsSection, commandsSection, soundsSection);

        newMenu.set("confirm", confirmChar + "");
        newMenu.set("cancel", cancelChar + "");

        newMenu.set("pattern", MenuConverter.buildPattern(1, patternChars, itemChars[charCounter]));

        return true;
    }

}
