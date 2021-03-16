package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.PlaceholderHook;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.commands.CommandUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

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
            MenuMemberRole.openInventory(plugin.getPlayers().getSuperiorPlayer(e.getWhoClicked()), this, targetPlayer);
        }

        else if(banSlot.contains(e.getRawSlot())){
            if(plugin.getSettings().banConfirm){
                Island island = superiorPlayer.getIsland();
                if(IslandUtils.checkBanRestrictions(superiorPlayer, island, targetPlayer)) {
                    previousMove = false;
                    MenuConfirmBan.openInventory(superiorPlayer, this, targetPlayer);
                }
            }
            else {
                CommandUtils.dispatchSubCommand(e.getWhoClicked(), "ban " + targetPlayer.getName());
            }
        }

        else if(kickSlot.contains(e.getRawSlot())){
            if(plugin.getSettings().kickConfirm){
                Island island = superiorPlayer.getIsland();
                if(island == null)
                    return;
                if(IslandUtils.checkKickRestrictions(superiorPlayer, island, targetPlayer)) {
                    previousMove = false;
                    MenuConfirmKick.openInventory(superiorPlayer, this, targetPlayer);
                }
            }
            else {
                CommandUtils.dispatchSubCommand(e.getWhoClicked(), "kick " + targetPlayer.getName());
            }
        }
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
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

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuMemberManage, "member-manage.yml", cfg);

        rolesSlot = getSlots(cfg, "roles", charSlots);
        banSlot = getSlots(cfg, "ban", charSlots);
        kickSlot = getSlots(cfg, "kick", charSlots);

        charSlots.delete();

        menuMemberManage.markCompleted();
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
