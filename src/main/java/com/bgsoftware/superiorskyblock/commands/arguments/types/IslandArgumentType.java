package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandSuggestions;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class IslandArgumentType extends AbstractIslandArgumentType<IslandArgumentType.Result> {

    public static final IslandArgumentType INCLUDE_PLAYERS = new IslandArgumentType(true);
    public static final IslandArgumentType NO_PLAYERS = new IslandArgumentType(false);

    private IslandArgumentType(boolean includePlayerSearch) {
        super(includePlayerSearch);
    }

    @Override
    public IslandArgumentType.Result parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String name = reader.read();

        ParseResult parseResult = parseIsland(plugin, context, name);

        return new Result(parseResult.island, parseResult.targetPlayer);
    }

    @Override
    public List<String> getSuggestions(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read().toLowerCase(Locale.ENGLISH);
        List<String> suggestions = new LinkedList<>();
        return CommandSuggestions.getIslandSuggestions(plugin, argument, null, suggestions);
    }

    public static class Result {

        private final Island island;
        @Nullable
        private final SuperiorPlayer targetPlayer;

        public Result(Island island, @Nullable SuperiorPlayer targetPlayer) {
            this.island = island;
            this.targetPlayer = targetPlayer;
        }

        public Island getIsland() {
            return this.island;
        }

        @Nullable
        public SuperiorPlayer getTargetPlayer() {
            return this.targetPlayer;
        }

    }

}
