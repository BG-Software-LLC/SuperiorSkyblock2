package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class WarpCategoryArgumentType implements CommandArgumentType<IslandWarp> {

    public static final WarpCategoryArgumentType INSTANCE = new WarpCategoryArgumentType();

    private WarpCategoryArgumentType() {

    }

    @Override
    public IslandWarp parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        StringBuilder warpName = new StringBuilder(reader.read());
        while (reader.hasNext()) warpName.append(" ").append(reader.read());

        Island island = context.getRequiredArgument("island", IslandArgumentType.Result.class).getIsland();
        IslandWarp islandWarp = island.getWarp(warpName.toString());

        if (islandWarp == null) {
            Message.INVALID_WARP.send(context.getDispatcher(), warpName);
            throw new CommandSyntaxException("Invalid warp name: " + warpName);
        }

        return islandWarp;
    }

    @Override
    public List<String> getSuggestions(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read().toLowerCase(Locale.ENGLISH);

        Island island = context.getRequiredArgument("island", IslandArgumentType.Result.class).getIsland();

        List<String> suggestions = new LinkedList<>();

        for (String warpCategoryName : island.getWarpCategories().keySet()) {
            warpCategoryName = warpCategoryName.toLowerCase(Locale.ENGLISH);
            if (warpCategoryName.contains(argument))
                suggestions.add(warpCategoryName);
        }

        return suggestions;
    }

}
