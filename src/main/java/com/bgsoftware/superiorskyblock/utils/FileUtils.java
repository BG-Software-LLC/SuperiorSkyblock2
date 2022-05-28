package com.bgsoftware.superiorskyblock.utils;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.formatting.Formatters;
import com.bgsoftware.superiorskyblock.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.BackButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.DummyButton;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.utils.items.EnchantsUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public final class FileUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Object fileMutex = new Object();

    private FileUtils() {

    }

    public static void copyResource(String resourcePath) {
        String fixedPath = resourcePath + ".jar";
        File dstFile = new File(plugin.getDataFolder(), fixedPath);

        if (dstFile.exists())
            //noinspection ResultOfMethodCallIgnored
            dstFile.delete();

        plugin.saveResource(resourcePath, true);

        File file = new File(plugin.getDataFolder(), resourcePath);
        //noinspection ResultOfMethodCallIgnored
        file.renameTo(dstFile);
    }

    public static void saveResource(String resourcePath) {
        saveResource(resourcePath, resourcePath);
    }

    public static void saveResource(String destination, String resourcePath) {
        try {
            for (ServerVersion serverVersion : ServerVersion.getByOrder()) {
                String version = serverVersion.name().substring(1);
                if (resourcePath.endsWith(".yml") && plugin.getResource(resourcePath.replace(".yml", version + ".yml")) != null) {
                    resourcePath = resourcePath.replace(".yml", version + ".yml");
                    break;
                } else if (resourcePath.endsWith(".schematic") && plugin.getResource(resourcePath.replace(".schematic", version + ".schematic")) != null) {
                    resourcePath = resourcePath.replace(".schematic", version + ".schematic");
                    break;
                }
            }

            File file = new File(plugin.getDataFolder(), resourcePath);
            plugin.saveResource(resourcePath, true);

            if (!destination.equals(resourcePath)) {
                File dest = new File(plugin.getDataFolder(), destination);
                //noinspection ResultOfMethodCallIgnored
                file.renameTo(dest);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
        }
    }

    public static InputStream getResource(String resourcePath) {
        try {
            for (ServerVersion serverVersion : ServerVersion.getByOrder()) {
                String version = serverVersion.name().substring(1);
                if (resourcePath.endsWith(".yml") && plugin.getResource(resourcePath.replace(".yml", version + ".yml")) != null) {
                    resourcePath = resourcePath.replace(".yml", version + ".yml");
                    break;
                } else if (resourcePath.endsWith(".schematic") && plugin.getResource(resourcePath.replace(".schematic", version + ".schematic")) != null) {
                    resourcePath = resourcePath.replace(".schematic", version + ".schematic");
                    break;
                }
            }

            return plugin.getResource(resourcePath);
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
            return null;
        }
    }

    public static List<Class<?>> getClasses(URL jar, Class<?> clazz) {
        return getClasses(jar, clazz, clazz.getClassLoader());
    }

    public static List<Class<?>> getClasses(URL jar, Class<?> clazz, ClassLoader classLoader) {
        List<Class<?>> list = new ArrayList<>();

        try (URLClassLoader cl = new URLClassLoader(new URL[]{jar}, classLoader); JarInputStream jis = new JarInputStream(jar.openStream())) {
            JarEntry jarEntry;
            while ((jarEntry = jis.getNextJarEntry()) != null) {
                String name = jarEntry.getName();

                if (!name.endsWith(".class")) {
                    continue;
                }

                name = name.replace("/", ".");
                String clazzName = name.substring(0, name.lastIndexOf(".class"));

                Class<?> c = cl.loadClass(clazzName);

                if (clazz.isAssignableFrom(c)) {
                    list.add(c);
                }
            }
        } catch (Throwable ignored) {
        }

        return list;
    }

    public static void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] childFiles = directory.listFiles();
            if (childFiles != null) {
                for (File file : childFiles)
                    deleteDirectory(file);
            }
        }

        //noinspection ResultOfMethodCallIgnored
        directory.delete();
    }

    public static void replaceString(File file, String str, String replace) {
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
                PluginDebugger.debug(ex);
            }
        }
    }

}
