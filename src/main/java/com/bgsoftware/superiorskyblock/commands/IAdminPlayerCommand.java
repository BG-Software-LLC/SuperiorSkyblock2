package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public interface IAdminPlayerCommand extends ISuperiorCommand {

    @Override
    default void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        if (!supportMultiplePlayers()) {
            SuperiorPlayer targetPlayer = CommandArguments.getPlayer(plugin, sender, args[2]);
            if (targetPlayer != null) {
                Island playerIsland = targetPlayer.getIsland();

                if (requireIsland() && playerIsland == null) {
                    Message.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
                    return;
                }

                execute(plugin, sender, targetPlayer, args);
            }
        } else {
            List<SuperiorPlayer> players = CommandArguments.getMultiplePlayers(plugin, sender, args[2]);
            if (!players.isEmpty())
                execute(plugin, sender, players, args);
        }
    }

    @Override
    default List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> tabVariables = new LinkedList<>();

        if (args.length == 3) {
            if (supportMultiplePlayers() && "*".contains(args[2]))
                tabVariables.add("*");
            tabVariables.addAll(CommandTabCompletes.getOnlinePlayers(plugin, args[2], false));
        } else if (args.length > 3) {
            if (supportMultiplePlayers()) {
                tabVariables = adminTabComplete(plugin, sender, null, args);
            } else {
                SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(args[2]);
                if (targetPlayer != null) {
                    tabVariables = adminTabComplete(plugin, sender, targetPlayer, args);
                }
            }
        }

        return Collections.unmodifiableList(tabVariables);
    }

    boolean supportMultiplePlayers();

    default boolean requireIsland() {
        return false;
    }

    default void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, String[] args) {
        // Not all commands should implement this method.
    }

    default void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, List<SuperiorPlayer> targetPlayers, String[] args) {
        // Not all commands should implement this method.
    }

    default List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, String[] args) {
        return Collections.emptyList();
    }

}
