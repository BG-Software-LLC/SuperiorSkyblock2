package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

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

    @Override
    public List<String> getSuggestions(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read().toLowerCase(Locale.ENGLISH);

        List<String> suggestions = new LinkedList<>();

        for (PlayerRole islandRole : plugin.getRoles().getRoles()) {
            String islandRoleName = islandRole.getName().toLowerCase(Locale.ENGLISH);
            if (islandRoleName.contains(argument))
                suggestions.add(islandRoleName);
        }

        return suggestions;
    }

}
