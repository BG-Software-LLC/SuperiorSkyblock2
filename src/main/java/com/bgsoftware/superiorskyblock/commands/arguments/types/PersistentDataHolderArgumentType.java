package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.persistence.IPersistentDataHolder;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandSuggestions;
import com.bgsoftware.superiorskyblock.commands.arguments.SuggestionsSelectors;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PersistentDataHolderArgumentType implements CommandArgumentType<IPersistentDataHolder> {

    public static final PersistentDataHolderArgumentType INSTANCE = new PersistentDataHolderArgumentType();

    private PersistentDataHolderArgumentType() {

    }

    public enum HolderType {

        PLAYER,
        ISLAND

    }

    @Override
    public IPersistentDataHolder parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String name = reader.read();

        HolderType holderType = context.getOptionalArgument("holder-type", HolderType.class).orElse(null);

        if (holderType == null) {
            throw new CommandSyntaxException("Unknown type: " + context.getInputArgument("holder-type"));
        }

        IPersistentDataHolder persistentDataHolder;

        switch (holderType) {
            case PLAYER: {
                SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(name);
                if (superiorPlayer == null) {
                    if (name.equalsIgnoreCase(context.getDispatcher().getName())) {
                        Message.INVALID_ISLAND.send(context.getDispatcher());
                    } else {
                        Message.INVALID_ISLAND_OTHER.send(context.getDispatcher(), name);
                    }

                    throw new CommandSyntaxException("Invalid player");
                }

                persistentDataHolder = superiorPlayer;
                break;
            }
            case ISLAND: {
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

                    persistentDataHolder = superiorPlayer;
                } else {
                    Island island = plugin.getGrid().getIsland(name);
                    if (island == null) {
                        if (name.equalsIgnoreCase(context.getDispatcher().getName())) {
                            Message.INVALID_ISLAND.send(context.getDispatcher());
                        } else {
                            Message.INVALID_ISLAND_OTHER_NAME.send(context.getDispatcher(), Formatters.STRIP_COLOR_FORMATTER.format(name));
                        }

                        throw new CommandSyntaxException("Missing island");
                    }

                    persistentDataHolder = island;
                }

                break;
            }

            default:
                throw new AssertionError();
        }

        return persistentDataHolder;
    }

    @Override
    public List<String> getSuggestions(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String name = reader.read();

        HolderType holderType = context.getOptionalArgument("holder-type", HolderType.class).orElse(null);

        if (holderType == null)
            return Collections.emptyList();

        List<String> suggestions = new LinkedList<>();

        switch (holderType) {
            case ISLAND:
                CommandSuggestions.getIslandSuggestions(plugin, name, null, suggestions);
                return CommandSuggestions.getPlayerSuggestions(plugin, context, name, SuggestionsSelectors.PLAYERS_WITH_ISLAND, suggestions);
            case PLAYER:
                return CommandSuggestions.getPlayerSuggestions(plugin, context, name, null, suggestions);
        }

        return suggestions;
    }

}
