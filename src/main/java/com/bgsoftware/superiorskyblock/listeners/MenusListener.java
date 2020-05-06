package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenuSettings;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class MenusListener implements Listener {

    private final Registry<UUID, ItemStack> latestClickedItem = Registry.createRegistry();

    /**
     * The following two events are here for patching a dupe glitch caused
     * by shift clicking and closing the inventory in the same time.
     */

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClickMonitor(InventoryClickEvent e){
        if(e.getCurrentItem() != null && e.isCancelled() && e.getInventory().getHolder() instanceof SuperiorMenu) {
            latestClickedItem.add(e.getWhoClicked().getUniqueId(), e.getCurrentItem());
            Executor.sync(() -> latestClickedItem.remove(e.getWhoClicked().getUniqueId()), 20L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryCloseMonitor(InventoryCloseEvent e){
        if(latestClickedItem.containsKey(e.getPlayer().getUniqueId())){
            ItemStack clickedItem = latestClickedItem.get(e.getPlayer().getUniqueId());
            Executor.sync(() -> {
                e.getPlayer().getInventory().removeItem(clickedItem);
                ((Player) e.getPlayer()).updateInventory();
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMenuClick(InventoryClickEvent e){
        InventoryHolder inventoryHolder = e.getClickedInventory() == null || e.getView().getTopInventory() == null ? null : e.getView().getTopInventory().getHolder();

        if(!(inventoryHolder instanceof SuperiorMenu) || !(e.getWhoClicked() instanceof Player))
            return;

        e.setCancelled(true);

        if(e.getClickedInventory().equals(e.getView().getTopInventory()))
            ((SuperiorMenu) inventoryHolder).onClick(e);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMenuClose(InventoryCloseEvent e){
        InventoryHolder inventoryHolder = e.getInventory() == null ? null : e.getInventory().getHolder();

        if(!(inventoryHolder instanceof SuperiorMenu) || !(e.getPlayer() instanceof Player))
            return;

        ((SuperiorMenu) inventoryHolder).closeInventory(SSuperiorPlayer.of(e.getPlayer()));
    }

    /**
     * Settings menu listeners
     */

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e){
        if(!SuperiorMenuSettings.configValues.containsKey(e.getPlayer().getUniqueId()))
            return;

        e.setCancelled(true);

        String path = SuperiorMenuSettings.configValues.get(e.getPlayer().getUniqueId());
        Object value = e.getMessage();

        if(!value.toString().equalsIgnoreCase("-cancel")){
            if(SuperiorMenuSettings.config.isConfigurationSection(path)){
                Matcher matcher;
                if(!(matcher = Pattern.compile("(.*):(.*)").matcher(value.toString())).matches()){
                    e.getPlayer().sendMessage(ChatColor.RED + "Please follow the <sub-section>:<value> format");
                }else {
                    String key = matcher.group(1);
                    path = path + "." + matcher.group(1);
                    value = matcher.group(2);

                    if(SuperiorMenuSettings.config.get(path) != null && SuperiorMenuSettings.config.get(path).toString().equals(value.toString())){
                        e.getPlayer().sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Removed the value " + matcher.group(1) + " from " + path);
                        value = null;
                    }else{
                        e.getPlayer().sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Added the value " + value.toString() + " to " + path);

                        try{
                            value = Integer.valueOf(value.toString());
                        }catch(IllegalArgumentException ex){
                            if(value.toString().equalsIgnoreCase("true") || value.toString().equalsIgnoreCase("false")){
                                value = Boolean.valueOf(value.toString());
                            }
                        }

                    }

                    SuperiorMenuSettings.config.set(path, value);
                }
            }

            else if(SuperiorMenuSettings.config.isList(path)){
                List<String> list = SuperiorMenuSettings.config.getStringList(path);

                if (list.contains(value.toString())) {
                    list.remove(value.toString());
                    e.getPlayer().sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Removed the value " + value.toString() + " from " + path);
                } else {
                    list.add(value.toString());
                    e.getPlayer().sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Added the value " + value.toString() + " to " + path);
                }

                SuperiorMenuSettings.config.set(path, list);
            }

            else{
                boolean valid = true;
                if(SuperiorMenuSettings.config.isInt(path)){
                    try{
                        value = Integer.valueOf(value.toString());
                    }catch(IllegalArgumentException ex){
                        e.getPlayer().sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                }

                else if(SuperiorMenuSettings.config.isDouble(path)){
                    try{
                        value = Double.valueOf(value.toString());
                    }catch(IllegalArgumentException ex){
                        e.getPlayer().sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                }

                else if(SuperiorMenuSettings.config.isBoolean(path)){
                    if(value.toString().equalsIgnoreCase("true") || value.toString().equalsIgnoreCase("false")){
                        value = Boolean.valueOf(value.toString());
                    }else{
                        e.getPlayer().sendMessage(ChatColor.RED + "Please specify a valid boolean");
                        valid = false;
                    }
                }

                if(valid) {
                    SuperiorMenuSettings.config.set(path, value);
                    e.getPlayer().sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Changed value of " + path + " to " + value.toString());
                }
            }
        }

        Executor.sync(() -> {
            int page = SuperiorMenuSettings.lastPage.remove(e.getPlayer().getUniqueId());
            SuperiorMenuSettings.openInventory(SSuperiorPlayer.of(e.getPlayer()), null, Math.max(1, page));
            SuperiorMenuSettings.configValues.remove(e.getPlayer().getUniqueId());
        });
    }

}
