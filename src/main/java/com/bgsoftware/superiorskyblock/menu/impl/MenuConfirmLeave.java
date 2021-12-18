package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Callum Jay Seabrook (BomBardyGamer)
 */
public final class MenuConfirmLeave extends SuperiorMenu {

    private static List<Integer> confirmSlot, cancelSlot;

    private MenuConfirmLeave(SuperiorPlayer superiorPlayer) {
        super("menuConfirmLeave", superiorPlayer);
    }

    public static void init() {
        MenuConfirmLeave menuConfirmLeave = new MenuConfirmLeave(null);

        File file = new File(plugin.getDataFolder(), "menus/confirm-leave.yml");

        if (!file.exists())
            FileUtils.saveResource("menus/confirm-leave.yml");

        CommentedConfiguration config = CommentedConfiguration.loadConfiguration(file);

        MenuPatternSlots menuPatternSlots = FileUtils.loadGUI(menuConfirmLeave, "confirm-leave.yml", config);

        confirmSlot = getSlots(config, "confirm", menuPatternSlots);
        cancelSlot = getSlots(config, "cancel", menuPatternSlots);

        menuConfirmLeave.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu) {
        new MenuConfirmLeave(superiorPlayer).open(previousMenu);
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent e) {
        Island island = superiorPlayer.getIsland();

        if (island == null)
            return;

        if (confirmSlot.contains(e.getRawSlot())) {
            if (EventsCaller.callIslandQuitEvent(superiorPlayer, island)) {
                island.kickMember(superiorPlayer);

                IslandUtils.sendMessage(island, Message.LEAVE_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName());

                Message.LEFT_ISLAND.send(superiorPlayer);

                previousMove = false;
                e.getWhoClicked().closeInventory();
            }
        } else if (cancelSlot.contains(e.getRawSlot())) {
            previousMove = false;
            e.getWhoClicked().closeInventory();
        }
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu);
    }
}
