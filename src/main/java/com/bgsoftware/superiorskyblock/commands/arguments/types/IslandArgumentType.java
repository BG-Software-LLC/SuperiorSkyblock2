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

public class IslandArgumentType implements CommandArgumentType<IslandArgumentType.Result, CommandContext> {

    public static final IslandArgumentType INCLUDE_PLAYERS = new IslandArgumentType(true);
    public static final IslandArgumentType NO_PLAYERS = new IslandArgumentType(false);

    private final boolean includePlayerSearch;

    private IslandArgumentType(boolean includePlayerSearch) {
        this.includePlayerSearch = includePlayerSearch;
    }

    @Override
    public IslandArgumentType.Result parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String name = reader.read();

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

                return new Result(island, superiorPlayer);
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

        return new Result(island, null);
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
