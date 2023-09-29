package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandsCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultipleIslandsArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PlayerArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandsCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CmdAdminRemoveRatings implements InternalIslandsCommand {
    @Override
    public List<String> getAliases() {
        return Arrays.asList("removeratings", "rratings", "rr");
    }

    @Override
    public String getPermission() {
        return "superior.admin.removeratings";
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("islands", MultipleIslandsArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArgument.optional("player", PlayerArgumentType.ALL_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .build();
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_REMOVE_RATINGS.getMessage(locale);
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandsCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        List<Island> islands = context.getIslands();
        SuperiorPlayer targetPlayer = context.getOptionalArgument("player", SuperiorPlayer.class).orElse(null);

        boolean removingAllRatings = targetPlayer == null;
        Collection<Island> iterIslands = removingAllRatings ? islands : plugin.getGrid().getIslands();

        boolean anyIslandChanged = false;

        for (Island island : iterIslands) {
            if (removingAllRatings) {
                if (plugin.getEventsBus().callIslandClearRatingsEvent(dispatcher, island)) {
                    anyIslandChanged = true;
                    island.removeRatings();
                }
            } else if (plugin.getEventsBus().callIslandRemoveRatingEvent(dispatcher, targetPlayer, island)) {
                anyIslandChanged = true;
                island.removeRating(targetPlayer);
            }
        }

        if (!anyIslandChanged)
            return;

        if (!removingAllRatings)
            Message.RATE_REMOVE_ALL.send(dispatcher, targetPlayer.getName());
        else if (islands.size() == 1)
            Message.RATE_REMOVE_ALL.send(dispatcher, islands.get(0).getName());
        else
            Message.RATE_REMOVE_ALL_ISLANDS.send(dispatcher);
    }

}
