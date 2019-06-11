package com.bgsoftware.superiorskyblock.listeners;

import static com.bgsoftware.superiorskyblock.gui.GUIInventory.ROLE_PAGE_IDENTIFIER;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.gui.GUIIdentifier;
import com.bgsoftware.superiorskyblock.gui.GUIInventory;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class PanelListener implements Listener {

    private SuperiorSkyblockPlugin plugin;

    public PanelListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    private Map<UUID, ItemStack> latestClickedItem = new HashMap<>();

    /**
     * The following two events are here for patching a dupe glitch caused
     * by shift clicking and closing the inventory in the same time.
     */

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClickMonitor(InventoryClickEvent e){
        if(e.getCurrentItem() != null && e.isCancelled() && e.getInventory().getHolder() instanceof GUIIdentifier) {
            latestClickedItem.put(e.getWhoClicked().getUniqueId(), e.getCurrentItem());
            Bukkit.getScheduler().runTaskLater(plugin, () -> latestClickedItem.remove(e.getWhoClicked().getUniqueId()), 20L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryCloseMonitor(InventoryCloseEvent e){
        if(latestClickedItem.containsKey(e.getPlayer().getUniqueId())){
            ItemStack clickedItem = latestClickedItem.get(e.getPlayer().getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                e.getPlayer().getInventory().removeItem(clickedItem);
                ((Player) e.getPlayer()).updateInventory();
            }, 1L);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player) || e.getClickedInventory() == null)
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());

        GUIInventory guiInventory = GUIInventory.from(superiorPlayer);

        if(guiInventory == null)
            return;

        e.setCancelled(true);

        switch (guiInventory.getIdentifier()){
            case ROLE_PAGE_IDENTIFIER: {
                rolesPage(e, guiInventory, superiorPlayer);
                break;
            }
        }

    }

    private void rolesPage(InventoryClickEvent e, GUIInventory guiInventory, SuperiorPlayer superiorPlayer){
        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(ChatColor.stripColor(e.getClickedInventory().getName()));

        if(e.getRawSlot() == guiInventory.get("memberSlot", Integer.class)){
            Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island setrole " + targetPlayer.getName() + " member");
        }

        else if(e.getRawSlot() == guiInventory.get("modSlot", Integer.class)){
            Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island setrole " + targetPlayer.getName() + " moderator");
        }

        else if(e.getRawSlot() == guiInventory.get("adminSlot", Integer.class)){
            Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island setrole " + targetPlayer.getName() + " admin");
        }

        else if(e.getRawSlot() == guiInventory.get("leaderSlot", Integer.class)){
            Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island transfer " + targetPlayer.getName());
        }
    }

}
