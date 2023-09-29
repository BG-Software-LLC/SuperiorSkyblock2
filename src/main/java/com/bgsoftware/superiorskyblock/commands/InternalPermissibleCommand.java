package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.context.CommandContextImpl;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface InternalPermissibleCommand extends ISuperiorCommand<IslandCommandContext> {

    @Override
    default IslandCommandContext createContext(SuperiorSkyblockPlugin plugin, CommandContextImpl context) throws CommandSyntaxException {
        CommandSender dispatcher = context.getDispatcher();

        Island island = null;

        if (!canBeExecutedByConsole() || dispatcher instanceof Player) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(dispatcher);
            island = superiorPlayer.getIsland();

            if (island == null) {
                Message.INVALID_ISLAND.send(superiorPlayer);
                throw new CommandSyntaxException("Invalid island");
            }

            if (!superiorPlayer.hasPermission(getPrivilege())) {
                getPermissionLackMessage().send(superiorPlayer, island.getRequiredPlayerRole(getPrivilege()));
                throw new CommandSyntaxException("Missing privilege");
            }
        }

        return IslandCommandContext.fromContext(context, island, null);
    }

    IslandPrivilege getPrivilege();

    Message getPermissionLackMessage();

}
