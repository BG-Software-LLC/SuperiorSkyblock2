package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.InternalIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class CmdAdminClose implements InternalIslandCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("close", "lock");
    }

    @Override
    public String getPermission() {
        return "superior.admin.close";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_CLOSE.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("island", IslandArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME))
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

        if (!plugin.getEventsBus().callIslandCloseEvent(island, dispatcher))
            return;

        island.setLocked(true);
        Message.ISLAND_CLOSED.send(dispatcher);
    }

}
