package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.commands.InternalIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandWarpArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.warp.SignWarp;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminDelWarp implements InternalIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("delwarp");
    }

    @Override
    public String getPermission() {
        return "superior.admin.delwarp";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_DEL_WARP.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("island", IslandArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME))
                .add(CommandArguments.required("island-warp", IslandWarpArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_WARP_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean isSelfIsland() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        Island island = context.getIsland();
        IslandWarp islandWarp = context.getRequiredArgument("island-warp", IslandWarp.class);

        if (!plugin.getEventsBus().callIslandDeleteWarpEvent(dispatcher, island, islandWarp))
            return;

        island.deleteWarp(islandWarp.getName());
        Message.DELETE_WARP.send(dispatcher, islandWarp.getName());

        ChunksProvider.loadChunk(ChunkPosition.of(islandWarp.getLocation()), ChunkLoadReason.WARP_SIGN_BREAK, chunk -> {
            SignWarp.trySignWarpBreak(islandWarp, dispatcher);
        });
    }

}
