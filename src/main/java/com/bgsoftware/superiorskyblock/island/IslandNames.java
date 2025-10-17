package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.Locale;

public class IslandNames {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private IslandNames() {

    }

    public static boolean isValidName(SuperiorPlayer superiorPlayer, @Nullable Island currentIsland, String islandName) {
        return isValidName(superiorPlayer.asPlayer(), currentIsland, islandName);
    }

    public static boolean isValidName(CommandSender sender, Island currentIsland, String islandName) {
        String strippedName = Formatters.STRIP_COLOR_FORMATTER.format(islandName);

        int maxLength = plugin.getSettings().getIslandNames().getMaxLength();
        int minLength = plugin.getSettings().getIslandNames().getMinLength();

        if (strippedName.length() > maxLength) {
            Message.NAME_TOO_LONG.send(sender, maxLength);
            return false;
        }

        if (strippedName.length() < minLength) {
            Message.NAME_TOO_SHORT.send(sender, minLength);
            return false;
        }

        if (plugin.getSettings().getIslandNames().isPreventPlayerNames() && plugin.getPlayers().getSuperiorPlayer(strippedName) != null) {
            Message.NAME_SAME_AS_PLAYER.send(sender);
            return false;
        }

        String lookupName = islandName.toLowerCase(Locale.ENGLISH);
        if (plugin.getSettings().getIslandNames().getFilteredNames().stream().anyMatch(lookupName::contains)) {
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

    public static String getNameForDatabase(Island island) {
        return island.getFormattedName().replace(ChatColor.COLOR_CHAR, '&');
    }

    public static String getNameForLookup(String name) {
        return Formatters.STRIP_COLOR_FORMATTER.format(name).toLowerCase(Locale.ENGLISH);
    }

    public static boolean isWarpNameLengthValid(String warpName) {
        return warpName.length() <= getMaxWarpNameLength();
    }

    public static int getMaxWarpNameLength() {
        return 255;
    }

    public static boolean isValidWarpName(SuperiorPlayer superiorPlayer, Island island,
                                          String warpName, boolean sendMessage) {
        if (warpName.isEmpty() || warpName.contains(" ")) {
            if (sendMessage)
                Message.WARP_INVALID_NAME.send(superiorPlayer);
            return false;
        }

        if (!isWarpNameLengthValid(warpName)) {
            if (sendMessage)
                Message.WARP_NAME_TOO_LONG.send(superiorPlayer);
            return false;
        }

        if (island.getWarp(warpName) != null) {
            if (sendMessage)
                Message.WARP_ALREADY_EXIST.send(superiorPlayer);
            return false;
        }

        return true;
    }

    public static boolean isValidWarpCategoryName(SuperiorPlayer superiorPlayer, String categoryName) {
        if (categoryName.isEmpty() || categoryName.contains(" ")) {
            Message.WARP_CATEGORY_INVALID_NAME.send(superiorPlayer);
            return false;
        }

        if (!isWarpNameLengthValid(categoryName)) {
            Message.WARP_CATEGORY_NAME_TOO_LONG.send(superiorPlayer);
            return false;
        }

        return true;
    }

}
