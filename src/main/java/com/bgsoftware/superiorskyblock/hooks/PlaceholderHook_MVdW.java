package com.bgsoftware.superiorskyblock.hooks;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;

public final class PlaceholderHook_MVdW extends PlaceholderHook {

    PlaceholderHook_MVdW(){
        be.maximvdw.placeholderapi.PlaceholderAPI.registerPlaceholder(plugin, "superior_*", e ->
                parsePlaceholder(e.getOfflinePlayer(), e.getPlaceholder().replace("superior_", "")));
    }

    public static String parse(OfflinePlayer offlinePlayer, String str){
        str = PlaceholderAPI.replacePlaceholders(offlinePlayer, str.replaceAll("\\{(\\d)}", "{%$1}"));
        return str.replace("{%", "{");
    }

}
