package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandSettings;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MenuSettings extends SuperiorMenu {

    private static List<Integer> slots;
    private static int previousSlot, currentSlot, nextSlot;
    private static List<IslandSettings> islandSettings = new ArrayList<>();

    private final Island island;
    private int currentPage = 1;

    private MenuSettings(SuperiorPlayer superiorPlayer, Island island){
        super("menuSettings", superiorPlayer);
        this.island = island;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        if(e.getRawSlot() == previousSlot || e.getRawSlot() == nextSlot || e.getRawSlot() == currentSlot){
            if(e.getRawSlot() == currentSlot)
                return;

            boolean nextPage = slots.size() * currentPage < islandSettings.size();

            if((!nextPage && e.getRawSlot() == nextSlot) || (currentPage == 1 && e.getRawSlot() == previousSlot))
                return;

            currentPage = e.getRawSlot() == nextSlot ? currentPage + 1 : currentPage - 1;

            previousMove = false;
            open(previousMenu);
        }
        else {
            if(e.getCurrentItem() == null)
                return;

            int settingsAmount = islandSettings.size();

            int indexOf = slots.indexOf(e.getRawSlot());

            if(indexOf >= settingsAmount || indexOf == -1)
                return;

            IslandSettings settings = islandSettings.get(indexOf + (slots.size() * (currentPage - 1)));
            String settingsName = settings.name().toLowerCase();

            if(!containsData(settingsName + "-settings-enabled"))
                return;

            if(island.hasSettingsEnabled(settings)){
                island.disableSettings(settings);
            }
            else{
                island.enableSettings(settings);
            }

            Locale.UPDATED_SETTINGS.send(superiorPlayer, StringUtils.format(settingsName));

            SoundWrapper soundWrapper = (SoundWrapper) getData(settingsName + "-sound");
            if (soundWrapper != null)
                soundWrapper.playSound(superiorPlayer.asPlayer());
            //noinspection unchecked
            List<String> commands = (List<String>) getData(settingsName + "-commands");
            if (commands != null)
                commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));

            previousMove = false;
            open(previousMenu);

        }
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = super.getInventory();

        int settingsAmount = islandSettings.size();

        for(int i = 0; i < slots.size() && (i + (slots.size() * (currentPage - 1))) < settingsAmount; i++){
            IslandSettings settings = islandSettings.get(i + (slots.size() * (currentPage - 1)));

            ItemBuilder settingsItem = new ItemBuilder(Material.AIR);
            String settingsName = settings.name().toLowerCase();

            if (containsData(settingsName + "-settings-enabled"))
                settingsItem = (ItemBuilder) getData(settingsName + "-settings-" + (island.hasSettingsEnabled(settings) ? "enabled" : "disabled"));

            inventory.setItem(slots.get(i), settingsItem.clone().build(superiorPlayer));
        }

        inventory.setItem(previousSlot, new ItemBuilder(inventory.getItem(previousSlot))
                .replaceAll("{0}", (currentPage == 1 ? "&c" : "&a")).build(superiorPlayer));

        inventory.setItem(currentSlot, new ItemBuilder(inventory.getItem(currentSlot))
                .replaceAll("{0}", currentPage + "").build(superiorPlayer));

        inventory.setItem(nextSlot, new ItemBuilder(inventory.getItem(nextSlot))
                .replaceAll("{0}", (settingsAmount > currentPage * slots.size() ? "&a" : "&c")).build(superiorPlayer));

        return inventory;
    }

    public static void init(){
        MenuSettings menuSettings = new MenuSettings(null, null);

        File file = new File(plugin.getDataFolder(), "menus/settings.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/settings.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuSettings, cfg);

        previousSlot = charSlots.getOrDefault(cfg.getString("previous-page", "%").charAt(0), Collections.singletonList(-1)).get(0);
        currentSlot = charSlots.getOrDefault(cfg.getString("current-page", "*").charAt(0), Collections.singletonList(-1)).get(0);
        nextSlot = charSlots.getOrDefault(cfg.getString("next-page", "^").charAt(0), Collections.singletonList(-1)).get(0);

        slots = charSlots.getOrDefault(cfg.getString("slots", "@").charAt(0), Collections.singletonList(-1));
        slots.sort(Integer::compareTo);

        islandSettings = new ArrayList<>();

        ConfigurationSection settingsSection = cfg.getConfigurationSection("settings");

        for(IslandSettings islandSettings : IslandSettings.values()){
            String settings = islandSettings.name().toLowerCase();
            if(settingsSection.contains(settings)){
                ConfigurationSection section = settingsSection.getConfigurationSection(settings);
                menuSettings.addData(settings + "-sound", FileUtils.getSound(section.getConfigurationSection("sound")));
                menuSettings.addData(settings + "-commands", section.getStringList("commands"));
                menuSettings.addData(settings + "-settings-enabled", FileUtils.getItemStack(section.getConfigurationSection("settings-enabled")));
                menuSettings.addData(settings + "-settings-disabled", FileUtils.getItemStack(section.getConfigurationSection("settings-disabled")));
                MenuSettings.islandSettings.add(islandSettings);
            }
        }
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuSettings(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(){
        SuperiorMenu.refreshMenus(MenuSettings.class);
    }

}
