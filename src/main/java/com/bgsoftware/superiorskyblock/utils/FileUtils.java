package com.bgsoftware.superiorskyblock.utils;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.utils.items.EnchantsUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FileUtils {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static ItemBuilder getItemStack(String fileName, ConfigurationSection section){
        if(section == null || !section.contains("type"))
            return null;

        Material type;
        short data;

        try{
            type = Material.valueOf(section.getString("type"));
            data = (short) section.getInt("data");
        }catch(IllegalArgumentException ex){
            SuperiorSkyblockPlugin.log("&c[" + fileName + "] Couldn't convert " + section.getCurrentPath() + " into an itemstack. Check type & database sections!");
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
                    SuperiorSkyblockPlugin.log("&c[" + fileName + "] Couldn't convert " + section.getCurrentPath() + ".enchants." + _enchantment + " into an enchantment, skipping...");
                    continue;
                }

                itemBuilder.withEnchant(enchantment, section.getInt("enchants." + _enchantment));
            }
        }

        if(section.getBoolean("glow", false)){
            itemBuilder.withEnchant(EnchantsUtils.getGlowEnchant(), 1);
        }

        if(section.contains("flags")){
            for(String flag : section.getStringList("flags"))
                itemBuilder.withFlags(ItemFlag.valueOf(flag));
        }

        if(section.contains("skull")){
            itemBuilder.asSkullOf(section.getString("skull"));
        }

        return itemBuilder;
    }

    public static Map<Character, List<Integer>> loadGUI(SuperiorMenu menu, String fileName, YamlConfiguration cfg){
        Map<Character, List<Integer>> charSlots = new HashMap<>();

        menu.resetData();

        menu.setTitle(ChatColor.translateAlternateColorCodes('&', cfg.getString("title", "")));
        menu.setInventoryType(InventoryType.valueOf(cfg.getString("type", "CHEST")));

        List<String> pattern = cfg.getStringList("pattern");

        menu.setRowsSize(pattern.size());

        for(int row = 0; row < pattern.size(); row++){
            String patternLine = pattern.get(row);
            int slot = row * 9;

            for(int i = 0; i < patternLine.length(); i++){
                char ch = patternLine.charAt(i);
                if(ch != ' '){
                    ItemBuilder itemBuilder = getItemStack(fileName, cfg.getConfigurationSection("items." + ch));

                    if(itemBuilder != null) {
                        List<String> commands = cfg.getStringList("commands." + ch);
                        SoundWrapper sound = getSound(cfg.getConfigurationSection("sounds." + ch));

                        menu.addFillItem(slot, itemBuilder);
                        menu.addCommands(slot, commands);
                        menu.addSound(slot, sound);
                    }

                    if(!charSlots.containsKey(ch))
                        charSlots.put(ch, new ArrayList<>());

                    charSlots.get(ch).add(slot);

                    slot++;
                }
            }
        }

        return charSlots;
    }

    public static String fromLocation(Location location){
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," +
                location.getYaw() + "," + location.getPitch();
    }

    public static Location toLocation(String location){
        String[] sections = location.split(",");
        return new Location(Bukkit.getWorld(sections[0]), Double.parseDouble(sections[1]), Double.parseDouble(sections[2]),
                Double.parseDouble(sections[3]), Float.parseFloat(sections[4]), Float.parseFloat(sections[5]));
    }

    public static void saveResource(String resourcePath){
        try {
            String destination = resourcePath;

            if(ServerVersion.isEquals(ServerVersion.v1_15))
                resourcePath = resourcePath.replace(".yml", "1_13.yml")
                        .replace(".schematic", "1_15.schematic");
            else if(ServerVersion.isEquals(ServerVersion.v1_14))
                resourcePath = resourcePath.replace(".yml", "1_13.yml")
                        .replace(".schematic", "1_14.schematic");
            else if(!ServerVersion.isLegacy())
                resourcePath = resourcePath.replace(".yml", "1_13.yml")
                    .replace(".schematic", "1_13.schematic");

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

    public static SoundWrapper getSound(ConfigurationSection section){
        Sound sound = null;

        try{
            sound = Sound.valueOf(section.getString("type"));
        }catch(Exception ignored){}

        if(sound == null)
            return null;

        return new SoundWrapper(sound, (float) section.getDouble("volume"), (float) section.getDouble("pitch"));
    }

}
