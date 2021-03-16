package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
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

    @Override
    protected void onPlayerClick(InventoryClickEvent e) {
        Island island = superiorPlayer.getIsland();

        if(island == null)
            return;

        if (confirmSlot.contains(e.getRawSlot())) {
            if (EventsCaller.callIslandQuitEvent(superiorPlayer, island)) {
                island.kickMember(superiorPlayer);

                IslandUtils.sendMessage(island, Locale.LEAVE_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName());

                Locale.LEFT_ISLAND.send(superiorPlayer);

                previousMove = false;
                e.getWhoClicked().closeInventory();
            }
        } else if (cancelSlot.contains(e.getRawSlot())) {
            previousMove = false;
            e.getWhoClicked().closeInventory();
        }
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu);
    }

    public static void init() {
        MenuConfirmLeave menuConfirmLeave = new MenuConfirmLeave(null);

        File file = new File(plugin.getDataFolder(), "menus/confirm-leave.yml");

        if (!file.exists())
            FileUtils.saveResource("menus/confirm-leave.yml");

        CommentedConfiguration config = CommentedConfiguration.loadConfiguration(file);

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuConfirmLeave, "confirm-leave.yml", config);

        confirmSlot = getSlots(config, "confirm", charSlots);
        cancelSlot = getSlots(config, "cancel", charSlots);

        charSlots.delete();

        menuConfirmLeave.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu) {
        new MenuConfirmLeave(superiorPlayer).open(previousMenu);
    }
}
