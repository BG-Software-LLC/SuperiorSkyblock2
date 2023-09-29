package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalPlayerCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PlayerArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.PlayerCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminMsg implements InternalPlayerCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("msg");
    }

    @Override
    public String getPermission() {
        return "superior.admin.msg";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_MSG.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("player", PlayerArgumentType.ONLINE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .add(CommandArgument.required("message", StringArgumentType.MULTIPLE_COLORIZE, Message.COMMAND_ARGUMENT_MESSAGE))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean requireIslandFromPlayer() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, PlayerCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        SuperiorPlayer targetPlayer = context.getSuperiorPlayer();
        String message = context.getRequiredArgument("message", String.class);

        Message.CUSTOM.send(targetPlayer, message, false);
        Message.MESSAGE_SENT.send(dispatcher, targetPlayer.getName());
    }

}
