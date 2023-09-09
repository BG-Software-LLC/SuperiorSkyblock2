package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.IslandArgument;
import com.bgsoftware.superiorskyblock.commands.arguments.IslandsListArgument;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public interface IAdminIslandCommand extends ISuperiorCommand {

    @Override
    default void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        if (!supportMultipleIslands()) {
            IslandArgument arguments = CommandArguments.getIsland(plugin, sender, args[2]);
            if (arguments.getIsland() != null)
                execute(plugin, sender, arguments.getSuperiorPlayer(), arguments.getIsland(), args);
        } else {
            IslandsListArgument arguments = CommandArguments.getMultipleIslands(plugin, sender, args[2]);
            if (!arguments.getIslands().isEmpty())
                execute(plugin, sender, arguments.getSuperiorPlayer(), arguments.getIslands(), args);
        }
    }

    @Override
    default List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> tabVariables = new LinkedList<>();

        if (args.length == 3) {
            if (supportMultipleIslands() && "*".contains(args[2]))
                tabVariables.add("*");
            tabVariables.addAll(CommandTabCompletes.getOnlinePlayersWithIslands(plugin, args[2], false, null));
        } else if (args.length > 3) {
            if (supportMultipleIslands()) {
                tabVariables = adminTabComplete(plugin, sender, null, args);
            } else {
                SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(args[2]);
                Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();
                if (island != null) {
                    tabVariables = adminTabComplete(plugin, sender, island, args);
                    if (tabVariables.isEmpty())
                        tabVariables = adminTabComplete(plugin, sender, island, args);
                }
            }
        }

        return tabVariables == null ? Collections.emptyList() : Collections.unmodifiableList(tabVariables);
    }

    boolean supportMultipleIslands();

    default void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer,
                         Island island, String[] args) {
        // Not all commands should implement this method.
    }

    default void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer,
                         List<Island> islands, String[] args) {
        // Not all commands should implement this method.
    }

    default List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return Collections.emptyList();
    }

}
