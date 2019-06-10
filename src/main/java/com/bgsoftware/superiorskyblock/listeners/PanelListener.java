package com.bgsoftware.superiorskyblock.listeners;

import static com.bgsoftware.superiorskyblock.gui.GUIInventory.MAIN_PAGE_IDENTIFIER;
import static com.bgsoftware.superiorskyblock.gui.GUIInventory.MEMBERS_PAGE_IDENTIFIER;
import static com.bgsoftware.superiorskyblock.gui.GUIInventory.VISITORS_PAGE_IDENTIFIER;
import static com.bgsoftware.superiorskyblock.gui.GUIInventory.PLAYER_PAGE_IDENTIFIER;
import static com.bgsoftware.superiorskyblock.gui.GUIInventory.ISLAND_CREATION_PAGE_IDENTIFIER;
import static com.bgsoftware.superiorskyblock.gui.GUIInventory.BIOMES_PAGE_IDENTIFIER;
import static com.bgsoftware.superiorskyblock.gui.GUIInventory.WARPS_PAGE_IDENTIFIER;
import static com.bgsoftware.superiorskyblock.gui.GUIInventory.VALUES_PAGE_IDENTIFIER;
import static com.bgsoftware.superiorskyblock.gui.GUIInventory.ISLAND_TOP_PAGE_IDENTIFIER;
import static com.bgsoftware.superiorskyblock.gui.GUIInventory.ROLE_PAGE_IDENTIFIER;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.gui.GUIIdentifier;
import com.bgsoftware.superiorskyblock.gui.GUIInventory;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private Set<UUID> movingBetweenPages = new HashSet<>();

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
            case MAIN_PAGE_IDENTIFIER: {
                mainPage(e, guiInventory, superiorPlayer);
                break;
            }
            case MEMBERS_PAGE_IDENTIFIER: {
                membersPage(e, guiInventory, superiorPlayer);
                break;
            }
            case VISITORS_PAGE_IDENTIFIER: {
                visitorsPage(e, guiInventory, superiorPlayer);
                break;
            }
            case PLAYER_PAGE_IDENTIFIER: {
                playerPage(e, guiInventory, superiorPlayer);
                break;
            }
            case ROLE_PAGE_IDENTIFIER: {
                rolesPage(e, guiInventory, superiorPlayer);
                break;
            }
            case ISLAND_CREATION_PAGE_IDENTIFIER: {
                islandCreationPage(e, guiInventory, superiorPlayer);
                break;
            }
            case BIOMES_PAGE_IDENTIFIER: {
                biomesPage(e, guiInventory, superiorPlayer);
                break;
            }
            case WARPS_PAGE_IDENTIFIER: {
                warpsPage(e, guiInventory, superiorPlayer);
                break;
            }
            case VALUES_PAGE_IDENTIFIER: {
                break;
            }
            case ISLAND_TOP_PAGE_IDENTIFIER: {
                islandTopPage(e, guiInventory, superiorPlayer);
                break;
            }
        }

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        if(!(e.getPlayer() instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        GUIInventory guiInventory = GUIInventory.from(superiorPlayer);
        String title = e.getInventory().getName();

        if(movingBetweenPages.contains(superiorPlayer.getUniqueId())){
            movingBetweenPages.remove(superiorPlayer.getUniqueId());
            return;
        }

        if(guiInventory == null)
            return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            switch (guiInventory.getIdentifier()){
                case MEMBERS_PAGE_IDENTIFIER:
                case VISITORS_PAGE_IDENTIFIER:
                    plugin.getPanel().openPanel(superiorPlayer);
                    break;
                case PLAYER_PAGE_IDENTIFIER:
                    plugin.getPanel().openMembersPanel(superiorPlayer, 1);
                    break;
                case ROLE_PAGE_IDENTIFIER:
                    plugin.getPanel().openPlayerPanel(superiorPlayer, SSuperiorPlayer.of(ChatColor.stripColor(title)));
                    break;
                case VALUES_PAGE_IDENTIFIER:
                    plugin.getGrid().openTopIslands(superiorPlayer);
                    break;
                default:
                    guiInventory.closeInventory(superiorPlayer);
                    break;
            }
        }, 1L);
    }

    private void mainPage(InventoryClickEvent e, GUIInventory guiInventory, SuperiorPlayer superiorPlayer){
        if(e.getRawSlot() == guiInventory.get("membersSlot", Integer.class)){
            movingBetweenPages.add(superiorPlayer.getUniqueId());
            plugin.getPanel().openMembersPanel(superiorPlayer, 1);
        }

        else if(e.getRawSlot() == guiInventory.get("visitorsSlot", Integer.class)){
            movingBetweenPages.add(superiorPlayer.getUniqueId());
            plugin.getPanel().openVisitorsPanel(superiorPlayer, 1);
        }
    }

    private void membersPage(InventoryClickEvent e, GUIInventory guiInventory, SuperiorPlayer superiorPlayer){
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

    private void visitorsPage(InventoryClickEvent e, GUIInventory guiInventory, SuperiorPlayer superiorPlayer){
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

    private void playerPage(InventoryClickEvent e, GUIInventory guiInventory, SuperiorPlayer superiorPlayer){
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

    private void islandCreationPage(InventoryClickEvent e, GUIInventory guiInventory, SuperiorPlayer superiorPlayer){
        for(String schematic : plugin.getSchematics().getSchematics()){
            if(guiInventory.contains(schematic + "-slot")) {
                int slot = guiInventory.get(schematic + "-slot", Integer.class);
                String permission = guiInventory.get(schematic + "-permission", String.class);

                if (superiorPlayer.hasPermission(permission) && slot == e.getRawSlot()) {
                    BigDecimal bonusWorth = new BigDecimal(guiInventory.get(schematic + "-bonus", Long.class));
                    Biome biome = Biome.valueOf(guiInventory.get(schematic + "-biome", String.class));
                    superiorPlayer.asPlayer().closeInventory();
                    Locale.ISLAND_CREATE_PROCCESS_REQUEST.send(superiorPlayer);
                    plugin.getGrid().createIsland(superiorPlayer, schematic, bonusWorth, biome);
                    break;
                }
            }
        }
    }

    private void biomesPage(InventoryClickEvent e, GUIInventory guiInventory, SuperiorPlayer superiorPlayer){
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

    private void warpsPage(InventoryClickEvent e, GUIInventory guiInventory, SuperiorPlayer superiorPlayer){
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

            Island island = plugin.getPanel().getIsland(superiorPlayer);

            //noinspection unchecked
            List<Integer> slots = plugin.getPanel().warpsPage.get("slots", List.class);

            List<String> warps = new ArrayList<>(island.getAllWarps());
            warps.sort(String::compareTo);

            int indexOf = slots.indexOf(e.getRawSlot());

            if(indexOf >= warps.size() || indexOf == -1)
                return;

            String warpName = warps.get(indexOf);
            Location location = island.getWarpLocation(warpName);

            if(location != null) {
                island.warpPlayer(superiorPlayer, warpName);
            }
        }
    }

    private void islandTopPage(InventoryClickEvent e, GUIInventory guiInventory, SuperiorPlayer superiorPlayer){
        Integer[] slots = plugin.getGrid().getTopIslands().get("slots", Integer[].class);

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

}
