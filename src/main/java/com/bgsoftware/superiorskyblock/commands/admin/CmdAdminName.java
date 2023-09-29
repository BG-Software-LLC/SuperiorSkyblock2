package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandNames;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class CmdAdminName implements InternalIslandCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("name", "setname", "rename");
    }

    @Override
    public String getPermission() {
        return "superior.admin.name";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_NAME.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("island", IslandArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME))
                .add(CommandArguments.required("island-name", StringArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_ISLAND_NAME))
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
        String islandName = context.getRequiredArgument("island-name", String.class);

        EventResult<String> eventResult = plugin.getEventsBus().callIslandRenameEvent(island, islandName);

        if (eventResult.isCancelled())
            return;

        islandName = eventResult.getResult();

        if (!IslandNames.isValidName(dispatcher, island, islandName))
            return;

        String oldName = island.getName();
        island.setName(islandName);

        String coloredName = plugin.getSettings().getIslandNames().isColorSupport() ?
                Formatters.COLOR_FORMATTER.format(islandName) : islandName;

        for (Player player : Bukkit.getOnlinePlayers())
            Message.NAME_ANNOUNCEMENT.send(player, dispatcher.getName(), coloredName);

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (targetPlayer == null)
            Message.CHANGED_NAME_OTHER_NAME.send(dispatcher, oldName, coloredName);
        else
            Message.CHANGED_NAME_OTHER.send(dispatcher, targetPlayer.getName(), coloredName);
    }

}
