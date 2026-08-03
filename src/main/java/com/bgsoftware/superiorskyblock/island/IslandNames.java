package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.config.StringMatcher;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsDispatcher;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Locale;

public class IslandNames {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static StringMatcher blacklistedIslandNamesCache = null;
    private static StringMatcher blacklistedWarpNamesCache = null;

    private IslandNames() {

    }

    public static boolean isValidName(SuperiorPlayer superiorPlayer, @Nullable Island currentIsland, String islandName) {
        return isValidName(superiorPlayer.asPlayer(), currentIsland, islandName);
    }

    public static boolean isValidName(CommandSender sender, Island currentIsland, String islandName) {
        String strippedName = Formatters.STRIP_COLOR_FORMATTER.format(islandName);

        int maxLength = plugin.getSettings().getIslandNames().getMaxLength();
        if (strippedName.length() > maxLength) {
            Message.NAME_TOO_LONG.send(sender, maxLength);
            return false;
        }

        int minLength = plugin.getSettings().getIslandNames().getMinLength();
        if (strippedName.length() < minLength) {
            Message.NAME_TOO_SHORT.send(sender, minLength);
            return false;
        }

        if (plugin.getSettings().getIslandNames().isPreventPlayerNames() && plugin.getPlayers().getSuperiorPlayer(strippedName) != null) {
            Message.NAME_SAME_AS_PLAYER.send(sender);
            return false;
        }

        if (isIslandNameBlacklisted(islandName.toLowerCase(Locale.ENGLISH))) {
            Message.NAME_BLACKLISTED.send(sender);
            return false;
        }

        if (currentIsland != null) {
            String formattedName = Formatters.COLOR_FORMATTER.format(islandName);

            if (currentIsland.getFormattedName().equalsIgnoreCase(formattedName)) {
                Message.SAME_NAME_CHANGE.send(sender);
                return false;
            }
        }

        Island islandWithName = plugin.getGrid().getIsland(strippedName);
        if (islandWithName != null && islandWithName != currentIsland) {
            Message.ISLAND_ALREADY_EXIST.send(sender);
            return false;
        }

        return true;
    }

    public static void announceChange(Island island, Message message, Object... args) {
        if (plugin.getSettings().getIslandNames().isAnnounceChangeToAll())
            for (Player player : Bukkit.getOnlinePlayers())
                message.send(player, args);
        else
            IslandUtils.sendMessage(island, message, Collections.emptyList(), args);
    }

    public static String getNameForDatabase(Island island) {
        return island.getFormattedName().replace(ChatColor.COLOR_CHAR, '&');
    }

    public static String getNameForLookup(String name) {
        return Formatters.STRIP_COLOR_FORMATTER.format(name).toLowerCase(Locale.ENGLISH);
    }

    public static boolean isWarpNameLengthValid(String warpName) {
        return !Text.isBlank(warpName) && warpName.length() >= BuiltinModules.WARPS.getConfiguration().getNamesMinLength()
                && warpName.length() <= BuiltinModules.WARPS.getConfiguration().getNamesMaxLength();
    }

    public static boolean isValidWarpName(@Nullable SuperiorPlayer superiorPlayer, Island island, @Nullable String warpName) {
        if (Text.isBlank(warpName) || warpName.indexOf(" ") > 0) {
            Message.WARP_NAME_INVALID.send(superiorPlayer);
            return false;
        }

        int maxLength = BuiltinModules.WARPS.getConfiguration().getNamesMaxLength();
        if (warpName.length() > maxLength) {
            Message.WARP_NAME_TOO_LONG.send(superiorPlayer, maxLength);
            return false;
        }

        int minLength = BuiltinModules.WARPS.getConfiguration().getNamesMinLength();
        if (warpName.length() < minLength) {
            Message.WARP_NAME_TOO_SHORT.send(superiorPlayer, minLength);
            return false;
        }

        if (isWarpNameBlacklisted(warpName.toLowerCase(Locale.ENGLISH))) {
            Message.WARP_NAME_BLACKLISTED.send(superiorPlayer);
            return false;
        }

        if (island.getWarp(warpName) != null) {
            Message.WARP_ALREADY_EXIST.send(superiorPlayer);
            return false;
        }

        return true;
    }

    public static boolean isValidWarpCategoryName(@Nullable SuperiorPlayer superiorPlayer, @Nullable String categoryName) {
        if (Text.isBlank(categoryName) || categoryName.indexOf(" ") > 0) {
            Message.WARP_CATEGORY_NAME_INVALID.send(superiorPlayer);
            return false;
        }

        int maxLength = BuiltinModules.WARPS.getConfiguration().getNamesMaxLength();
        if (categoryName.length() > maxLength) {
            Message.WARP_CATEGORY_NAME_TOO_LONG.send(superiorPlayer);
            return false;
        }

        int minLength = BuiltinModules.WARPS.getConfiguration().getNamesMinLength();
        if (categoryName.length() < minLength) {
            Message.WARP_CATEGORY_NAME_TOO_SHORT.send(superiorPlayer);
            return false;
        }

        if (isWarpNameBlacklisted(categoryName.toLowerCase(Locale.ENGLISH))) {
            Message.WARP_CATEGORY_NAME_BLACKLISTED.send(superiorPlayer);
            return false;
        }

        return true;
    }

    public static void registerListeners(PluginEventsDispatcher dispatcher) {
        dispatcher.registerCallback(PluginEventType.SETTINGS_UPDATE_EVENT, IslandNames::onSettingsUpdate);
    }

    private static boolean isIslandNameBlacklisted(String name) {
        if (blacklistedIslandNamesCache == null) {
            blacklistedIslandNamesCache = new StringMatcher(plugin.getSettings().getIslandNames().getFilteredNames());
        }

        return blacklistedIslandNamesCache.matches(name);
    }

    private static boolean isWarpNameBlacklisted(String name) {
        if (blacklistedWarpNamesCache == null) {
            blacklistedWarpNamesCache = new StringMatcher(BuiltinModules.WARPS.getConfiguration().getNamesBlacklist());
        }

        return blacklistedWarpNamesCache.matches(name);
    }

    private static void onSettingsUpdate() {
        blacklistedIslandNamesCache = null;
        blacklistedWarpNamesCache = null;
    }

}
