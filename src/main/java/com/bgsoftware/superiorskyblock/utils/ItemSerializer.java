package com.bgsoftware.superiorskyblock.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemSerializer {

    public static ItemStack getItem(String serialized) {
        String[] split = serialized.split(":");

        Material material;
        int data;

        try {
            material = Material.matchMaterial(split[0]);
        } catch (Exception e) {
            material = Material.STONE;
            e.printStackTrace();
        }

        if (split.length > 1) {
            data = Integer.valueOf(split[1]);
        } else
            data = 0;

        ItemStack item = new ItemStack(material);
        item.setDurability((short) data);

        return item;
    }

    @SuppressWarnings("unchecked")
    public static ItemStack getItem(String material, ConfigurationSection map) {
        ItemStack item = getItem(material);
        ItemMeta meta = item.getItemMeta();

        meta.addItemFlags(ItemFlag.values());

        if (map.contains("name"))
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', map.getString("name")));

        if (map.contains("lore")) {
            List<String> lore = new ArrayList<>();
            map.getStringList("lore").forEach(line -> lore.add(ChatColor.translateAlternateColorCodes('&', line)));
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getItem(ConfigurationSection map) {
        return getItem(map.getString("material"), map);
    }

    public static ItemStack replace(ItemStack item, String... placeholders) {
        ItemMeta meta = item.getItemMeta();

        for (int i = 0; i < placeholders.length; i++) {
            if (meta.hasDisplayName())
                meta.setDisplayName(meta.getDisplayName().replace("{" + i + "}", placeholders[i]));

            if (meta.hasLore()) {
                List<String> lore = new ArrayList<>();
                for (String line : meta.getLore()) {
                    lore.add(line.replace("{" + i + "}", placeholders[i]));
                }
                meta.setLore(lore);
            }
        }

        item.setItemMeta(meta);
        return item;
    }

}
