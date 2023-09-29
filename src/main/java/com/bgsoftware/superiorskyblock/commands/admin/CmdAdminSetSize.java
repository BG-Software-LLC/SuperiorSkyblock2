package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandsCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IntArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultipleIslandsArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandsCommandContext;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class CmdAdminSetSize implements InternalIslandsCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("setsize", "setislandsize", "setbordersize");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setsize";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_SIZE.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("islands", MultipleIslandsArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArguments.required("size", IntArgumentType.SIZE, Message.COMMAND_ARGUMENT_SIZE))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandsCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        List<Island> islands = context.getIslands();
        int size = context.getRequiredArgument("size", Integer.class);

        if (size > plugin.getSettings().getMaxIslandSize()) {
            Message.SIZE_BIGGER_MAX.send(dispatcher);
            return;
        }

        boolean anyIslandChanged = false;

        for (Island island : islands) {
            EventResult<Integer> eventResult = plugin.getEventsBus().callIslandChangeBorderSizeEvent(dispatcher, island, size);
            anyIslandChanged |= !eventResult.isCancelled();
            if (!eventResult.isCancelled())
                island.setIslandSize(eventResult.getResult());
        }

        if (!anyIslandChanged)
            return;

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (islands.size() > 1)
            Message.CHANGED_ISLAND_SIZE_ALL.send(dispatcher);
        else if (targetPlayer == null)
            Message.CHANGED_ISLAND_SIZE_NAME.send(dispatcher, islands.get(0).getName());
        else
            Message.CHANGED_ISLAND_SIZE.send(dispatcher, targetPlayer.getName());

        if (plugin.getSettings().isBuildOutsideIsland())
            Message.CHANGED_ISLAND_SIZE_BUILD_OUTSIDE.send(dispatcher);
    }

}
