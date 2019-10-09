package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IslandBiomesMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static String title = "";

    private static Map<String, Object> biomesData = new HashMap<>();

    private SuperiorPlayer superiorPlayer;

    private IslandBiomesMenu(){
        super("biomesPage");
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        for(Biome biome : Biome.values()){
            String biomeName = biome.name().toLowerCase();
            if(biomesData.containsKey(biomeName + "-slot")) {
                int slot = get(biomeName + "-slot", Integer.class);
                String permission = get(biomeName + "-permission", String.class);

                if(slot == e.getRawSlot()){
                    if (superiorPlayer.hasPermission(permission)) {
                        SoundWrapper soundWrapper = get(biomeName + "-has-access-item-sound", SoundWrapper.class);
                        if(soundWrapper != null)
                            soundWrapper.playSound(superiorPlayer.asPlayer());
                        //noinspection unchecked
                        List<String> commands = get(biomeName + "-has-access-item-commands", List.class);
                        if(commands != null)
                            commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));
                        superiorPlayer.getIsland().setBiome(biome);
                        Locale.CHANGED_BIOME.send(superiorPlayer, biomeName);
                        break;
                    }
                    else{
                        SoundWrapper soundWrapper = get(biomeName + "-no-access-item-sound", SoundWrapper.class);
                        if(soundWrapper != null)
                            soundWrapper.playSound(superiorPlayer.asPlayer());
                        //noinspection unchecked
                        List<String> commands = get(biomeName + "-no-access-item-commands", List.class);
                        if(commands != null)
                            commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));
                    }
                }
            }
        }
    }

    @Override
    public void open(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu) {
        this.superiorPlayer = superiorPlayer;
        super.open(superiorPlayer, previousMenu);
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, inventory.getSize(), title);
        inv.setContents(inventory.getContents());

        for(Biome biome : Biome.values()){
            String biomeName = biome.name().toLowerCase();
            if(biomesData.containsKey(biomeName + "-has-access-item")) {
                ItemStack biomeItem = get(biomeName + "-has-access-item", ItemStack.class);
                String permission = get(biomeName + "-permission", String.class);
                int slot = get(biomeName + "-slot", Integer.class);

                if(!superiorPlayer.hasPermission(permission))
                    biomeItem = get(biomeName + "-no-access-item", ItemStack.class);

                inv.setItem(slot, biomeItem);
            }
        }

       return inv;
    }

    private static <T> T get(String key, Class<T> type){
        return type.cast(biomesData.get(key));
    }

    public static void init(){
        IslandBiomesMenu islandBiomesMenu = new IslandBiomesMenu();
        File file = new File(plugin.getDataFolder(), "guis/biomes-gui.yml");

        if(!file.exists())
            FileUtils.saveResource("guis/biomes-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        title = ChatColor.translateAlternateColorCodes('&', cfg.getString("biomes-gui.title", "Select a biome"));
        inventory = FileUtils.loadGUI(islandBiomesMenu, cfg.getConfigurationSection("biomes-gui"), 1, "Select a biome");

        ConfigurationSection section = cfg.getConfigurationSection("biomes-gui.biomes");

        for(String biome : section.getKeys(false)){
            biome = biome.toLowerCase();
            biomesData.put(biome + "-permission", section.getString(biome + ".required-permission"));
            biomesData.put(biome + "-slot", section.getInt(biome + ".slot"));
            biomesData.put(biome + "-has-access-item",
                    FileUtils.getItemStack(section.getConfigurationSection(biome + ".has-access-item")));
            biomesData.put(biome + "-has-access-item-sound",
                    FileUtils.getSound(section.getConfigurationSection(biome + ".has-access-item.sound")));
            biomesData.put(biome + "-has-access-item-commands", section.getStringList(biome + ".has-access-item.commands"));
            biomesData.put(biome + "-no-access-item",
                    FileUtils.getItemStack(section.getConfigurationSection(biome + ".no-access-item")));
            biomesData.put(biome + "-no-access-item-sound",
                    FileUtils.getSound(section.getConfigurationSection(biome + ".no-access-item.sound")));
            biomesData.put(biome + "-no-access-item-commands", section.getStringList(biome + ".no-access-item.commands"));
        }
    }

    public static SuperiorMenu getMenu(){
        return new IslandBiomesMenu();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new IslandBiomesMenu().open(superiorPlayer, previousMenu);
    }

}
