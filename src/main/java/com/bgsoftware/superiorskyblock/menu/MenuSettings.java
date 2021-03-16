package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
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
import java.util.List;

public final class MenuSettings extends PagedSuperiorMenu<IslandFlag> {

    private static List<IslandFlag> islandSettings = new ArrayList<>();

    private final Island island;

    private MenuSettings(SuperiorPlayer superiorPlayer, Island island){
        super("menuSettings", superiorPlayer);
        this.island = island;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, IslandFlag islandFlag) {
        String settingsName = islandFlag.getName().toLowerCase();

        if(!containsData(settingsName + "-settings-enabled"))
            return;

        String permission = (String) getData(settingsName + "-permission");
        SoundWrapper noAccessSound = (SoundWrapper) getData(settingsName + "-no-access-sound");
        if(!permission.isEmpty() && !superiorPlayer.hasPermission(permission)){
            if(noAccessSound != null)
                noAccessSound.playSound(event.getWhoClicked());
            return;
        }

        if(island.hasSettingsEnabled(islandFlag)){
            island.disableSettings(islandFlag);
        }
        else{
            island.enableSettings(islandFlag);
        }

        Locale.UPDATED_SETTINGS.send(superiorPlayer, StringUtils.format(settingsName));

        SoundWrapper soundWrapper = (SoundWrapper) getData(settingsName + "-sound");
        if (soundWrapper != null)
            soundWrapper.playSound(event.getWhoClicked());
        //noinspection unchecked
        List<String> commands = (List<String>) getData(settingsName + "-commands");
        if (commands != null)
            commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));

        previousMove = false;
        open(previousMenu);
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, island);
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, IslandFlag islandFlag) {
        try {
            String settingsName = islandFlag.getName().toLowerCase();
            return (containsData(settingsName + "-settings-enabled") ?
                    (ItemBuilder) getData(settingsName + "-settings-" + (island.hasSettingsEnabled(islandFlag) ? "enabled" : "disabled")) :
                    new ItemBuilder(Material.AIR)
            ).clone().build(superiorPlayer);
        }catch(Exception ex){
            SuperiorSkyblockPlugin.log("Failed to load menu because of flag: " + islandFlag.getName());
            throw ex;
        }
    }

    @Override
    protected List<IslandFlag> requestObjects() {
        return islandSettings;
    }

    public static void init(){
        MenuSettings menuSettings = new MenuSettings(null, null);

        File file = new File(plugin.getDataFolder(), "menus/settings.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/settings.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            try {
                cfg.save(file);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuSettings, "settings.yml", cfg);

        ConfigurationSection settingsSection = cfg.getConfigurationSection("settings");

        islandSettings = new ArrayList<>();
        int position = 0;

        for(String key : settingsSection.getKeys(false)){
            try {
                updateSettings(IslandFlag.getByName(key.toLowerCase()), cfg, position++);
            }catch (Exception ignored){}
        }

        menuSettings.setPreviousSlot(getSlots(cfg, "previous-page", charSlots));
        menuSettings.setCurrentSlot(getSlots(cfg, "current-page", charSlots));
        menuSettings.setNextSlot(getSlots(cfg, "next-page", charSlots));
        menuSettings.setSlots(getSlots(cfg, "slots", charSlots));

        charSlots.delete();

        menuSettings.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuSettings(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island){
        SuperiorMenu.refreshMenus(MenuSettings.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    public static void updateSettings(IslandFlag islandFlag){
        File file = new File(plugin.getDataFolder(), "menus/settings.yml");
        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        int position = 0;

        for(String key : cfg.getConfigurationSection("settings").getKeys(false)){
            if(islandFlag.getName().equalsIgnoreCase(key))
                break;

            position++;
        }

        updateSettings(islandFlag, cfg, position);
    }

    public static void updateSettings(IslandFlag islandFlag, YamlConfiguration cfg, int position){
        if(!islandSettings.contains(islandFlag)) {
            String settings = islandFlag.getName().toLowerCase();
            if (cfg.contains("settings." + settings)) {
                ConfigurationSection section = cfg.getConfigurationSection("settings." + settings);
                MenuSettings menuSettings = new MenuSettings(null, null);
                menuSettings.addData(settings + "-sound", FileUtils.getSound(section.getConfigurationSection("sound")));
                menuSettings.addData(settings + "-commands", section.getStringList("commands"));
                menuSettings.addData(settings + "-permission", section.getString("required-permission", ""));
                menuSettings.addData(settings + "-no-access-sound", FileUtils.getSound(section.getConfigurationSection("no-access-sound")));
                menuSettings.addData(settings + "-settings-enabled", FileUtils.getItemStack("settings.yml", section.getConfigurationSection("settings-enabled")));
                menuSettings.addData(settings + "-settings-disabled", FileUtils.getItemStack("settings.yml", section.getConfigurationSection("settings-disabled")));
                if(position >= 0)
                    islandSettings.add(position, islandFlag);
                else
                    islandSettings.add(islandFlag);
            }
        }
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
