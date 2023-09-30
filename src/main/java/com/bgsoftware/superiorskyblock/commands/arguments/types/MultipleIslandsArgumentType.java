package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MultipleIslandsArgumentType extends AbstractIslandArgumentType<MultipleIslandsArgumentType.Result> {

    public static final MultipleIslandsArgumentType INCLUDE_PLAYERS = new MultipleIslandsArgumentType(true);
    public static final MultipleIslandsArgumentType NO_PLAYERS = new MultipleIslandsArgumentType(false);

    private MultipleIslandsArgumentType(boolean includePlayerSearch) {
        super(includePlayerSearch);
    }

    @Override
    public Result parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String name = reader.read();

        if (name.equals("*")) {
            return new Result(plugin.getGrid().getIslands(), null);
        }

        ParseResult parseResult = parseIsland(plugin, context, name);

        return new Result(Collections.singletonList(parseResult.island), parseResult.targetPlayer);
    }

    @Override
    public List<String> getSuggestions(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read().toLowerCase(Locale.ENGLISH);

        List<String> suggestions = new LinkedList<>();

        if ("*".contains(argument))
            suggestions.add("*");

        return getIslandSuggestions(plugin, argument, suggestions);
    }

    public static class Result {

        private final List<Island> islands;
        @Nullable
        private final SuperiorPlayer targetPlayer;

        public Result(List<Island> islands, @Nullable SuperiorPlayer targetPlayer) {
            this.islands = islands;
            this.targetPlayer = targetPlayer;
        }

        public List<Island> getIslands() {
            return this.islands;
        }

        @Nullable
        public SuperiorPlayer getTargetPlayer() {
            return this.targetPlayer;
        }

    }

}
