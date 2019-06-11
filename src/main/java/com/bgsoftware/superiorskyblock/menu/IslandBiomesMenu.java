package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class IslandBiomesMenu extends SuperiorMenu {

    private static Inventory inventory = null;

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

                if (superiorPlayer.hasPermission(permission) && slot == e.getRawSlot()) {
                    superiorPlayer.getIsland().setBiome(biome);
                    Locale.CHANGED_BIOME.send(superiorPlayer, biomeName);
                    break;
                }
            }
        }
    }

    @Override
    public void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu) {
        this.superiorPlayer = superiorPlayer;
        super.openInventory(superiorPlayer, previousMenu);
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, inventory.getSize(), inventory.getTitle());
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
            FileUtil.saveResource("guis/biomes-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtil.loadGUI(islandBiomesMenu, cfg.getConfigurationSection("biomes-gui"), 1, "&lSelect a biome");

        ConfigurationSection section = cfg.getConfigurationSection("biomes-gui.biomes");

        for(String biome : section.getKeys(false)){
            biome = biome.toLowerCase();
            biomesData.put(biome + "-permission", section.getString(biome + ".required-permission"));
            biomesData.put(biome + "-slot", section.getInt(biome + ".slot"));
            biomesData.put(biome + "-has-access-item",
                    FileUtil.getItemStack(section.getConfigurationSection(biome + ".has-access-item")));
            biomesData.put(biome + "-no-access-item",
                    FileUtil.getItemStack(section.getConfigurationSection(biome + ".no-access-item")));
        }
    }

    public static IslandBiomesMenu createInventory(){
        return new IslandBiomesMenu();
    }

}
