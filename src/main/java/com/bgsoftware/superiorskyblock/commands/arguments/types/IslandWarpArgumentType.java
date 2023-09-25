package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;

public class IslandWarpArgumentType implements CommandArgumentType<IslandWarp, IslandCommandContext> {

    public static final IslandWarpArgumentType INSTANCE = new IslandWarpArgumentType();

    private IslandWarpArgumentType() {

    }

    @Override
    public IslandWarp parse(SuperiorSkyblock plugin, IslandCommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        StringBuilder warpName = new StringBuilder(reader.read());
        while (reader.hasNext())
            warpName.append(" ").append(reader.read());

        Island island = context.getIsland();
        IslandWarp islandWarp = island.getWarp(warpName.toString());

        if (islandWarp == null) {
            Message.INVALID_WARP.send(context.getDispatcher(), warpName);
            throw new CommandSyntaxException("Invalid warp name: " + warpName);
        }

        return islandWarp;
    }

}
