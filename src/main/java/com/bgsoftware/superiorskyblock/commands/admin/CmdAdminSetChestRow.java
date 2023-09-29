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
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminSetChestRow implements InternalIslandsCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setchestrow");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setchestrow";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_CHEST_ROW.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("islands", MultipleIslandsArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArguments.required("page", IntArgumentType.PAGE, Message.COMMAND_ARGUMENT_PAGE))
                .add(CommandArguments.required("rows", IntArgumentType.ROWS, Message.COMMAND_ARGUMENT_ROWS))
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
        int page = context.getRequiredArgument("page", Integer.class);
        int rows = context.getRequiredArgument("rows", Integer.class);

        BukkitExecutor.data(() -> islands.forEach(island -> island.setChestRows(page - 1, rows)));

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (islands.size() > 1)
            Message.CHANGED_CHEST_SIZE_ALL.send(dispatcher, page, rows);
        else if (targetPlayer == null)
            Message.CHANGED_CHEST_SIZE_NAME.send(dispatcher, page, rows, islands.get(0).getName());
        else
            Message.CHANGED_CHEST_SIZE.send(dispatcher, page, rows, targetPlayer.getName());
    }

}
