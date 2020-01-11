package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MenuMemberManage extends SuperiorMenu {

    private static int rolesSlot, banSlot, kickSlot;

    private SuperiorPlayer targetPlayer;

    private MenuMemberManage(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer){
        super("menuMemberManage", superiorPlayer);
        this.targetPlayer = targetPlayer;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        if(e.getRawSlot() == rolesSlot){
            previousMove = false;
            MenuMemberRole.openInventory(SSuperiorPlayer.of(e.getWhoClicked()), this, targetPlayer);
        }

        else if(e.getRawSlot() == banSlot){
            Bukkit.dispatchCommand(e.getWhoClicked(), "island ban " + targetPlayer.getName());
        }

        else if(e.getRawSlot() == kickSlot){
            Bukkit.dispatchCommand(e.getWhoClicked(), "island kick " + targetPlayer.getName());
        }
    }

    @Override
    public Inventory getInventory() {
        return super.buildInventory(title -> title.replace("{}", targetPlayer.getName()));
    }

    public static void init(){
        MenuMemberManage menuMemberManage = new MenuMemberManage(null, null);

        File file = new File(plugin.getDataFolder(), "menus/member-manage.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/member-manage.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            cfg.save(file);
        }

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuMemberManage, "member-manage.yml", cfg);

        rolesSlot = charSlots.getOrDefault(cfg.getString("roles", "%").charAt(0), Collections.singletonList(-1)).get(0);
        banSlot = charSlots.getOrDefault(cfg.getString("ban", "*").charAt(0), Collections.singletonList(-1)).get(0);
        kickSlot = charSlots.getOrDefault(cfg.getString("kick", "^").charAt(0), Collections.singletonList(-1)).get(0);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, SuperiorPlayer targetPlayer){
        new MenuMemberManage(superiorPlayer, targetPlayer).open(previousMenu);
    }

    public static void destroyMenus(SuperiorPlayer targetPlayer){
        destroyMenus(MenuMemberManage.class, menuMemberManage -> menuMemberManage.targetPlayer.equals(targetPlayer));
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("players-panel.title"));

        int size = cfg.getInt("players-panel.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("players-panel.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("players-panel.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char rolesChar = itemChars[charCounter++], banChar = itemChars[charCounter++], kickChar = itemChars[charCounter++];

        MenuConverter.convertItem(cfg.getConfigurationSection("players-panel.roles"), patternChars, rolesChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("players-panel.ban"), patternChars, banChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(cfg.getConfigurationSection("players-panel.kick"), patternChars, kickChar,
                itemsSection, commandsSection, soundsSection);

        newMenu.set("roles", rolesChar + "");
        newMenu.set("ban", banChar + "");
        newMenu.set("kick", kickChar + "");

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

}
