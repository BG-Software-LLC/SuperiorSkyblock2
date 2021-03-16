package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.List;

public final class MenuConfirmBan extends SuperiorMenu {

    private static List<Integer> confirmSlot, cancelSlot;

    private MenuConfirmBan(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer){
        super("menuConfirmBan", superiorPlayer);
        updateTargetPlayer(targetPlayer);
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent e) {
        Island island = superiorPlayer.getIsland();

        if(island == null)
            return;

        boolean closeMenu = false;

        if(confirmSlot.contains(e.getRawSlot())){
            IslandUtils.handleBanPlayer(superiorPlayer, island, targetPlayer);
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
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, targetPlayer);
    }

    public static void init(){
        MenuConfirmBan menuConfirmBan = new MenuConfirmBan(null, null);

        File file = new File(plugin.getDataFolder(), "menus/confirm-ban.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/confirm-ban.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuConfirmBan, "confirm-ban.yml", cfg);

        confirmSlot = getSlots(cfg, "confirm", charSlots);
        cancelSlot = getSlots(cfg, "cancel", charSlots);

        charSlots.delete();

        menuConfirmBan.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, SuperiorPlayer targetPlayer){
        new MenuConfirmBan(superiorPlayer, targetPlayer).open(previousMenu);
    }

}
