package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.support.PlaceholderHook;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.menu.converter.MenuConverter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class MenuMemberManage extends SuperiorMenu {

    private static List<Integer> rolesSlot, banSlot, kickSlot;

    private final SuperiorPlayer targetPlayer;

    private MenuMemberManage(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer){
        super("menuMemberManage", superiorPlayer);
        this.targetPlayer = targetPlayer;
        updateTargetPlayer(targetPlayer);
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        if(rolesSlot.contains(e.getRawSlot())){
            previousMove = false;
            plugin.getMenus().openMemberRole(superiorPlayer, this, targetPlayer);
        }

        else if(banSlot.contains(e.getRawSlot())){
            if(plugin.getSettings().isBanConfirm()){
                Island island = superiorPlayer.getIsland();
                if(IslandUtils.checkBanRestrictions(superiorPlayer, island, targetPlayer)) {
                    previousMove = false;
                    plugin.getMenus().openConfirmBan(superiorPlayer, this, island, targetPlayer);
                }
            }
            else {
                plugin.getCommands().dispatchSubCommand(e.getWhoClicked(), "ban", targetPlayer.getName());
            }
        }

        else if(kickSlot.contains(e.getRawSlot())){
            if(plugin.getSettings().isKickConfirm()){
                Island island = superiorPlayer.getIsland();
                if(island == null)
                    return;
                if(IslandUtils.checkKickRestrictions(superiorPlayer, island, targetPlayer)) {
                    previousMove = false;
                    plugin.getMenus().openConfirmKick(superiorPlayer, this, island, targetPlayer);
                }
            }
            else {
                plugin.getCommands().dispatchSubCommand(e.getWhoClicked(), "kick", targetPlayer.getName());
            }
        }
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, targetPlayer);
    }

    @Override
    public Inventory getInventory() {
        return buildInventory(title -> PlaceholderHook.parse(targetPlayer.asOfflinePlayer(), title.replace("{}", targetPlayer.getName())));
    }

    public static void init(){
        MenuMemberManage menuMemberManage = new MenuMemberManage(null, null);

        File file = new File(plugin.getDataFolder(), "menus/member-manage.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/member-manage.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            try {
                cfg.save(file);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuMemberManage, "member-manage.yml", cfg);

        rolesSlot = getSlots(cfg, "roles", charSlots);
        banSlot = getSlots(cfg, "ban", charSlots);
        kickSlot = getSlots(cfg, "kick", charSlots);

        menuMemberManage.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, SuperiorPlayer targetPlayer){
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
