package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
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
import java.math.BigDecimal;
import java.util.List;

public final class MenuIslandCreation extends SuperiorMenu {

    private String islandName;

    private MenuIslandCreation(SuperiorPlayer superiorPlayer, String islandName){
        super("menuIslandCreation", superiorPlayer);
        this.islandName = islandName;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        for(String schematic : plugin.getSchematics().getSchematics()){
            if(containsData(schematic + "-slot")) {
                int slot = (int) getData(schematic + "-slot");
                if(slot == e.getRawSlot()) {
                    clickSchematic(superiorPlayer, schematic);
                    break;
                }
            }
        }
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = super.getInventory();

        for(String schematic : plugin.getSchematics().getSchematics()){
            if(containsData(schematic + "-has-access-item")) {
                String permission = (String) getData(schematic + "-permission");
                String schematicItemKey = superiorPlayer.hasPermission(permission) ? schematic + "-has-access-item" : schematic + "-no-access-item";
                ItemBuilder schematicItem = (ItemBuilder) getData(schematicItemKey);
                int slot = (int) getData(schematic + "-slot");
                inv.setItem(slot, schematicItem.clone().build(superiorPlayer));
            }
        }

       return inv;
    }

    private void clickSchematic(SuperiorPlayer superiorPlayer, String schematic){
        String permission = (String) getData(schematic + "-permission");
        if (superiorPlayer.hasPermission(permission)) {
            BigDecimal bonusWorth = new BigDecimal((long) getData(schematic + "-bonus"));
            Biome biome = Biome.valueOf((String) getData(schematic + "-biome"));
            SoundWrapper sound = (SoundWrapper) getData(schematic + "-has-access-item-sound");
            if(sound != null)
                sound.playSound(superiorPlayer.asPlayer());
            //noinspection unchecked
            List<String> commands = (List<String>) getData(schematic + "-has-access-item-commands");
            if(commands != null)
                commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));

            previousMove = false;
            superiorPlayer.asPlayer().closeInventory();

            Locale.ISLAND_CREATE_PROCCESS_REQUEST.send(superiorPlayer);
            plugin.getGrid().createIsland(superiorPlayer, schematic, bonusWorth, biome, islandName);
        }
        else{
            SoundWrapper sound = (SoundWrapper) getData(schematic + "-no-access-item-sound");
            if(sound != null)
                sound.playSound(superiorPlayer.asPlayer());
            //noinspection unchecked
            List<String> commands = (List<String>) getData(schematic + "-no-access-item-commands");
            if(commands != null)
                commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));
        }
    }

    private boolean hasOnlyOneItem(){
        return plugin.getSchematics().getSchematics().stream()
                .filter(schematic ->  containsData(schematic + "-has-access-item"))
                .count() == 1;
    }

    private String getOnlyOneItem(){
        return plugin.getSchematics().getSchematics().stream()
                .filter(schematic ->  containsData(schematic + "-has-access-item"))
                .findFirst().orElse(null);
    }

    public static void init(){
        MenuIslandCreation menuIslandCreation = new MenuIslandCreation(null, "");

        File file = new File(plugin.getDataFolder(), "menus/island-creation.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/island-creation.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        /*We must implement our own FileUtils.loadGUI for the menu, because of how complicated the menu is.*/

        menuIslandCreation.resetData();

        menuIslandCreation.setTitle(ChatColor.translateAlternateColorCodes('&', cfg.getString("title", "")));
        menuIslandCreation.setInventoryType(InventoryType.valueOf(cfg.getString("type", "CHEST")));

        List<String> pattern = cfg.getStringList("pattern");

        menuIslandCreation.setRowsSize(pattern.size());

        for(int row = 0; row < pattern.size(); row++){
            String patternLine = pattern.get(row);
            int slot = row * 9;

            for(int i = 0; i < patternLine.length(); i++){
                char ch = patternLine.charAt(i);
                if(ch != ' '){
                    if(cfg.contains("items." + ch + ".schematic")){
                        ConfigurationSection itemSection = cfg.getConfigurationSection("items." + ch);
                        ConfigurationSection soundSection = cfg.getConfigurationSection("sounds." + ch);
                        ConfigurationSection commandSection = cfg.getConfigurationSection("commands." + ch);
                        String schematic = itemSection.getString("schematic").toLowerCase();

                        menuIslandCreation.addData(schematic + "-slot", slot);
                        menuIslandCreation.addData(schematic + "-permission", itemSection.getString("required-permission"));
                        menuIslandCreation.addData(schematic + "-biome", itemSection.getString("biome", "PLAINS"));
                        menuIslandCreation.addData(schematic + "-bonus", itemSection.getLong("bonus", 0));
                        menuIslandCreation.addData(schematic + "-has-access-item", FileUtils.getItemStack("island-creation.yml", itemSection.getConfigurationSection("access")));
                        menuIslandCreation.addData(schematic + "-no-access-item", FileUtils.getItemStack("island-creation.yml", itemSection.getConfigurationSection("no-access")));

                        if(soundSection != null) {
                            menuIslandCreation.addData(schematic + "-has-access-item-sound", FileUtils.getSound(soundSection.getConfigurationSection("access")));
                            menuIslandCreation.addData(schematic + "-no-access-item-sound", FileUtils.getSound(soundSection.getConfigurationSection("no-access")));
                        }
                        if(commandSection != null) {
                            menuIslandCreation.addData(schematic + "-has-access-item-commands", commandSection.getStringList("access"));
                            menuIslandCreation.addData(schematic + "-no-access-item-commands", commandSection.getStringList("no-access"));
                        }
                    }

                    else{
                        menuIslandCreation.addFillItem(slot,  FileUtils.getItemStack("island-creation.yml", cfg.getConfigurationSection("items." + ch)));
                        menuIslandCreation.addCommands(slot, cfg.getStringList("commands." + ch));
                        menuIslandCreation.addSound(slot, FileUtils.getSound(cfg.getConfigurationSection("sounds." + ch)));
                    }

                    slot++;
                }
            }
        }
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, String islandName){
        MenuIslandCreation menuIslandCreation = new MenuIslandCreation(superiorPlayer, islandName);
        if(plugin.getSettings().skipOneItemMenus && menuIslandCreation.hasOnlyOneItem()){
            menuIslandCreation.clickSchematic(superiorPlayer, menuIslandCreation.getOnlyOneItem());
        }
        else {
            menuIslandCreation.open(previousMenu);
        }
    }

}
