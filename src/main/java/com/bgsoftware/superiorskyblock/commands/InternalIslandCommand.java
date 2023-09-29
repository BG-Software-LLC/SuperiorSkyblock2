package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.CommandContextImpl;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface InternalIslandCommand extends ISuperiorCommand<IslandCommandContext> {

    @Override
    default IslandCommandContext createContext(SuperiorSkyblockPlugin plugin, CommandContextImpl context) throws CommandSyntaxException {
        Island island;
        SuperiorPlayer targetPlayer;

        if (isSelfIsland()) {
            CommandSender dispatcher = context.getDispatcher();

            // In case canBeExecutedByConsole was set to true for no reason
            if (canBeExecutedByConsole() && !(dispatcher instanceof Player)) {
                Message.CUSTOM.send(context.getDispatcher(), "&cCan be executed only by players!", true);
                throw new CommandSyntaxException("Can only be executed by players");
            }

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(dispatcher);
            island = superiorPlayer.getIsland();
            if (island == null) {
                Message.INVALID_ISLAND.send(superiorPlayer);
                throw new CommandSyntaxException("Invalid island");
            }

            targetPlayer = null;
        } else {
            IslandArgumentType.Result islandResult = context.getRequiredArgument("island", IslandArgumentType.Result.class);
            island = islandResult.getIsland();
            targetPlayer = islandResult.getTargetPlayer();
        }

        return IslandCommandContext.fromContext(context, island, targetPlayer);
    }

    boolean isSelfIsland();

}
