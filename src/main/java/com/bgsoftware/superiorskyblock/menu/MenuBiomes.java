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
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

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
                        SoundWrapper soundWrapper = (SoundWrapper) getData(biomeName + "-has-access-item-sound");
                        if(soundWrapper != null)
                            soundWrapper.playSound(superiorPlayer.asPlayer());
                        //noinspection unchecked
                        List<String> commands = (List<String>) getData(biomeName + "-has-access-item-commands");
                        if(commands != null)
                            commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));

                        superiorPlayer.getIsland().setBiome(biome);
                        Locale.CHANGED_BIOME.send(superiorPlayer, biomeName);

                        previousMove = false;
                        superiorPlayer.asPlayer().closeInventory();

                        break;
                    }
                    else{
                        SoundWrapper soundWrapper = (SoundWrapper) getData(biomeName + "-no-access-item-sound");
                        if(soundWrapper != null)
                            soundWrapper.playSound(superiorPlayer.asPlayer());
                        //noinspection unchecked
                        List<String> commands = (List<String>) getData(biomeName + "-no-access-item-commands");
                        if(commands != null)
                            commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));
                    }
                }
            }
        }
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = super.getInventory();

        for(Biome biome : Biome.values()){
            String biomeName = biome.name().toLowerCase();
            if(containsData(biomeName + "-has-access-item")) {
                ItemStack biomeItem = (ItemStack) getData(biomeName + "-has-access-item");
                String permission = (String) getData(biomeName + "-permission");
                int slot = (int) getData(biomeName + "-slot");

                if(!superiorPlayer.hasPermission(permission))
                    biomeItem = (ItemStack) getData(biomeName + "-no-access-item");

                inv.setItem(slot, new ItemBuilder(biomeItem).build(superiorPlayer));
            }
        }

       return inv;
    }

    public static void init(){
        MenuBiomes menuBiomes = new MenuBiomes(null);

        File file = new File(plugin.getDataFolder(), "menus/biomes.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/biomes.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

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
                        menuBiomes.addData(biome + "-has-access-item", FileUtils.getItemStack(itemSection.getConfigurationSection("access")));
                        menuBiomes.addData(biome + "-no-access-item", FileUtils.getItemStack(itemSection.getConfigurationSection("no-access")));

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
                        menuBiomes.addFillItem(slot,  FileUtils.getItemStack(cfg.getConfigurationSection("items." + ch)));
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

}
