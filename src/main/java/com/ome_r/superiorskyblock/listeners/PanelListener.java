package com.ome_r.superiorskyblock.listeners;

import com.ome_r.superiorskyblock.Locale;
import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.gui.GUIInventory;
import com.ome_r.superiorskyblock.handlers.PanelHandler;
import com.ome_r.superiorskyblock.island.Island;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;

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

public class PanelListener implements Listener {

    private SuperiorSkyblock plugin;

    public PanelListener(SuperiorSkyblock plugin){
        this.plugin = plugin;
    }

    private Set<UUID> movingBetweenPages = new HashSet<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player) || e.getClickedInventory() == null)
            return;

        WrappedPlayer wrappedPlayer = WrappedPlayer.of(e.getWhoClicked());
        PanelHandler.PanelType openedPanel = plugin.getPanel().getOpenedPanelType(wrappedPlayer);

        GUIInventory guiInventory;

        if(openedPanel == PanelHandler.PanelType.GENERAL){
            e.setCancelled(true);
            guiInventory = plugin.getPanel().mainPage;

            if(e.getRawSlot() == guiInventory.get("membersSlot", Integer.class)){
                movingBetweenPages.add(wrappedPlayer.getUniqueId());
                plugin.getPanel().openMembersPanel(wrappedPlayer, 1);
            }

            else if(e.getRawSlot() == guiInventory.get("settingsSlot", Integer.class)){

            }

            else if(e.getRawSlot() == guiInventory.get("visitorsSlot", Integer.class)){
                movingBetweenPages.add(wrappedPlayer.getUniqueId());
                plugin.getPanel().openVisitorsPanel(wrappedPlayer, 1);
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

                movingBetweenPages.add(wrappedPlayer.getUniqueId());
                plugin.getPanel().openMembersPanel(wrappedPlayer, e.getRawSlot() == nextPage ? currentPage + 1 : currentPage - 1);
            }

            else{
                if(e.getCurrentItem() == null)
                    return;

                if(e.getCurrentItem().hasItemMeta()) {
                    WrappedPlayer targetPlayer = WrappedPlayer.of(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()));

                    if (targetPlayer != null) {
                        movingBetweenPages.add(wrappedPlayer.getUniqueId());
                        plugin.getPanel().openPlayerPanel(wrappedPlayer, targetPlayer);
                    }
                }
            }
        }

        else if(openedPanel == PanelHandler.PanelType.PLAYER){
            e.setCancelled(true);
            guiInventory = plugin.getPanel().playerPage;

            WrappedPlayer targetPlayer = WrappedPlayer.of(ChatColor.stripColor(e.getClickedInventory().getName()));

            if(e.getRawSlot() == guiInventory.get("rolesSlot", Integer.class)){
                movingBetweenPages.add(wrappedPlayer.getUniqueId());
                plugin.getPanel().openRolePanel(wrappedPlayer, targetPlayer);
            }

            else if(e.getRawSlot() == guiInventory.get("banSlot", Integer.class)){
                Bukkit.dispatchCommand(wrappedPlayer.asPlayer(), "island ban " + targetPlayer.getName());
            }

            else if(e.getRawSlot() == guiInventory.get("kickSlot", Integer.class)){
                Bukkit.dispatchCommand(wrappedPlayer.asPlayer(), "island kick " + targetPlayer.getName());
            }
        }

        else if(openedPanel == PanelHandler.PanelType.ROLE){
            e.setCancelled(true);
            guiInventory = plugin.getPanel().rolePage;

            WrappedPlayer targetPlayer = WrappedPlayer.of(ChatColor.stripColor(e.getClickedInventory().getName()));

            if(e.getRawSlot() == guiInventory.get("memberSlot", Integer.class)){
                Bukkit.dispatchCommand(wrappedPlayer.asPlayer(), "island setrole " + targetPlayer.getName() + " member");
            }

            else if(e.getRawSlot() == guiInventory.get("modSlot", Integer.class)){
                Bukkit.dispatchCommand(wrappedPlayer.asPlayer(), "island setrole " + targetPlayer.getName() + " moderator");
            }

            else if(e.getRawSlot() == guiInventory.get("adminSlot", Integer.class)){
                Bukkit.dispatchCommand(wrappedPlayer.asPlayer(), "island setrole " + targetPlayer.getName() + " admin");
            }

            else if(e.getRawSlot() == guiInventory.get("leaderSlot", Integer.class)){
                Bukkit.dispatchCommand(wrappedPlayer.asPlayer(), "island transfer " + targetPlayer.getName());
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

                movingBetweenPages.add(wrappedPlayer.getUniqueId());
                plugin.getPanel().openVisitorsPanel(wrappedPlayer, e.getRawSlot() == nextPage ? currentPage + 1 : currentPage - 1);
            }

            else{
                if(e.getCurrentItem() == null)
                    return;

                if(e.getCurrentItem().hasItemMeta()) {
                    WrappedPlayer targetPlayer = WrappedPlayer.of(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()));

                    if (targetPlayer != null) {
                        if (e.getClick().name().contains("RIGHT")) {
                            Bukkit.dispatchCommand(wrappedPlayer.asPlayer(), "island invite " + targetPlayer.getName());
                        } else if (e.getClick().name().contains("LEFT")) {
                            Bukkit.dispatchCommand(wrappedPlayer.asPlayer(), "island expel " + targetPlayer.getName());
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

                    if (wrappedPlayer.hasPermission(permission) && slot == e.getRawSlot()) {
                        wrappedPlayer.asPlayer().closeInventory();
                        Locale.ISLAND_CREATE_PROCCESS_REQUEST.send(wrappedPlayer);
                        plugin.getGrid().createIsland(wrappedPlayer, schematic);
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

                    if (wrappedPlayer.hasPermission(permission) && slot == e.getRawSlot()) {
                        wrappedPlayer.getIsland().setBiome(biome);
                        Locale.CHANGED_BIOME.send(wrappedPlayer, biomeName);
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

                movingBetweenPages.add(wrappedPlayer.getUniqueId());
                plugin.getPanel().openWarpsPanel(wrappedPlayer, e.getRawSlot() == nextPage ? currentPage + 1 : currentPage - 1);
            }

            else{
                if(e.getCurrentItem() == null)
                    return;

                if(e.getCurrentItem().hasItemMeta()) {
                    String warpName = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
                    Island island = plugin.getPanel().getIsland(wrappedPlayer);
                    Location location = island.getWarpLocation(warpName);

                    if(location != null) {
                        island.warpPlayer(wrappedPlayer, warpName);
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
                        if(e.getAction() == InventoryAction.PICKUP_HALF){
                            Bukkit.dispatchCommand(wrappedPlayer.asPlayer(), "island warp " + island.getOwner().getName());
                        } else {
                            wrappedPlayer.asPlayer().closeInventory();
                            Bukkit.getScheduler().runTaskLater(plugin, () ->
                                plugin.getPanel().openValuesPanel(wrappedPlayer, island), 1L);
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

        WrappedPlayer wrappedPlayer = WrappedPlayer.of(e.getPlayer());
        PanelHandler.PanelType panelType = plugin.getPanel().getOpenedPanelType(wrappedPlayer);
        String title = e.getInventory().getName();

        if(movingBetweenPages.contains(wrappedPlayer.getUniqueId())){
            movingBetweenPages.remove(wrappedPlayer.getUniqueId());
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(panelType == PanelHandler.PanelType.MEMBERS || panelType == PanelHandler.PanelType.VISITORS){
                plugin.getPanel().openPanel(wrappedPlayer);
            }else if(panelType == PanelHandler.PanelType.PLAYER){
                plugin.getPanel().openMembersPanel(wrappedPlayer, 1);
            }else if(panelType == PanelHandler.PanelType.ROLE) {
                plugin.getPanel().openPlayerPanel(wrappedPlayer, WrappedPlayer.of(ChatColor.stripColor(title)));
            }else if(panelType == PanelHandler.PanelType.VALUES){
                plugin.getGrid().openTopIslands(wrappedPlayer);
            }else {
                plugin.getPanel().closeInventory(WrappedPlayer.of(e.getPlayer()));
            }
        }, 1L);
    }

}
