package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;

public class IslandRoleArgumentType implements CommandArgumentType<PlayerRole> {

    public static final IslandRoleArgumentType INSTANCE = new IslandRoleArgumentType();

    @Override
    public PlayerRole parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read();
        PlayerRole playerRole = null;

        try {
            playerRole = SPlayerRole.of(argument);
        } catch (IllegalArgumentException ignored) {
        }

        if (playerRole == null) {
            Message.INVALID_ROLE.send(context.getDispatcher(), argument, SPlayerRole.getValuesString());
            throw new CommandSyntaxException("Invalid island role: " + argument);
        }

        return playerRole;
    }

}
