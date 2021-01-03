package com.bgsoftware.superiorskyblock.utils;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.utils.items.EnchantsUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public final class FileUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private FileUtils(){

    }

    public static ItemBuilder getItemStack(String fileName, ConfigurationSection section){
        if(section == null || !section.contains("type"))
            return null;

        Material type;
        short data;

        try{
            type = Material.valueOf(section.getString("type"));
            data = (short) section.getInt("data");
        }catch(IllegalArgumentException ex){
            SuperiorSkyblockPlugin.log("&c[" + fileName + "] Couldn't convert " + section.getCurrentPath() + " into an itemstack. Check type & data sections!");
            return null;
        }

        ItemBuilder itemBuilder = new ItemBuilder(type, data);

        if(section.contains("name"))
            itemBuilder.withName(StringUtils.translateColors(section.getString("name")));

        if(section.contains("lore"))
            itemBuilder.withLore(section.getStringList("lore"));

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

        if(section.getBoolean("unbreakable", false)){
            itemBuilder.setUnbreakable();
        }

        if(section.contains("effects")){
            ConfigurationSection effectsSection = section.getConfigurationSection("effects");
            for(String _effect : effectsSection.getKeys(false)) {
                PotionEffectType potionEffectType = PotionEffectType.getByName(_effect);

                if(potionEffectType == null){
                    SuperiorSkyblockPlugin.log("&c[" + fileName + "] Couldn't convert " + effectsSection.getCurrentPath() + "." + _effect + " into a potion effect, skipping...");
                    continue;
                }

                int duration = effectsSection.getInt(_effect + ".duration", -1);
                int amplifier = effectsSection.getInt(_effect + ".amplifier", 0);

                if(duration == -1){
                    SuperiorSkyblockPlugin.log("&c[" + fileName + "] Potion effect " + effectsSection.getCurrentPath() + "." + _effect + " is missing duration, skipping...");
                    continue;
                }

                itemBuilder.withPotionEffect(new PotionEffect(potionEffectType, duration, amplifier));
            }
        }

        if(section.contains("entity")){
            String entity = section.getString("entity");
            try{
                itemBuilder.withEntityType(EntityType.valueOf(entity.toUpperCase()));
            }catch (IllegalArgumentException ex){
                SuperiorSkyblockPlugin.log("&c[" + fileName + "] Couldn't convert " + entity + " into an entity type, skipping...");
            }
        }

        if(section.contains("customModel")){
            itemBuilder.withCustomModel(section.getInt("customModel"));
        }

        return itemBuilder;
    }

    public static Registry<Character, List<Integer>> loadGUI(SuperiorMenu menu, String fileName, YamlConfiguration cfg){
        Registry<Character, List<Integer>> charSlots = Registry.createRegistry();

        menu.resetData();

        menu.setTitle(StringUtils.translateColors(cfg.getString("title", "")));
        menu.setInventoryType(InventoryType.valueOf(cfg.getString("type", "CHEST")));
        menu.setPreviousMoveAllowed(cfg.getBoolean("previous-menu", true));
        menu.setOpeningSound(FileUtils.getSound(cfg.getConfigurationSection("open-sound")));

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
                        String permission = cfg.getString("permissions." + ch + ".permission");
                        SoundWrapper noAccessSound = getSound(cfg.getConfigurationSection("permissions." + ch + ".no-access-sound"));

                        menu.addFillItem(slot, itemBuilder);
                        menu.addCommands(slot, commands);
                        menu.addPermission(slot, permission, noAccessSound);
                        menu.addSound(slot, sound);
                    }

                    if(!charSlots.containsKey(ch))
                        charSlots.add(ch, new ArrayList<>());

                    charSlots.get(ch).add(slot);

                    slot++;
                }
            }
        }

        int backButton = charSlots.get(cfg.getString("back", " ").charAt(0), Collections.singletonList(-1)).get(0);
        menu.setBackButton(backButton);

        if(plugin.getSettings().onlyBackButton && backButton == -1)
            SuperiorSkyblockPlugin.log("&c[" + fileName + "] Menu doesn't have a back button, it's impossible to close it.");

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

    public static void copyResource(String resourcePath){
        String fixedPath = resourcePath + ".jar";
        File dstFile = new File(plugin.getDataFolder(), fixedPath);

        if(dstFile.exists())
            //noinspection ResultOfMethodCallIgnored
            dstFile.delete();

        plugin.saveResource(resourcePath, true);

        File file = new File(plugin.getDataFolder(), resourcePath);
        //noinspection ResultOfMethodCallIgnored
        file.renameTo(dstFile);
    }

    public static void saveResource(String resourcePath){
        saveResource(resourcePath, resourcePath);
    }

    public static void saveResource(String destination, String resourcePath){
        try {
            for(ServerVersion serverVersion : ServerVersion.getByOrder()){
                String version = serverVersion.name().substring(1);
                if(resourcePath.endsWith(".yml") && plugin.getResource(resourcePath.replace(".yml", version + ".yml")) != null) {
                    resourcePath = resourcePath.replace(".yml", version + ".yml");
                    break;
                }

                else if(resourcePath.endsWith(".schematic") && plugin.getResource(resourcePath.replace(".schematic", version + ".schematic")) != null) {
                    resourcePath = resourcePath.replace(".schematic", version + ".schematic");
                    break;
                }
            }

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

    public static InputStream getResource(String resourcePath){
        try {
            for(ServerVersion serverVersion : ServerVersion.getByOrder()){
                String version = serverVersion.name().substring(1);
                if(resourcePath.endsWith(".yml") && plugin.getResource(resourcePath.replace(".yml", version + ".yml")) != null) {
                    resourcePath = resourcePath.replace(".yml", version + ".yml");
                    break;
                }

                else if(resourcePath.endsWith(".schematic") && plugin.getResource(resourcePath.replace(".schematic", version + ".schematic")) != null) {
                    resourcePath = resourcePath.replace(".schematic", version + ".schematic");
                    break;
                }
            }

            return plugin.getResource(resourcePath);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
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

    public static List<Class<?>> getClasses(URL jar, Class<?> clazz) {
        List<Class<?>> list = new ArrayList<>();

        try (URLClassLoader cl = new URLClassLoader(new URL[]{jar}, clazz.getClassLoader()); JarInputStream jis = new JarInputStream(jar.openStream())) {
            JarEntry jarEntry;
            while ((jarEntry = jis.getNextJarEntry()) != null){
                String name = jarEntry.getName();

                if (name == null || name.isEmpty() || !name.endsWith(".class")) {
                    continue;
                }

                name = name.replace("/", ".");
                String clazzName = name.substring(0, name.lastIndexOf(".class"));

                Class<?> c = cl.loadClass(clazzName);

                if (clazz.isAssignableFrom(c)) {
                    list.add(c);
                }
            }
        } catch (Throwable ignored) { }

        return list;
    }

    private static final Object fileMutex = new Object();

    public static void replaceString(File file, String str, String replace){
        synchronized (fileMutex) {
            StringBuilder stringBuilder = new StringBuilder();

            try {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null)
                        stringBuilder.append("\n").append(line);
                }

                if (stringBuilder.length() > 0) {
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(stringBuilder.substring(1).replace(str, replace));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
