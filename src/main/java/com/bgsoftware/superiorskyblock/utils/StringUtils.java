package com.bgsoftware.superiorskyblock.utils;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.formatting.Formatters;
import com.bgsoftware.superiorskyblock.lang.Message;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class StringUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private StringUtils() {

    }

    public static boolean isBlank(@Nullable String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isValidName(SuperiorPlayer superiorPlayer, Island currentIsland, String islandName) {
        return isValidName(superiorPlayer.asPlayer(), currentIsland, islandName);
    }

    public static boolean isValidName(CommandSender sender, Island currentIsland, String islandName) {
        String coloredName = plugin.getSettings().getIslandNames().isColorSupport() ?
                Formatters.COLOR_FORMATTER.format(islandName) : islandName;
        String strippedName = plugin.getSettings().getIslandNames().isColorSupport() ?
                Formatters.STRIP_COLOR_FORMATTER.format(coloredName) : islandName;

        if (strippedName.length() > plugin.getSettings().getIslandNames().getMaxLength()) {
            Message.NAME_TOO_LONG.send(sender);
            return false;
        }

        if (strippedName.length() < plugin.getSettings().getIslandNames().getMinLength()) {
            Message.NAME_TOO_SHORT.send(sender);
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

    public static String removeNonAlphabet(String text, List<Character> allowedChars) {
        StringBuilder newText = new StringBuilder();

        for (char ch : text.toCharArray()) {
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') ||
                    (ch >= '1' && ch <= '9') || allowedChars.contains(ch))
                newText.append(ch);
        }

        return newText.toString();
    }

}
