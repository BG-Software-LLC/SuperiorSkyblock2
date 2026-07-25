package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.IslandArgument;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public interface IPermissibleCommand extends ISuperiorCommand {

    @Override
    default void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        Island island = null;
        SuperiorPlayer superiorPlayer = null;

        if (!canBeExecutedByConsole() || sender instanceof Player) {
            IslandArgument arguments = CommandArguments.getSenderIsland(plugin, sender);

            island = arguments.getIsland();

            if (island == null)
                return;

            superiorPlayer = arguments.getSuperiorPlayer();

            if (!superiorPlayer.hasPermission(getPrivilege())) {
                getPermissionLackMessage().send(superiorPlayer, island.getRequiredPlayerRole(getPrivilege()));
                return;
            }
        }

        execute(plugin, superiorPlayer, island, args);
    }

    @Override
    default List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        Island island = null;
        SuperiorPlayer superiorPlayer = null;

        if (!canBeExecutedByConsole() || sender instanceof Player) {
            superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
            island = superiorPlayer.getIsland();
        }

        return superiorPlayer == null || (island != null && superiorPlayer.hasPermission(getPrivilege())) ?
                tabComplete(plugin, superiorPlayer, island, args) : Collections.emptyList();
    }

    IslandPrivilege getPrivilege();

    Message getPermissionLackMessage();

    void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args);

    default List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return Collections.emptyList();
    }

}
