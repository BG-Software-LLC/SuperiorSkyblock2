package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalPlayersCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IntArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultiplePlayersArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.PlayersCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminSetDisbands implements InternalPlayersCommand {
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setdisbands");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setdisbands";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_DISBANDS.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("players", MultiplePlayersArgumentType.ALL_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ALL_PLAYERS))
                .add(CommandArguments.required("amount", IntArgumentType.LIMIT, Message.COMMAND_ARGUMENT_AMOUNT))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, PlayersCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        List<SuperiorPlayer> targetPlayers = context.getPlayers();
        int amount = context.getRequiredArgument("amount", Integer.class);

        BukkitExecutor.data(() -> targetPlayers.forEach(superiorPlayer -> superiorPlayer.setDisbands(amount)));

        if (targetPlayers.size() > 1) {
            Message.DISBAND_SET_ALL.send(dispatcher, amount);
        } else if (!dispatcher.equals(targetPlayers.get(0).asPlayer()))
            Message.DISBAND_SET_OTHER.send(dispatcher, targetPlayers.get(0).getName(), amount);

        targetPlayers.forEach(superiorPlayer -> Message.DISBAND_SET.send(superiorPlayer, amount));
    }

}
