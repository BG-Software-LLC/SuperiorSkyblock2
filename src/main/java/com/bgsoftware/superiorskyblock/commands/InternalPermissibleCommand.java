package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface InternalPermissibleCommand extends InternalSuperiorCommand {

    @Override
    default void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        Island island = null;

        CommandSender dispatcher = context.getDispatcher();

        if (!canBeExecutedByConsole() || dispatcher instanceof Player) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(dispatcher);
            island = superiorPlayer.getIsland();

            if (island == null) {
                Message.INVALID_ISLAND.send(superiorPlayer);
                return;
            }

            if (!superiorPlayer.hasPermission(getPrivilege())) {
                getPermissionLackMessage().send(superiorPlayer, island.getRequiredPlayerRole(getPrivilege()));
                return;
            }
        }

        execute(plugin, IslandCommandContext.fromContext(context, island));
    }

    IslandPrivilege getPrivilege();

    Message getPermissionLackMessage();

    void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context);

}
