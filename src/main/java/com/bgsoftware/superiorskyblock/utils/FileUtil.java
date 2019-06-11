package com.bgsoftware.superiorskyblock.utils;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class FileUtil {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static boolean v1_13 = Bukkit.getBukkitVersion().contains("1.13");

    public static ItemStack getItemStack(ConfigurationSection section){
        if(section == null)
            return null;

        Material type;
        short data;

        try{
            type = Material.valueOf(section.getString("type"));
            data = (short) section.getInt("data");
        }catch(IllegalArgumentException ex){
            SuperiorSkyblockPlugin.log("Couldn't convert " + section.getCurrentPath() + " into an itemstack. Check type & database sections!");
            return null;
        }

        ItemBuilder itemBuilder = new ItemBuilder(type, data);

        if(section.contains("name"))
            itemBuilder.withName(ChatColor.translateAlternateColorCodes('&', section.getString("name")));

        if(section.contains("lore")){
            List<String> lore = new ArrayList<>();

            for(String line : section.getStringList("lore"))
                lore.add(ChatColor.translateAlternateColorCodes('&', line));

            itemBuilder.withLore(lore);
        }

        if(section.contains("enchants")){
            for(String _enchantment : section.getConfigurationSection("enchants").getKeys(false)) {
                Enchantment enchantment;

                try {
                    enchantment = Enchantment.getByName(_enchantment);
                } catch (Exception ex) {
                    SuperiorSkyblockPlugin.log("Couldn't convert " + section.getCurrentPath() + ".enchants." + _enchantment + " into an enchantment, skipping...");
                    continue;
                }

                itemBuilder.withEnchant(enchantment, section.getInt("enchants." + _enchantment));
            }
        }

        if(section.contains("flags")){
            for(String flag : section.getStringList("flags"))
                itemBuilder.withFlags(ItemFlag.valueOf(flag));
        }

        if(section.contains("skull")){
            itemBuilder.asSkullOf(section.getString("skull"));
        }

        return itemBuilder.build();
    }

    public static Inventory loadGUI(SuperiorMenu menu, ConfigurationSection section, int defaultSize, String defaultTitle){
        String title = ChatColor.translateAlternateColorCodes('&', section.getString("title", defaultTitle));
        int size = section.getInt("size", defaultSize);

        Sound openSound = getSound(section.getString("open-sound", ""));
        Sound closeSound = getSound(section.getString("close-sound", ""));

        Inventory inventory = Bukkit.createInventory(menu, 9 * size, title);

        if(section.contains("fill-items")){
            ConfigurationSection fillItems = section.getConfigurationSection("fill-items");
            for(String _name : fillItems.getKeys(false)){
                String[] slots = fillItems.getString(_name + ".slots").split(",");
                ItemStack fillItem = getItemStack(fillItems.getConfigurationSection(_name));
                for(String slot : slots)
                    inventory.setItem(Integer.valueOf(slot), fillItem);
            }
        }

        menu.setOpenSound(openSound);
        menu.setCloseSound(closeSound);

        return inventory;
    }

    public static String fromLocation(Location location){
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," +
                location.getYaw() + "," + location.getPitch();
    }

    public static Location toLocation(String location){
        String[] sections = location.split(",");
        return new Location(Bukkit.getWorld(sections[0]), Double.valueOf(sections[1]), Double.valueOf(sections[2]),
                Double.valueOf(sections[3]), Float.valueOf(sections[4]), Float.valueOf(sections[5]));
    }

    public static void saveResource(String resourcePath){
        try {
            String destination = resourcePath;

            if(v1_13) resourcePath = resourcePath.replace(".yml", "1_13.yml");

            File file = new File(plugin.getDataFolder(), resourcePath);
            plugin.saveResource(resourcePath, true);

            if(!destination.equals(resourcePath)){
                File dest = new File(plugin.getDataFolder(), destination);
                //noinspection ResultOfMethodCallIgnored
                file.renameTo(dest);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private static Sound getSound(String sound){
        try{
            return Sound.valueOf(sound);
        }catch(IllegalArgumentException ex){
            return null;
        }
    }

}
