package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Locale;

public class IslandNames {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private IslandNames() {

    }

    public static boolean isValidName(SuperiorPlayer superiorPlayer, Island currentIsland, String islandName) {
        return isValidName(superiorPlayer.asPlayer(), currentIsland, islandName);
    }

    public static boolean isValidName(CommandSender sender, Island currentIsland, String islandName) {
        String coloredName = plugin.getSettings().getIslandNames().isColorSupport() ?
                Formatters.COLOR_FORMATTER.format(islandName) : islandName;
        String strippedName = plugin.getSettings().getIslandNames().isColorSupport() ?
                Formatters.STRIP_COLOR_FORMATTER.format(coloredName) : islandName;
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

        if (currentIsland != null && currentIsland.getName().equalsIgnoreCase(islandName)) {
            Message.SAME_NAME_CHANGE.send(sender);
            return false;
        }

        if (plugin.getGrid().getIsland(islandName) != null) {
            Message.ISLAND_ALREADY_EXIST.send(sender);
            return false;
        }

        return true;
    }

}
