package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.events.IslandBiomeChangeEvent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class MenuBiomes extends SuperiorMenu {

    private MenuBiomes(SuperiorPlayer superiorPlayer){
        super("menuBiomes", superiorPlayer);
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        for(Biome biome : Biome.values()){
            String biomeName = biome.name().toLowerCase();
            if(containsData(biomeName + "-slot")) {
                int slot = (int) getData(biomeName + "-slot");
                String permission = (String) getData(biomeName + "-permission");

                if(slot == e.getRawSlot()){
                    if (superiorPlayer.hasPermission(permission)) {
                        IslandBiomeChangeEvent islandBiomeChangeEvent = new IslandBiomeChangeEvent(superiorPlayer, superiorPlayer.getIsland(), biome);
                        Bukkit.getPluginManager().callEvent(islandBiomeChangeEvent);

                        if(!islandBiomeChangeEvent.isCancelled()) {
                            SoundWrapper soundWrapper = (SoundWrapper) getData(biomeName + "-has-access-item-sound");
                            if (soundWrapper != null)
                                soundWrapper.playSound(superiorPlayer.asPlayer());
                            //noinspection unchecked
                            List<String> commands = (List<String>) getData(biomeName + "-has-access-item-commands");
                            if (commands != null)
                                commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));

                            superiorPlayer.getIsland().setBiome(islandBiomeChangeEvent.getBiome());
                            Locale.CHANGED_BIOME.send(superiorPlayer, islandBiomeChangeEvent.getBiome().name().toLowerCase());

                            Executor.sync(() -> {
                                previousMove = false;
                                superiorPlayer.asPlayer().closeInventory();
                            }, 1L);

                            break;
                        }
                    }

                    SoundWrapper soundWrapper = (SoundWrapper) getData(biomeName + "-no-access-item-sound");
                    if(soundWrapper != null)
                        soundWrapper.playSound(superiorPlayer.asPlayer());
                    //noinspection unchecked
                    List<String> commands = (List<String>) getData(biomeName + "-no-access-item-commands");
                    if(commands != null)
                        commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));

                    break;
                }
            }
        }
    }

    @Override
    protected Inventory buildInventory(Function<String, String> titleReplacer) {
        Inventory inv = super.buildInventory(titleReplacer);

        for(Biome biome : Biome.values()){
            String biomeName = biome.name().toLowerCase();
            if(containsData(biomeName + "-has-access-item")) {
                ItemBuilder biomeItem = (ItemBuilder) getData(biomeName + "-has-access-item");
                String permission = (String) getData(biomeName + "-permission");
                int slot = (int) getData(biomeName + "-slot");

                if(!superiorPlayer.hasPermission(permission))
                    biomeItem = (ItemBuilder) getData(biomeName + "-no-access-item");

                inv.setItem(slot, biomeItem.clone().build(superiorPlayer));
            }
        }

       return inv;
    }

    public static void init(){
        MenuBiomes menuBiomes = new MenuBiomes(null);

        File file = new File(plugin.getDataFolder(), "menus/biomes.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/biomes.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            cfg.save(file);
        }

        /*We must implement our own FileUtils.loadGUI for the menu, because of how complicated the menu is.*/

        menuBiomes.resetData();

        menuBiomes.setTitle(ChatColor.translateAlternateColorCodes('&', cfg.getString("title", "")));
        menuBiomes.setInventoryType(InventoryType.valueOf(cfg.getString("type", "CHEST")));

        List<String> pattern = cfg.getStringList("pattern");

        menuBiomes.setRowsSize(pattern.size());

        for(int row = 0; row < pattern.size(); row++){
            String patternLine = pattern.get(row);
            int slot = row * 9;

            for(int i = 0; i < patternLine.length(); i++){
                char ch = patternLine.charAt(i);
                if(ch != ' '){
                    if(cfg.contains("items." + ch + ".biome")){
                        ConfigurationSection itemSection = cfg.getConfigurationSection("items." + ch);
                        ConfigurationSection soundSection = cfg.getConfigurationSection("sounds." + ch);
                        ConfigurationSection commandSection = cfg.getConfigurationSection("commands." + ch);
                        String biome = itemSection.getString("biome").toLowerCase();

                        menuBiomes.addData(biome + "-slot", slot);

                        menuBiomes.addData(biome + "-permission", itemSection.getString("required-permission"));
                        menuBiomes.addData(biome + "-has-access-item", FileUtils.getItemStack("biomes.yml", itemSection.getConfigurationSection("access")));
                        menuBiomes.addData(biome + "-no-access-item", FileUtils.getItemStack("biomes.yml", itemSection.getConfigurationSection("no-access")));

                        if(soundSection != null) {
                            menuBiomes.addData(biome + "-has-access-item-sound", FileUtils.getSound(soundSection.getConfigurationSection("access")));
                            menuBiomes.addData(biome + "-no-access-item-sound", FileUtils.getSound(soundSection.getConfigurationSection("no-access")));
                        }
                        if(commandSection != null) {
                            menuBiomes.addData(biome + "-has-access-item-commands", commandSection.getStringList("access"));
                            menuBiomes.addData(biome + "-no-access-item-commands", commandSection.getStringList("no-access"));
                        }
                    }

                    else{
                        menuBiomes.addFillItem(slot,  FileUtils.getItemStack("biomes.yml", cfg.getConfigurationSection("items." + ch)));
                        menuBiomes.addCommands(slot, cfg.getStringList("commands." + ch));
                        menuBiomes.addSound(slot, FileUtils.getSound(cfg.getConfigurationSection("sounds." + ch)));
                    }

                    slot++;
                }
            }
        }
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new MenuBiomes(superiorPlayer).open(previousMenu);
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/biomes-gui.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("biomes-gui.title"));

        int size = cfg.getInt("biomes-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("biomes-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("biomes-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        if(cfg.contains("biomes-gui.biomes")) {
            for (String biomeName : cfg.getConfigurationSection("biomes-gui.biomes").getKeys(false)){
                ConfigurationSection section = cfg.getConfigurationSection("biomes-gui.biomes." + biomeName);
                char itemChar = itemChars[charCounter++];
                section.set("biome", biomeName.toUpperCase());
                MenuConverter.convertItemAccess(section, patternChars, itemChar, itemsSection, commandsSection, soundsSection);
            }
        }

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

}
