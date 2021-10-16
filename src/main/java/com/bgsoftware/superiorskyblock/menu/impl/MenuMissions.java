package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.missions.MissionCategory;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.Optional;

public final class MenuMissions extends SuperiorMenu {

    private MenuMissions(SuperiorPlayer superiorPlayer){
        super("menuMissions", superiorPlayer);
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        Optional<MissionCategory> clickedMissionCategory = plugin.getMissions().getMissionCategories()
                .stream().filter(missionCategory -> missionCategory.getSlot() == e.getRawSlot()).findFirst();

        if(!clickedMissionCategory.isPresent())
            return;

        previousMove = false;
        plugin.getMenus().openMissionsCategory(superiorPlayer, this, clickedMissionCategory.get());
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu);
    }

    public static void init(){
        MenuMissions menuMissions = createEmptyInstance();

        File file = new File(plugin.getDataFolder(), "menus/missions.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/missions.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        FileUtils.loadGUI(menuMissions, "missions.yml", cfg);

        menuMissions.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu){
        new MenuMissions(superiorPlayer).open(previousMenu);
    }

    public static MenuMissions createEmptyInstance() {
        return new MenuMissions(null);
    }

}
