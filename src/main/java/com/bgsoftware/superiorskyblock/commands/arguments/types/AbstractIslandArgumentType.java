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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

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

    protected final List<String> getIslandSuggestions(SuperiorSkyblock plugin, String argument, List<String> suggestions) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            SuperiorPlayer onlinePlayer = plugin.getPlayers().getSuperiorPlayer(player);
            if (onlinePlayer.isShownAsOnline()) {
                String name = onlinePlayer.getName();
                if (name.toLowerCase(Locale.ENGLISH).contains(argument))
                    suggestions.add(name);

                Island onlineIsland = onlinePlayer.getIsland();
                if (onlineIsland == null)
                    continue;

                name = onlineIsland.getName();

                if (name.toLowerCase(Locale.ENGLISH).contains(argument))
                    suggestions.add(name);
            }
        }

        return suggestions;
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
