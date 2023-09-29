package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.persistence.IPersistentDataHolder;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;

public class PersistentDataHolderArgumentType implements CommandArgumentType<IPersistentDataHolder> {

    public static final PersistentDataHolderArgumentType INSTANCE = new PersistentDataHolderArgumentType();

    private PersistentDataHolderArgumentType() {

    }

    @Override
    public IPersistentDataHolder parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String name = reader.read();

        String holderType = context.getRequiredArgument("holder-type", String.class);

        IPersistentDataHolder persistentDataHolder;

        if (holderType.equalsIgnoreCase("player")) {
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
        } else if (holderType.equalsIgnoreCase("island")) {
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
        } else {
            throw new CommandSyntaxException("Unknown type: " + holderType);
        }

        return persistentDataHolder;
    }
}
