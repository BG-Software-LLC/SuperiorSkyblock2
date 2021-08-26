package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.missions.MissionCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
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
        MenuMissionsCategory.openInventory(superiorPlayer, this, clickedMissionCategory.get());
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu);
    }

    public static void init(){
        MenuMissions menuMissions = new MenuMissions(null);

        File file = new File(plugin.getDataFolder(), "menus/missions.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/missions.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        FileUtils.loadGUI(menuMissions, "missions.yml", cfg);

        menuMissions.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new MenuMissions(superiorPlayer).open(previousMenu);
    }

}
