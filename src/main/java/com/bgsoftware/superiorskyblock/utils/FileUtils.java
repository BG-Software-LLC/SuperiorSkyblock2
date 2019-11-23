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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class FileUtils {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static ItemStack getItemStack(ConfigurationSection section){
        if(section == null)
            return null;

        Material type;
        short data;

        try{
            type = Material.valueOf(section.getString("type"));
            data = (short) section.getInt("data");
        }catch(IllegalArgumentException ex){
            SuperiorSkyblockPlugin.log("&cCouldn't convert " + section.getCurrentPath() + " into an itemstack. Check type & database sections!");
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
                    SuperiorSkyblockPlugin.log("&cCouldn't convert " + section.getCurrentPath() + ".enchants." + _enchantment + " into an enchantment, skipping...");
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

        return itemBuilder.build();
    }

    public static Inventory loadGUI(SuperiorMenu menu, ConfigurationSection section, InventoryType inventoryType, String defaultTitle){
        String title = ChatColor.translateAlternateColorCodes('&', section.getString("title", defaultTitle));
        Inventory inventory = Bukkit.createInventory(menu, inventoryType, title);

        if(inventory.getHolder() == null){
            try{
                Class craftInventoryClass = ReflectionUtils.getClass("org.bukkit.craftbukkit.VERSION.inventory.CraftInventory");
                //noinspection all
                Field field = craftInventoryClass.getDeclaredField("inventory");
                field.setAccessible(true);
                field.set(inventory, plugin.getNMSAdapter().getCustomHolder(menu, title));
                field.setAccessible(false);
            }catch(Exception ignored){}
        }

        return loadGUI(menu, inventory, section);
    }

    public static Inventory loadGUI(SuperiorMenu menu, ConfigurationSection section, int defaultSize, String defaultTitle){
        String title = ChatColor.translateAlternateColorCodes('&', section.getString("title", defaultTitle));
        int size = section.getInt("size", defaultSize);
        Inventory inventory = Bukkit.createInventory(menu, 9 * size, title);
        return loadGUI(menu, inventory, section);
    }

    private static Inventory loadGUI(SuperiorMenu menu, Inventory inventory, ConfigurationSection section){
        if(section.contains("fill-items")){
            ConfigurationSection fillItems = section.getConfigurationSection("fill-items");
            for(String _name : fillItems.getKeys(false)){
                String[] slots = fillItems.getString(_name + ".slots").split(",");
                ItemStack fillItem = getItemStack(fillItems.getConfigurationSection(_name));
                List<String> commands = fillItems.getStringList(_name + ".commands");
                for(String _slot : slots) {
                    int slot = Integer.parseInt(_slot);
                    inventory.setItem(slot, fillItem);
                    if(!commands.isEmpty())
                        menu.addCommands(slot, commands);
                }
            }
        }

        return inventory;
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

            if(ServerVersion.isEquals(ServerVersion.v1_14))
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
