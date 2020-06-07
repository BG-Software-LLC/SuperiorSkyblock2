package com.bgsoftware.superiorskyblock.hooks;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import be.maximvdw.placeholderapi.events.PlaceholderAddedEvent;
import be.maximvdw.placeholderapi.internal.PlaceholderPack;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

public final class PlaceholderHook_MVdW extends PlaceholderHook {

    PlaceholderHook_MVdW(){
        for(IslandPrivilege islandPrivilege : IslandPrivilege.values()) {
            registerPlaceholder("superior_island_permission_" + islandPrivilege.getName());
            registerPlaceholder("superior_island_location_permission_" + islandPrivilege.getName());
        }

        for(Upgrade upgrade : plugin.getUpgrades().getUpgrades()) {
            registerPlaceholder("superior_island_upgrade_" + upgrade.getName());
            registerPlaceholder("superior_island_location_upgrade_" + upgrade.getName());
        }

        registerPlaceholder("superior_player_texture");
        registerPlaceholder("superior_player_role");
        registerPlaceholder("superior_player_locale");
        registerPlaceholder("superior_player_world_border");
        registerPlaceholder("superior_player_blocks_stacker");
        registerPlaceholder("superior_player_schematics");
        registerPlaceholder("superior_player_team_chat");
        registerPlaceholder("superior_player_bypass");
        registerPlaceholder("superior_player_disbands");
        registerPlaceholder("superior_player_panel");
        registerPlaceholder("superior_player_fly");
        registerPlaceholder("superior_player_chat_spy");
        registerPlaceholder("superior_player_border_color");
        registerPlaceholder("superior_player_missions_completed");

        registerPlaceholder("superior_island_center");
        registerPlaceholder("superior_island_x");
        registerPlaceholder("superior_island_y");
        registerPlaceholder("superior_island_z");
        registerPlaceholder("superior_island_world");
        registerPlaceholder("superior_island_team_size");
        registerPlaceholder("superior_island_team_limit");
        registerPlaceholder("superior_island_leader");
        registerPlaceholder("superior_island_size_format");
        registerPlaceholder("superior_island_size");
        registerPlaceholder("superior_island_radius");
        registerPlaceholder("superior_island_biome");
        registerPlaceholder("superior_island_level");
        registerPlaceholder("superior_island_level_raw");
        registerPlaceholder("superior_island_level_format");
        registerPlaceholder("superior_island_level_int");
        registerPlaceholder("superior_island_worth");
        registerPlaceholder("superior_island_worth_raw");
        registerPlaceholder("superior_island_worth_format");
        registerPlaceholder("superior_island_worth_int");
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
        registerPlaceholder("superior_island_name_leader");
        registerPlaceholder("superior_island_is_leader");
        registerPlaceholder("superior_island_is_member");
        registerPlaceholder("superior_island_is_coop");
        registerPlaceholder("superior_island_rating");
        registerPlaceholder("superior_island_rating_stars");
        registerPlaceholder("superior_island_warps_limit");
        registerPlaceholder("superior_island_warps");

        registerPlaceholder("superior_island_location_center");
        registerPlaceholder("superior_island_location_x");
        registerPlaceholder("superior_island_location_y");
        registerPlaceholder("superior_island_location_z");
        registerPlaceholder("superior_island_location_world");
        registerPlaceholder("superior_island_location_team_size");
        registerPlaceholder("superior_island_location_team_limit");
        registerPlaceholder("superior_island_location_leader");
        registerPlaceholder("superior_island_location_size_format");
        registerPlaceholder("superior_island_location_size");
        registerPlaceholder("superior_island_location_radius");
        registerPlaceholder("superior_island_location_biome");
        registerPlaceholder("superior_island_location_level");
        registerPlaceholder("superior_island_location_level_raw");
        registerPlaceholder("superior_island_location_level_format");
        registerPlaceholder("superior_island_location_level_int");
        registerPlaceholder("superior_island_location_worth");
        registerPlaceholder("superior_island_location_worth_raw");
        registerPlaceholder("superior_island_location_worth_format");
        registerPlaceholder("superior_island_location_worth_int");
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
        registerPlaceholder("superior_island_location_name_leader");
        registerPlaceholder("superior_island_location_is_leader");
        registerPlaceholder("superior_island_location_is_member");
        registerPlaceholder("superior_island_location_is_coop");
        registerPlaceholder("superior_island_location_rating");
        registerPlaceholder("superior_island_location_rating_stars");
        registerPlaceholder("superior_island_location_warps_limit");
        registerPlaceholder("superior_island_location_warps");
    }

    //Register placeholder without logging to console
    private void registerPlaceholder(String placeholder){
        PlaceholderPack customPlaceholders = getPlaceholderPack();
        List<PlaceholderAddedEvent> placeholderAddedHandlers = getEventHandlers();

        if(customPlaceholders == null || placeholderAddedHandlers == null)
            return;

        Iterator var3 = placeholderAddedHandlers.iterator();
        PlaceholderReplacer replacer = e -> parsePlaceholder(e.getOfflinePlayer(), e.getPlaceholder().replace("superior_", ""));

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

    public static String parse(OfflinePlayer offlinePlayer, String str){
        str = PlaceholderAPI.replacePlaceholders(offlinePlayer, str.replaceAll("\\{(\\d)}", "{%$1}"));
        return str.replace("{%", "{");
    }

}
