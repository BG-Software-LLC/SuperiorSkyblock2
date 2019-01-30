package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.gui.GUIInventory;
import com.bgsoftware.superiorskyblock.handlers.PanelHandler;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public final class PanelListener implements Listener {

    private SuperiorSkyblockPlugin plugin;

    public PanelListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    private Set<UUID> movingBetweenPages = new HashSet<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player) || e.getClickedInventory() == null)
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());
        PanelHandler.PanelType openedPanel = plugin.getPanel().getOpenedPanelType(superiorPlayer);

        GUIInventory guiInventory;

        if(openedPanel == PanelHandler.PanelType.GENERAL){
            e.setCancelled(true);
            guiInventory = plugin.getPanel().mainPage;

            if(e.getRawSlot() == guiInventory.get("membersSlot", Integer.class)){
                movingBetweenPages.add(superiorPlayer.getUniqueId());
                plugin.getPanel().openMembersPanel(superiorPlayer, 1);
            }

//            else if(e.getRawSlot() == guiInventory.get("settingsSlot", Integer.class)){
//
//            }

            else if(e.getRawSlot() == guiInventory.get("visitorsSlot", Integer.class)){
                movingBetweenPages.add(superiorPlayer.getUniqueId());
                plugin.getPanel().openVisitorsPanel(superiorPlayer, 1);
            }
        }

        else if(openedPanel == PanelHandler.PanelType.MEMBERS){
            e.setCancelled(true);
            guiInventory = plugin.getPanel().membersPage;

            if(e.getRawSlot() == guiInventory.get("previousSlot", Integer.class) ||
                    e.getRawSlot() == guiInventory.get("nextSlot", Integer.class) ||
                    e.getRawSlot() == guiInventory.get("currentSlot", Integer.class)){
                if(e.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.RED + ""))
                    return;

                int currentSlot = guiInventory.get("currentSlot", Integer.class);

                if(e.getRawSlot() == currentSlot)
                    return;

                int currentPage = Integer.valueOf(ChatColor.stripColor(e.getInventory().getItem(currentSlot)
                        .getItemMeta().getLore().get(0)).split(" ")[1]);
                int nextPage = guiInventory.get("nextSlot", Integer.class);

                movingBetweenPages.add(superiorPlayer.getUniqueId());
                plugin.getPanel().openMembersPanel(superiorPlayer, e.getRawSlot() == nextPage ? currentPage + 1 : currentPage - 1);
            }

            else{
                if(e.getCurrentItem() == null)
                    return;

                if(e.getCurrentItem().hasItemMeta()) {
                    SuperiorPlayer targetPlayer = SSuperiorPlayer.of(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()));

                    if (targetPlayer != null) {
                        movingBetweenPages.add(superiorPlayer.getUniqueId());
                        plugin.getPanel().openPlayerPanel(superiorPlayer, targetPlayer);
                    }
                }
            }
        }

        else if(openedPanel == PanelHandler.PanelType.PLAYER){
            e.setCancelled(true);
            guiInventory = plugin.getPanel().playerPage;

            SuperiorPlayer targetPlayer = SSuperiorPlayer.of(ChatColor.stripColor(e.getClickedInventory().getName()));

            if(e.getRawSlot() == guiInventory.get("rolesSlot", Integer.class)){
                movingBetweenPages.add(superiorPlayer.getUniqueId());
                plugin.getPanel().openRolePanel(superiorPlayer, targetPlayer);
            }

            else if(e.getRawSlot() == guiInventory.get("banSlot", Integer.class)){
                Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island ban " + targetPlayer.getName());
            }

            else if(e.getRawSlot() == guiInventory.get("kickSlot", Integer.class)){
                Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island kick " + targetPlayer.getName());
            }
        }

        else if(openedPanel == PanelHandler.PanelType.ROLE){
            e.setCancelled(true);
            guiInventory = plugin.getPanel().rolePage;

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

        else if(openedPanel == PanelHandler.PanelType.VISITORS){
            e.setCancelled(true);
            guiInventory = plugin.getPanel().visitorsPage;


            if(e.getRawSlot() == guiInventory.get("previousSlot", Integer.class) ||
                    e.getRawSlot() == guiInventory.get("nextSlot", Integer.class) ||
                    e.getRawSlot() == guiInventory.get("currentSlot", Integer.class)){
                if(e.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.RED + ""))
                    return;

                int currentSlot = guiInventory.get("currentSlot", Integer.class);

                if(e.getRawSlot() == currentSlot)
                    return;

                int currentPage = Integer.valueOf(ChatColor.stripColor(e.getInventory().getItem(currentSlot)
                        .getItemMeta().getLore().get(0)).split(" ")[1]);
                int nextPage = guiInventory.get("nextSlot", Integer.class);

                movingBetweenPages.add(superiorPlayer.getUniqueId());
                plugin.getPanel().openVisitorsPanel(superiorPlayer, e.getRawSlot() == nextPage ? currentPage + 1 : currentPage - 1);
            }

            else{
                if(e.getCurrentItem() == null)
                    return;

                if(e.getCurrentItem().hasItemMeta()) {
                    SuperiorPlayer targetPlayer = SSuperiorPlayer.of(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()));

                    if (targetPlayer != null) {
                        if (e.getClick().name().contains("RIGHT")) {
                            Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island invite " + targetPlayer.getName());
                        } else if (e.getClick().name().contains("LEFT")) {
                            Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island expel " + targetPlayer.getName());
                        }
                    }
                }
            }
        }

        else if(openedPanel == PanelHandler.PanelType.SCHEMATICS){
            e.setCancelled(true);
            guiInventory = plugin.getPanel().islandCreationPage;

            for(String schematic : plugin.getSchematics().getSchematics()){
                if(guiInventory.contains(schematic + "-slot")) {
                    int slot = guiInventory.get(schematic + "-slot", Integer.class);
                    String permission = guiInventory.get(schematic + "-permission", String.class);

                    if (superiorPlayer.hasPermission(permission) && slot == e.getRawSlot()) {
                        superiorPlayer.asPlayer().closeInventory();
                        Locale.ISLAND_CREATE_PROCCESS_REQUEST.send(superiorPlayer);
                        plugin.getGrid().createIsland(superiorPlayer, schematic);
                        break;
                    }
                }
            }

        }

        else if(openedPanel == PanelHandler.PanelType.BIOMES){
            e.setCancelled(true);
            guiInventory = plugin.getPanel().biomesPage;

            for(Biome biome : Biome.values()){
                String biomeName = biome.name().toLowerCase();
                if(guiInventory.contains(biomeName + "-slot")) {
                    int slot = guiInventory.get(biomeName + "-slot", Integer.class);
                    String permission = guiInventory.get(biomeName + "-permission", String.class);

                    if (superiorPlayer.hasPermission(permission) && slot == e.getRawSlot()) {
                        superiorPlayer.getIsland().setBiome(biome);
                        Locale.CHANGED_BIOME.send(superiorPlayer, biomeName);
                        break;
                    }
                }
            }

        }

        else if(openedPanel == PanelHandler.PanelType.WARPS){
            e.setCancelled(true);
            guiInventory = plugin.getPanel().warpsPage;

            if(e.getRawSlot() == guiInventory.get("previousSlot", Integer.class) ||
                    e.getRawSlot() == guiInventory.get("nextSlot", Integer.class) ||
                    e.getRawSlot() == guiInventory.get("currentSlot", Integer.class)){
                if(e.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.RED + ""))
                    return;

                int currentSlot = guiInventory.get("currentSlot", Integer.class);

                if(e.getRawSlot() == currentSlot)
                    return;

                int currentPage = Integer.valueOf(ChatColor.stripColor(e.getInventory().getItem(currentSlot)
                        .getItemMeta().getLore().get(0)).split(" ")[1]);
                int nextPage = guiInventory.get("nextSlot", Integer.class);

                movingBetweenPages.add(superiorPlayer.getUniqueId());
                plugin.getPanel().openWarpsPanel(superiorPlayer, e.getRawSlot() == nextPage ? currentPage + 1 : currentPage - 1);
            }

            else{
                if(e.getCurrentItem() == null)
                    return;

                if(e.getCurrentItem().hasItemMeta()) {
                    String warpName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
                    Island island = plugin.getPanel().getIsland(superiorPlayer);
                    Location location = island.getWarpLocation(warpName);

                    if(location != null) {
                        island.warpPlayer(superiorPlayer, warpName);
                    }
                }
            }
        }

        else if(openedPanel == PanelHandler.PanelType.TOP){
            e.setCancelled(true);

            Integer[] slots = (Integer[]) plugin.getGrid().getTopIslands().getData().get("slots");

            for(int i = 0; i < slots.length; i++){
                if(slots[i] == e.getRawSlot()){
                    Island island = plugin.getGrid().getIsland(i);

                    if(island != null) {
                        superiorPlayer.asPlayer().closeInventory();
                        if(e.getAction() == InventoryAction.PICKUP_HALF){
                            Bukkit.getScheduler().runTaskLater(plugin, () ->
                                    Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island warp " + island.getOwner().getName()), 1L);
                        } else {
                            Bukkit.getScheduler().runTaskLater(plugin, () ->
                                plugin.getPanel().openValuesPanel(superiorPlayer, island), 1L);
                        }
                        break;
                    }

                }
            }

        }

        else if(openedPanel == PanelHandler.PanelType.VALUES){
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        if(!(e.getPlayer() instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        PanelHandler.PanelType panelType = plugin.getPanel().getOpenedPanelType(superiorPlayer);
        String title = e.getInventory().getName();

        if(movingBetweenPages.contains(superiorPlayer.getUniqueId())){
            movingBetweenPages.remove(superiorPlayer.getUniqueId());
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(panelType == PanelHandler.PanelType.MEMBERS || panelType == PanelHandler.PanelType.VISITORS){
                plugin.getPanel().openPanel(superiorPlayer);
            }else if(panelType == PanelHandler.PanelType.PLAYER){
                plugin.getPanel().openMembersPanel(superiorPlayer, 1);
            }else if(panelType == PanelHandler.PanelType.ROLE) {
                plugin.getPanel().openPlayerPanel(superiorPlayer, SSuperiorPlayer.of(ChatColor.stripColor(title)));
            }else if(panelType == PanelHandler.PanelType.VALUES){
                plugin.getGrid().openTopIslands(superiorPlayer);
            }else {
                plugin.getPanel().closeInventory(SSuperiorPlayer.of(e.getPlayer()));
            }
        }, 1L);
    }

}
