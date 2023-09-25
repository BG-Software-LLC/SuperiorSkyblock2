package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface InternalIslandCommand extends InternalSuperiorCommand {

    @Override
    default void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        Island island;

        if (isSelfIsland()) {
            CommandSender dispatcher = context.getDispatcher();

            // In case canBeExecutedByConsole was set to true for no reason
            if (canBeExecutedByConsole() && !(dispatcher instanceof Player)) {
                Message.CUSTOM.send(context.getDispatcher(), "&cCan be executed only by players!", true);
                return;
            }

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(dispatcher);
            island = superiorPlayer.getIsland();
            if (island == null) {
                Message.INVALID_ISLAND.send(superiorPlayer);
                return;
            }
        } else {
            island = context.getRequiredArgument("island", Island.class);
        }

        execute(plugin, IslandCommandContext.fromContext(context, island));
    }

    boolean isSelfIsland();

    void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context);

}
