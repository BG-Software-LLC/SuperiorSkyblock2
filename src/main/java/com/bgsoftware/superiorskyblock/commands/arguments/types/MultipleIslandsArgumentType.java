package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Collections;
import java.util.List;

public class MultipleIslandsArgumentType implements CommandArgumentType<MultipleIslandsArgumentType.Result> {

    public static final MultipleIslandsArgumentType INCLUDE_PLAYERS = new MultipleIslandsArgumentType(true);
    public static final MultipleIslandsArgumentType NO_PLAYERS = new MultipleIslandsArgumentType(false);

    private final boolean includePlayerSearch;

    private MultipleIslandsArgumentType(boolean includePlayerSearch) {
        this.includePlayerSearch = includePlayerSearch;
    }

    @Override
    public Result parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String name = reader.read();

        if (name.equals("*")) {
            return new Result(plugin.getGrid().getIslands(), null);
        }

        if (this.includePlayerSearch) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(name);
            if (superiorPlayer != null) {
                Island island = superiorPlayer.getIsland();
                if (island == null) {
                    if (name.equalsIgnoreCase(context.getDispatcher().getName())) {
                        Message.INVALID_ISLAND.send(context.getDispatcher());
                    } else {
                        Message.INVALID_ISLAND_OTHER.send(context.getDispatcher(), superiorPlayer.getName());
                    }

                    throw new CommandSyntaxException("Missing island");
                }

                return new Result(Collections.singletonList(island), superiorPlayer);
            }
        }

        Island island = plugin.getGrid().getIsland(name);
        if (island == null) {
            if (name.equalsIgnoreCase(context.getDispatcher().getName())) {
                Message.INVALID_ISLAND.send(context.getDispatcher());
            } else {
                Message.INVALID_ISLAND_OTHER_NAME.send(context.getDispatcher(), Formatters.STRIP_COLOR_FORMATTER.format(name));
            }

            throw new CommandSyntaxException("Missing island");
        }

        return new Result(Collections.singletonList(island), null);
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
