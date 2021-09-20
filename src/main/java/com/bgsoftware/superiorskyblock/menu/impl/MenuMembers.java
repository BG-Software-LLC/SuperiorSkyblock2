package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.menu.converter.MenuConverter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class MenuMembers extends PagedSuperiorMenu<SuperiorPlayer> {

    private final Island island;

    private MenuMembers(SuperiorPlayer superiorPlayer, Island island){
        super("menuMembers", superiorPlayer);
        this.island = island;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, SuperiorPlayer targetPlayer) {
        previousMove = false;
        plugin.getMenus().openMemberManage(superiorPlayer, this, targetPlayer);
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, island);
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, SuperiorPlayer superiorPlayer) {
        try {
            return new ItemBuilder(clickedItem)
                    .replaceAll("{0}", superiorPlayer.getName())
                    .replaceAll("{1}", superiorPlayer.getPlayerRole() + "")
                    .asSkullOf(superiorPlayer).build(superiorPlayer);
        }catch(Exception ex){
            SuperiorSkyblockPlugin.log("Failed to load menu because of player: " + superiorPlayer.getName());
            throw ex;
        }
    }

    @Override
    protected List<SuperiorPlayer> requestObjects() {
        return island.getIslandMembers(true);
    }

    public static void init(){
        MenuMembers menuMembers = new MenuMembers(null, null);

        File file = new File(plugin.getDataFolder(), "menus/members.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/members.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            try {
                cfg.save(file);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuMembers, "members.yml", cfg);

        menuMembers.setPreviousSlot(getSlots(cfg, "previous-page", charSlots));
        menuMembers.setCurrentSlot(getSlots(cfg, "current-page", charSlots));
        menuMembers.setNextSlot(getSlots(cfg, "next-page", charSlots));
        menuMembers.setSlots(getSlots(cfg, "slots", charSlots));

        menuMembers.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island island){
        new MenuMembers(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island){
        SuperiorMenu.refreshMenus(MenuMembers.class, superiorMenu -> superiorMenu.island.equals(island));
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

        newMenu.set("title", cfg.getString("members-panel.title"));

        int size = cfg.getInt("members-panel.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("members-panel.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("members-panel.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = itemChars[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("members-panel"),
                cfg.getConfigurationSection("members-panel.member-item"), newMenu, patternChars,
                slotsChar, itemChars[charCounter++], itemChars[charCounter++], itemChars[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

}
