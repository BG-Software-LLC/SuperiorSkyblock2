package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;

public abstract class AbstractIslandArgumentType<E> implements CommandArgumentType<E> {

    private final boolean includePlayerSearch;

    protected AbstractIslandArgumentType(boolean includePlayerSearch) {
        this.includePlayerSearch = includePlayerSearch;
    }

    protected final ParseResult parseIsland(SuperiorSkyblock plugin, CommandContext context, String name) throws CommandSyntaxException {
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

                return new ParseResult(island, superiorPlayer);
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

        return new ParseResult(island, null);
    }

    protected static class ParseResult {

        protected final Island island;
        @Nullable
        protected final SuperiorPlayer targetPlayer;

        ParseResult(Island island, @Nullable SuperiorPlayer targetPlayer) {
            this.island = island;
            this.targetPlayer = targetPlayer;
        }

    }

}
