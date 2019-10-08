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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IslandCreationMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static String title = "";

    private static Map<String, Object> schematicsData = new HashMap<>();

    private SuperiorPlayer superiorPlayer;
    private String islandName;

    private IslandCreationMenu(String islandName){
        super("islandCreationPage");
        this.islandName = islandName;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        for(String schematic : plugin.getSchematics().getSchematics()){
            if(schematicsData.containsKey(schematic + "-slot")) {
                int slot = get(schematic + "-slot", Integer.class);
                String permission = get(schematic + "-permission", String.class);

                if(slot == e.getRawSlot()) {
                    if (superiorPlayer.hasPermission(permission)) {
                        BigDecimal bonusWorth = new BigDecimal(get(schematic + "-bonus", Long.class));
                        Biome biome = Biome.valueOf(get(schematic + "-biome", String.class));
                        superiorPlayer.asPlayer().closeInventory();
                        SoundWrapper sound = get(schematic + "-has-access-item-sound", SoundWrapper.class);
                        if(sound != null)
                            sound.playSound(superiorPlayer.asPlayer());
                        //noinspection unchecked
                        List<String> commands = get(schematic + "-has-access-item-commands", List.class);
                        if(commands != null)
                            commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));
                        Locale.ISLAND_CREATE_PROCCESS_REQUEST.send(superiorPlayer);
                        plugin.getGrid().createIsland(superiorPlayer, schematic, bonusWorth, biome, islandName);
                        break;
                    }
                    else{
                        SoundWrapper sound = get(schematic + "-no-access-item-sound", SoundWrapper.class);
                        if(sound != null)
                            sound.playSound(superiorPlayer.asPlayer());
                        //noinspection unchecked
                        List<String> commands = get(schematic + "-no-access-item-commands", List.class);
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

        for(String schematic : plugin.getSchematics().getSchematics()){
            if(schematicsData.containsKey(schematic + "-has-access-item")) {
                String permission = get(schematic + "-permission", String.class);
                String schematicItemKey = superiorPlayer.hasPermission(permission) ? schematic + "-has-access-item" : schematic + "-no-access-item";
                ItemStack schematicItem = get(schematicItemKey, ItemStack.class);
                int slot = get(schematic + "-slot", Integer.class);
                inv.setItem(slot, schematicItem);
            }
        }

       return inv;
    }

    private static <T> T get(String key, Class<T> type){
        return type.cast(schematicsData.get(key));
    }

    public static void init(){
        IslandCreationMenu islandCreationMenu = new IslandCreationMenu("");
        File file = new File(plugin.getDataFolder(), "guis/creation-gui.yml");

        if(!file.exists())
            FileUtils.saveResource("guis/creation-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtils.loadGUI(islandCreationMenu, cfg.getConfigurationSection("creation-gui"), 1, "&lCreate a new island...");
        title = ChatColor.translateAlternateColorCodes('&', cfg.getString("creation-gui.title"));

        ConfigurationSection section = cfg.getConfigurationSection("creation-gui.schematics");

        for(String schematic : section.getKeys(false)){
            schematicsData.put(schematic + "-permission", section.getString(schematic + ".required-permission"));
            schematicsData.put(schematic + "-bonus", section.getLong(schematic + ".bonus-worth", 0));
            schematicsData.put(schematic + "-biome", section.getString(schematic + ".biome", "PLAINS"));
            schematicsData.put(schematic + "-slot", section.getInt(schematic + ".slot"));
            schematicsData.put(schematic + "-has-access-item",
                    FileUtils.getItemStack(section.getConfigurationSection(schematic + ".has-access-item")));
            schematicsData.put(schematic + "-has-access-item-sound",
                    FileUtils.getSound(section.getConfigurationSection(schematic + ".has-access-item.sound")));
            schematicsData.put(schematic + "-has-access-item-commands", section.getStringList(schematic + ".has-access-item.commands"));
            schematicsData.put(schematic + "-no-access-item",
                    FileUtils.getItemStack(section.getConfigurationSection(schematic + ".no-access-item")));
            schematicsData.put(schematic + "-no-access-item-sound",
                    FileUtils.getSound(section.getConfigurationSection(schematic + ".no-access-item.sound")));
            schematicsData.put(schematic + "-no-access-item-commands", section.getStringList(schematic + ".no-access-item.commands"));
        }
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, String islandName){
        new IslandCreationMenu(islandName).open(superiorPlayer, previousMenu);
    }

}
