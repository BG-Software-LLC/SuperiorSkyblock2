package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.List;
import java.util.Map;

public final class MenuConfirmBan extends SuperiorMenu {

    private static List<Integer> confirmSlot, cancelSlot;

    private final Island targetIsland;

    private MenuConfirmBan(SuperiorPlayer superiorPlayer, Island targetIsland, SuperiorPlayer targetPlayer){
        super("menuConfirmBan", superiorPlayer);
        this.targetIsland = targetIsland;
        updateTargetPlayer(targetPlayer);
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent e) {
        boolean closeMenu = false;

        if(confirmSlot.contains(e.getRawSlot())){
            IslandUtils.handleBanPlayer(superiorPlayer, targetIsland, targetPlayer);
            closeMenu = true;
        }
        else if(cancelSlot.contains(e.getRawSlot())) {
            closeMenu = true;
        }

        if(closeMenu){
            previousMove = false;
            e.getWhoClicked().closeInventory();
        }
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, targetIsland, targetPlayer);
    }

    public static void init(){
        MenuConfirmBan menuConfirmBan = new MenuConfirmBan(null, null, null);

        File file = new File(plugin.getDataFolder(), "menus/confirm-ban.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/confirm-ban.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuConfirmBan, "confirm-ban.yml", cfg);

        confirmSlot = getSlots(cfg, "confirm", charSlots);
        cancelSlot = getSlots(cfg, "cancel", charSlots);

        menuConfirmBan.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island targetIsland, SuperiorPlayer targetPlayer){
        new MenuConfirmBan(superiorPlayer, targetIsland, targetPlayer).open(previousMenu);
    }

}
