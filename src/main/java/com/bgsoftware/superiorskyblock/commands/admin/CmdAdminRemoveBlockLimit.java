package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandsCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultipleIslandsArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandsCommandContext;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class CmdAdminRemoveBlockLimit implements InternalIslandsCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("removeblocklimit", "remblocklimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.removeblocklimit";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_REMOVE_BLOCK_LIMIT.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("islands", MultipleIslandsArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArguments.required("material", StringArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_MATERIAL))
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
        Key key = Keys.ofMaterialAndData(context.getRequiredArgument("material", String.class));

        boolean anyIslandChanged = false;

        for (Island island : islands) {
            if (plugin.getEventsBus().callIslandRemoveBlockLimitEvent(dispatcher, island, key)) {
                anyIslandChanged = true;
                island.removeBlockLimit(key);
            }
        }

        if (!anyIslandChanged)
            return;

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (islands.size() > 1)
            Message.CHANGED_BLOCK_LIMIT_ALL.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(key.getGlobalKey()));
        else if (targetPlayer == null)
            Message.CHANGED_BLOCK_LIMIT_NAME.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(key.getGlobalKey()), islands.get(0).getName());
        else
            Message.CHANGED_BLOCK_LIMIT.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(key.getGlobalKey()), targetPlayer.getName());
    }

}
