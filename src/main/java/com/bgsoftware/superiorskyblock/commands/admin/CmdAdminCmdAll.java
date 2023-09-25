package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalAdminSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.BoolArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultipleIslandsArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminCmdAll implements InternalAdminSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("cmdall");
    }

    @Override
    public String getPermission() {
        return "superior.admin.cmdall";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_CMD_ALL.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("islands", MultipleIslandsArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArgument.required("onlineFilter", "online-filter[true/false]", BoolArgumentType.INSTANCE))
                .add(CommandArguments.required("command", StringArgumentType.MULTIPLE, Message.COMMAND_ARGUMENT_COMMAND))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        MultipleIslandsArgumentType.Result islandsResult = context.getRequiredArgument("islands", MultipleIslandsArgumentType.Result.class);
        boolean onlineFilter = context.getRequiredArgument("onlineFilter", boolean.class);
        String command = context.getRequiredArgument("command", String.class);

        List<Island> islands = islandsResult.getIslands();
        SuperiorPlayer targetPlayer = islandsResult.getTargetPlayer();

        islands.forEach(island -> island.executeCommand(command, onlineFilter));

        if (targetPlayer == null)
            Message.GLOBAL_COMMAND_EXECUTED_NAME.send(dispatcher, islands.size() == 1 ? islands.get(0).getName() : "all");
        else
            Message.GLOBAL_COMMAND_EXECUTED.send(dispatcher, targetPlayer.getName());
    }

}
