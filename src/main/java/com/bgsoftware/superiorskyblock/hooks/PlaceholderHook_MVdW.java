package com.bgsoftware.superiorskyblock.hooks;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import be.maximvdw.placeholderapi.events.PlaceholderAddedEvent;
import be.maximvdw.placeholderapi.internal.PlaceholderPack;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public final class PlaceholderHook_MVdW extends PlaceholderHook {

    PlaceholderHook_MVdW(){
        for(IslandPermission islandPermission : IslandPermission.values()) {
            registerPlaceholder("superior_island_permission_" + islandPermission.name());
            registerPlaceholder("superior_island_location_permission_" + islandPermission.name());
        }

        for(String upgrade : plugin.getUpgrades().getAllUpgrades()) {
            registerPlaceholder("superior_island_upgrade_" + upgrade);
            registerPlaceholder("superior_island_location_upgrade_" + upgrade);
        }

        registerPlaceholder("superior_island_center");
        registerPlaceholder("superior_island_x");
        registerPlaceholder("superior_island_y");
        registerPlaceholder("superior_island_z");
        registerPlaceholder("superior_island_world");
        registerPlaceholder("superior_island_team_size");
        registerPlaceholder("superior_island_team_limit");
        registerPlaceholder("superior_island_leader");
        registerPlaceholder("superior_island_size");
        registerPlaceholder("superior_island_radius");
        registerPlaceholder("superior_island_biome");
        registerPlaceholder("superior_island_level");
        registerPlaceholder("superior_island_level_format");
        registerPlaceholder("superior_island_worth");
        registerPlaceholder("superior_island_worth_format");
        registerPlaceholder("superior_island_raw_worth");
        registerPlaceholder("superior_island_raw_worth_format");
        registerPlaceholder("superior_island_bank");
        registerPlaceholder("superior_island_bank_format");
        registerPlaceholder("superior_island_hoppers_limit");
        registerPlaceholder("superior_island_crops_multiplier");
        registerPlaceholder("superior_island_spawners_multiplier");
        registerPlaceholder("superior_island_drops_multiplier");
        registerPlaceholder("superior_island_discord");
        registerPlaceholder("superior_island_paypal");
        registerPlaceholder("superior_island_discord_all");
        registerPlaceholder("superior_island_paypal_all");
        registerPlaceholder("superior_island_exists");
        registerPlaceholder("superior_island_locked");
        registerPlaceholder("superior_island_name");

        registerPlaceholder("superior_island_location_center");
        registerPlaceholder("superior_island_location_x");
        registerPlaceholder("superior_island_location_y");
        registerPlaceholder("superior_island_location_z");
        registerPlaceholder("superior_island_location_world");
        registerPlaceholder("superior_island_location_team_size");
        registerPlaceholder("superior_island_location_team_limit");
        registerPlaceholder("superior_island_location_leader");
        registerPlaceholder("superior_island_location_size");
        registerPlaceholder("superior_island_location_radius");
        registerPlaceholder("superior_island_location_biome");
        registerPlaceholder("superior_island_location_level");
        registerPlaceholder("superior_island_location_level_format");
        registerPlaceholder("superior_island_location_worth");
        registerPlaceholder("superior_island_location_worth_format");
        registerPlaceholder("superior_island_location_raw_worth");
        registerPlaceholder("superior_island_location_raw_worth_format");
        registerPlaceholder("superior_island_location_bank");
        registerPlaceholder("superior_island_location_bank_format");
        registerPlaceholder("superior_island_location_hoppers_limit");
        registerPlaceholder("superior_island_location_crops_multiplier");
        registerPlaceholder("superior_island_location_spawners_multiplier");
        registerPlaceholder("superior_island_location_drops_multiplier");
        registerPlaceholder("superior_island_location_discord");
        registerPlaceholder("superior_island_location_paypal");
        registerPlaceholder("superior_island_location_discord_all");
        registerPlaceholder("superior_island_location_paypal_all");
        registerPlaceholder("superior_island_location_exists");
        registerPlaceholder("superior_island_location_locked");
        registerPlaceholder("superior_island_location_name");
    }

    //Register placeholder without logging to console
    private void registerPlaceholder(String placeholder){
        PlaceholderPack customPlaceholders = getPlaceholderPack();
        List<PlaceholderAddedEvent> placeholderAddedHandlers = getEventHandlers();

        if(customPlaceholders == null || placeholderAddedHandlers == null)
            return;

        Iterator var3 = placeholderAddedHandlers.iterator();
        PlaceholderReplacer replacer = e -> parsePlaceholder(e.getPlayer(), e.getPlaceholder().replace("superior_", ""));

        while(var3.hasNext()) {
            PlaceholderAddedEvent event = (PlaceholderAddedEvent)var3.next();
            event.onPlaceholderAdded(plugin, placeholder.toLowerCase(), replacer);
        }

        //noinspection all
        customPlaceholders.addOfflinePlaceholder(placeholder, "Custom MVdWPlaceholderAPI placeholder", false, new be.maximvdw.placeholderapi.internal.PlaceholderReplacer<String>(String.class, new Object[]{replacer}) {
            public String getResult(String placeholder, OfflinePlayer player) {
                PlaceholderReplacer replacer = (PlaceholderReplacer)this.getArguments()[0];
                PlaceholderReplaceEvent event = new PlaceholderReplaceEvent(player, placeholder);
                return replacer.onPlaceholderReplace(event);
            }
        });
    }

    private PlaceholderPack getPlaceholderPack(){
        try {
            Field packField = PlaceholderAPI.class.getDeclaredField("customPlaceholders");
            packField.setAccessible(true);
            Object obj = packField.get(null);
            packField.setAccessible(false);
            return (PlaceholderPack) obj;
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    private List<PlaceholderAddedEvent> getEventHandlers(){
        try {
            Field eventHandlersField = PlaceholderAPI.class.getDeclaredField("placeholderAddedHandlers");
            eventHandlersField.setAccessible(true);
            Object obj = eventHandlersField.get(null);
            eventHandlersField.setAccessible(false);
            //noinspection unchecked
            return (List<PlaceholderAddedEvent>) obj;
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

}
