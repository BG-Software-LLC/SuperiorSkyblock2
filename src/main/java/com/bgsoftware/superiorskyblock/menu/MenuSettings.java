package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandSettings;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MenuSettings extends PagedSuperiorMenu<IslandSettings> {

    private static List<IslandSettings> islandSettings = new ArrayList<>();

    private final Island island;

    private MenuSettings(SuperiorPlayer superiorPlayer, Island island){
        super("menuSettings", superiorPlayer);
        this.island = island;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, IslandSettings settings) {
        String settingsName = settings.name().toLowerCase();

        if(!containsData(settingsName + "-settings-enabled"))
            return;

        if(island.hasSettingsEnabled(settings)){
            island.disableSettings(settings);
        }
        else{
            island.enableSettings(settings);
        }

        Locale.UPDATED_SETTINGS.send(superiorPlayer, StringUtils.format(settingsName));

        SoundWrapper soundWrapper = (SoundWrapper) getData(settingsName + "-sound");
        if (soundWrapper != null)
            soundWrapper.playSound(superiorPlayer.asPlayer());
        //noinspection unchecked
        List<String> commands = (List<String>) getData(settingsName + "-commands");
        if (commands != null)
            commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));

        previousMove = false;
        open(previousMenu);
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, IslandSettings settings) {
        String settingsName = settings.name().toLowerCase();
        return (containsData(settingsName + "-settings-enabled") ?
                (ItemBuilder) getData(settingsName + "-settings-" + (island.hasSettingsEnabled(settings) ? "enabled" : "disabled")) :
                new ItemBuilder(Material.AIR)
        ).clone().build(superiorPlayer);
    }

    @Override
    protected List<IslandSettings> requestObjects() {
        return islandSettings;
    }

    public static void init(){
        MenuSettings menuSettings = new MenuSettings(null, null);

        File file = new File(plugin.getDataFolder(), "menus/settings.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/settings.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            cfg.save(file);
        }

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuSettings, "settings.yml", cfg);

        islandSettings = new ArrayList<>();

        ConfigurationSection settingsSection = cfg.getConfigurationSection("settings");

        for(IslandSettings islandSettings : IslandSettings.values()){
            String settings = islandSettings.name().toLowerCase();
            if(settingsSection.contains(settings)){
                ConfigurationSection section = settingsSection.getConfigurationSection(settings);
                menuSettings.addData(settings + "-sound", FileUtils.getSound(section.getConfigurationSection("sound")));
                menuSettings.addData(settings + "-commands", section.getStringList("commands"));
                menuSettings.addData(settings + "-settings-enabled", FileUtils.getItemStack("settings.yml", section.getConfigurationSection("settings-enabled")));
                menuSettings.addData(settings + "-settings-disabled", FileUtils.getItemStack("settings.yml", section.getConfigurationSection("settings-disabled")));
                MenuSettings.islandSettings.add(islandSettings);
            }
        }

        menuSettings.setPreviousSlot(charSlots.getOrDefault(cfg.getString("previous-page", " ").charAt(0), Collections.singletonList(-1)).get(0));
        menuSettings.setCurrentSlot(charSlots.getOrDefault(cfg.getString("current-page", " ").charAt(0), Collections.singletonList(-1)).get(0));
        menuSettings.setNextSlot(charSlots.getOrDefault(cfg.getString("next-page", " ").charAt(0), Collections.singletonList(-1)).get(0));
        menuSettings.setSlots(charSlots.getOrDefault(cfg.getString("slots", " ").charAt(0), Collections.singletonList(-1)));
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuSettings(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(){
        SuperiorMenu.refreshMenus(MenuSettings.class);
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/settings-gui.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("settings-gui.title"));

        int size = cfg.getInt("settings-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("settings-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("settings-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = itemChars[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("settings-gui"), newMenu,
                patternChars, slotsChar, itemChars[charCounter++], itemChars[charCounter++], itemChars[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("settings", cfg.getConfigurationSection("settings-gui.settings"));
        newMenu.set("sounds", null);
        newMenu.set("commands", null);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

}
