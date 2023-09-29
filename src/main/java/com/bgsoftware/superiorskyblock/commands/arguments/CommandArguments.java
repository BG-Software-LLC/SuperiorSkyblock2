package com.bgsoftware.superiorskyblock.commands.arguments;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CommandArguments {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private CommandArguments() {

    }

    public static IslandArgument getIsland(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument) {
        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(argument);
        Island island = targetPlayer == null ? plugin.getGrid().getIsland(argument) : targetPlayer.getIsland();

        if (island == null) {
            if (argument.equalsIgnoreCase(sender.getName())) Message.INVALID_ISLAND.send(sender);
            else if (targetPlayer == null)
                Message.INVALID_ISLAND_OTHER_NAME.send(sender, Formatters.STRIP_COLOR_FORMATTER.format(argument));
            else Message.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
        }

        return new IslandArgument(island, targetPlayer);
    }

    public static SuperiorPlayer getPlayer(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument) {
        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(argument);

        if (targetPlayer == null) Message.INVALID_PLAYER.send(sender, argument);

        return targetPlayer;
    }

    public static Map<String, String> parseArguments(String[] args) {
        Map<String, String> parsedArgs = new HashMap<>();
        String currentKey = null;
        StringBuilder stringBuilder = new StringBuilder();

        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (currentKey != null && stringBuilder.length() > 0) {
                    parsedArgs.put(currentKey, stringBuilder.substring(1));
                }

                currentKey = arg.substring(1).toLowerCase(Locale.ENGLISH);
                stringBuilder = new StringBuilder();
            } else if (currentKey != null) {
                stringBuilder.append(" ").append(arg);
            }
        }

        if (currentKey != null && stringBuilder.length() > 0) {
            parsedArgs.put(currentKey, stringBuilder.substring(1));
        }

        return parsedArgs;
    }

    public static <E, C extends CommandContext> CommandArgument<E> required(String identifier, CommandArgumentType<E> argumentType, Object... displayNames) {
        return plugin.getCommands().createArgument(identifier, argumentType, false, displayNames);
    }

    public static <E, C extends CommandContext> CommandArgument<E> optional(String identifier, CommandArgumentType<E> argumentType, Object... displayNames) {
        return plugin.getCommands().createArgument(identifier, argumentType, true, displayNames);
    }

}
