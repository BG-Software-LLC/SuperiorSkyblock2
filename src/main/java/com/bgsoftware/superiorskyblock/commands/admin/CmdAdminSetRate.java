package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.EnumArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PlayerArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CmdAdminSetRate implements InternalIslandCommand {
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setrate");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setrate";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_RATE.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("island", IslandArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME))
                .add(CommandArgument.required("player", PlayerArgumentType.ALL_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .add(CommandArgument.required("rating", EnumArgumentType.RATING, Message.COMMAND_ARGUMENT_RATING))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean isSelfIsland() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        Island island = context.getIsland();
        SuperiorPlayer targetPlayer = context.getRequiredArgument("player", SuperiorPlayer.class);
        Rating rating = context.getRequiredArgument("rating", Rating.class);

        SuperiorPlayer superiorPlayer = dispatcher instanceof Player ?
                plugin.getPlayers().getSuperiorPlayer(dispatcher) : null;

        if (rating == Rating.UNKNOWN) {
            if (plugin.getEventsBus().callIslandRemoveRatingEvent(dispatcher, superiorPlayer, island)) {
                island.removeRating(targetPlayer);
                Message.RATE_REMOVE_ALL.send(dispatcher, targetPlayer.getName());
            }
        } else if (plugin.getEventsBus().callIslandRateEvent(dispatcher, superiorPlayer, island, rating)) {
            island.setRating(targetPlayer, rating);
            Message.RATE_CHANGE_OTHER.send(dispatcher, targetPlayer.getName(), Formatters.CAPITALIZED_FORMATTER.format(rating.name()));
        }
    }

}
