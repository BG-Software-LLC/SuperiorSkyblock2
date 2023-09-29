package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandWarpArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.warp.SignWarp;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdDelWarp implements InternalPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("delwarp");
    }

    @Override
    public String getPermission() {
        return "superior.island.delwarp";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_DEL_WARP.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("warp-name", IslandWarpArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_WARP_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.DELETE_WARP;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_DELETE_WARP_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(dispatcher);
        Island island = context.getIsland();

        IslandWarp islandWarp = context.getRequiredArgument("warp-name", IslandWarp.class);

        if (!plugin.getEventsBus().callIslandDeleteWarpEvent(superiorPlayer, island, islandWarp))
            return;

        island.deleteWarp(islandWarp.getName());
        Message.DELETE_WARP.send(superiorPlayer, islandWarp.getName());

        ChunksProvider.loadChunk(ChunkPosition.of(islandWarp.getLocation()), ChunkLoadReason.WARP_SIGN_BREAK, chunk -> {
            SignWarp.trySignWarpBreak(islandWarp, dispatcher);
        });
    }

}
